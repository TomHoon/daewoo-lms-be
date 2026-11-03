package com.daewoo.lms.training.trainingJobCategory.controller;

import com.daewoo.lms.training.trainingJobCategory.dto.TrainingJobCategoryDTO;
import com.daewoo.lms.training.trainingJobCategory.service.TrainingJobCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "01. 훈련직종 분류", description = "훈련직종 분류 등록·수정·삭제")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/trainingJobCategories")
public class TrainingJobCategoryController {

    private final TrainingJobCategoryService trainingJobCategoryService;

    @Operation(
        summary = "훈련직종 분류 전체 조회",
        description = "숨김 여부와 사용 여부에 관계없이 모든 훈련직종 분류를 반환합니다. 정렬순서, 분류 ID 순으로 정렬하며 데이터가 없으면 빈 " + "배열을 반환합니다."
    )
    @GetMapping
    public List<TrainingJobCategoryDTO> getAll() {
        return trainingJobCategoryService.getAll();
    }

    @Operation(
        summary = "훈련직종 분류 등록",
        description = "분류명, 정렬순서, 재직자 과정 여부, 숨김/사용 여부를 JSON으로 입력합니다. 처리 결과를 boolean으로 반환합니다."
    )
    @PostMapping
    public boolean createTrainingJobCategory(
        @RequestBody
        TrainingJobCategoryDTO dto
    ) {
        return trainingJobCategoryService.createTrainingJobCategory(dto);
    }

    @Operation(
        summary = "훈련직종 분류 일괄 수정",
        description = "JSON 배열로 수정할 분류 목록을 전달합니다. 각 항목의 trainingJobCategoryId는 필수입니다. " + "하나라도 수정에 실패하면 전체를 롤백하며, 모두 성공하면 true를 반환합니다."
    )
    @PutMapping
    public boolean updateTrainingJobCategory(
        @RequestBody
        List<TrainingJobCategoryDTO> list
    ) {
        return trainingJobCategoryService.updateTrainingJobCategory(list);
    }

    @Operation(
        summary = "훈련직종 분류 삭제",
        description = "JSON 본문의 trainingJobCategoryId에 해당하는 분류를 DB에서 실제 삭제합니다. 연결 데이터가 있으면 DB " + "제약에 따라 실패할 수 있습니다."
    )
    @DeleteMapping("/{trainingJobCategoryId}")
    public boolean deleteTrainingJobCategory(
        @PathVariable
        Long trainingJobCategoryId
    ) {
        return trainingJobCategoryService.deleteTrainingJobCategory(trainingJobCategoryId);
    }
}
