package com.daewoo.lms.training.course.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.*;
import lombok.Data;

@Data
@Schema(description = "업로드한 파일의 메타데이터")
public class CourseFileDTO {

    @Schema(description = "파일 ID", example = "1")
    private Long fileId;

    @Schema(description = "파일 참조 업무유형")
    private String refType;

    @Schema(description = "파일 참조 데이터 ID", example = "1")
    private Long refId;

    @Schema(description = "원본 파일명", example = "photo.png")
    private String originalFileName;

    @Schema(description = "서버에 저장된 파일명")
    private String savedFileName;

    @Schema(description = "서버 파일 저장 경로")
    private String filePath;

    @Schema(description = "파일 크기(바이트)", example = "1")
    private Long fileSize;

    @Schema(description = "파일 확장자", example = "png")
    private String fileExtension;

    @Schema(description = "파일 MIME 유형", example = "image/png")
    private String mimeType;

    @Schema(
        description = "등록일시",
        example = "2026-11-03T09:00:00",
        accessMode = Schema.AccessMode.READ_ONLY
    )
    private LocalDateTime createdAt;

    @Schema(description = "등록자 사용자 ID", example = "1")
    private Long createdBy;
}
