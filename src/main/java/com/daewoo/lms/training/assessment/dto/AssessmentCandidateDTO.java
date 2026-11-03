package com.daewoo.lms.training.assessment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "평가 대상으로 선택할 수 있는 기수 소속 훈련생")
public class AssessmentCandidateDTO {

    @Schema(description = "훈련생 ID", example = "1")
    private Long traineeId;

    @Schema(description = "이름 또는 선택 항목명")
    private String name;

    @Schema(description = "처리 상태 코드")
    private String status;
}
