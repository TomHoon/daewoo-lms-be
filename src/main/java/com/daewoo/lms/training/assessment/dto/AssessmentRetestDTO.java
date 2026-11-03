package com.daewoo.lms.training.assessment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Data;

@Data
@Schema(description = "재시험 생성 요청. 원평가 정보는 복사하고 대상자는 별도 선택합니다.")
public class AssessmentRetestDTO {

    @Schema(description = "원평가 기본정보 중 재시험에서 변경할 값")
    private AssessmentDTO overrides;

    @Schema(
        description = "선택한 평가 대상 훈련생 ID 목록. 선택한 훈련생만 대상자로 등록합니다. 생략 시 기존 목록 유지, []이면 전체 " + "제외합니다. 정식 평가에는 최소 1명이 필요합니다.",
        example = "[1,2]"
    )
    private List<Long> traineeIds;

    @Schema(
        description = "재시험 임시저장 여부. 재시험 생성 시 기본 true(DRAFT). false이면 필수값을 검증해 WAIT로 정식 등록합니다.",
        example = "false"
    )
    private Boolean draft;
}
