package com.daewoo.lms.training.assessment.service;

import com.daewoo.lms.training.assessment.dto.*;
import com.daewoo.lms.training.assessment.mapper.AssessmentMapper;
import com.daewoo.lms.training.ncs.dto.*;
import com.daewoo.lms.training.ncs.mapper.NcsMapper;
import java.math.BigDecimal;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.*;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AssessmentService {

    private final AssessmentMapper mapper;

    private final NcsMapper ncsMapper;

    private static final Set<String> IMMUTABLE = Set.of(
        "courseId",
        "batchId",
        "assessmentId",
        "password",
        "status",
        "retestYn",
        "retestNo",
        "baseAssessmentId",
        "createdAt",
        "createdBy",
        "updatedAt"
    );

    public List<AssessmentDTO> list(Long courseId, Long batchId, String status, int limit, int offset) {
        check(limit > 0 && limit <= 200 && offset >= 0, "limit은 1~200, offset은 0 이상이어야 합니다.");
        if (status != null && !status.isBlank())
            check(Set.of(
                "DRAFT",
                "WAIT",
                "TESTING",
                "END",
                "CANCEL"
            ).contains(status), "평가 상태가 올바르지 않습니다.");
        return mapper.list(
            courseId,
            batchId,
            status,
            limit,
            offset
        );
    }

    public AssessmentDetailDTO detail(Long courseId, Long batchId, Long assessmentId) {
        AssessmentDTO key = key(courseId, batchId, assessmentId);
        AssessmentDetailDTO result = new AssessmentDetailDTO();
        result.setAssessment(found(mapper.get(key), "평가를 찾을 수 없습니다."));
        result.setTargets(mapper.targets(key));
        result.setSheets(mapper.sheets(key));
        result.setLevels(mapper.levels(key));
        return result;
    }

    public List<AssessmentCandidateDTO> candidates(Long courseId, Long batchId) {
        found(ncsMapper.context(courseId, batchId), "과정·기수를 찾을 수 없습니다.");
        return mapper.candidates(courseId, batchId);
    }

    @Transactional
    public AssessmentDetailDTO create(Long courseId, Long batchId, AssessmentSaveDTO request, boolean draft) {
        lock(courseId, batchId);
        AssessmentDTO current = new AssessmentDTO();
        current.setCourseId(courseId);
        current.setBatchId(batchId);
        current.setRetestYn("N");
        current.setRetestNo(0);
        return save(
            current,
            request,
            draft,
            true
        );
    }

    @Transactional
    public AssessmentDetailDTO update(Long courseId, Long batchId, Long assessmentId, AssessmentSaveDTO request, boolean draft) {
        lock(courseId, batchId);
        AssessmentDTO current = editable(courseId, batchId, assessmentId);
        if (draft)
            conflictIf(!"DRAFT".equals(current.getStatus()), "등록된 평가를 임시저장 상태로 변경할 수 없습니다.");
        return save(
            current,
            request,
            draft,
            false
        );
    }

    @Transactional
    public AssessmentDetailDTO publish(Long courseId, Long batchId, Long assessmentId) {
        lock(courseId, batchId);
        AssessmentDTO current = editable(courseId, batchId, assessmentId);
        conflictIf(!"DRAFT".equals(current.getStatus()), "임시저장된 평가만 정식 등록할 수 있습니다.");
        return save(
            current,
            new AssessmentSaveDTO(),
            false,
            false
        );
    }

    @Transactional
    public void cancel(Long courseId, Long batchId, Long assessmentId, Long updatedBy) {
        lock(courseId, batchId);
        AssessmentDTO current = found(mapper.get(key(courseId, batchId, assessmentId)), "평가를 찾을 수 없습니다.");
        current.setUpdatedBy(updatedBy);
        mapper.cancel(current);
    }

    @Transactional
    public AssessmentDetailDTO retest(Long courseId, Long batchId, Long baseId, AssessmentRetestDTO request) {
        lock(courseId, batchId);
        check(request != null, "재시험 입력이 필요합니다.");
        AssessmentDTO base = found(mapper.get(key(courseId, batchId, baseId)), "원평가를 찾을 수 없습니다.");
        check(
            "N".equals(base.getRetestYn()) && !Set.of("DRAFT", "CANCEL").contains(base.getStatus()),
            "등록된 원평가를 선택해 주세요."
        );
        AssessmentDTO copy = new AssessmentDTO();
        BeanUtils.copyProperties(base, copy);
        copy.setAssessmentId(null);
        copy.setCreatedAt(null);
        copy.setUpdatedAt(null);
        copy.setCreatedBy(null);
        copy.setUpdatedBy(null);
        copy.setRetestYn("Y");
        copy.setBaseAssessmentId(baseId);
        copy.setRetestNo(mapper.nextRetestNo(copy));
        AssessmentSaveDTO save = new AssessmentSaveDTO();
        save.setAssessment(request.getOverrides());
        save.setTraineeIds(request.getTraineeIds());
        List<AssessmentSheetDTO> sheets = mapper.sheets(base);
        for (AssessmentSheetDTO sheet : sheets) {
            sheet.setSheetId(null);
            sheet.setCreatedAt(null);
            sheet.setCreatedBy(null);
            sheet.setUpdatedBy(null);
        }
        save.setSheets(sheets);
        List<AssessmentLevelDTO> levels = mapper.levels(base);
        for (AssessmentLevelDTO level : levels) {
            level.setLevelId(null);
            level.setCreatedAt(null);
        }
        save.setLevels(levels);
        return save(
            copy,
            save,
            !Boolean.FALSE.equals(request.getDraft()),
            true
        );
    }

    private AssessmentDetailDTO save(AssessmentDTO current, AssessmentSaveDTO request, boolean draft, boolean creating) {
        check(request != null, "저장할 입력값이 필요합니다.");
        AssessmentDTO patch = request.getAssessment();
        if (patch != null) {
            merge(patch, current, IMMUTABLE);
            if (creating)
                current.setCreatedBy(patch.getCreatedBy());
            if (patch.getPassword() != null)
                current.setPassword(patch.getPassword().isEmpty() ? null : hash(patch.getPassword()));
        }
        current.setStatus(draft ? "DRAFT" : "WAIT");
        defaults(current);
        validate(current, draft);
        List<AssessmentTargetDTO> existingTargets = creating ? List.of() : mapper.targets(current);
        List<Long> selected = request.getTraineeIds();
        if (selected == null)
            selected = existingTargets.stream().filter(t -> !"EXCLUDED".equals(t.getTargetStatus())).map(AssessmentTargetDTO::getTraineeId).toList();
        check(draft || !selected.isEmpty(), "평가 대상 훈련생을 선택해 주세요.");
        check(
            selected.size() <= 2000 && selected.stream().allMatch(id -> id != null && id > 0),
            "평가 대상자는 유효한 ID로 최대 2000명까지 선택할 수 있습니다."
        );
        check(new HashSet<>(selected).size() == selected.size(), "평가 대상자가 중복되었습니다.");
        for (Long traineeId : selected) check(mapper.eligible(current, traineeId) > 0, "해당 기수의 훈련 중인 대상자만 선택할 수 있습니다.");
        for (AssessmentTargetDTO target : existingTargets) conflictIf(
            "COMPLETE".equals(target.getTargetStatus()) && !selected.contains(target.getTraineeId()),
            "응시 완료한 대상자는 제외할 수 없습니다."
        );
        if (creating) {
            current.setAssessmentId(mapper.nextId(current));
            mapper.insert(current);
        } else
            mapper.update(current);
        if (request.getTraineeIds() != null) {
            mapper.excludeTargets(current, selected);
            for (Long traineeId : selected) mapper.saveTarget(current, traineeId);
        }
        if (request.getSheets() != null)
            saveSheets(
                current,
                request.getSheets(),
                draft,
                creating
            );
        else if (!draft)
            for (AssessmentSheetDTO sheet : mapper.sheets(current)) validateSheet(sheet, false);
        if (request.getLevels() != null)
            saveLevels(
                current,
                request.getLevels(),
                draft,
                creating
            );
        else if (!draft)
            for (AssessmentLevelDTO level : mapper.levels(current)) validateLevel(level, current, false);
        return detail(current.getCourseId(), current.getBatchId(), current.getAssessmentId());
    }

    private void saveSheets(AssessmentDTO key, List<AssessmentSheetDTO> requests, boolean draft, boolean creating) {
        check(requests.size() <= 100, "평가지는 최대 100개까지 저장할 수 있습니다.");
        Map<Long, AssessmentSheetDTO> existing = new HashMap<>();
        if (!creating)
            for (AssessmentSheetDTO sheet : mapper.sheets(key)) existing.put(sheet.getSheetId(), sheet);
        long next = mapper.nextSheetId(key);
        List<Long> ids = new ArrayList<>();
        for (AssessmentSheetDTO patch : requests) {
            check(patch != null, "평가지 입력값이 비어 있습니다.");
            boolean fresh = patch.getSheetId() == null;
            AssessmentSheetDTO sheet = fresh ? new AssessmentSheetDTO() : found(existing.get(patch.getSheetId()), "해당 평가의 평가지를 찾을 수 없습니다.");
            merge(
                patch,
                sheet,
                Set.of(
                    "courseId",
                    "batchId",
                    "assessmentId",
                    "sheetId",
                    "createdAt",
                    "createdBy",
                    "updatedAt"
                )
            );
            sheet.setCourseId(key.getCourseId());
            sheet.setBatchId(key.getBatchId());
            sheet.setAssessmentId(key.getAssessmentId());
            if (fresh) {
                sheet.setSheetId(next++);
                sheet.setCreatedBy(key.getCreatedBy() != null ? key.getCreatedBy() : key.getUpdatedBy());
            }
            check(!ids.contains(sheet.getSheetId()), "평가지 ID가 중복되었습니다.");
            ids.add(sheet.getSheetId());
            if (sheet.getSheetName() == null)
                sheet.setSheetName("");
            if (sheet.getQuestionCount() == null)
                sheet.setQuestionCount(0);
            if (sheet.getStatus() == null)
                sheet.setStatus("DRAFT");
            validateSheet(sheet, draft);
            if (fresh)
                mapper.insertSheet(sheet);
            else
                mapper.updateSheet(sheet);
        }
        mapper.removeSheets(key, ids);
    }

    private void saveLevels(AssessmentDTO key, List<AssessmentLevelDTO> requests, boolean draft, boolean creating) {
        check(requests.size() <= 100, "수행수준은 최대 100개까지 저장할 수 있습니다.");
        Map<Long, AssessmentLevelDTO> existing = new HashMap<>();
        if (!creating)
            for (AssessmentLevelDTO level : mapper.levels(key)) existing.put(level.getLevelId(), level);
        long next = mapper.nextLevelId(key);
        List<Long> ids = new ArrayList<>();
        Set<BigDecimal> scores = new HashSet<>();
        for (AssessmentLevelDTO patch : requests) {
            check(patch != null, "수행수준 입력값이 비어 있습니다.");
            boolean fresh = patch.getLevelId() == null;
            AssessmentLevelDTO level = fresh ? new AssessmentLevelDTO() : found(existing.get(patch.getLevelId()), "해당 평가의 수행수준을 찾을 수 없습니다.");
            merge(patch, level, Set.of(
                "courseId",
                "batchId",
                "assessmentId",
                "levelId",
                "createdAt",
                "updatedAt"
            ));
            level.setCourseId(key.getCourseId());
            level.setBatchId(key.getBatchId());
            level.setAssessmentId(key.getAssessmentId());
            if (fresh)
                level.setLevelId(next++);
            check(!ids.contains(level.getLevelId()), "수행수준 ID가 중복되었습니다.");
            ids.add(level.getLevelId());
            if (level.getMinScore() == null)
                level.setMinScore(BigDecimal.ZERO);
            if (level.getDescription() == null)
                level.setDescription("");
            if (level.getSortOrder() == null)
                level.setSortOrder(ids.size() - 1);
            validateLevel(level, key, draft);
            check(scores.add(level.getMinScore().stripTrailingZeros()), "수행수준 최소점수가 중복되었습니다.");
            if (fresh)
                mapper.insertLevel(level);
            else
                mapper.updateLevel(level);
        }
        mapper.removeLevels(key, ids);
    }

    private void validate(AssessmentDTO dto, boolean draft) {
        check(dto.getSubjectId() != null && dto.getUnitId() != null, "교과목과 능력단위를 선택해 주세요.");
        NcsSubjectDTO subject = found(ncsMapper.subject(dto.getCourseId(), dto.getSubjectId()), "교과목을 찾을 수 없습니다.");
        check("Y".equals(subject.getUseYn()), "사용 중인 교과목을 선택해 주세요.");
        NcsUnitDTO key = new NcsUnitDTO();
        key.setCourseId(dto.getCourseId());
        key.setBatchId(dto.getBatchId());
        key.setSubjectId(dto.getSubjectId());
        key.setUnitId(dto.getUnitId());
        check("Y".equals(found(ncsMapper.unit(key), "능력단위를 찾을 수 없습니다.").getUseYn()), "사용 중인 능력단위를 선택해 주세요.");
        if (dto.getTargetLevel() == null)
            dto.setTargetLevel(dto.getElementId() == null ? "UNIT" : "ELEMENT");
        check(Set.of("UNIT", "ELEMENT").contains(dto.getTargetLevel()), "평가 대상 수준은 UNIT/ELEMENT이어야 합니다.");
        if ("ELEMENT".equals(dto.getTargetLevel())) {
            check(dto.getElementId() != null, "능력단위 요소를 선택해 주세요.");
            NcsElementDTO element = new NcsElementDTO();
            BeanUtils.copyProperties(key, element);
            element.setElementId(dto.getElementId());
            check(
                "Y".equals(found(ncsMapper.element(element), "능력단위 요소를 찾을 수 없습니다.").getUseYn()),
                "사용 중인 요소를 선택해 주세요."
            );
        } else
            check(dto.getElementId() == null, "능력단위 평가에는 요소 ID를 지정할 수 없습니다.");
        text(
            dto.getAssessmentName(),
            250,
            !draft,
            "평가명"
        );
        text(
            dto.getSheetType(),
            50,
            !draft,
            "평가지 유형"
        );
        if (dto.getAssessmentType() != null)
            check(Set.of("PRE", "MID", "FINAL").contains(dto.getAssessmentType()), "평가구분은 PRE/MID/FINAL이어야 합니다.");
        if (dto.getToolType() != null)
            check(
                Set.of(
                    "ESSAY",
                    "SCENARIO",
                    "CHECKLIST",
                    "PORTFOLIO"
                ).contains(dto.getToolType()),
                "평가도구 코드가 올바르지 않습니다."
            );
        if ("MID".equals(dto.getAssessmentType()))
            check("ELEMENT".equals(dto.getTargetLevel()), "중간평가는 요소를 선택해 주세요.");
        if ("FINAL".equals(dto.getAssessmentType()))
            check("UNIT".equals(dto.getTargetLevel()), "최종평가는 능력단위를 선택해 주세요.");
        check(
            draft || (dto.getAssessmentType() != null && dto.getToolType() != null && dto.getAuthorInstructorId() != null && dto.getAssessmentDate() != null && dto.getExamMinutes() > 0),
            "평가구분·도구·출제자·평가일자·평가시간을 입력해 주세요."
        );
        if (dto.getAuthorInstructorId() != null)
            check(mapper.authorExists(dto) > 0, "해당 과정·기수에 배정된 출제자를 선택해 주세요.");
        if (dto.getPlanId() != null)
            check(mapper.planExists(dto) > 0, "해당 과정·기수의 평가계획을 선택해 주세요.");
        check(dto.getExamMinutes() >= 0, "평가시간은 음수일 수 없습니다.");
        score(dto.getTotalScore(), "총점");
        score(dto.getPassScore(), "패스점수");
        check(
            dto.getPassScore().compareTo(dto.getTotalScore()) <= 0 && (draft || dto.getTotalScore().signum() > 0),
            "패스점수는 총점 이하이고 총점은 0보다 커야 합니다."
        );
        score(dto.getRetestBonusRate(), "재시험 가점율");
        check(dto.getRetestBonusRate().compareTo(BigDecimal.valueOf(100)) <= 0, "재시험 가점율은 100 이하여야 합니다.");
        check(Set.of("Y", "N").contains(dto.getSelfDiagnosisYn()), "학생 자가진단 여부는 Y/N이어야 합니다.");
        if (dto.getStartDatetime() != null || dto.getEndDatetime() != null)
            check(
                dto.getStartDatetime() != null && dto.getEndDatetime() != null && dto.getStartDatetime().isBefore(dto.getEndDatetime()),
                "평가 시작·종료일시를 올바르게 입력해 주세요."
            );
    }

    private static void validateSheet(AssessmentSheetDTO dto, boolean draft) {
        text(
            dto.getSheetName(),
            250,
            !draft,
            "평가지명"
        );
        text(
            dto.getSheetType(),
            50,
            !draft,
            "평가지 유형"
        );
        check(dto.getQuestionCount() != null && dto.getQuestionCount() >= 0, "문항수는 0 이상이어야 합니다.");
        check(
            dto.getStatus() != null && Set.of("DRAFT", "COMPLETE").contains(dto.getStatus()),
            "평가지 상태는 DRAFT/COMPLETE이어야 합니다."
        );
    }

    private static void validateLevel(AssessmentLevelDTO level, AssessmentDTO key, boolean draft) {
        score(level.getMinScore(), "수행수준 최소점수");
        check(level.getMinScore().compareTo(key.getTotalScore()) <= 0, "수행수준 최소점수는 총점 이하여야 합니다.");
        text(
            level.getDescription(),
            16000,
            !draft,
            "수행수준 설명"
        );
        check(level.getSortOrder() != null && level.getSortOrder() >= 0, "수행수준 정렬순서는 0 이상이어야 합니다.");
    }

    private AssessmentDTO editable(Long courseId, Long batchId, Long assessmentId) {
        AssessmentDTO current = found(mapper.get(key(courseId, batchId, assessmentId)), "평가를 찾을 수 없습니다.");
        conflictIf(!Set.of("DRAFT", "WAIT").contains(current.getStatus()), "임시저장 또는 대기 중인 평가만 수정할 수 있습니다.");
        return current;
    }

    private void lock(Long courseId, Long batchId) {
        found(mapper.lockBatch(courseId, batchId), "과정·기수를 찾을 수 없습니다.");
    }

    private static AssessmentDTO key(Long courseId, Long batchId, Long assessmentId) {
        AssessmentDTO key = new AssessmentDTO();
        key.setCourseId(courseId);
        key.setBatchId(batchId);
        key.setAssessmentId(assessmentId);
        return key;
    }

    private static void defaults(AssessmentDTO dto) {
        if (dto.getAssessmentName() == null)
            dto.setAssessmentName("");
        if (dto.getExamMinutes() == null)
            dto.setExamMinutes(0);
        if (dto.getTotalScore() == null)
            dto.setTotalScore(BigDecimal.valueOf(100));
        if (dto.getPassScore() == null)
            dto.setPassScore(BigDecimal.valueOf(60));
        if (dto.getRetestBonusRate() == null)
            dto.setRetestBonusRate(BigDecimal.ZERO);
        if (dto.getSelfDiagnosisYn() == null)
            dto.setSelfDiagnosisYn("N");
    }

    private static void merge(Object patch, Object target, Set<String> excluded) {
        BeanWrapperImpl source = new BeanWrapperImpl(patch), destination = new BeanWrapperImpl(target);
        for (var property : source.getPropertyDescriptors()) {
            String name = property.getName();
            if (excluded.contains(name) || !destination.isWritableProperty(name))
                continue;
            Object value = source.getPropertyValue(name);
            if (value != null)
                destination.setPropertyValue(name, value);
        }
    }

    private static String hash(String password) {
        check(password.length() <= 100, "비밀번호는 최대 100자입니다.");
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        PBEKeySpec spec = new PBEKeySpec(
            password.toCharArray(),
            salt,
            120000,
            256
        );
        try {
            byte[] hash = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
            return "pbkdf2$120000$" + Base64.getEncoder().withoutPadding().encodeToString(salt) + "$" + Base64.getEncoder().withoutPadding().encodeToString(hash);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("비밀번호 해시 생성 실패", e);
        } finally {
            spec.clearPassword();
        }
    }

    private static void score(BigDecimal value, String name) {
        check(
            value != null && value.signum() >= 0 && value.compareTo(BigDecimal.valueOf(100000)) < 0 && value.stripTrailingZeros().scale() <= 2,
            name + "은 소수 둘째 자리까지의 0 이상 100000 미만 값이어야 합니다."
        );
    }

    private static void text(String value, int max, boolean required, String name) {
        check(!required || (value != null && !value.isBlank()), name + "이 필요합니다.");
        check(value == null || value.codePointCount(0, value.length()) <= max, name + "은 최대 " + max + "자입니다.");
    }

    private static void check(boolean condition, String message) {
        if (!condition)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }

    private static void conflictIf(boolean condition, String message) {
        if (condition)
            throw new ResponseStatusException(HttpStatus.CONFLICT, message);
    }

    private static <T> T found(T value, String message) {
        if (value == null)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, message);
        return value;
    }
}
