package com.daewoo.lms.training.course.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.*;
import lombok.Data;

@Data
@Schema(description = "기수별 운영·모집·수강료·훈련수당 정보")
public class CourseBatchDTO {

    @Schema(description = "훈련과정 ID", example = "1")
    private Long courseId;

    @Schema(description = "훈련기수 ID", example = "1")
    private Long batchId;

    @Schema(description = "기수 번호", example = "1")
    private Integer batchNo;

    @Schema(description = "기수명", example = "1기")
    private String batchName;

    @Schema(description = "캠퍼스 ID", example = "1")
    private Long campusId;

    @Schema(description = "훈련코드")
    private String trainingCode;

    @Schema(description = "기수 업무 상태 코드. 정식 등록에는 DRAFT/임시등록을 사용할 수 없습니다.", example = "RECRUITING")
    private String status;

    @Schema(description = "모집분류 코드", example = "FIXED")
    private String recruitType;

    @Schema(description = "시작일", example = "2026-11-11")
    private LocalDate startDate;

    @Schema(description = "종료일", example = "2026-11-11")
    private LocalDate endDate;

    @Schema(description = "모집인원", example = "0")
    private Integer recruitCount;

    @Schema(description = "승인인원", example = "0")
    private Integer approvalCount;

    @Schema(description = "수강료", example = "0")
    private BigDecimal tuitionAmount;

    @Schema(description = "수강료 노출 여부", example = "Y", allowableValues = { "Y", "N" })
    private String tuitionVisibleYn;

    @Schema(description = "정부지원금", example = "0")
    private BigDecimal govSupportAmount;

    @Schema(description = "본인부담금 안내")
    private String selfPaymentDesc;

    @Schema(description = "수당 지급유형 코드", example = "MONTHLY")
    private String allowanceType;

    @Schema(description = "훈련수당 금액", example = "0")
    private BigDecimal allowanceAmount;

    @Schema(description = "수강료·훈련수당 안내")
    private String allowanceDesc;

    @Schema(description = "대표이미지 파일 ID", example = "1")
    private Long representativeFileId;

    @Schema(description = "수업 시작시간", example = "09:00:00")
    private LocalTime classStartTime;

    @Schema(description = "수업 종료시간", example = "09:00:00")
    private LocalTime classEndTime;

    @Schema(description = "하루 수업시간", example = "8")
    private BigDecimal dailyHours;

    @Schema(description = "수업일수", example = "0")
    private Integer trainingDays;

    @Schema(description = "교육장소", example = "1강의실")
    private String trainingPlace;

    @Schema(description = "HRD·고용24 과정 링크")
    private String hrdLink;

    @Schema(description = "생활기록부 공개 여부", example = "Y", allowableValues = { "Y", "N" })
    private String studentRecordOpenYn;

    @Schema(description = "운영대장 출력 여부", example = "Y", allowableValues = { "Y", "N" })
    private String ledgerVisibleYn;

    @Schema(description = "조회수", example = "0")
    private Integer clickCount;

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
