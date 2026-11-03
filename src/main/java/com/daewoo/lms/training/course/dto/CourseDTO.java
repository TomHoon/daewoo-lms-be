package com.daewoo.lms.training.course.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.*;
import lombok.Data;

@Data
@Schema(description = "과정 공통정보. 변경 내용은 같은 과정의 모든 기수에 적용됩니다.")
public class CourseDTO {

    @Schema(description = "훈련과정 ID", example = "1")
    private Long courseId;

    @Schema(description = "훈련직종 분류 ID", example = "1")
    private Long trainingJobCategoryId;

    @Schema(description = "교육과정 분류 ID", example = "1")
    private Long educationCategoryId;

    @Schema(description = "과정명", example = "JAVA 개발 실무")
    private String courseName;

    @Schema(description = "과정유형 코드")
    private String courseType;

    @Schema(description = "과정명 구분")
    private String courseDivision;

    @Schema(description = "과정 표시 색상", example = "#3366ff")
    private String courseColor;

    @Schema(description = "설명·안내 내용", example = "평가 진행 전 안내사항을 확인하세요.")
    private String description;

    @Schema(description = "총 훈련시간", example = "0")
    private Integer totalHours;

    @Schema(description = "과정소개")
    private String introduction;

    @Schema(description = "지원자격")
    private String qualification;

    @Schema(description = "교육비·환급 안내")
    private String tuitionRefundInfo;

    @Schema(description = "교육내용")
    private String curriculumContent;

    @Schema(description = "수료 후 진로")
    private String careerPath;

    @Schema(description = "관련 자격증")
    private String relatedCertification;

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
