package com.daewoo.lms.training.assessment.mapper;

import com.daewoo.lms.training.assessment.dto.*;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface AssessmentMapper {

    Long lockBatch(@Param("courseId") Long courseId, @Param("batchId") Long batchId);

    List<AssessmentDTO> list(@Param("courseId") Long courseId, @Param("batchId") Long batchId, @Param("status") String status, @Param("limit") int limit, @Param("offset") int offset);

    AssessmentDTO get(AssessmentDTO key);

    Long nextId(AssessmentDTO key);

    Integer nextRetestNo(AssessmentDTO key);

    int insert(AssessmentDTO dto);

    int update(AssessmentDTO dto);

    int cancel(AssessmentDTO key);

    List<AssessmentCandidateDTO> candidates(@Param("courseId") Long courseId, @Param("batchId") Long batchId);

    int eligible(@Param("key") AssessmentDTO key, @Param("traineeId") Long traineeId);

    int authorExists(AssessmentDTO dto);

    int planExists(AssessmentDTO dto);

    List<AssessmentTargetDTO> targets(AssessmentDTO key);

    int saveTarget(@Param("key") AssessmentDTO key, @Param("traineeId") Long traineeId);

    int excludeTargets(@Param("key") AssessmentDTO key, @Param("traineeIds") List<Long> traineeIds);

    List<AssessmentSheetDTO> sheets(AssessmentDTO key);

    Long nextSheetId(AssessmentDTO key);

    int insertSheet(AssessmentSheetDTO dto);

    int updateSheet(AssessmentSheetDTO dto);

    int removeSheets(@Param("key") AssessmentDTO key, @Param("ids") List<Long> ids);

    List<AssessmentLevelDTO> levels(AssessmentDTO key);

    Long nextLevelId(AssessmentDTO key);

    int insertLevel(AssessmentLevelDTO dto);

    int updateLevel(AssessmentLevelDTO dto);

    int removeLevels(@Param("key") AssessmentDTO key, @Param("ids") List<Long> ids);
}
