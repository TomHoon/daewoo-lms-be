package com.daewoo.lms.training.course.service;

import com.daewoo.lms.training.course.dto.*;
import com.daewoo.lms.training.course.mapper.CourseMapper;
import java.math.BigDecimal;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CourseService {

    private final CourseMapper mapper;

    private final ObjectMapper objectMapper;

    private static final Set<String> DAYS = Set.of(
        "MON",
        "TUE",
        "WED",
        "THU",
        "FRI",
        "SAT",
        "SUN"
    );

    public Map<String, List<CourseOptionDTO>> options() {
        return Map.of(
            "trainingJobCategories",
            mapper.trainingJobCategories(),
            "educationCategories",
            mapper.educationCategories(),
            "campuses",
            mapper.campuses(),
            "instructors",
            mapper.instructors()
        );
    }

    public List<CourseDTO> courses(Long trainingJobCategoryId, Long educationCategoryId) {
        return mapper.courses(trainingJobCategoryId, educationCategoryId);
    }

    public CourseDTO course(Long courseId) {
        return found(mapper.course(courseId), "과정을 찾을 수 없습니다.");
    }

    public List<CourseBatchDTO> batches(Long courseId) {
        course(courseId);
        return mapper.batches(courseId);
    }

    public CourseSaveDTO detail(Long courseId, Long batchId) {
        CourseSaveDTO dto = new CourseSaveDTO();
        dto.setCourseId(courseId);
        dto.setBatchId(batchId);
        dto.setCourse(course(courseId));
        dto.setBatch(found(mapper.batch(courseId, batchId), "기수를 찾을 수 없습니다."));
        dto.setDays(mapper.days(courseId, batchId));
        dto.setInstructors(mapper.batchInstructors(courseId, batchId));
        dto.setExtraInfos(mapper.extraInfos(courseId, batchId));
        if (dto.getBatch().getRepresentativeFileId() != null) {
            dto.setRepresentativeFile(mapper.file(dto.getBatch().getRepresentativeFileId()));
        }
        return dto;
    }

    @Transactional
    public CourseSaveDTO createCourse(CourseSaveDTO request) {
        check(request != null && request.getCourse() != null, "과정 기본정보를 입력해 주세요.");
        check(request.getBatch() != null, "기수 정보를 입력해 주세요.");
        CourseDTO course = request.getCourse();
        course.setCourseId(null);
        if (course.getUseYn() == null) {
            course.setUseYn("Y");
        }
        if (course.getCourseColor() == null) {
            course.setCourseColor("#000000");
        }
        validate(course, request.getBatch(), request);
        mapper.insertCourse(course);
        request.setCourseId(course.getCourseId());
        request.setBatchId(null);
        return save(request);
    }

    @Transactional
    public CourseSaveDTO create(Long courseId, CourseSaveDTO request) {
        request.setCourseId(courseId);
        request.setBatchId(null);
        return save(request);
    }

    @Transactional
    public CourseSaveDTO update(Long courseId, Long batchId, CourseSaveDTO request) {
        request.setCourseId(courseId);
        request.setBatchId(batchId);
        return save(request);
    }

    private CourseSaveDTO save(CourseSaveDTO request) {
        Long courseId = request.getCourseId();
        CourseDTO common = found(mapper.lockCourse(courseId), "과정을 찾을 수 없습니다.");
        boolean creating = request.getBatchId() == null;
        CourseBatchDTO batch = creating ? new CourseBatchDTO() : found(mapper.batch(courseId, request.getBatchId()), "기수를 찾을 수 없습니다.");
        merge(request.getCourse(), common);
        merge(request.getBatch(), batch);
        common.setCourseId(courseId);
        batch.setCourseId(courseId);
        // COURSE row lock serializes allocation of the composite batch key.
        batch.setBatchId(creating ? mapper.nextBatchId(courseId) : request.getBatchId());
        if (Boolean.TRUE.equals(request.getRemoveRepresentativeImage()))
            batch.setRepresentativeFileId(null);
        validate(common, batch, request);
        mapper.updateCourse(common);
        if (creating)
            mapper.insertBatch(batch);
        else {
            mapper.updateBatch(batch);
            if (Boolean.TRUE.equals(request.getRemoveRepresentativeImage()))
                mapper.clearRepresentativeFile(courseId, batch.getBatchId());
        }
        Long batchId = batch.getBatchId();
        if (request.getDays() != null) {
            mapper.deleteDays(courseId, batchId);
            for (String day : request.getDays()) mapper.insertDay(
                courseId,
                batchId,
                day,
                batch.getCreatedBy()
            );
        }
        if (request.getInstructors() != null) {
            mapper.deleteInstructors(courseId, batchId);
            for (CourseInstructorDTO instructor : request.getInstructors()) {
                instructor.setCourseId(courseId);
                instructor.setBatchId(batchId);
                instructor.setCreatedBy(batch.getUpdatedBy() != null ? batch.getUpdatedBy() : batch.getCreatedBy());
                mapper.insertInstructor(instructor);
            }
        }
        if (request.getExtraInfos() != null) {
            mapper.deleteExtraInfos(courseId, batchId);
            long index = 0;
            for (CourseExtraInfoDTO extra : request.getExtraInfos()) {
                extra.setCourseId(courseId);
                extra.setBatchId(batchId);
                extra.setExtraInfoId(++index);
                if (extra.getSortOrder() == null)
                    extra.setSortOrder((int) index);
                extra.setCreatedBy(batch.getUpdatedBy() != null ? batch.getUpdatedBy() : batch.getCreatedBy());
                mapper.insertExtraInfo(extra);
            }
        }
        return detail(courseId, batchId);
    }

    @Transactional
    public void delete(Long courseId, Long batchId) {
        found(mapper.lockCourse(courseId), "과정을 찾을 수 없습니다.");
        found(mapper.batch(courseId, batchId), "기수를 찾을 수 없습니다.");
        mapper.deleteBatchDrafts(courseId, batchId);
        mapper.deleteDays(courseId, batchId);
        mapper.deleteInstructors(courseId, batchId);
        mapper.deleteExtraInfos(courseId, batchId);
        mapper.deleteBatch(courseId, batchId);
    }

    public List<CourseDraftDTO> drafts(Long courseId) {
        return mapper.drafts(courseId);
    }

    public CourseSaveDTO draft(Long draftId) {
        return decode(found(mapper.draft(draftId), "임시저장을 찾을 수 없습니다."));
    }

    @Transactional
    public CourseDraftDTO createDraft(CourseSaveDTO request) {
        check(request.getCourseId() != null, "기존 과정을 선택해 주세요.");
        found(mapper.lockCourse(request.getCourseId()), "과정을 찾을 수 없습니다.");
        if (request.getBatchId() != null) {
            found(mapper.batch(request.getCourseId(), request.getBatchId()), "기수를 찾을 수 없습니다.");
        }
        CourseDraftDTO draft = new CourseDraftDTO();
        draft.setCourseId(request.getCourseId());
        draft.setBatchId(request.getBatchId());
        draft.setPayload(objectMapper.writeValueAsString(request));
        mapper.insertDraft(draft);
        return mapper.draft(draft.getDraftId());
    }

    @Transactional
    public CourseDraftDTO updateDraft(Long draftId, CourseSaveDTO request) {
        CourseDraftDTO draft = found(mapper.lockDraft(draftId), "임시저장을 찾을 수 없습니다.");
        check(
            Objects.equals(draft.getCourseId(), request.getCourseId()) && Objects.equals(draft.getBatchId(), request.getBatchId()),
            "임시저장의 과정과 기수는 변경할 수 없습니다."
        );
        draft.setPayload(objectMapper.writeValueAsString(request));
        mapper.updateDraft(draft);
        return mapper.draft(draftId);
    }

    @Transactional
    public CourseSaveDTO publishDraft(Long draftId) {
        CourseDraftDTO draft = found(mapper.lockDraft(draftId), "임시저장을 찾을 수 없습니다.");
        CourseSaveDTO result = save(decode(draft));
        mapper.deleteDraft(draftId);
        return result;
    }

    @Transactional
    public void deleteDraft(Long draftId) {
        if (mapper.deleteDraft(draftId) == 0)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "임시저장을 찾을 수 없습니다.");
    }

    public CourseFileDTO file(Long fileId) {
        return found(mapper.file(fileId), "파일을 찾을 수 없습니다.");
    }

    @Transactional
    public CourseFileDTO createFile(CourseFileDTO file) {
        check(file.getFileId() == null, "파일 ID는 자동 생성됩니다.");
        check(file.getOriginalFileName() != null && !file.getOriginalFileName().isBlank(), "원본 파일명이 필요합니다.");
        check(file.getFilePath() != null && !file.getFilePath().isBlank(), "저장된 파일 경로가 필요합니다.");
        mapper.insertFile(file);
        return mapper.file(file.getFileId());
    }

    private CourseSaveDTO decode(CourseDraftDTO draft) {
        return objectMapper.readValue(draft.getPayload(), CourseSaveDTO.class);
    }

    private void validate(CourseDTO course, CourseBatchDTO batch, CourseSaveDTO request) {
        check(course.getCourseName() != null && !course.getCourseName().isBlank(), "과정명이 필요합니다.");
        check("Y".equals(course.getUseYn()), "사용 중인 과정을 선택해 주세요.");
        reference("trainingJobCategory", course.getTrainingJobCategoryId(), true);
        reference("educationCategory", course.getEducationCategoryId(), true);
//        reference("campus", batch.getCampusId(), true);
        check(
            batch.getStatus() != null && !batch.getStatus().isBlank() && !"DRAFT".equalsIgnoreCase(batch.getStatus()) && !"임시등록".equals(batch.getStatus()),
            "정식 등록 상태를 지정해 주세요."
        );
        check(batch.getRecruitType() != null && !batch.getRecruitType().isBlank(), "모집분류가 필요합니다.");
        if (batch.getStartDate() != null && batch.getEndDate() != null)
            check(!batch.getStartDate().isAfter(batch.getEndDate()), "교육 종료일은 시작일 이후여야 합니다.");
        if (batch.getClassStartTime() != null && batch.getClassEndTime() != null)
            check(batch.getClassStartTime().isBefore(batch.getClassEndTime()), "수업 종료시간은 시작시간 이후여야 합니다.");
        for (Object dto : List.of(course, batch)) {
            BeanWrapperImpl wrapper = new BeanWrapperImpl(dto);
            for (var property : wrapper.getPropertyDescriptors()) {
                if (property.getName().equals("class") || property.getName().endsWith("Id") || property.getName().endsWith("By"))
                    continue;
                Object value = wrapper.getPropertyValue(property.getName());
                if (value instanceof Number number)
                    check(new BigDecimal(number.toString()).signum() >= 0, property.getName() + "은 음수일 수 없습니다.");
                if (property.getName().endsWith("Yn") && value != null)
                    check(Set.of("Y", "N").contains(value), property.getName() + "은 Y 또는 N이어야 합니다.");
            }
        }
        if (batch.getRepresentativeFileId() != null)
            file(batch.getRepresentativeFileId());
        if (request.getDays() != null) {
            check(
                request.getDays().stream().allMatch(day -> day != null && DAYS.contains(day)),
                "수업요일 코드가 올바르지 않습니다."
            );
            check(new HashSet<>(request.getDays()).size() == request.getDays().size(), "수업요일이 중복되었습니다.");
        }
        if (request.getInstructors() != null) {
            check(request.getInstructors().size() <= 10, "강사는 최대 10명입니다.");
            Set<Long> ids = new HashSet<>();
            int mainCount = 0;
            for (CourseInstructorDTO instructor : request.getInstructors()) {
                check(instructor != null, "강사 정보가 비어 있습니다.");
                reference("instructor", instructor.getInstructorId(), true);
                check(ids.add(instructor.getInstructorId()), "강사가 중복되었습니다.");
                if (instructor.getMainYn() == null)
                    instructor.setMainYn("N");
                check(Set.of("Y", "N").contains(instructor.getMainYn()), "담임 여부는 Y 또는 N이어야 합니다.");
                if ("Y".equals(instructor.getMainYn()))
                    mainCount++;
                if (instructor.getStartDate() != null && instructor.getEndDate() != null)
                    check(!instructor.getStartDate().isAfter(instructor.getEndDate()), "강의 종료일이 시작일보다 빠릅니다.");
            }
            check(mainCount <= 1, "담임교사는 한 명만 지정할 수 있습니다.");
        }
        if (request.getExtraInfos() != null)
            for (CourseExtraInfoDTO extra : request.getExtraInfos()) {
                check(
                    extra != null && extra.getItemName() != null && !extra.getItemName().isBlank(),
                    "추가정보 항목명이 필요합니다."
                );
                check(extra.getSortOrder() == null || extra.getSortOrder() >= 0, "정렬순서는 음수일 수 없습니다.");
            }
    }

    private void reference(String type, Long id, boolean required) {
        if (id == null) {
            check(!required, type + "을 선택해 주세요.");
            return;
        }
        check(mapper.existsReference(type, id) > 0, type + "을 찾을 수 없습니다.");
    }

    private static void merge(Object patch, Object target) {
        if (patch == null)
            return;
        BeanWrapperImpl source = new BeanWrapperImpl(patch);
        BeanWrapperImpl destination = new BeanWrapperImpl(target);
        for (var property : source.getPropertyDescriptors()) {
            String name = property.getName();
            if (!destination.isWritableProperty(name) || Set.of(
                "courseId",
                "batchId",
                "createdAt",
                "updatedAt",
                "clickCount"
            ).contains(name))
                continue;
            Object value = source.getPropertyValue(name);
            if (value != null)
                destination.setPropertyValue(name, value);
        }
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
}
