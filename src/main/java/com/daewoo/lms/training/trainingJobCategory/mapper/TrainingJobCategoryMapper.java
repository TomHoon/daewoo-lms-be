package com.daewoo.lms.training.trainingJobCategory.mapper;

import com.daewoo.lms.training.trainingJobCategory.dto.TrainingJobCategoryDTO;
import java.util.List;

public interface TrainingJobCategoryMapper {

    List<TrainingJobCategoryDTO> getAll();

    int createTrainingJobCategory(TrainingJobCategoryDTO dto);

    int updateTrainingJobCategory(TrainingJobCategoryDTO dto);

    int deleteTrainingJobCategory(Long trainingJobCategoryId);
}
