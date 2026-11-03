package com.daewoo.lms.training.educationCourseCategory.repository;

import com.daewoo.lms.training.educationCourseCategory.entity.EducationCourseCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EducationCourseCategoryRepository extends JpaRepository<EducationCourseCategory, Long> {
}
