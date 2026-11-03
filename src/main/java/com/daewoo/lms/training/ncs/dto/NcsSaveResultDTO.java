package com.daewoo.lms.training.ncs.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "능력단위별 성공·실패 결과. 실패한 단위만 롤백됩니다.")
public record NcsSaveResultDTO(@Schema(description = "처리한 능력단위 수", example = "2") int totalCount, @Schema(description = "저장 성공한 능력단위 수", example = "1") int successCount, @Schema(description = "저장 실패한 능력단위 수", example = "1") int failureCount, @Schema(description = "능력단위별 처리 결과") List<Item> items) {

    @Schema(description = "능력단위 하나의 저장 결과")
    public record Item(@Schema(description = "요청 units 배열의 0부터 시작하는 인덱스", example = "0") int index, @Schema(description = "저장 성공 여부", example = "true") boolean success, @Schema(description = "저장한 능력단위 ID. 신규 등록 실패 시 null", example = "1") Long unitId, @Schema(description = "CSV 원본 행 번호 목록. 일반 저장에서는 빈 목록", example = "[2,3]") List<Integer> rowNumbers, @Schema(description = "실패 사유. 성공 시 null") String message) {
    }
}
