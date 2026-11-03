package com.daewoo.lms.training.admission.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "CSV 접수 결과. 정상 행만 등록하고 실패 행과 사유를 반환합니다.")
public record AdmissionCsvResultDTO(@Schema(description = "처리한 데이터 행 수", example = "10") int totalCount, @Schema(description = "등록 성공한 행 수", example = "9") int successCount, @Schema(description = "등록 실패한 행 수", example = "1") int failureCount, @Schema(description = "CSV 행별 처리 결과") List<RowResult> rows) {

    @Schema(description = "CSV 한 행의 접수 결과")
    public record RowResult(@Schema(description = "CSV 원본 행 번호. 헤더 다음 행은 2", example = "2") int rowNumber, @Schema(description = "접수 성공 여부", example = "true") boolean success, @Schema(description = "생성된 접수 ID. 실패 시 null", example = "1") Long applicationId, @Schema(description = "실패 사유. 성공 시 null") String message) {
    }
}
