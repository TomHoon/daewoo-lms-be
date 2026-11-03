package com.daewoo.lms.training.admission.controller;

import com.daewoo.lms.training.admission.dto.*;
import com.daewoo.lms.training.admission.service.AdmissionCsvService;
import com.daewoo.lms.training.admission.service.AdmissionService;
import com.daewoo.lms.training.course.dto.CourseFileDTO;
import com.daewoo.lms.training.course.dto.CourseOptionDTO;
import com.daewoo.lms.training.course.service.CourseFileService;
import com.daewoo.lms.training.course.service.CourseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "04. 입학접수", description = "방문 입력, 기존 훈련생 선택, 회원 선택 및 CSV 접수")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admissions")
public class AdmissionController {

    private final AdmissionService service;

    private final AdmissionCsvService csvService;

    private final CourseService courseService;

    private final CourseFileService fileService;

    @Operation(
        summary = "접수용 분류 목록",
        description = "훈련직종 분류와 교육과정 분류 선택 목록을 반환합니다."
    )
    @GetMapping("/options")
    public Map<String, List<CourseOptionDTO>> options() {
        var options = courseService.options();
        return Map.of(
            "trainingJobCategories",
            options.get("trainingJobCategories"),
            "educationCategories",
            options.get("educationCategories")
        );
    }

    @Operation(
        summary = "접수할 과정·기수 목록",
        description = "분류별 과정·기수와 교육기간을 조회합니다. 반환된 courseId와 batchId를 접수 API에 사용합니다."
    )
    @GetMapping("/batches")
    public List<AdmissionBatchOptionDTO> batches(
        @Parameter(description = "훈련직종 분류 ID. 생략하면 해당 분류로 제한하지 않습니다.", example = "1")
        @RequestParam(required = false)
        Long trainingJobCategoryId,
        @Parameter(description = "교육과정 분류 ID. 생략하면 해당 분류로 제한하지 않습니다.", example = "1")
        @RequestParam(required = false)
        Long educationCategoryId
    ) {
        return service.batches(trainingJobCategoryId, educationCategoryId);
    }

    @Operation(
        summary = "입학접수 목록",
        description = "해당 과정·기수의 접수를 조회합니다. 이름/연락처 검색과 접수 상태 필터, limit/offset 페이지 조회를 지원합니다."
    )
    @GetMapping
    public List<AdmissionApplicationDTO> applications(
        @Parameter(description = "훈련과정 ID. 기존 과정 조회에서 확인합니다.", example = "1")
        @RequestParam
        Long courseId,
        @Parameter(description = "훈련기수 ID. courseId와 함께 기수를 식별합니다.", example = "1")
        @RequestParam
        Long batchId,
        @Parameter(description = "이름 또는 연락처 검색어. 생략하면 검색어로 제한하지 않습니다.", example = "홍길동")
        @RequestParam(required = false)
        String keyword,
        @Parameter(
            description = "접수 상태: PRE/RECEIVED/INTERVIEW/PASS/FAIL/CANCEL/CANDIDATE/WAIT. 생략하면 전체.",
            example = "PRE"
        )
        @RequestParam(required = false)
        String status,
        @Parameter(description = "조회할 최대 건수: 1~200, 기본값 50.", example = "50")
        @RequestParam(defaultValue = "50")
        int limit,
        @Parameter(description = "건너뛸 건수: 0 이상. 첫 페이지는 0.", example = "0")
        @RequestParam(defaultValue = "0")
        int offset
    ) {
        return service.applications(
            courseId,
            batchId,
            keyword,
            status,
            limit,
            offset
        );
    }

    @Operation(
        summary = "입학접수 상세",
        description = "접수 당시 입력 정보, 연결 훈련생, 과정 연결 정보, 사진 메타데이터를 반환합니다."
    )
    @GetMapping("/{courseId}/{batchId}/{applicationId}")
    public AdmissionDetailDTO detail(
        @Parameter(description = "훈련과정 ID. 기존 과정 조회에서 확인합니다.", example = "1")
        @PathVariable
        Long courseId,
        @Parameter(description = "훈련기수 ID. courseId와 함께 기수를 식별합니다.", example = "1")
        @PathVariable
        Long batchId,
        @Parameter(description = "해당 과정·기수의 입학접수 ID.", example = "1")
        @PathVariable
        Long applicationId
    ) {
        return service.detail(courseId, batchId, applicationId);
    }

    @Operation(
        summary = "입학접수 등록",
        description = "inputType은 VISITOR/TRAINEE/MEMBER/CSV입니다. 접수·훈련생·과정 연결을 함께 생성/연결합니다. " + "기존 훈련생은 수정하지 않습니다. 같은 기수에 훈련생 ID 또는 이름·생년월일·정규화 연락처가 일치하면 409로 차단합니다."
    )
    @PostMapping("/{courseId}/{batchId}")
    @ResponseStatus(HttpStatus.CREATED)
    public AdmissionDetailDTO create(
        @Parameter(description = "훈련과정 ID. 기존 과정 조회에서 확인합니다.", example = "1")
        @PathVariable
        Long courseId,
        @Parameter(description = "훈련기수 ID. courseId와 함께 기수를 식별합니다.", example = "1")
        @PathVariable
        Long batchId,
        @RequestBody
        AdmissionApplicationDTO request
    ) {
        return service.create(courseId, batchId, request);
    }

    @Operation(
        summary = "입학접수 수정",
        description = "전달한 필드만 접수별 정보에 반영합니다. 원본 훈련생/회원과 과정 연결 ID는 변경하지 않습니다. " + "removePhoto=true이면 사진 연결을 해제합니다."
    )
    @PutMapping("/{courseId}/{batchId}/{applicationId}")
    public AdmissionDetailDTO update(
        @Parameter(description = "훈련과정 ID. 기존 과정 조회에서 확인합니다.", example = "1")
        @PathVariable
        Long courseId,
        @Parameter(description = "훈련기수 ID. courseId와 함께 기수를 식별합니다.", example = "1")
        @PathVariable
        Long batchId,
        @Parameter(description = "해당 과정·기수의 입학접수 ID.", example = "1")
        @PathVariable
        Long applicationId,
        @RequestBody
        AdmissionApplicationDTO request
    ) {
        return service.update(
            courseId,
            batchId,
            applicationId,
            request
        );
    }

    @Operation(
        summary = "입학접수 취소",
        description = "접수 상태만 CANCEL로 변경합니다. 훈련생, 과정 연결, 사진 파일은 모두 유지합니다."
    )
    @DeleteMapping("/{courseId}/{batchId}/{applicationId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancel(
        @Parameter(description = "훈련과정 ID. 기존 과정 조회에서 확인합니다.", example = "1")
        @PathVariable
        Long courseId,
        @Parameter(description = "훈련기수 ID. courseId와 함께 기수를 식별합니다.", example = "1")
        @PathVariable
        Long batchId,
        @Parameter(description = "해당 과정·기수의 입학접수 ID.", example = "1")
        @PathVariable
        Long applicationId,
        @Parameter(description = "수정자 사용자 ID. 현재 인증 도입 전 수동 전달하는 선택값.", example = "1")
        @RequestParam(required = false)
        Long updatedBy
    ) {
        service.cancel(
            courseId,
            batchId,
            applicationId,
            updatedBy
        );
    }

    @Operation(
        summary = "기존 훈련생 검색",
        description = "이름/연락처로 기존 훈련생을 검색합니다. 선택한 traineeId를 inputType=TRAINEE 요청의 " + "sourceTraineeId에 넣습니다."
    )
    @GetMapping("/trainees")
    public List<AdmissionTraineeDTO> trainees(
        @Parameter(description = "이름 또는 연락처 검색어. 생략하면 검색어로 제한하지 않습니다.", example = "홍길동")
        @RequestParam(required = false)
        String keyword,
        @Parameter(description = "조회할 최대 건수: 1~200, 기본값 50.", example = "50")
        @RequestParam(defaultValue = "50")
        int limit,
        @Parameter(description = "건너뛸 건수: 0 이상. 첫 페이지는 0.", example = "0")
        @RequestParam(defaultValue = "0")
        int offset
    ) {
        return service.trainees(keyword, limit, offset);
    }

    @Operation(
        summary = "기존 훈련생 상세",
        description = "선택한 훈련생의 기본정보를 조회해 접수 입력값으로 사용합니다."
    )
    @GetMapping("/trainees/{traineeId}")
    public AdmissionTraineeDTO trainee(
        @Parameter(description = "기존 훈련생 ID.", example = "1")
        @PathVariable
        Long traineeId
    ) {
        return service.trainee(traineeId);
    }

    @Operation(
        summary = "기존 회원 검색",
        description = "사용 중인 회원의 ID·이름·연락처·이메일을 조회합니다. 선택한 userId를 inputType=MEMBER 요청의 " + "sourceUserId에 넣습니다."
    )
    @GetMapping("/members")
    public List<AdmissionMemberDTO> members(
        @Parameter(description = "이름 또는 연락처 검색어. 생략하면 검색어로 제한하지 않습니다.", example = "홍길동")
        @RequestParam(required = false)
        String keyword,
        @Parameter(description = "조회할 최대 건수: 1~200, 기본값 50.", example = "50")
        @RequestParam(defaultValue = "50")
        int limit,
        @Parameter(description = "건너뛸 건수: 0 이상. 첫 페이지는 0.", example = "0")
        @RequestParam(defaultValue = "0")
        int offset
    ) {
        return service.members(keyword, limit, offset);
    }

    @Operation(
        summary = "기존 회원 상세",
        description = "사용 중인 회원의 기본정보를 조회합니다. 비밀번호는 반환하지 않습니다."
    )
    @GetMapping("/members/{userId}")
    public AdmissionMemberDTO member(
        @Parameter(description = "기존 회원 ID.", example = "1")
        @PathVariable
        Long userId
    ) {
        return service.member(userId);
    }

    @Operation(
        summary = "CSV 입학접수 일괄등록",
        description = "UTF-8 CSV를 file로 업로드합니다. 필수 헤더는 applicantName,birthDate,phone입니다. 최대 " + "2000행/10MB이며 정상 행만 등록하고 실패 행 번호와 사유를 반환합니다. 날짜는 YYYY-MM-DD 형식입니다."
    )
    @PostMapping(value = "/{courseId}/{batchId}/csv", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public AdmissionCsvResultDTO csv(
        @Parameter(description = "훈련과정 ID. 기존 과정 조회에서 확인합니다.", example = "1")
        @PathVariable
        Long courseId,
        @Parameter(description = "훈련기수 ID. courseId와 함께 기수를 식별합니다.", example = "1")
        @PathVariable
        Long batchId,
        @Parameter(description = "multipart/form-data로 전달할 파일. 이미지 최대 10MB, CSV는 UTF-8 형식.")
        @RequestPart("file")
        MultipartFile file,
        @Parameter(description = "등록자 사용자 ID. 현재 인증 도입 전 수동 전달하는 선택값.", example = "1")
        @RequestParam(required = false)
        Long createdBy
    ) {
        return csvService.register(
            courseId,
            batchId,
            file,
            createdBy
        );
    }

    @Operation(
        summary = "지원자 사진 업로드",
        description = "multipart/form-data file에 PNG/JPEG/GIF/WebP 이미지를 전달합니다. 반환된 fileId를 " + "photoFileId로 사용합니다. 최대 10MB입니다."
    )
    @PostMapping(value = "/photos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public CourseFileDTO uploadPhoto(
        @Parameter(description = "multipart/form-data로 전달할 파일. 이미지 최대 10MB, CSV는 UTF-8 형식.")
        @RequestPart("file")
        MultipartFile file,
        @Parameter(description = "등록자 사용자 ID. 현재 인증 도입 전 수동 전달하는 선택값.", example = "1")
        @RequestParam(required = false)
        Long createdBy
    ) {
        return fileService.uploadAdmissionPhoto(file, createdBy);
    }

    @Operation(
        summary = "지원자 사진 보기",
        description = "저장한 지원자 이미지 파일을 반환합니다."
    )
    @GetMapping("/photos/{fileId}/content")
    public ResponseEntity<Resource> photo(
        @Parameter(description = "업로드 응답에서 반환된 파일 ID.", example = "1")
        @PathVariable
        Long fileId
    ) {
        CourseFileDTO file = courseService.file(fileId);
        MediaType type = file.getMimeType() != null && Set.of(
            "image/png",
            "image/jpeg",
            "image/gif",
            "image/webp"
        ).contains(file.getMimeType()) ? MediaType.parseMediaType(file.getMimeType()) : MediaType.APPLICATION_OCTET_STREAM;
        return ResponseEntity.ok().contentType(type).header("X-Content-Type-Options", "nosniff").body(fileService.content(fileId));
    }
}
