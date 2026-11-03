package com.daewoo.lms.training.educationCourseCategory.service;

import com.daewoo.lms.training.educationCourseCategory.dto.EducationCourseDTO;
import com.daewoo.lms.training.educationCourseCategory.mapper.EducationCourseCategoryMapper;
import java.util.List;
import java.util.HashSet;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class EducationCourseCategoryService {

    private final EducationCourseCategoryMapper eccMapper;

    @Transactional(readOnly = true)
    public List<EducationCourseDTO> getAll() {
        return eccMapper.getAll();
    }

    public boolean createEcc(EducationCourseDTO dto) {
        if (eccMapper.createEcc(dto) <= 0) {
            return false;
        }
        return true;
    }

    @Transactional
    public boolean updateEducationCourseCategory(List<EducationCourseDTO> list) {
        if (list == null || list.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "수정할 교육과정 분류 목록이 필요합니다.");
        }
        Set<Long> ids = new HashSet<>();
        for (EducationCourseDTO dto : list) {
            if (dto == null || dto.getEducationCategoryId() == null || dto.getEducationCategoryId() <= 0 || !ids.add(dto.getEducationCategoryId())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "중복되지 않는 유효한 educationCategoryId가 필요합니다.");
            }
        }
        for (EducationCourseDTO dto : list) {
            if (eccMapper.updateEducationCourseCategory(dto) == 0) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "수정할 교육과정 분류를 찾을 수 없습니다: " + dto.getEducationCategoryId());
            }
        }
        return true;
    }

    public boolean deleteEducationCourseCategory(Long educationCategoryId) {
        return eccMapper.deleteEducationCourseCategory(educationCategoryId) > 0;
    }
}
