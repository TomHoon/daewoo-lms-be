package com.daewoo.lms.training.educationCourseCategory.controller;

import com.daewoo.lms.training.educationCourseCategory.dto.EducationCourseDTO;
import com.daewoo.lms.training.educationCourseCategory.service.EducationCourseCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "02. 교육과정 분류", description = "교육과정 분류 관리")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/educationCategories")
public class EducationCourseCategoryController {

    private final EducationCourseCategoryService eccService;

    @Operation(
        summary = "교육과정 분류 전체 조회",
        description = "숨김 여부와 사용 여부에 관계없이 모든 교육과정 분류를 반환합니다. 정렬순서, 분류 ID 순으로 정렬하며 데이터가 없으면 빈 " + "배열을 반환합니다."
    )
    @GetMapping
    public List<EducationCourseDTO> getAll() {
        return eccService.getAll();
    }

    @Operation(
        summary = "교육과정 분류 등록",
        description = "분류명과 표시 설정을 JSON으로 등록합니다. 처리 결과를 boolean으로 반환합니다."
    )
    @PostMapping
    public boolean createEducationCourse(
        @RequestBody
        EducationCourseDTO dto
    ) {
        return eccService.createEcc(dto);
    }

    @Operation(
        summary = "교육과정 분류 일괄 수정",
        description = "JSON 배열로 수정할 분류 목록을 전달합니다. 각 항목의 educationCategoryId는 필수입니다. " + "하나라도 수정에 실패하면 전체를 롤백하며, 모두 성공하면 true를 반환합니다."
    )
    @PutMapping
    public boolean updateEducationCourseCategory(
        @RequestBody
        List<EducationCourseDTO> list
    ) {
        return eccService.updateEducationCourseCategory(list);
    }

    @Operation(
        summary = "교육과정 분류 삭제",
        description = "현재 구현은 쿼리 또는 폼 파라미터를 DTO에 바인딩합니다. 삭제는 실제 DB 삭제이며 처리 결과는 boolean입니다. 기존 " + "분류 ID 매핑은 DTO와 Mapper 정의를 확인해야 합니다."
    )
    @DeleteMapping("/{educationCategoryId}")
    public boolean deleteEducationCourse(
        @PathVariable
        Long educationCategoryId
    ) {
        return eccService.deleteEducationCourseCategory(educationCategoryId);
    }
}
