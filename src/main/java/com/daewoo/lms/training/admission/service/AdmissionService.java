package com.daewoo.lms.training.admission.service;

import com.daewoo.lms.training.admission.dto.*;
import com.daewoo.lms.training.admission.mapper.AdmissionMapper;
import com.daewoo.lms.training.course.mapper.CourseMapper;
import java.time.LocalDate;
import java.time.ZoneId;
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
public class AdmissionService {

    private final AdmissionMapper mapper;

    private final CourseMapper courseMapper;

    private static final Set<String> APPLICATION_STATUSES = Set.of(
        "PRE",
        "RECEIVED",
        "INTERVIEW",
        "PASS",
        "FAIL",
        "CANCEL",
        "CANDIDATE",
        "WAIT"
    );

    private static final Set<String> APPROVAL_STATUSES = Set.of(
        "UNAPPROVED",
        "REVIEW",
        "APPROVED",
        "REJECTED"
    );

    private static final Set<String> IMMUTABLE = Set.of(
        "courseId",
        "batchId",
        "applicationId",
        "traineeId",
        "sourceUserId",
        "sourceTraineeId",
        "inputType",
        "createdAt",
        "createdBy",
        "updatedAt",
        "removePhoto"
    );

    public List<AdmissionBatchOptionDTO> batches(Long trainingJobCategoryId, Long educationCategoryId) {
        return mapper.batches(trainingJobCategoryId, educationCategoryId);
    }

    public List<AdmissionApplicationDTO> applications(Long courseId, Long batchId, String keyword, String status, int limit, int offset) {
        page(limit, offset);
        if (status != null && !status.isBlank())
            require(APPLICATION_STATUSES.contains(status), "접수 상태가 올바르지 않습니다.");
        return mapper.applications(
            courseId,
            batchId,
            keyword,
            status,
            limit,
            offset
        );
    }

    public AdmissionDetailDTO detail(Long courseId, Long batchId, Long applicationId) {
        AdmissionApplicationDTO application = found(mapper.application(courseId, batchId, applicationId), "접수를 찾을 수 없습니다.");
        AdmissionDetailDTO result = new AdmissionDetailDTO();
        result.setApplication(application);
        if (application.getTraineeId() != null) {
            result.setTrainee(mapper.trainee(application.getTraineeId()));
            result.setEnrollment(mapper.enrollment(courseId, batchId, application.getTraineeId()));
        }
        if (application.getPhotoFileId() != null)
            result.setPhoto(courseMapper.file(application.getPhotoFileId()));
        return result;
    }

    public List<AdmissionTraineeDTO> trainees(String keyword, int limit, int offset) {
        page(limit, offset);
        return mapper.trainees(keyword, limit, offset);
    }

    public AdmissionTraineeDTO trainee(Long traineeId) {
        return found(mapper.trainee(traineeId), "훈련생을 찾을 수 없습니다.");
    }

    public List<AdmissionMemberDTO> members(String keyword, int limit, int offset) {
        page(limit, offset);
        return mapper.members(keyword, limit, offset);
    }

    public AdmissionMemberDTO member(Long userId) {
        return found(mapper.member(userId), "사용 중인 회원을 찾을 수 없습니다.");
    }

    @Transactional
    public AdmissionDetailDTO create(Long courseId, Long batchId, AdmissionApplicationDTO request) {
        lockBatch(courseId, batchId);
        request.setCourseId(courseId);
        request.setBatchId(batchId);
        request.setApplicationId(null);
        request.setTraineeId(null);
        request.setApplicationStatus("PRE");
        request.setApprovalStatus("UNAPPROVED");
        if (request.getInputType() == null)
            request.setInputType("VISITOR");
        require(
            Set.of(
                "VISITOR",
                "TRAINEE",
                "MEMBER",
                "CSV"
            ).contains(request.getInputType()),
            "입력 유형이 올바르지 않습니다."
        );
        AdmissionTraineeDTO existing = null;
        if ("TRAINEE".equals(request.getInputType())) {
            require(request.getSourceTraineeId() != null && request.getSourceUserId() == null, "기존 훈련생만 선택해 주세요.");
            existing = trainee(request.getSourceTraineeId());
            fillFromTrainee(request, existing);
        } else if ("MEMBER".equals(request.getInputType())) {
            require(request.getSourceUserId() != null && request.getSourceTraineeId() == null, "회원을 선택해 주세요.");
            AdmissionMemberDTO member = found(mapper.lockMember(request.getSourceUserId()), "사용 중인 회원을 찾을 수 없습니다.");
            List<AdmissionTraineeDTO> linked = mapper.traineesByUser(member.getUserId());
            require(linked.size() <= 1, "회원에 연결된 훈련생이 여러 명입니다. 훈련생 선택 탭을 사용해 주세요.");
            if (!linked.isEmpty()) {
                existing = linked.getFirst();
                fillFromTrainee(request, existing);
            }
            if (request.getApplicantName() == null)
                request.setApplicantName(member.getName());
            if (request.getPhone() == null)
                request.setPhone(member.getPhone());
            if (request.getEmail() == null)
                request.setEmail(member.getEmail());
        } else {
            require(
                request.getSourceUserId() == null && request.getSourceTraineeId() == null,
                "직접 입력과 CSV에서는 원본 회원/훈련생 ID를 지정할 수 없습니다."
            );
        }
        defaults(request);
        validate(request);
        if (existing == null) {
            List<AdmissionTraineeDTO> matched = mapper.traineesByIdentity(request);
            require(matched.size() <= 1, "동일한 인적사항의 훈련생이 여러 명입니다. 훈련생 선택 탭을 사용해 주세요.");
            if (!matched.isEmpty()) {
                existing = matched.getFirst();
                if ("MEMBER".equals(request.getInputType()) && existing.getUserId() != null)
                    require(existing.getUserId().equals(request.getSourceUserId()), "다른 회원에 연결된 훈련생 정보입니다.");
            }
        }
        if (existing != null)
            request.setTraineeId(existing.getTraineeId());
        duplicate(request);
        if (existing == null) {
            AdmissionTraineeDTO trainee = new AdmissionTraineeDTO();
            trainee.setUserId(request.getSourceUserId());
            trainee.setName(request.getApplicantName());
            trainee.setBirthDate(request.getBirthDate());
            trainee.setGender(request.getGender());
            trainee.setPhone(request.getPhone());
            trainee.setEmail(request.getEmail());
            trainee.setAddress(request.getAddress());
            trainee.setStatus("APPLICANT");
            trainee.setCreatedBy(request.getCreatedBy());
            mapper.insertTrainee(trainee);
            request.setTraineeId(trainee.getTraineeId());
        }
        request.setApplicationId(mapper.nextApplicationId(courseId, batchId));
        mapper.insertApplication(request);
        AdmissionEnrollmentDTO enrollment = new AdmissionEnrollmentDTO();
        enrollment.setCourseId(courseId);
        enrollment.setBatchId(batchId);
        enrollment.setTraineeId(request.getTraineeId());
        enrollment.setApplicationId(request.getApplicationId());
        enrollment.setEnrollDate(request.getApplicationDate());
        enrollment.setStatus("TRAINING");
        enrollment.setStudentRecordOpenYn("N");
        enrollment.setCreatedBy(request.getCreatedBy());
        mapper.insertEnrollment(enrollment);
        return detail(courseId, batchId, request.getApplicationId());
    }

    @Transactional
    public AdmissionDetailDTO update(Long courseId, Long batchId, Long applicationId, AdmissionApplicationDTO request) {
        lockBatch(courseId, batchId);
        AdmissionApplicationDTO current = found(mapper.application(courseId, batchId, applicationId), "접수를 찾을 수 없습니다.");
        BeanWrapperImpl source = new BeanWrapperImpl(request);
        BeanWrapperImpl target = new BeanWrapperImpl(current);
        for (var property : source.getPropertyDescriptors()) {
            String name = property.getName();
            if (IMMUTABLE.contains(name) || !target.isWritableProperty(name))
                continue;
            Object value = source.getPropertyValue(name);
            if (value != null)
                target.setPropertyValue(name, value);
        }
        if (Boolean.TRUE.equals(request.getRemovePhoto()))
            current.setPhotoFileId(null);
        validate(current);
        // Ignore this application's own identity and enrollment when checking updates.
        if (mapper.duplicate(current) > 0)
            conflict("같은 과정·기수에 동일한 지원자의 접수가 있습니다.");
        mapper.updateApplication(current);
        return detail(courseId, batchId, applicationId);
    }

    @Transactional
    public void cancel(Long courseId, Long batchId, Long applicationId, Long updatedBy) {
        lockBatch(courseId, batchId);
        found(mapper.application(courseId, batchId, applicationId), "접수를 찾을 수 없습니다.");
        mapper.cancel(
            courseId,
            batchId,
            applicationId,
            updatedBy
        );
    }

    private void duplicate(AdmissionApplicationDTO request) {
        if (mapper.duplicate(request) > 0 || (request.getTraineeId() != null && mapper.enrollmentCount(request.getCourseId(), request.getBatchId(), request.getTraineeId()) > 0)) {
            conflict("같은 과정·기수에 이미 접수되었거나 연결된 지원자입니다.");
        }
    }

    private void lockBatch(Long courseId, Long batchId) {
        require(courseId != null && batchId != null && courseId > 0 && batchId > 0, "과정과 기수를 선택해 주세요.");
        found(mapper.lockBatch(courseId, batchId), "과정·기수를 찾을 수 없습니다.");
    }

    private static void fillFromTrainee(AdmissionApplicationDTO dto, AdmissionTraineeDTO trainee) {
        if (dto.getApplicantName() == null)
            dto.setApplicantName(trainee.getName());
        if (dto.getBirthDate() == null)
            dto.setBirthDate(trainee.getBirthDate());
        if (dto.getGender() == null)
            dto.setGender(trainee.getGender());
        if (dto.getPhone() == null)
            dto.setPhone(trainee.getPhone());
        if (dto.getEmail() == null)
            dto.setEmail(trainee.getEmail());
        if (dto.getAddress() == null)
            dto.setAddress(trainee.getAddress());
    }

    private static void defaults(AdmissionApplicationDTO dto) {
        if (dto.getApplicationDate() == null)
            dto.setApplicationDate(LocalDate.now(ZoneId.of("Asia/Seoul")));
        if (dto.getPreTestYn() == null)
            dto.setPreTestYn("N");
        if (dto.getNationalityType() == null)
            dto.setNationalityType("DOMESTIC");
        if (dto.getReceptionMethod() == null)
            dto.setReceptionMethod("VISIT");
        if (dto.getEmploymentSupportType() == null)
            dto.setEmploymentSupportType("NONE");
    }

    private void validate(AdmissionApplicationDTO dto) {
        require(dto.getApplicantName() != null && !dto.getApplicantName().isBlank(), "이름이 필요합니다.");
        dto.setApplicantName(dto.getApplicantName().trim());
        require(
            dto.getBirthDate() != null && !dto.getBirthDate().isAfter(LocalDate.now(ZoneId.of("Asia/Seoul"))),
            "올바른 생년월일이 필요합니다."
        );
        require(dto.getPhone() != null && dto.getPhone().matches("[+0-9() .-]+"), "올바른 대표연락처가 필요합니다.");
        dto.setPhone(dto.getPhone().replaceAll("[^0-9]", ""));
        require(dto.getPhone().length() >= 7 && dto.getPhone().length() <= 20, "대표연락처는 숫자 7~20자리여야 합니다.");
        require(dto.getApplicationDate() != null, "접수일이 필요합니다.");
        require(
            dto.getApplicationStatus() != null && APPLICATION_STATUSES.contains(dto.getApplicationStatus()),
            "접수 상태가 올바르지 않습니다."
        );
        require(
            dto.getApprovalStatus() != null && APPROVAL_STATUSES.contains(dto.getApprovalStatus()),
            "승인 상태가 올바르지 않습니다."
        );
        require(
            dto.getPreTestYn() != null && Set.of("Y", "N").contains(dto.getPreTestYn()),
            "사전평가 여부는 Y/N이어야 합니다."
        );
        if (dto.getGender() != null)
            require(Set.of("M", "F").contains(dto.getGender()), "성별은 M/F이어야 합니다.");
        if (dto.getNationalityType() != null)
            require(Set.of("DOMESTIC", "FOREIGN").contains(dto.getNationalityType()), "내·외국인 코드가 올바르지 않습니다.");
        if (dto.getReceptionMethod() != null)
            require(
                Set.of(
                    "VISIT",
                    "WEB",
                    "MOBILE",
                    "PHONE",
                    "FAX"
                ).contains(dto.getReceptionMethod()),
                "접수방법 코드가 올바르지 않습니다."
            );
        if (dto.getEmail() != null && !dto.getEmail().isBlank())
            require(dto.getEmail().matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+"), "이메일 형식이 올바르지 않습니다.");
        Map<String, Integer> lengths = Map.ofEntries(
            Map.entry("applicantName", 100),
            Map.entry("extraPhone", 30),
            Map.entry("email", 150),
            Map.entry("zipCode", 10),
            Map.entry("region", 100),
            Map.entry("address", 500),
            Map.entry("addressDetail", 500),
            Map.entry("lastSchoolName", 200),
            Map.entry("educationLevel", 50),
            Map.entry("graduationType", 50),
            Map.entry("applyRoute", 100),
            Map.entry("applicantMessage", 1000),
            Map.entry("employmentSupportType", 30),
            Map.entry("employmentSupportCenter", 200),
            Map.entry("employmentSupportManager", 100),
            Map.entry("employmentSupportPhone", 30)
        );
        BeanWrapperImpl wrapper = new BeanWrapperImpl(dto);
        lengths.forEach(
            (name, max) -> {
                String value = (String) wrapper.getPropertyValue(name);
                require(value == null || value.codePointCount(0, value.length()) <= max, name + "은 최대 " + max + "자입니다.");
            }
        );
        if (dto.getPhotoFileId() != null) {
            var file = found(courseMapper.file(dto.getPhotoFileId()), "사진 파일을 찾을 수 없습니다.");
            require(
                file.getMimeType() != null && Set.of(
                    "image/png",
                    "image/jpeg",
                    "image/gif",
                    "image/webp"
                ).contains(file.getMimeType()),
                "사진은 이미지 파일이어야 합니다."
            );
        }
    }

    private static void page(int limit, int offset) {
        require(limit >= 1 && limit <= 200 && offset >= 0, "limit은 1~200, offset은 0 이상이어야 합니다.");
    }

    private static void require(boolean condition, String message) {
        if (!condition)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }

    private static void conflict(String message) {
        throw new ResponseStatusException(HttpStatus.CONFLICT, message);
    }

    private static <T> T found(T value, String message) {
        if (value == null)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, message);
        return value;
    }
}
