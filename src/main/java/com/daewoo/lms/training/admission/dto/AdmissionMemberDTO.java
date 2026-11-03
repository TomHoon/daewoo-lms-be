package com.daewoo.lms.training.admission.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "선택 가능한 회원 기본정보. 비밀번호는 포함하지 않습니다.")
public class AdmissionMemberDTO {

    @Schema(description = "회원 사용자 ID", example = "1")
    private Long userId;

    @Schema(description = "이름 또는 선택 항목명")
    private String name;

    @Schema(description = "대표연락처", example = "01012345678")
    private String phone;

    @Schema(description = "이메일 주소", example = "hong@example.com")
    private String email;
}
