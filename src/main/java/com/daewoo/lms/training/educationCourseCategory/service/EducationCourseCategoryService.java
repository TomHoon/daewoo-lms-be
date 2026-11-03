package com.daewoo.lms.training.educationCourseCategory.service;

import com.daewoo.lms.training.educationCourseCategory.dto.EducationCourseDTO;
import com.daewoo.lms.training.educationCourseCategory.mapper.EducationCourseCategoryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EducationCourseCategoryService {

	private final EducationCourseCategoryMapper eccMapper;

	public boolean createEcc(EducationCourseDTO dto) {
		if (eccMapper.createEcc(dto) <= 0) {
			return false;
		}

		return true;
	}

	public boolean updateEducationCourseCategory(EducationCourseDTO dto){
		return eccMapper.updateEducationCourseCategory(dto) > 0;
	}

	public boolean deleteEducationCourseCategory(EducationCourseDTO dto){
		return eccMapper.deleteEducationCourseCategory(dto) > 0;
	}

}
