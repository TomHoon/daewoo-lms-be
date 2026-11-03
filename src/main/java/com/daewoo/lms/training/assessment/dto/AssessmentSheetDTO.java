package com.daewoo.lms.training.assessment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.*;
import lombok.Data;

@Data
@Schema(description = "평가지 메타정보. 실제 문항은 평가지 API에서 작성합니다.")
public class AssessmentSheetDTO {

    @Schema(description = "훈련과정 ID", example = "1")
    private Long courseId;

    @Schema(description = "훈련기수 ID", example = "1")
    private Long batchId;

    @Schema(description = "평가 ID", example = "1")
    private Long assessmentId;

    @Schema(description = "평가지 메타정보 ID", example = "1")
    private Long sheetId;

    @Schema(description = "평가지명", example = "서술형 평가지")
    private String sheetName;

    @Schema(description = "평가지 유형 코드", example = "ESSAY")
    private String sheetType;

    @Schema(description = "평가지 메타정보의 문항수", example = "10")
    private Integer questionCount;

    @Schema(description = "설명·안내 내용", example = "평가 진행 전 안내사항을 확인하세요.")
    private String description;

    @Schema(
        description = "평가지 작성 상태: 작성중/완료.",
        example = "DRAFT",
        allowableValues = { "DRAFT", "COMPLETE" }
    )
    private String status;

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
