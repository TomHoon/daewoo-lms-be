package com.daewoo.lms.training.trainingJobCategory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "훈련직종 분류 정보")
public class TrainingJobCategoryDTO {

    @Schema(description = "훈련직종 분류 ID", example = "1")
    private Long trainingJobCategoryId;

    @Schema(description = "분류명", example = "프로그래밍")
    private String categoryName;

    @Schema(description = "정렬순서", example = "0")
    private Integer sortOrder;

    @Schema(description = "재직자 과정 여부", example = "Y", allowableValues = { "Y", "N" })
    private String incumbentYn;

    @Schema(description = "목록 숨김 여부", example = "Y", allowableValues = { "Y", "N" })
    private String hiddenYn;

    @Schema(description = "사용 여부", example = "Y", allowableValues = { "Y", "N" })
    private String useYn;

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
