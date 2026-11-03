package com.daewoo.lms.training.educationCourseCategory.mapper;

import com.daewoo.lms.training.educationCourseCategory.dto.EducationCourseDTO;

import java.util.List;
import java.util.Map;

public interface EducationCourseCategoryMapper {
	List<Map<String, String>> getAll();
	int createEcc(EducationCourseDTO dto);
	int updateEducationCourseCategory(EducationCourseDTO dto);
	int deleteEducationCourseCategory(EducationCourseDTO dto);
}
