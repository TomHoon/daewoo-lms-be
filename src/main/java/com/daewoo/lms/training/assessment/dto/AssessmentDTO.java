package com.daewoo.lms.training.assessment.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.*;
import lombok.Data;

@Data
@Schema(description = "평가 기본정보와 운영 설정. 비밀번호는 요청 전용입니다.")
public class AssessmentDTO {

    @Schema(description = "훈련과정 ID", example = "1")
    private Long courseId;

    @Schema(description = "훈련기수 ID", example = "1")
    private Long batchId;

    @Schema(description = "평가 ID", example = "1")
    private Long assessmentId;

    @Schema(description = "평가계획 ID", example = "1")
    private Long planId;

    @Schema(description = "과정 공통 교과목 ID", example = "1")
    private Long subjectId;

    @Schema(description = "능력단위 ID", example = "1")
    private Long unitId;

    @Schema(description = "능력단위 요소 ID", example = "1")
    private Long elementId;

    @Schema(description = "평가 대상 수준", example = "UNIT", allowableValues = { "UNIT", "ELEMENT" })
    private String targetLevel;

    @Schema(description = "평가명·시험명", example = "커피 원두 선택 최종평가")
    private String assessmentName;

    @Schema(description = "평가구분", example = "PRE", allowableValues = { "PRE", "MID", "FINAL" })
    private String assessmentType;

    @Schema(
        description = "평가도구 유형",
        example = "ESSAY",
        allowableValues = { "ESSAY", "SCENARIO", "CHECKLIST", "PORTFOLIO" }
    )
    private String toolType;

    @Schema(description = "평가지 유형 코드", example = "ESSAY")
    private String sheetType;

    @Schema(description = "출제 강사 ID", example = "1")
    private Long authorInstructorId;

    @Schema(description = "평가일자", example = "2026-11-11")
    private LocalDate assessmentDate;

    @Schema(description = "평가 시작일시", example = "2026-11-11T09:00:00")
    private LocalDateTime startDatetime;

    @Schema(description = "평가 종료일시", example = "2026-11-11T09:00:00")
    private LocalDateTime endDatetime;

    @Schema(description = "평가시간(분)", example = "60")
    private Integer examMinutes;

    @Schema(description = "총 배점", example = "100")
    private BigDecimal totalScore;

    @Schema(description = "패스점수·합격점수", example = "60")
    private BigDecimal passScore;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Schema(
        description = "평가 응시 비밀번호. 요청 전용입니다. 저장 시 해시 처리하며 응답에 포함하지 않습니다. 수정 시 생략하면 유지, 빈 " + "문자열이면 해제합니다.",
        example = "1234",
        accessMode = Schema.AccessMode.WRITE_ONLY
    )
    private String password;

    @Schema(description = "학생 자가진단 사용 여부", example = "Y", allowableValues = { "Y", "N" })
    private String selfDiagnosisYn;

    @Schema(description = "재시험 여부", example = "Y", allowableValues = { "Y", "N" })
    private String retestYn;

    @Schema(description = "재시험 차수", example = "0")
    private Integer retestNo;

    @Schema(description = "원평가 ID", example = "1")
    private Long baseAssessmentId;

    @Schema(description = "재시험 가점율(%). 0~100, 소수 둘째 자리까지 입력합니다.", example = "0")
    private BigDecimal retestBonusRate;

    @Schema(
        description = "평가 상태: 임시저장/대기/시험중/종료/취소.",
        example = "DRAFT",
        allowableValues = { "DRAFT", "WAIT", "TESTING", "END", "CANCEL" }
    )
    private String status;

    @Schema(description = "설명·안내 내용", example = "평가 진행 전 안내사항을 확인하세요.")
    private String description;

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
}
