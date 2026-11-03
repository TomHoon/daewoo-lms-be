package com.daewoo.lms.training.course.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.*;
import lombok.Data;

@Data
@Schema(description = "기수에 배정된 강사와 담임 여부")
public class CourseInstructorDTO {

    @Schema(description = "훈련과정 ID", example = "1")
    private Long courseId;

    @Schema(description = "훈련기수 ID", example = "1")
    private Long batchId;

    @Schema(description = "강사 ID", example = "1")
    private Long instructorId;

    @Schema(description = "담임교사 여부", example = "Y", allowableValues = { "Y", "N" })
    private String mainYn;

    @Schema(description = "시작일", example = "2026-11-11")
    private LocalDate startDate;

    @Schema(description = "종료일", example = "2026-11-11")
    private LocalDate endDate;

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
