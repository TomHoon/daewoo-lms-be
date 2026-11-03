package com.daewoo.lms.training.assessmentPaper.mapper;

import com.daewoo.lms.training.assessment.dto.AssessmentDTO;
import com.daewoo.lms.training.assessmentPaper.dto.*;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface AssessmentPaperMapper {

    List<PaperQuestionDTO> questions(@Param("key") AssessmentDTO key, @Param("includeDeleted") boolean includeDeleted);

    List<PaperOptionDTO> options(@Param("key") PaperQuestionDTO key, @Param("includeDeleted") boolean includeDeleted);

    Long nextQuestionId(AssessmentDTO key);

    Long nextOptionId(PaperQuestionDTO key);

    int insertQuestion(PaperQuestionDTO dto);

    int updateQuestion(PaperQuestionDTO dto);

    int insertOption(PaperOptionDTO dto);

    int updateOption(PaperOptionDTO dto);

    int disableMissingQuestions(@Param("key") AssessmentDTO key, @Param("ids") List<Long> ids);

    int disableMissingQuestionOptions(@Param("key") AssessmentDTO key, @Param("ids") List<Long> ids);

    int disableMissingOptions(@Param("key") PaperQuestionDTO key, @Param("ids") List<Long> ids);
}
