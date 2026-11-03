package com.daewoo.lms.training.assessmentPaper.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

@Data
@Schema(description = "문항 내용·정답·배점과 보기 목록")
public class PaperQuestionDTO {

    @Schema(description = "훈련과정 ID", example = "1")
    private Long courseId;

    @Schema(description = "훈련기수 ID", example = "1")
    private Long batchId;

    @Schema(description = "평가 ID", example = "1")
    private Long assessmentId;

    @Schema(description = "문항 ID", example = "1")
    private Long questionId;

    @Schema(description = "문제 번호", example = "1")
    private Integer questionNo;

    @Schema(
        description = "문항 유형",
        example = "OBJECTIVE",
        allowableValues = { "OBJECTIVE", "SUBJECTIVE", "ESSAY", "CHECKLIST" }
    )
    private String questionType;

    @Schema(description = "문제 내용", example = "커피 원두 선택 기준으로 가장 적절한 것은?")
    private String questionText;

    @Schema(description = "문항 배점", example = "5")
    private BigDecimal score;

    @Schema(description = "정답 또는 모범답안. 객관식은 정답 보기 번호 문자열입니다. 주관식·서술형·체크리스트는 필수 모범답안입니다.", example = "2")
    private String correctAnswer;

    @Schema(description = "정답 해설")
    private String explanation;

    @Schema(
        description = "삭제 여부. N=활성, Y=삭제 표시. 문항 삭제 시 해당 보기들도 삭제 표시됩니다.",
        example = "Y",
        allowableValues = { "Y", "N" }
    )
    private String deletedYn;

    @Schema(
        description = "등록일시",
        example = "2026-11-03T09:00:00",
        accessMode = Schema.AccessMode.READ_ONLY
    )
    private LocalDateTime createdAt;

    @Schema(description = "등록자 사용자 ID", example = "1")
    private Long createdBy;

    @Schema(
        description = "수정일시",
        example = "2026-11-03T10:00:00",
        accessMode = Schema.AccessMode.READ_ONLY
    )
    private LocalDateTime updatedAt;

    @Schema(description = "수정자 사용자 ID", example = "1")
    private Long updatedBy;

    @Schema(
        description = "문항의 보기·항목 수. 조회 시 활성 보기 개수입니다. 저장 요청에서 지정하면 실제 보기 개수와 일치해야 합니다.",
        example = "4"
    )
    private Integer itemCount;

    @Schema(description = "문항의 보기 목록. 생략하면 기존 활성 보기를 유지합니다. 배열을 전달하면 전체 목록을 교체하며 누락된 보기는 삭제 표시합니다.")
    private List<PaperOptionDTO> options;
}
