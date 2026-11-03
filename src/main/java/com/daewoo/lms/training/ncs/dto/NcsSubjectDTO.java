package com.daewoo.lms.training.ncs.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@Schema(description = "과정 공통 교과목. 이 API에서는 기존 교과목을 선택합니다.")
public class NcsSubjectDTO {

    @Schema(description = "훈련과정 ID", example = "1")
    private Long courseId;

    @Schema(description = "과정 공통 교과목 ID", example = "1")
    private Long subjectId;

    @Schema(description = "교과목명", example = "바리스타")
    private String subjectName;

    @Schema(description = "교과목 구분")
    private String subjectType;

    @Schema(description = "훈련시간", example = "20")
    private Integer trainingHours;

    @Schema(description = "정렬순서", example = "0")
    private Integer sortOrder;

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
