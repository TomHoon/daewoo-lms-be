package com.daewoo.lms.training.assessmentPaper.service;

import com.daewoo.lms.training.assessment.dto.*;
import com.daewoo.lms.training.assessment.mapper.AssessmentMapper;
import com.daewoo.lms.training.assessmentPaper.dto.*;
import com.daewoo.lms.training.assessmentPaper.mapper.AssessmentPaperMapper;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AssessmentPaperService {

    private final AssessmentMapper assessmentMapper;

    private final AssessmentPaperMapper mapper;

    private static final Set<String> TYPES = Set.of(
        "OBJECTIVE",
        "SUBJECTIVE",
        "ESSAY",
        "CHECKLIST"
    );

    private static final Set<String> PAPER_FIELDS = Set.of(
        "assessmentName",
        "authorInstructorId",
        "assessmentDate",
        "status",
        "totalScore",
        "passScore",
        "description",
        "retestBonusRate",
        "updatedBy"
    );

    public PaperDetailDTO detail(Long courseId, Long batchId, Long assessmentId, boolean includeDeleted) {
        AssessmentDTO key = key(courseId, batchId, assessmentId);
        PaperDetailDTO result = new PaperDetailDTO();
        result.setAssessment(found(assessmentMapper.get(key), "평가를 찾을 수 없습니다."));
        List<PaperQuestionDTO> questions = mapper.questions(key, includeDeleted);
        for (PaperQuestionDTO question : questions) {
            question.setOptions(mapper.options(question, includeDeleted));
            question.setItemCount((int) question.getOptions().stream().filter(o -> !"Y".equals(o.getDeletedYn())).count());
        }
        result.setQuestions(questions);
        List<PaperQuestionDTO> active = questions.stream().filter(q -> !"Y".equals(q.getDeletedYn())).toList();
        result.setTotalQuestionCount(active.size());
        result.setQuestionScoreTotal(
            active.stream().map(PaperQuestionDTO::getScore).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add)
        );
        result.setLevels(assessmentMapper.levels(key));
        return result;
    }

    @Transactional
    public PaperDetailDTO save(Long courseId, Long batchId, Long assessmentId, PaperSaveDTO request, boolean createOnly) {
        AssessmentDTO current = lock(courseId, batchId, assessmentId);
        List<PaperQuestionDTO> existing = mapper.questions(current, true);
        if (createOnly && existing.stream().anyMatch(q -> !"Y".equals(q.getDeletedYn())))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 작성한 평가지가 있습니다. 수정 API를 사용해 주세요.");
        return save(current, request, existing);
    }

    @Transactional
    public PaperDetailDTO deleteQuestion(Long courseId, Long batchId, Long assessmentId, Long questionId, PaperSaveDTO request) {
        AssessmentDTO current = lock(courseId, batchId, assessmentId);
        List<PaperQuestionDTO> existing = mapper.questions(current, true);
        check(existing.stream().anyMatch(q -> q.getQuestionId().equals(questionId)), "해당 평가의 문항을 찾을 수 없습니다.");
        check(request != null, "삭제 후 총 문항수·총 배점을 전달해 주세요.");
        List<PaperQuestionDTO> remaining = new ArrayList<>();
        for (PaperQuestionDTO question : existing) {
            if (question.getQuestionId().equals(questionId) || "Y".equals(question.getDeletedYn()))
                continue;
            question.setOptions(mapper.options(question, false));
            remaining.add(question);
        }
        request.setQuestions(remaining);
        return save(current, request, existing);
    }

    @Transactional
    public void cancel(Long courseId, Long batchId, Long assessmentId, Long updatedBy) {
        AssessmentDTO current = lock(courseId, batchId, assessmentId);
        current.setUpdatedBy(updatedBy);
        assessmentMapper.cancel(current);
    }

    private PaperDetailDTO save(AssessmentDTO current, PaperSaveDTO request, List<PaperQuestionDTO> existing) {
        check(!"CANCEL".equals(current.getStatus()), "취소된 평가의 평가지는 수정할 수 없습니다.");
        check(
            request != null && request.getAssessment() != null && request.getAssessment().getTotalScore() != null,
            "평가 기본정보와 입력한 총 배점이 필요합니다."
        );
        check(
            request.getTotalQuestionCount() != null && request.getTotalQuestionCount() >= 0,
            "입력한 총 문항수가 필요합니다."
        );
        check(
            request.getQuestions() != null && request.getQuestions().size() <= 2000,
            "questions 배열은 필수이며 최대 2000문항입니다."
        );
        merge(request.getAssessment(), current, PAPER_FIELDS);
        validateAssessment(current);
        Map<Long, PaperQuestionDTO> old = new HashMap<>();
        for (PaperQuestionDTO question : existing) old.put(question.getQuestionId(), question);
        Set<Long> suppliedIds = new HashSet<>();
        Set<Integer> numbers = new HashSet<>();
        List<Prepared> prepared = new ArrayList<>();
        long next = mapper.nextQuestionId(current);
        BigDecimal total = BigDecimal.ZERO;
        for (PaperQuestionDTO patch : request.getQuestions()) {
            check(patch != null, "문항 입력값이 비어 있습니다.");
            yn(patch.getDeletedYn(), "문항 삭제여부");
            boolean fresh = patch.getQuestionId() == null;
            if (!fresh)
                check(suppliedIds.add(patch.getQuestionId()), "문항 ID가 중복되었습니다.");
            PaperQuestionDTO question = fresh ? new PaperQuestionDTO() : found(old.get(patch.getQuestionId()), "해당 평가의 문항을 찾을 수 없습니다.");
            if ("Y".equals(patch.getDeletedYn())) {
                check(!fresh, "저장하지 않은 문항은 삭제할 수 없습니다.");
                continue;
            }
            check(fresh || !"Y".equals(question.getDeletedYn()), "삭제한 문항은 수정할 수 없습니다.");
            merge(
                patch,
                question,
                Set.of(
                    "questionNo",
                    "questionType",
                    "questionText",
                    "score",
                    "correctAnswer",
                    "explanation",
                    "updatedBy"
                )
            );
            question.setCourseId(current.getCourseId());
            question.setBatchId(current.getBatchId());
            question.setAssessmentId(current.getAssessmentId());
            question.setDeletedYn("N");
            if (fresh) {
                question.setQuestionId(next++);
                question.setCreatedBy(patch.getCreatedBy() != null ? patch.getCreatedBy() : current.getUpdatedBy());
            }
            if (question.getUpdatedBy() == null)
                question.setUpdatedBy(current.getUpdatedBy());
            check(
                question.getQuestionNo() != null && question.getQuestionNo() > 0 && numbers.add(question.getQuestionNo()),
                "문제 번호는 중복되지 않는 양수여야 합니다."
            );
            check(
                question.getQuestionType() != null && TYPES.contains(question.getQuestionType()),
                "문항 유형이 올바르지 않습니다."
            );
            required(question.getQuestionText(), "문제 내용");
            score(question.getScore(), "문항 배점");
            List<PreparedOption> options = prepareOptions(question, patch.getOptions(), fresh);
            if (patch.getItemCount() != null)
                check(patch.getItemCount() == options.size(), "입력한 항목수와 실제 보기 개수가 다릅니다.");
            validateAnswer(question, options);
            total = total.add(question.getScore());
            prepared.add(new Prepared(question, fresh, options));
        }
        check(request.getTotalQuestionCount() == prepared.size(), "입력한 총 문항수와 실제 문항수가 다릅니다.");
        check(current.getTotalScore().compareTo(total) == 0, "입력한 총 배점과 문항 배점 합계가 다릅니다.");
        // Validate and persist every related change in this single transaction, including edits during TESTING/END.
        saveLevels(current, request.getLevels());
        assessmentMapper.update(current);
        List<Long> activeIds = new ArrayList<>();
        for (Prepared item : prepared) {
            if (item.fresh())
                mapper.insertQuestion(item.question());
            else
                mapper.updateQuestion(item.question());
            activeIds.add(item.question().getQuestionId());
            List<Long> optionIds = new ArrayList<>();
            for (PreparedOption option : item.options()) {
                if (option.fresh())
                    mapper.insertOption(option.option());
                else
                    mapper.updateOption(option.option());
                optionIds.add(option.option().getOptionId());
            }
            mapper.disableMissingOptions(item.question(), optionIds);
        }
        mapper.disableMissingQuestionOptions(current, activeIds);
        mapper.disableMissingQuestions(current, activeIds);
        return detail(
            current.getCourseId(),
            current.getBatchId(),
            current.getAssessmentId(),
            false
        );
    }

    private List<PreparedOption> prepareOptions(PaperQuestionDTO question, List<PaperOptionDTO> input, boolean freshQuestion) {
        List<PaperOptionDTO> existing = freshQuestion ? List.of() : mapper.options(question, true);
        if (input == null)
            input = existing.stream().filter(o -> !"Y".equals(o.getDeletedYn())).toList();
        check(input.size() <= 100, "문항당 보기는 최대 100개입니다.");
        Map<Long, PaperOptionDTO> old = new HashMap<>();
        for (PaperOptionDTO option : existing) old.put(option.getOptionId(), option);
        long next = freshQuestion ? 1 : mapper.nextOptionId(question);
        Set<Long> suppliedIds = new HashSet<>();
        Set<Integer> numbers = new HashSet<>();
        List<PreparedOption> result = new ArrayList<>();
        for (PaperOptionDTO patch : input) {
            check(patch != null, "보기 입력값이 비어 있습니다.");
            yn(patch.getDeletedYn(), "보기 삭제여부");
            boolean fresh = patch.getOptionId() == null;
            if (!fresh)
                check(suppliedIds.add(patch.getOptionId()), "보기 ID가 중복되었습니다.");
            PaperOptionDTO option = fresh ? new PaperOptionDTO() : found(old.get(patch.getOptionId()), "해당 문항의 보기를 찾을 수 없습니다.");
            if ("Y".equals(patch.getDeletedYn())) {
                check(!fresh, "저장하지 않은 보기는 삭제할 수 없습니다.");
                continue;
            }
            check(fresh || !"Y".equals(option.getDeletedYn()), "삭제한 보기는 수정할 수 없습니다.");
            merge(patch, option, Set.of(
                "optionNo",
                "optionText",
                "correctYn",
                "updatedBy"
            ));
            option.setCourseId(question.getCourseId());
            option.setBatchId(question.getBatchId());
            option.setAssessmentId(question.getAssessmentId());
            option.setQuestionId(question.getQuestionId());
            option.setDeletedYn("N");
            if (fresh) {
                option.setOptionId(next++);
                option.setCreatedBy(patch.getCreatedBy() != null ? patch.getCreatedBy() : question.getCreatedBy());
            }
            if (option.getUpdatedBy() == null)
                option.setUpdatedBy(question.getUpdatedBy());
            if (option.getCorrectYn() == null)
                option.setCorrectYn("N");
            check(
                option.getOptionNo() != null && option.getOptionNo() > 0 && numbers.add(option.getOptionNo()),
                "보기 번호는 중복되지 않는 양수여야 합니다."
            );
            required(option.getOptionText(), "보기 내용");
            yn(option.getCorrectYn(), "정답 여부");
            result.add(new PreparedOption(option, fresh));
        }
        return result;
    }

    private static void validateAnswer(PaperQuestionDTO question, List<PreparedOption> options) {
        if ("OBJECTIVE".equals(question.getQuestionType())) {
            check(options.size() >= 2, "객관식 문항에는 최소 2개의 보기가 필요합니다.");
            List<PaperOptionDTO> marked = options.stream().map(PreparedOption::option).filter(o -> "Y".equals(o.getCorrectYn())).toList();
            check(marked.size() <= 1, "객관식 정답은 하나만 선택할 수 있습니다.");
            if (question.getCorrectAnswer() == null || question.getCorrectAnswer().isBlank()) {
                check(marked.size() == 1, "객관식 정답을 지정해 주세요.");
                question.setCorrectAnswer(marked.getFirst().getOptionNo().toString());
            }
            String answer = question.getCorrectAnswer().trim();
            check(
                options.stream().anyMatch(o -> o.option().getOptionNo().toString().equals(answer)),
                "객관식 정답은 존재하는 보기 번호여야 합니다."
            );
            check(
                marked.isEmpty() || marked.getFirst().getOptionNo().toString().equals(answer),
                "정답 번호와 보기의 정답 표시가 다릅니다."
            );
            for (PreparedOption option : options) option.option().setCorrectYn(option.option().getOptionNo().toString().equals(answer) ? "Y" : "N");
            question.setCorrectAnswer(answer);
        } else {
            required(question.getCorrectAnswer(), "정답/모범답안");
            if (!"CHECKLIST".equals(question.getQuestionType()))
                check(options.isEmpty(), "주관식·서술형 문항에는 보기를 등록할 수 없습니다.");
        }
    }

    private void saveLevels(AssessmentDTO key, List<AssessmentLevelDTO> input) {
        List<AssessmentLevelDTO> existing = assessmentMapper.levels(key);
        if (input == null) {
            for (AssessmentLevelDTO level : existing) validateLevel(level, key);
            return;
        }
        check(input.size() <= 100, "수행정도 기준은 최대 100개입니다.");
        Map<Long, AssessmentLevelDTO> old = new HashMap<>();
        for (AssessmentLevelDTO level : existing) old.put(level.getLevelId(), level);
        long next = assessmentMapper.nextLevelId(key);
        List<Long> ids = new ArrayList<>();
        Set<BigDecimal> thresholds = new HashSet<>();
        for (AssessmentLevelDTO patch : input) {
            check(patch != null, "수행정도 기준이 비어 있습니다.");
            boolean fresh = patch.getLevelId() == null;
            AssessmentLevelDTO level = fresh ? new AssessmentLevelDTO() : found(old.get(patch.getLevelId()), "해당 평가의 수행정도 기준을 찾을 수 없습니다.");
            merge(patch, level, Set.of("minScore", "description", "sortOrder"));
            level.setCourseId(key.getCourseId());
            level.setBatchId(key.getBatchId());
            level.setAssessmentId(key.getAssessmentId());
            if (fresh)
                level.setLevelId(next++);
            if (level.getSortOrder() == null)
                level.setSortOrder(ids.size());
            check(!ids.contains(level.getLevelId()), "수행정도 기준 ID가 중복되었습니다.");
            ids.add(level.getLevelId());
            validateLevel(level, key);
            check(thresholds.add(level.getMinScore().stripTrailingZeros()), "최소 수행점수가 중복되었습니다.");
            if (fresh)
                assessmentMapper.insertLevel(level);
            else
                assessmentMapper.updateLevel(level);
        }
        assessmentMapper.removeLevels(key, ids);
    }

    private static void validateLevel(AssessmentLevelDTO level, AssessmentDTO key) {
        score(level.getMinScore(), "최소 수행점수");
        required(level.getDescription(), "수행정도 설명");
        check(level.getMinScore().compareTo(key.getTotalScore()) <= 0, "최소 수행점수는 총 배점 이하여야 합니다.");
        check(
            level.getDescription().getBytes(StandardCharsets.UTF_8).length <= 65535,
            "수행정도 설명은 UTF-8 기준 65535바이트 이하여야 합니다."
        );
        check(level.getSortOrder() != null && level.getSortOrder() >= 0, "수행정도 정렬순서는 0 이상이어야 합니다.");
    }

    private void validateAssessment(AssessmentDTO current) {
        required(current.getAssessmentName(), "시험명");
        check(
            current.getAssessmentName().codePointCount(0, current.getAssessmentName().length()) <= 250,
            "시험명은 최대 250자입니다."
        );
        check(
            current.getStatus() != null && Set.of(
                "DRAFT",
                "WAIT",
                "TESTING",
                "END"
            ).contains(current.getStatus()),
            "평가 상태가 올바르지 않습니다."
        );
        score(current.getTotalScore(), "총 배점");
        score(current.getPassScore(), "패스점수");
        check(current.getPassScore().compareTo(current.getTotalScore()) <= 0, "패스점수는 총 배점 이하여야 합니다.");
        if (current.getAuthorInstructorId() != null)
            check(assessmentMapper.authorExists(current) > 0, "해당 기수에 배정된 출제자를 선택해 주세요.");
        if (current.getRetestBonusRate() != null) {
            score(current.getRetestBonusRate(), "재시험 가점율");
            check(current.getRetestBonusRate().compareTo(BigDecimal.valueOf(100)) <= 0, "재시험 가점율은 100 이하여야 합니다.");
        }
    }

    private AssessmentDTO lock(Long courseId, Long batchId, Long assessmentId) {
        found(assessmentMapper.lockBatch(courseId, batchId), "과정·기수를 찾을 수 없습니다.");
        return found(assessmentMapper.get(key(courseId, batchId, assessmentId)), "평가를 찾을 수 없습니다.");
    }

    private static AssessmentDTO key(Long courseId, Long batchId, Long assessmentId) {
        AssessmentDTO key = new AssessmentDTO();
        key.setCourseId(courseId);
        key.setBatchId(batchId);
        key.setAssessmentId(assessmentId);
        return key;
    }

    private static void merge(Object patch, Object target, Set<String> fields) {
        BeanWrapperImpl from = new BeanWrapperImpl(patch), to = new BeanWrapperImpl(target);
        for (String name : fields) {
            Object value = from.getPropertyValue(name);
            if (value != null)
                to.setPropertyValue(name, value);
        }
    }

    private static void required(String value, String name) {
        check(value != null && !value.isBlank(), name + "이 필요합니다.");
    }

    private static void yn(String value, String name) {
        check(value == null || Set.of("Y", "N").contains(value), name + "는 Y/N이어야 합니다.");
    }

    private static void score(BigDecimal value, String name) {
        check(
            value != null && value.signum() >= 0 && value.compareTo(BigDecimal.valueOf(100000)) < 0 && value.stripTrailingZeros().scale() <= 2,
            name + "은 소수 둘째 자리까지의 0 이상 100000 미만 값이어야 합니다."
        );
    }

    private static void check(boolean condition, String message) {
        if (!condition)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }

    private static <T> T found(T value, String message) {
        if (value == null)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, message);
        return value;
    }

    private record Prepared(PaperQuestionDTO question, boolean fresh, List<PreparedOption> options) {
    }

    private record PreparedOption(PaperOptionDTO option, boolean fresh) {
    }
}
