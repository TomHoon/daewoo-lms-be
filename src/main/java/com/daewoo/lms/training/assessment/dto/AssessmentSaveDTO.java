package com.daewoo.lms.training.assessment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Data;

@Data
@Schema(description = "평가 기본정보·대상자·평가지 메타정보·수행수준 저장 요청")
public class AssessmentSaveDTO {

    @Schema(description = "평가 기본정보")
    private AssessmentDTO assessment;

    @Schema(
        description = "선택한 평가 대상 훈련생 ID 목록. 선택한 훈련생만 대상자로 등록합니다. 생략 시 기존 목록 유지, []이면 전체 " + "제외합니다. 정식 평가에는 최소 1명이 필요합니다.",
        example = "[1,2]"
    )
    private List<Long> traineeIds;

    @Schema(description = "평가지 메타정보 목록. 문항 내용이 아닌 평가지 메타정보입니다. 생략하면 유지하고 배열을 전달하면 교체합니다.")
    private List<AssessmentSheetDTO> sheets;

    @Schema(description = "수행수준·수행정도 기준 목록. 생략/null이면 유지하고, []이면 전체 삭제합니다. 전달한 배열로 기준 목록을 교체합니다.")
    private List<AssessmentLevelDTO> levels;
}
