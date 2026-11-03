package com.daewoo.lms.training.educationCourseCategory.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EducationCourseDTO {
	private Long trainingJobCategoryId;

	private String categoryName;

	private Integer sortOrder;

	private String incumbentYn;

	private String hiddenYn;

	private String useYn;

	private LocalDateTime createdAt;

	private Long createdBy;

	private LocalDateTime updatedAt;

	private Long updatedBy;
}
