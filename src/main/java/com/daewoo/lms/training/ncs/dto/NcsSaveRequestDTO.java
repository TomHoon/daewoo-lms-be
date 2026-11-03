package com.daewoo.lms.training.ncs.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Data;

@Data
@Schema(description = "능력단위별로 처리할 일괄 저장 요청")
public class NcsSaveRequestDTO {

    @Schema(description = "저장할 능력단위 목록. 최대 200개. unitId가 없으면 신규 등록하고 있으면 수정합니다. 능력단위별 성공/실패를 반환합니다.")
    private List<NcsUnitDTO> units;
}
