package com.daewoo.lms.training.assessment.controller;

import com.daewoo.lms.training.assessment.dto.*;
import com.daewoo.lms.training.assessment.service.AssessmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Tag(name = "06. NCS 평가 등록", description = "평가 기본정보·대상 훈련생·평가지 메타정보·수행수준 관리")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/assessments/{courseId}/{batchId}")
public class AssessmentController {

    private final AssessmentService service;

    @Operation(
        summary = "NCS 평가 목록",
        description = "기수별 평가를 조회합니다. 상태 DRAFT/WAIT/TESTING/END/CANCEL로 필터링할 수 있습니다. 비밀번호는 " + "반환하지 않습니다."
    )
    @GetMapping
    public List<AssessmentDTO> list(
        @Parameter(description = "훈련과정 ID. 기존 과정 조회에서 확인합니다.", example = "1")
        @PathVariable
        Long courseId,
        @Parameter(description = "훈련기수 ID. courseId와 함께 기수를 식별합니다.", example = "1")
        @PathVariable
        Long batchId,
        @Parameter(description = "평가 상태: DRAFT/WAIT/TESTING/END/CANCEL. 생략하면 전체.", example = "WAIT")
        @RequestParam(required = false)
        String status,
        @Parameter(description = "조회할 최대 건수: 1~200, 기본값 50.", example = "50")
        @RequestParam(defaultValue = "50")
        int limit,
        @Parameter(description = "건너뛸 건수: 0 이상. 첫 페이지는 0.", example = "0")
        @RequestParam(defaultValue = "0")
        int offset
    ) {
        return service.list(
            courseId,
            batchId,
            status,
            limit,
            offset
        );
    }

    @Operation(
        summary = "평가 대상 훈련생 선택 목록",
        description = "해당 과정·기수에서 훈련 중인 훈련생을 반환합니다. 선택한 ID들을 traineeIds로 전달합니다."
    )
    @GetMapping("/candidates")
    public List<AssessmentCandidateDTO> candidates(
        @Parameter(description = "훈련과정 ID. 기존 과정 조회에서 확인합니다.", example = "1")
        @PathVariable
        Long courseId,
        @Parameter(description = "훈련기수 ID. courseId와 함께 기수를 식별합니다.", example = "1")
        @PathVariable
        Long batchId
    ) {
        return service.candidates(courseId, batchId);
    }

    @Operation(
        summary = "NCS 평가 상세",
        description = "평가 기본정보, 대상 훈련생 상태, 평가지 메타정보와 수행수준을 함께 반환합니다."
    )
    @GetMapping("/{assessmentId}")
    public AssessmentDetailDTO detail(
        @Parameter(description = "훈련과정 ID. 기존 과정 조회에서 확인합니다.", example = "1")
        @PathVariable
        Long courseId,
        @Parameter(description = "훈련기수 ID. courseId와 함께 기수를 식별합니다.", example = "1")
        @PathVariable
        Long batchId,
        @Parameter(description = "해당 과정·기수의 평가 ID. 재시험 생성에서는 원평가 ID.", example = "1")
        @PathVariable
        Long assessmentId
    ) {
        return service.detail(courseId, batchId, assessmentId);
    }

    @Operation(
        summary = "NCS 평가 정식 등록",
        description = "평가명·대상 항목·구분·도구·평가지 유형·출제자·일자·평가시간 및 선택 대상자를 검증해 WAIT로 등록합니다. 문항 내용은 " + "평가지 API에서 작성합니다."
    )
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AssessmentDetailDTO create(
        @Parameter(description = "훈련과정 ID. 기존 과정 조회에서 확인합니다.", example = "1")
        @PathVariable
        Long courseId,
        @Parameter(description = "훈련기수 ID. courseId와 함께 기수를 식별합니다.", example = "1")
        @PathVariable
        Long batchId,
        @RequestBody
        AssessmentSaveDTO request
    ) {
        return service.create(
            courseId,
            batchId,
            request,
            false
        );
    }

    @Operation(
        summary = "NCS 평가 임시저장",
        description = "최소 입력은 assessment.subjectId와 unitId입니다. 요소 평가이면 elementId도 지정합니다. 다른 " + "항목이 비어 있어도 DRAFT로 저장합니다."
    )
    @PostMapping("/drafts")
    @ResponseStatus(HttpStatus.CREATED)
    public AssessmentDetailDTO draft(
        @Parameter(description = "훈련과정 ID. 기존 과정 조회에서 확인합니다.", example = "1")
        @PathVariable
        Long courseId,
        @Parameter(description = "훈련기수 ID. courseId와 함께 기수를 식별합니다.", example = "1")
        @PathVariable
        Long batchId,
        @RequestBody
        AssessmentSaveDTO request
    ) {
        return service.create(
            courseId,
            batchId,
            request,
            true
        );
    }

    @Operation(
        summary = "NCS 평가 수정 및 등록",
        description = "DRAFT/WAIT 평가만 수정할 수 있으며 저장 후 WAIT가 됩니다. 일반 필드 null은 유지합니다. 대상자·평가지·수준 " + "배열은 생략하면 유지하고 전달하면 교체합니다."
    )
    @PutMapping("/{assessmentId}")
    public AssessmentDetailDTO update(
        @Parameter(description = "훈련과정 ID. 기존 과정 조회에서 확인합니다.", example = "1")
        @PathVariable
        Long courseId,
        @Parameter(description = "훈련기수 ID. courseId와 함께 기수를 식별합니다.", example = "1")
        @PathVariable
        Long batchId,
        @Parameter(description = "해당 과정·기수의 평가 ID. 재시험 생성에서는 원평가 ID.", example = "1")
        @PathVariable
        Long assessmentId,
        @RequestBody
        AssessmentSaveDTO request
    ) {
        return service.update(
            courseId,
            batchId,
            assessmentId,
            request,
            false
        );
    }

    @Operation(
        summary = "NCS 평가 임시저장 수정",
        description = "DRAFT 평가의 미완성 입력을 저장합니다. 등록된 평가를 DRAFT로 되돌리지는 않습니다."
    )
    @PutMapping("/{assessmentId}/draft")
    public AssessmentDetailDTO updateDraft(
        @Parameter(description = "훈련과정 ID. 기존 과정 조회에서 확인합니다.", example = "1")
        @PathVariable
        Long courseId,
        @Parameter(description = "훈련기수 ID. courseId와 함께 기수를 식별합니다.", example = "1")
        @PathVariable
        Long batchId,
        @Parameter(description = "해당 과정·기수의 평가 ID. 재시험 생성에서는 원평가 ID.", example = "1")
        @PathVariable
        Long assessmentId,
        @RequestBody
        AssessmentSaveDTO request
    ) {
        return service.update(
            courseId,
            batchId,
            assessmentId,
            request,
            true
        );
    }

    @Operation(
        summary = "NCS 평가 임시저장 정식 등록",
        description = "저장된 입력의 필수값과 선택 대상자를 검증한 뒤 DRAFT를 WAIT로 전환합니다."
    )
    @PostMapping("/{assessmentId}/publish")
    public AssessmentDetailDTO publish(
        @Parameter(description = "훈련과정 ID. 기존 과정 조회에서 확인합니다.", example = "1")
        @PathVariable
        Long courseId,
        @Parameter(description = "훈련기수 ID. courseId와 함께 기수를 식별합니다.", example = "1")
        @PathVariable
        Long batchId,
        @Parameter(description = "해당 과정·기수의 평가 ID. 재시험 생성에서는 원평가 ID.", example = "1")
        @PathVariable
        Long assessmentId
    ) {
        return service.publish(courseId, batchId, assessmentId);
    }

    @Operation(
        summary = "NCS 평가 취소",
        description = "STATUS만 CANCEL로 변경하고 평가 대상자·평가지·수행수준 등 연결 데이터는 유지합니다."
    )
    @DeleteMapping("/{assessmentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancel(
        @Parameter(description = "훈련과정 ID. 기존 과정 조회에서 확인합니다.", example = "1")
        @PathVariable
        Long courseId,
        @Parameter(description = "훈련기수 ID. courseId와 함께 기수를 식별합니다.", example = "1")
        @PathVariable
        Long batchId,
        @Parameter(description = "해당 과정·기수의 평가 ID. 재시험 생성에서는 원평가 ID.", example = "1")
        @PathVariable
        Long assessmentId,
        @Parameter(description = "수정자 사용자 ID. 현재 인증 도입 전 수동 전달하는 선택값.", example = "1")
        @RequestParam(required = false)
        Long updatedBy
    ) {
        service.cancel(
            courseId,
            batchId,
            assessmentId,
            updatedBy
        );
    }

    @Operation(
        summary = "NCS 재시험 생성",
        description = "등록된 원평가의 기본정보·평가지 메타정보·수행수준을 복사합니다. 대상자는 traineeIds로 별도 선택합니다. draft " + "기본값은 true이며 false이면 정식 등록합니다. 문항 내용은 복사하지 않습니다."
    )
    @PostMapping("/{assessmentId}/retests")
    @ResponseStatus(HttpStatus.CREATED)
    public AssessmentDetailDTO retest(
        @Parameter(description = "훈련과정 ID. 기존 과정 조회에서 확인합니다.", example = "1")
        @PathVariable
        Long courseId,
        @Parameter(description = "훈련기수 ID. courseId와 함께 기수를 식별합니다.", example = "1")
        @PathVariable
        Long batchId,
        @Parameter(description = "해당 과정·기수의 평가 ID. 재시험 생성에서는 원평가 ID.", example = "1")
        @PathVariable
        Long assessmentId,
        @RequestBody
        AssessmentRetestDTO request
    ) {
        return service.retest(
            courseId,
            batchId,
            assessmentId,
            request
        );
    }
}
