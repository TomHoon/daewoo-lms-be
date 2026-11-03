package com.daewoo.lms.training.course.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Data;

@Data
@Schema(description = "기수 등록·수정 또는 임시저장 입력정보")
public class CourseSaveDTO {

    @Schema(description = "훈련과정 ID", example = "1")
    private Long courseId;

    @Schema(description = "훈련기수 ID", example = "1")
    private Long batchId;

    @Schema(description = "모든 기수가 공유하는 과정 공통정보. 일부 필드 수정도 가능합니다. 정식 반영 시 같은 과정의 모든 기수에 적용됩니다.")
    private CourseDTO course;

    @Schema(description = "기수 정보")
    private CourseBatchDTO batch;

    @Schema(
        description = "수업요일 코드 목록. MON/TUE/WED/THU/FRI/SAT/SUN. 생략하면 유지하고 []이면 전체 삭제합니다.",
        example = "[\"MON\",\"TUE\",\"WED\",\"THU\",\"FRI\"]"
    )
    private List<String> days;

    @Schema(description = "배정 강사 목록. 기수 저장 시 생략하면 유지하고 []이면 전체 삭제합니다. 최대 10명, 담임은 최대 1명입니다.")
    private List<CourseInstructorDTO> instructors;

    @Schema(description = "추가정보 목록. 기수 저장 시 생략하면 유지하고 []이면 전체 삭제합니다.")
    private List<CourseExtraInfoDTO> extraInfos;

    @Schema(description = "대표이미지 파일 메타데이터. 상세 응답용입니다. 저장할 때에는 batch.representativeFileId를 사용합니다.")
    private CourseFileDTO representativeFile;

    @Schema(description = "대표이미지 연결 해제 여부", example = "false")
    private Boolean removeRepresentativeImage;
}
