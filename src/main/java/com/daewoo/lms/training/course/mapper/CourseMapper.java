package com.daewoo.lms.training.course.mapper;

import com.daewoo.lms.training.course.dto.*;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface CourseMapper {

    List<CourseOptionDTO> trainingJobCategories();

    List<CourseOptionDTO> educationCategories();

    List<CourseOptionDTO> campuses();

    List<CourseOptionDTO> instructors();

    List<CourseDTO> courses(@Param("trainingJobCategoryId") Long trainingJobCategoryId, @Param("educationCategoryId") Long educationCategoryId);

    CourseDTO course(@Param("courseId") Long courseId);

    CourseDTO lockCourse(@Param("courseId") Long courseId);

    List<CourseBatchDTO> batches(@Param("courseId") Long courseId);

    CourseBatchDTO batch(@Param("courseId") Long courseId, @Param("batchId") Long batchId);

    List<String> days(@Param("courseId") Long courseId, @Param("batchId") Long batchId);

    List<CourseInstructorDTO> batchInstructors(@Param("courseId") Long courseId, @Param("batchId") Long batchId);

    List<CourseExtraInfoDTO> extraInfos(@Param("courseId") Long courseId, @Param("batchId") Long batchId);

    Long nextBatchId(@Param("courseId") Long courseId);

    int updateCourse(CourseDTO dto);

    int insertCourse(CourseDTO dto);

    int insertBatch(CourseBatchDTO dto);

    int updateBatch(CourseBatchDTO dto);

    int clearRepresentativeFile(@Param("courseId") Long courseId, @Param("batchId") Long batchId);

    int insertDay(@Param("courseId") Long courseId, @Param("batchId") Long batchId, @Param("day") String day, @Param("createdBy") Long createdBy);

    int insertInstructor(CourseInstructorDTO dto);

    int insertExtraInfo(CourseExtraInfoDTO dto);

    int deleteDays(@Param("courseId") Long courseId, @Param("batchId") Long batchId);

    int deleteInstructors(@Param("courseId") Long courseId, @Param("batchId") Long batchId);

    int deleteExtraInfos(@Param("courseId") Long courseId, @Param("batchId") Long batchId);

    int deleteBatch(@Param("courseId") Long courseId, @Param("batchId") Long batchId);

    CourseFileDTO file(@Param("fileId") Long fileId);

    int insertFile(CourseFileDTO dto);

    List<CourseDraftDTO> drafts(@Param("courseId") Long courseId);

    CourseDraftDTO draft(@Param("draftId") Long draftId);

    CourseDraftDTO lockDraft(@Param("draftId") Long draftId);

    int insertDraft(CourseDraftDTO dto);

    int updateDraft(CourseDraftDTO dto);

    int deleteDraft(@Param("draftId") Long draftId);

    int deleteBatchDrafts(@Param("courseId") Long courseId, @Param("batchId") Long batchId);

    int existsReference(@Param("type") String type, @Param("id") Long id);
}
