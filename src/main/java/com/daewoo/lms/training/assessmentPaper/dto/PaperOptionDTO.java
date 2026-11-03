package com.daewoo.lms.training.assessmentPaper.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@Schema(description = "문항 보기. 객관식 정답은 하나만 지정할 수 있습니다.")
public class PaperOptionDTO {

    @Schema(description = "훈련과정 ID", example = "1")
    private Long courseId;

    @Schema(description = "훈련기수 ID", example = "1")
    private Long batchId;

    @Schema(description = "평가 ID", example = "1")
    private Long assessmentId;

    @Schema(description = "문항 ID", example = "1")
    private Long questionId;

    @Schema(description = "보기 ID", example = "1")
    private Long optionId;

    @Schema(description = "보기 번호", example = "1")
    private Integer optionNo;

    @Schema(description = "보기 내용", example = "원두의 신선도와 향을 확인한다.")
    private String optionText;

    @Schema(description = "정답 보기 여부", example = "Y", allowableValues = { "Y", "N" })
    private String correctYn;

    @Schema(
        description = "삭제 여부. N=활성, Y=삭제 표시. 문항 삭제 시 해당 보기들도 삭제 표시됩니다.",
        example = "Y",
        allowableValues = { "Y", "N" }
    )
    private String deletedYn;

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
