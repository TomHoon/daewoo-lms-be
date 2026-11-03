package com.daewoo.lms.training.educationCourseCategory.controller;

import com.daewoo.lms.training.educationCourseCategory.dto.EducationCourseDTO;
import com.daewoo.lms.training.educationCourseCategory.service.EducationCourseCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/educationCategories")
public class EducationCourseCategoryController {

	private final EducationCourseCategoryService eccService;

//	@GetMapping
//	public List<Map<String, String>> getAll() {
//		return eccMapper.getAll();
//	}

	@PostMapping
	public boolean createEducationCourse(@RequestBody EducationCourseDTO dto) {
		return eccService.createEcc(dto);
	}

	@PutMapping
	public boolean updateEducationCourseCategory(EducationCourseDTO dto) {
		return eccService.updateEducationCourseCategory(dto);
	}

	@DeleteMapping
	public boolean deleteEducationCourse(EducationCourseDTO dto) {
		return eccService.deleteEducationCourseCategory(dto);
	}

}
