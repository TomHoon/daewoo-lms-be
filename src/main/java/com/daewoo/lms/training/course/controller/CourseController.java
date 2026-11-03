package com.daewoo.lms.training.course.controller;

import com.daewoo.lms.training.course.dto.*;
import com.daewoo.lms.training.course.service.CourseFileService;
import com.daewoo.lms.training.course.service.CourseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "03. 교육과정·기수", description = "기존 과정에 기수를 추가하고 공통정보, 임시저장, 대표이미지를 관리합니다.")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/courses")
public class CourseController {

    private final CourseService service;

    private final CourseFileService fileService;

    @Operation(
        summary = "과정 등록 선택 목록",
        description = "훈련직종 분류, 교육과정 분류, 캠퍼스, 강사 선택 목록을 반환합니다."
    )
    @GetMapping("/options")
    public Map<String, List<CourseOptionDTO>> options() {
        return service.options();
    }

    @Operation(
        summary = "기존 과정 목록",
        description = "사용 중인 기존 과정을 조회합니다. 훈련직종/교육과정 분류 ID를 지정하면 목록을 필터링합니다."
    )
    @GetMapping
    public List<CourseDTO> courses(
        @Parameter(description = "훈련직종 분류 ID. 생략하면 해당 분류로 제한하지 않습니다.", example = "1")
        @RequestParam(required = false)
        Long trainingJobCategoryId,
        @Parameter(description = "교육과정 분류 ID. 생략하면 해당 분류로 제한하지 않습니다.", example = "1")
        @RequestParam(required = false)
        Long educationCategoryId
    ) {
        return service.courses(trainingJobCategoryId, educationCategoryId);
    }

    @Operation(
        summary = "과정 공통정보 조회",
        description = "과정명, 색상, 과정소개, 지원자격 등 모든 기수가 공유하는 정보를 조회합니다."
    )
    @GetMapping("/{courseId}")
    public CourseDTO course(
        @Parameter(description = "훈련과정 ID. 기존 과정 조회에서 확인합니다.", example = "1")
        @PathVariable
        Long courseId
    ) {
        return service.course(courseId);
    }

    @Operation(
        summary = "과정별 기수 목록",
        description = "선택한 과정에 개설된 기수 목록을 반환합니다."
    )
    @GetMapping("/{courseId}/batches")
    public List<CourseBatchDTO> batches(
        @Parameter(description = "훈련과정 ID. 기존 과정 조회에서 확인합니다.", example = "1")
        @PathVariable
        Long courseId
    ) {
        return service.batches(courseId);
    }

    @Operation(
        summary = "기수 상세 조회",
        description = "과정 공통정보, 기수 정보, 수업요일, 강사, 추가정보, 대표이미지 메타데이터를 함께 반환합니다."
    )
    @GetMapping("/{courseId}/batches/{batchId}")
    public CourseSaveDTO detail(
        @Parameter(description = "훈련과정 ID. 기존 과정 조회에서 확인합니다.", example = "1")
        @PathVariable
        Long courseId,
        @Parameter(description = "훈련기수 ID. courseId와 함께 기수를 식별합니다.", example = "1")
        @PathVariable
        Long batchId
    ) {
        return service.detail(courseId, batchId);
    }

    @Operation(
        summary = "교육과정 신규 등록",
        description = "화면에서 선택한 훈련직종 분류와 교육과정 분류로 새 과정과 첫 기수를 함께 생성합니다. " + "course.trainingJobCategoryId, course.educationCategoryId, 과정명과 기수 정보를 "
            + "전달합니다. " + "courseId와 batchId는 서버에서 생성하며, 관련 데이터는 하나의 트랜잭션으로 저장합니다."
    )
    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    public CourseSaveDTO createCourse(
        @RequestBody
        CourseSaveDTO request
    ) {
        return service.createCourse(request);
    }

    @Operation(
        summary = "기수 정식 등록",
        description = "기존 과정에 새 기수를 생성합니다. course 공통정보 변경은 같은 과정의 모든 기수에 반영됩니다. 관련 테이블을 한 " + "트랜잭션으로 저장합니다."
    )
    @PostMapping("/{courseId}/batches")
    @ResponseStatus(HttpStatus.CREATED)
    public CourseSaveDTO create(
        @Parameter(description = "훈련과정 ID. 기존 과정 조회에서 확인합니다.", example = "1")
        @PathVariable
        Long courseId,
        @RequestBody
        CourseSaveDTO request
    ) {
        return service.create(courseId, request);
    }

    @Operation(
        summary = "기수 및 공통정보 수정",
        description = "생략/null인 일반 필드는 유지합니다. days/instructors/extraInfos는 생략하면 유지하고 []이면 " + "삭제하며, 배열을 전달하면 교체합니다. 공통정보 변경은 다른 기수에도 적용됩니다."
    )
    @PutMapping("/{courseId}/batches/{batchId}")
    public CourseSaveDTO update(
        @Parameter(description = "훈련과정 ID. 기존 과정 조회에서 확인합니다.", example = "1")
        @PathVariable
        Long courseId,
        @Parameter(description = "훈련기수 ID. courseId와 함께 기수를 식별합니다.", example = "1")
        @PathVariable
        Long batchId,
        @RequestBody
        CourseSaveDTO request
    ) {
        return service.update(courseId, batchId, request);
    }

    @Operation(
        summary = "기수 실제 삭제",
        description = "기수와 연결된 요일·강사·추가정보·기수 수정 임시저장을 실제 삭제합니다. 공통 과정과 파일은 유지합니다."
    )
    @DeleteMapping("/{courseId}/batches/{batchId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
        @Parameter(description = "훈련과정 ID. 기존 과정 조회에서 확인합니다.", example = "1")
        @PathVariable
        Long courseId,
        @Parameter(description = "훈련기수 ID. courseId와 함께 기수를 식별합니다.", example = "1")
        @PathVariable
        Long batchId
    ) {
        service.delete(courseId, batchId);
    }

    @Operation(
        summary = "기수 임시저장 목록",
        description = "임시저장 ID와 과정/기수 ID, 저장일시를 조회합니다. courseId로 필터링할 수 있습니다."
    )
    @GetMapping("/drafts")
    public List<CourseDraftDTO> drafts(
        @Parameter(description = "훈련과정 ID. 기존 과정 조회에서 확인합니다.", example = "1")
        @RequestParam(required = false)
        Long courseId
    ) {
        return service.drafts(courseId);
    }

    @Operation(
        summary = "기수 임시저장 불러오기",
        description = "임시저장한 입력값을 반환합니다. 아직 공통 과정이나 정식 기수 데이터에 반영되지 않은 내용입니다."
    )
    @GetMapping("/drafts/{draftId}")
    public CourseSaveDTO draft(
        @Parameter(description = "임시저장 목록에서 반환한 임시저장 ID.", example = "1")
        @PathVariable
        Long draftId
    ) {
        return service.draft(draftId);
    }

    @Operation(
        summary = "기수 임시저장 생성",
        description = "최소 입력은 {\"courseId\":1}입니다. 기존 기수 수정 임시저장은 batchId도 지정합니다. 공통 과정과 정식 기수 " + "테이블은 변경하지 않습니다."
    )
    @PostMapping("/drafts")
    @ResponseStatus(HttpStatus.CREATED)
    public CourseDraftDTO createDraft(
        @RequestBody
        CourseSaveDTO request
    ) {
        return service.createDraft(request);
    }

    @Operation(
        summary = "기수 임시저장 교체",
        description = "기존 courseId/batchId를 유지하고 입력 내용 전체를 전달합니다. 정식 과정 정보는 변경하지 않습니다."
    )
    @PutMapping("/drafts/{draftId}")
    public CourseDraftDTO updateDraft(
        @Parameter(description = "임시저장 목록에서 반환한 임시저장 ID.", example = "1")
        @PathVariable
        Long draftId,
        @RequestBody
        CourseSaveDTO request
    ) {
        return service.updateDraft(draftId, request);
    }

    @Operation(
        summary = "임시저장 기수 정식 반영",
        description = "필수값을 검증하고 공통정보·기수·연결 데이터를 반영한 뒤 임시저장을 삭제합니다. 실패하면 전체를 롤백하고 임시저장을 유지합니다."
    )
    @PostMapping("/drafts/{draftId}/publish")
    public CourseSaveDTO publishDraft(
        @Parameter(description = "임시저장 목록에서 반환한 임시저장 ID.", example = "1")
        @PathVariable
        Long draftId
    ) {
        return service.publishDraft(draftId);
    }

    @Operation(
        summary = "기수 임시저장 삭제",
        description = "해당 임시저장만 실제 삭제합니다."
    )
    @DeleteMapping("/drafts/{draftId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteDraft(
        @Parameter(description = "임시저장 목록에서 반환한 임시저장 ID.", example = "1")
        @PathVariable
        Long draftId
    ) {
        service.deleteDraft(draftId);
    }

    @Operation(
        summary = "파일 메타데이터 조회",
        description = "파일명, 저장 경로, 크기, MIME 유형 등을 조회합니다. 실물 파일 다운로드는 content API를 사용합니다."
    )
    @GetMapping("/files/{fileId}")
    public CourseFileDTO file(
        @Parameter(description = "업로드 응답에서 반환된 파일 ID.", example = "1")
        @PathVariable
        Long fileId
    ) {
        return service.file(fileId);
    }

    @Operation(
        summary = "외부 업로드 파일 메타데이터 등록",
        description = "application/json으로 원본 파일명과 저장 경로 등을 등록합니다. 실물 파일은 생성하지 않습니다. 직접 업로드는 " + "multipart 요청을 사용합니다."
    )
    @PostMapping(value = "/files", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public CourseFileDTO createFile(
        @RequestBody
        CourseFileDTO request
    ) {
        return service.createFile(request);
    }

    @Operation(
        summary = "과정 대표이미지 업로드",
        description = "multipart/form-data의 file로 PNG/JPEG/GIF/WebP 이미지를 업로드합니다. 반환된 fileId를 " + "batch.representativeFileId에 지정합니다. 파일당 최대 10MB입니다."
    )
    @PostMapping(value = "/files", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public CourseFileDTO upload(
        @Parameter(description = "multipart/form-data로 전달할 파일. 이미지 최대 10MB, CSV는 UTF-8 형식.")
        @RequestPart("file")
        MultipartFile file,
        @Parameter(description = "등록자 사용자 ID. 현재 인증 도입 전 수동 전달하는 선택값.", example = "1")
        @RequestParam(required = false)
        Long createdBy
    ) {
        return fileService.upload(file, createdBy);
    }

    @Operation(
        summary = "과정 이미지 파일 다운로드",
        description = "설정된 업로드 폴더의 course 하위 파일을 다운로드합니다."
    )
    @GetMapping("/files/{fileId}/content")
    public ResponseEntity<Resource> content(
        @Parameter(description = "업로드 응답에서 반환된 파일 ID.", example = "1")
        @PathVariable
        Long fileId
    ) {
        CourseFileDTO file = service.file(fileId);
        Resource content = fileService.content(fileId);
        // Serve as a download; metadata may have been registered by an external uploader.
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_OCTET_STREAM).header("X-Content-Type-Options", "nosniff").header(
            "Content-Disposition",
            ContentDisposition.attachment().filename(file.getOriginalFileName() == null ? "image" : file.getOriginalFileName(), StandardCharsets.UTF_8).build().toString()
        ).body(content);
    }
}
