package com.daewoo.lms.training.assessment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.*;
import lombok.Data;

@Data
@Schema(description = "최소점수별 수행정도 설명")
public class AssessmentLevelDTO {

    @Schema(description = "훈련과정 ID", example = "1")
    private Long courseId;

    @Schema(description = "훈련기수 ID", example = "1")
    private Long batchId;

    @Schema(description = "평가 ID", example = "1")
    private Long assessmentId;

    @Schema(description = "수행수준 ID", example = "1")
    private Long levelId;

    @Schema(description = "수행수준 최소점수", example = "60")
    private BigDecimal minScore;

    @Schema(description = "설명·안내 내용", example = "평가 진행 전 안내사항을 확인하세요.")
    private String description;

    @Schema(description = "정렬순서", example = "0")
    private Integer sortOrder;

    @Schema(
        description = "등록일시",
        example = "2026-11-03T09:00:00",
        accessMode = Schema.AccessMode.READ_ONLY
    )
    private LocalDateTime createdAt;

    @Schema(
        description = "수정일시",
        example = "2026-11-03T10:00:00",
        accessMode = Schema.AccessMode.READ_ONLY
    )
    private LocalDateTime updatedAt;
}
