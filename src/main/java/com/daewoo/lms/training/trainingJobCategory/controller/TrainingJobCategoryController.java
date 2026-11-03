package com.daewoo.lms.training.trainingJobCategory.controller;

import com.daewoo.lms.training.trainingJobCategory.dto.TrainingJobCategoryDTO;
import com.daewoo.lms.training.trainingJobCategory.service.TrainingJobCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/trainingJobCategories")
public class TrainingJobCategoryController {
	private final TrainingJobCategoryService trainingJobCategoryService;
//
//	@PostMapping
//	public boolean createTrainingJobCategories(@RequestBody List<TrainingJobCategoryDTO> list) {
//		return trainingJobCategoryService.createTrainingJobCategories(list);
//	}

	@PostMapping
	public boolean createTrainingJobCategory(@RequestBody TrainingJobCategoryDTO dto) {
		return trainingJobCategoryService.createTrainingJobCategory(dto);
	}

	@PutMapping
	public boolean updateTrainingJobCategory(@RequestBody TrainingJobCategoryDTO dto) {
		return trainingJobCategoryService.updateTrainingJobCategory(dto);
	}

	@DeleteMapping
	public boolean deleteTrainingJobCategory(@RequestBody TrainingJobCategoryDTO dto) {
		return trainingJobCategoryService.deleteTrainingJobCategory(dto);
	}
}
