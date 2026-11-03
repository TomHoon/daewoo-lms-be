package com.daewoo.lms.training.admission.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.*;
import lombok.Data;

@Data
@Schema(description = "입학접수 당시 입력정보. 원본 훈련생과 분리해 보관합니다.")
public class AdmissionApplicationDTO {

    @Schema(description = "훈련과정 ID", example = "1")
    private Long courseId;

    @Schema(description = "훈련기수 ID", example = "1")
    private Long batchId;

    @Schema(description = "입학접수 ID", example = "1")
    private Long applicationId;

    @Schema(description = "훈련생 ID", example = "1")
    private Long traineeId;

    @Schema(description = "선택한 원본 회원 ID. inputType=MEMBER일 때 필수입니다.", example = "1")
    private Long sourceUserId;

    @Schema(
        description = "선택한 원본 훈련생 ID. inputType=TRAINEE일 때 필수입니다. 원본 훈련생 정보는 변경하지 않습니다.",
        example = "1"
    )
    private Long sourceTraineeId;

    @Schema(
        description = "접수 입력유형",
        example = "VISITOR",
        allowableValues = { "VISITOR", "TRAINEE", "MEMBER", "CSV" }
    )
    private String inputType;

    @Schema(
        description = "접수 상태",
        example = "PRE",
        allowableValues = { "PRE", "RECEIVED", "INTERVIEW", "PASS", "FAIL", "CANCEL", "CANDIDATE", "WAIT" }
    )
    private String applicationStatus;

    @Schema(
        description = "승인 상태",
        example = "UNAPPROVED",
        allowableValues = { "UNAPPROVED", "REVIEW", "APPROVED", "REJECTED" }
    )
    private String approvalStatus;

    @Schema(description = "지원자 이름", example = "홍길동")
    private String applicantName;

    @Schema(description = "생년월일", example = "1998-06-12")
    private LocalDate birthDate;

    @Schema(description = "성별 코드", example = "M", allowableValues = { "M", "F" })
    private String gender;

    @Schema(description = "내·외국인 구분", example = "DOMESTIC", allowableValues = { "DOMESTIC", "FOREIGN" })
    private String nationalityType;

    @Schema(description = "대표연락처", example = "01012345678")
    private String phone;

    @Schema(description = "추가 연락처")
    private String extraPhone;

    @Schema(description = "이메일 주소", example = "hong@example.com")
    private String email;

    @Schema(description = "우편번호")
    private String zipCode;

    @Schema(description = "지역")
    private String region;

    @Schema(description = "주소", example = "서울특별시 강서구")
    private String address;

    @Schema(description = "상세주소")
    private String addressDetail;

    @Schema(description = "사진 파일 ID", example = "1")
    private Long photoFileId;

    @Schema(description = "최종학교명")
    private String lastSchoolName;

    @Schema(description = "최종학력", example = "대졸")
    private String educationLevel;

    @Schema(description = "졸업구분 코드", example = "졸업")
    private String graduationType;

    @Schema(description = "지원·유입 경로")
    private String applyRoute;

    @Schema(
        description = "접수방법",
        example = "VISIT",
        allowableValues = { "VISIT", "WEB", "MOBILE", "PHONE", "FAX" }
    )
    private String receptionMethod;

    @Schema(description = "지원자가 남긴 말씀")
    private String applicantMessage;

    @Schema(description = "국민취업지원제도 참여유형 코드", example = "NONE")
    private String employmentSupportType;

    @Schema(description = "국민취업지원제도 참여센터")
    private String employmentSupportCenter;

    @Schema(description = "국민취업지원제도 담당자")
    private String employmentSupportManager;

    @Schema(description = "담당자 연락처")
    private String employmentSupportPhone;

    @Schema(description = "사전평가 여부", example = "Y", allowableValues = { "Y", "N" })
    private String preTestYn;

    @Schema(description = "접수일", example = "2026-11-11")
    private LocalDate applicationDate;

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

    @Schema(description = "사진 연결 해제 여부", example = "false")
    private Boolean removePhoto;
}
