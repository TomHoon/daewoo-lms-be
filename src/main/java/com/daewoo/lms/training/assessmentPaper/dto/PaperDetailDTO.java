package com.daewoo.lms.training.assessmentPaper.dto;

import com.daewoo.lms.training.assessment.dto.AssessmentDTO;
import com.daewoo.lms.training.assessment.dto.AssessmentLevelDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.List;
import lombok.Data;

@Data
@Schema(description = "평가지 상세와 실제 활성 문항수·배점 합계")
public class PaperDetailDTO {

    @Schema(description = "평가 기본정보")
    private AssessmentDTO assessment;

    @Schema(
        description = "입력 또는 조회한 총 문항수. 평가지 저장 시 필수입니다. 삭제 표시 문항을 제외한 실제 문항수와 같아야 합니다.",
        example = "3"
    )
    private Integer totalQuestionCount;

    @Schema(description = "활성 문항의 실제 배점 합계", example = "0")
    private BigDecimal questionScoreTotal;

    @Schema(
        description = "평가지 전체 문항 목록. 전체 활성 문항 목록입니다. 수정 시 누락한 기존 문항은 삭제 표시됩니다. 문항수와 배점 합계가 " + "입력한 총계와 일치해야 합니다."
    )
    private List<PaperQuestionDTO> questions;

    @Schema(description = "수행수준·수행정도 기준 목록. 생략/null이면 유지하고, []이면 전체 삭제합니다. 전달한 배열로 기준 목록을 교체합니다.")
    private List<AssessmentLevelDTO> levels;
}
