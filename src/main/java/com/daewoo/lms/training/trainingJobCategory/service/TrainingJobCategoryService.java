package com.daewoo.lms.training.trainingJobCategory.service;

import com.daewoo.lms.training.trainingJobCategory.dto.TrainingJobCategoryDTO;
import com.daewoo.lms.training.trainingJobCategory.mapper.TrainingJobCategoryMapper;
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
public class TrainingJobCategoryService {

    private final TrainingJobCategoryMapper trainingJobCategoryMapper;

    @Transactional(readOnly = true)
    public List<TrainingJobCategoryDTO> getAll() {
        return trainingJobCategoryMapper.getAll();
    }

    public boolean createTrainingJobCategories(List<TrainingJobCategoryDTO> list) {
        for (TrainingJobCategoryDTO dto : list) {
            if (trainingJobCategoryMapper.createTrainingJobCategory(dto) <= 0) {
                return false;
            }
        }
        return true;
    }

    @Transactional
    public boolean updateTrainingJobCategory(List<TrainingJobCategoryDTO> list) {
        if (list == null || list.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "수정할 훈련직종 분류 목록이 필요합니다.");
        }
        Set<Long> ids = new HashSet<>();
        for (TrainingJobCategoryDTO dto : list) {
            if (dto == null || dto.getTrainingJobCategoryId() == null || dto.getTrainingJobCategoryId() <= 0 || !ids.add(dto.getTrainingJobCategoryId())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "중복되지 않는 유효한 trainingJobCategoryId가 필요합니다.");
            }
        }
        for (TrainingJobCategoryDTO dto : list) {
            if (trainingJobCategoryMapper.updateTrainingJobCategory(dto) == 0) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "수정할 훈련직종 분류를 찾을 수 없습니다: " + dto.getTrainingJobCategoryId());
            }
        }
        return true;
    }

    public boolean deleteTrainingJobCategory(Long trainingJobCategoryId) {
        return trainingJobCategoryMapper.deleteTrainingJobCategory(trainingJobCategoryId) > 0;
    }

    public boolean createTrainingJobCategory(TrainingJobCategoryDTO dto) {
        return trainingJobCategoryMapper.createTrainingJobCategory(dto) > 0;
    }
}
