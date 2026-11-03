package com.daewoo.lms.training.admission.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.*;
import lombok.Data;

@Data
@Schema(description = "훈련생과 과정·기수 연결정보")
public class AdmissionEnrollmentDTO {

    @Schema(description = "훈련과정 ID", example = "1")
    private Long courseId;

    @Schema(description = "훈련기수 ID", example = "1")
    private Long batchId;

    @Schema(description = "훈련생 ID", example = "1")
    private Long traineeId;

    @Schema(description = "입학접수 ID", example = "1")
    private Long applicationId;

    @Schema(description = "과정 등록일", example = "2026-11-11")
    private LocalDate enrollDate;

    @Schema(description = "수료일", example = "2026-11-11")
    private LocalDate completeDate;

    @Schema(
        description = "처리 상태 코드",
        example = "TRAINING",
        allowableValues = { "TRAINING", "COMPLETE", "DROPOUT" }
    )
    private String status;

    @Schema(description = "생활기록부 공개 여부", example = "Y", allowableValues = { "Y", "N" })
    private String studentRecordOpenYn;

    @Schema(
        description = "등록일시",
        example = "2026-11-03T09:00:00",
        accessMode = Schema.AccessMode.READ_ONLY
    )
    private LocalDateTime createdAt;

    @Schema(description = "등록자 사용자 ID", example = "1")
    private Long createdBy;

    @Schema(
        description = "수정일시",
        example = "2026-11-03T10:00:00",
        accessMode = Schema.AccessMode.READ_ONLY
    )
    private LocalDateTime updatedAt;

    @Schema(description = "수정자 사용자 ID", example = "1")
    private Long updatedBy;
}
