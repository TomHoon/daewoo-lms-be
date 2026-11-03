package com.daewoo.lms.training.trainingJobCategory.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainingJobCategoryDTO {

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