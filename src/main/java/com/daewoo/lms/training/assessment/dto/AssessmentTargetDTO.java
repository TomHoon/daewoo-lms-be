package com.daewoo.lms.training.assessment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.*;
import lombok.Data;

@Data
@Schema(description = "평가 대상 훈련생과 응시·제외 상태")
public class AssessmentTargetDTO {

    @Schema(description = "훈련과정 ID", example = "1")
    private Long courseId;

    @Schema(description = "훈련기수 ID", example = "1")
    private Long batchId;

    @Schema(description = "평가 ID", example = "1")
    private Long assessmentId;

    @Schema(description = "훈련생 ID", example = "1")
    private Long traineeId;

    @Schema(
        description = "평가 대상자의 응시 상태",
        example = "READY",
        allowableValues = { "READY", "EXCLUDED", "COMPLETE" }
    )
    private String targetStatus;

    @Schema(description = "재시험 대상 여부", example = "Y", allowableValues = { "Y", "N" })
    private String retestTargetYn;

    @Schema(
        description = "등록일시",
        example = "2026-11-03T09:00:00",
        accessMode = Schema.AccessMode.READ_ONLY
    )
    private LocalDateTime createdAt;

    @Schema(description = "훈련생명")
    private String traineeName;
}
