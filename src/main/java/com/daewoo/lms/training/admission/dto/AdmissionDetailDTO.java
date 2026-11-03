package com.daewoo.lms.training.admission.dto;

import com.daewoo.lms.training.course.dto.CourseFileDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "접수 상세와 연결된 훈련생·기수·사진")
public class AdmissionDetailDTO {

    @Schema(description = "입학접수별 입력정보")
    private AdmissionApplicationDTO application;

    @Schema(description = "연결된 기존 훈련생 정보")
    private AdmissionTraineeDTO trainee;

    @Schema(description = "훈련생의 과정·기수 연결정보")
    private AdmissionEnrollmentDTO enrollment;

    @Schema(description = "사진 파일 메타데이터")
    private CourseFileDTO photo;
}
