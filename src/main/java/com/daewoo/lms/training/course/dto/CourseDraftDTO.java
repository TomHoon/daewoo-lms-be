package com.daewoo.lms.training.course.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@Schema(description = "기수 임시저장 관리정보")
public class CourseDraftDTO {

    @Schema(description = "임시저장 ID", example = "1")
    private Long draftId;

    @Schema(description = "훈련과정 ID", example = "1")
    private Long courseId;

    @Schema(description = "훈련기수 ID", example = "1")
    private Long batchId;

    @Schema(description = "임시저장 입력값 JSON 문자열")
    private String payload;

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
