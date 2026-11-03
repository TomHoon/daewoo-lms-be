package com.daewoo.lms.training.ncs.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

@Data
@Schema(description = "기수별 능력단위와 하위 요소")
public class NcsUnitDTO {

    @Schema(description = "훈련과정 ID", example = "1")
    private Long courseId;

    @Schema(description = "훈련기수 ID", example = "1")
    private Long batchId;

    @Schema(description = "과정 공통 교과목 ID", example = "1")
    private Long subjectId;

    @Schema(description = "능력단위 ID", example = "1")
    private Long unitId;

    @Schema(description = "능력단위명", example = "커피 원두 선택")
    private String unitName;

    @Schema(description = "능력단위 분류번호", example = "1601020101")
    private String classificationNo;

    @Schema(description = "훈련시간", example = "20")
    private Integer trainingHours;

    @Schema(description = "비NCS 여부", example = "Y", allowableValues = { "Y", "N" })
    private String nonNcsYn;

    @Schema(description = "사전평가 대상 여부", example = "Y", allowableValues = { "Y", "N" })
    private String preAssessmentYn;

    @Schema(description = "혼합형 여부", example = "Y", allowableValues = { "Y", "N" })
    private String mixedTypeYn;

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

    @Schema(description = "능력단위 요소 목록. 생략/null이면 유지, []이면 기존 요소 전체 비활성화, 배열이면 목록을 교체합니다.")
    private List<NcsElementDTO> elements;
}
