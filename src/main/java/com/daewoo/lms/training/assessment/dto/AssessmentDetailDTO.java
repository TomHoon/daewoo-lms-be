package com.daewoo.lms.training.assessment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Data;

@Data
@Schema(description = "평가 기본정보 및 연결된 대상자·평가지 메타정보·수행수준")
public class AssessmentDetailDTO {

    @Schema(description = "평가 기본정보")
    private AssessmentDTO assessment;

    @Schema(description = "평가 대상 훈련생과 상태")
    private List<AssessmentTargetDTO> targets;

    @Schema(description = "평가지 메타정보 목록. 문항 내용이 아닌 평가지 메타정보입니다. 생략하면 유지하고 배열을 전달하면 교체합니다.")
    private List<AssessmentSheetDTO> sheets;

    @Schema(description = "수행수준·수행정도 기준 목록. 생략/null이면 유지하고, []이면 전체 삭제합니다. 전달한 배열로 기준 목록을 교체합니다.")
    private List<AssessmentLevelDTO> levels;
}
