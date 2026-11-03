package com.daewoo.lms.training.course.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "선택 목록 항목의 ID와 표시명")
public class CourseOptionDTO {

    @Schema(description = "선택 항목 ID", example = "1")
    private Long id;

    @Schema(description = "이름 또는 선택 항목명")
    private String name;
}
