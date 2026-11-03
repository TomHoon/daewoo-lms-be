package com.daewoo.lms.training.assessmentPaper.controller;

import com.daewoo.lms.training.assessmentPaper.dto.*;
import com.daewoo.lms.training.assessmentPaper.service.AssessmentPaperService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Tag(name = "07. NCS 평가지·문항", description = "평가지 문항·보기·정답·배점과 수행정도 기준 관리")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/assessments/{courseId}/{batchId}/{assessmentId}/paper")
public class AssessmentPaperController {

    private final AssessmentPaperService service;

    @Operation(
        summary = "NCS 평가지 상세",
        description = "평가 기본정보, 문항·보기, 실제 총 문항수·배점 합계, 수행정도 기준을 조회합니다. 관리자용으로 정답을 포함합니다."
    )
    @GetMapping
    public PaperDetailDTO detail(
        @Parameter(description = "훈련과정 ID. 기존 과정 조회에서 확인합니다.", example = "1")
        @PathVariable
        Long courseId,
        @Parameter(description = "훈련기수 ID. courseId와 함께 기수를 식별합니다.", example = "1")
        @PathVariable
        Long batchId,
        @Parameter(description = "해당 과정·기수의 평가 ID. 재시험 생성에서는 원평가 ID.", example = "1")
        @PathVariable
        Long assessmentId,
        @Parameter(description = "true이면 삭제한 문항·보기도 포함합니다. 실제 문항수/배점 합계는 활성 문항 기준입니다.", example = "false")
        @RequestParam(defaultValue = "false")
        boolean includeDeleted
    ) {
        return service.detail(
            courseId,
            batchId,
            assessmentId,
            includeDeleted
        );
    }

    @Operation(
        summary = "NCS 평가지 최초 등록",
        description = "assessment.totalScore, totalQuestionCount와 questions 전체를 전달합니다. 입력 총 " + "문항수·배점과 실제 합계를 검증하고 기본정보·문항·보기·수행기준을 하나의 트랜잭션으로 저장합니다."
    )
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PaperDetailDTO create(
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
        PaperSaveDTO request
    ) {
        return service.save(
            courseId,
            batchId,
            assessmentId,
            request,
            true
        );
    }

    @Operation(
        summary = "NCS 평가지 전체 수정",
        description = "questions 전체 목록을 전달합니다. 누락된 문항/보기는 DELETED_YN=Y로 비활성화합니다. 객관식 단일 정답, " + "나머지 유형 모범답안을 검증합니다. TESTING/END 상태에도 수정 가능하며 CANCEL은 차단합니다."
    )
    @PutMapping
    public PaperDetailDTO update(
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
        PaperSaveDTO request
    ) {
        return service.save(
            courseId,
            batchId,
            assessmentId,
            request,
            false
        );
    }

    @Operation(
        summary = "NCS 문항 비활성화",
        description = "해당 문항과 보기를 비활성화합니다. 본문에 삭제 후 totalQuestionCount와 " + "assessment.totalScore를 전달하고 필요하면 passScore와 levels도 조정합니다. questions는 " + "서버가 남은 문항으로 구성합니다."
    )
    @DeleteMapping("/questions/{questionId}")
    public PaperDetailDTO deleteQuestion(
        @Parameter(description = "훈련과정 ID. 기존 과정 조회에서 확인합니다.", example = "1")
        @PathVariable
        Long courseId,
        @Parameter(description = "훈련기수 ID. courseId와 함께 기수를 식별합니다.", example = "1")
        @PathVariable
        Long batchId,
        @Parameter(description = "해당 과정·기수의 평가 ID. 재시험 생성에서는 원평가 ID.", example = "1")
        @PathVariable
        Long assessmentId,
        @Parameter(description = "해당 평가의 문항 ID.", example = "1")
        @PathVariable
        Long questionId,
        @RequestBody
        PaperSaveDTO request
    ) {
        return service.deleteQuestion(
            courseId,
            batchId,
            assessmentId,
            questionId,
            request
        );
    }

    @Operation(
        summary = "NCS 평가지 평가 취소",
        description = "평가 상태를 CANCEL로 변경합니다. 문항·보기·수행기준은 유지합니다."
    )
    @DeleteMapping
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
}
