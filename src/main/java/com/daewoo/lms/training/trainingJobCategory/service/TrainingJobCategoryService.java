package com.daewoo.lms.training.trainingJobCategory.service;

import com.daewoo.lms.training.trainingJobCategory.dto.TrainingJobCategoryDTO;
import com.daewoo.lms.training.trainingJobCategory.mapper.TrainingJobCategoryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TrainingJobCategoryService {
	private final TrainingJobCategoryMapper trainingJobCategoryMapper;

	public boolean createTrainingJobCategories(List<TrainingJobCategoryDTO> list) {
		for (TrainingJobCategoryDTO dto : list) {
			if (trainingJobCategoryMapper.createTrainingJobCategory(dto) <= 0) {
				return false;
			}
		}

		return true;
	}

	public boolean updateTrainingJobCategory(TrainingJobCategoryDTO dto) {
		return trainingJobCategoryMapper.updateTrainingJobCategory(dto) > 0;
	}

	public boolean deleteTrainingJobCategory(TrainingJobCategoryDTO dto) {
		return trainingJobCategoryMapper.deleteTrainingJobCategory(dto) > 0;
	}

	public boolean createTrainingJobCategory(TrainingJobCategoryDTO dto) {
		return trainingJobCategoryMapper.createTrainingJobCategory(dto) > 0;
	}
}
