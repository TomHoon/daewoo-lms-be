package com.daewoo.lms.training.admission.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import lombok.Data;

@Data
@Schema(description = "접수할 과정·기수와 교육기간")
public class AdmissionBatchOptionDTO {

    @Schema(description = "훈련과정 ID", example = "1")
    private Long courseId;

    @Schema(description = "훈련기수 ID", example = "1")
    private Long batchId;

    @Schema(description = "과정명", example = "JAVA 개발 실무")
    private String courseName;

    @Schema(description = "기수명", example = "1기")
    private String batchName;

    @Schema(description = "캠퍼스 ID", example = "1")
    private Long campusId;

    @Schema(description = "시작일", example = "2026-11-11")
    private LocalDate startDate;

    @Schema(description = "종료일", example = "2026-11-11")
    private LocalDate endDate;
}
