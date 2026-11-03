package com.daewoo.lms.training.admission.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.*;
import lombok.Data;

@Data
@Schema(description = "기존 훈련생 기본정보")
public class AdmissionTraineeDTO {

    @Schema(description = "훈련생 ID", example = "1")
    private Long traineeId;

    @Schema(description = "회원 사용자 ID", example = "1")
    private Long userId;

    @Schema(description = "훈련생 번호")
    private String traineeNo;

    @Schema(description = "이름 또는 선택 항목명")
    private String name;

    @Schema(description = "생년월일", example = "1998-06-12")
    private LocalDate birthDate;

    @Schema(description = "성별 코드", example = "M", allowableValues = { "M", "F" })
    private String gender;

    @Schema(description = "대표연락처", example = "01012345678")
    private String phone;

    @Schema(description = "이메일 주소", example = "hong@example.com")
    private String email;

    @Schema(description = "주소", example = "서울특별시 강서구")
    private String address;

    @Schema(description = "처리 상태 코드", example = "APPLICANT")
    private String status;

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
