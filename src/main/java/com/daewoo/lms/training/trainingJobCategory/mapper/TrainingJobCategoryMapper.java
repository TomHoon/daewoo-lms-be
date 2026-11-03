package com.daewoo.lms.training.trainingJobCategory.mapper;

import com.daewoo.lms.training.trainingJobCategory.dto.TrainingJobCategoryDTO;

public interface TrainingJobCategoryMapper {
	int createTrainingJobCategory(TrainingJobCategoryDTO dto);
	int updateTrainingJobCategory(TrainingJobCategoryDTO dto);
	int deleteTrainingJobCategory(TrainingJobCategoryDTO dto);
}
