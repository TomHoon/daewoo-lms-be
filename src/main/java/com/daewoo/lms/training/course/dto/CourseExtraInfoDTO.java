package com.daewoo.lms.training.course.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.*;
import lombok.Data;

@Data
@Schema(description = "기수의 추가정보 항목")
public class CourseExtraInfoDTO {

    @Schema(description = "훈련과정 ID", example = "1")
    private Long courseId;

    @Schema(description = "훈련기수 ID", example = "1")
    private Long batchId;

    @Schema(description = "추가정보 ID", example = "1")
    private Long extraInfoId;

    @Schema(description = "추가정보 항목명", example = "준비물")
    private String itemName;

    @Schema(description = "추가정보 내용", example = "노트북")
    private String itemValue;

    @Schema(description = "정렬순서", example = "0")
    private Integer sortOrder;

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
