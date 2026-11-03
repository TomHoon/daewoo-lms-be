package com.daewoo.lms.training.admission.mapper;

import com.daewoo.lms.training.admission.dto.*;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface AdmissionMapper {

    Long lockBatch(@Param("courseId") Long courseId, @Param("batchId") Long batchId);

    List<AdmissionBatchOptionDTO> batches(@Param("trainingJobCategoryId") Long trainingJobCategoryId, @Param("educationCategoryId") Long educationCategoryId);

    List<AdmissionApplicationDTO> applications(@Param("courseId") Long courseId, @Param("batchId") Long batchId, @Param("keyword") String keyword, @Param("status") String status, @Param("limit") int limit, @Param("offset") int offset);

    AdmissionApplicationDTO application(@Param("courseId") Long courseId, @Param("batchId") Long batchId, @Param("applicationId") Long applicationId);

    Long nextApplicationId(@Param("courseId") Long courseId, @Param("batchId") Long batchId);

    int duplicate(AdmissionApplicationDTO dto);

    int enrollmentCount(@Param("courseId") Long courseId, @Param("batchId") Long batchId, @Param("traineeId") Long traineeId);

    int insertApplication(AdmissionApplicationDTO dto);

    int updateApplication(AdmissionApplicationDTO dto);

    int cancel(@Param("courseId") Long courseId, @Param("batchId") Long batchId, @Param("applicationId") Long applicationId, @Param("updatedBy") Long updatedBy);

    AdmissionTraineeDTO trainee(@Param("traineeId") Long traineeId);

    List<AdmissionTraineeDTO> trainees(@Param("keyword") String keyword, @Param("limit") int limit, @Param("offset") int offset);

    List<AdmissionTraineeDTO> traineesByUser(@Param("userId") Long userId);

    List<AdmissionTraineeDTO> traineesByIdentity(AdmissionApplicationDTO dto);

    int insertTrainee(AdmissionTraineeDTO dto);

    AdmissionMemberDTO member(@Param("userId") Long userId);

    AdmissionMemberDTO lockMember(@Param("userId") Long userId);

    List<AdmissionMemberDTO> members(@Param("keyword") String keyword, @Param("limit") int limit, @Param("offset") int offset);

    int insertEnrollment(AdmissionEnrollmentDTO dto);

    AdmissionEnrollmentDTO enrollment(@Param("courseId") Long courseId, @Param("batchId") Long batchId, @Param("traineeId") Long traineeId);
}
