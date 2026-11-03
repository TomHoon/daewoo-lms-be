package com.daewoo.lms.training.ncs.mapper;

import com.daewoo.lms.training.ncs.dto.*;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface NcsMapper {

    NcsContextDTO context(@Param("courseId") Long courseId, @Param("batchId") Long batchId);

    Long lockBatch(@Param("courseId") Long courseId, @Param("batchId") Long batchId);

    List<NcsSubjectDTO> subjects(@Param("courseId") Long courseId);

    NcsSubjectDTO subject(@Param("courseId") Long courseId, @Param("subjectId") Long subjectId);

    List<NcsUnitDTO> units(@Param("courseId") Long courseId, @Param("batchId") Long batchId, @Param("subjectId") Long subjectId, @Param("includeInactive") boolean includeInactive);

    NcsUnitDTO unit(NcsUnitDTO key);

    List<NcsElementDTO> elements(NcsUnitDTO key);

    NcsElementDTO element(NcsElementDTO key);

    Long nextUnitId(NcsUnitDTO key);

    Long nextElementId(NcsUnitDTO key);

    int insertUnit(NcsUnitDTO dto);

    int updateUnit(NcsUnitDTO dto);

    int insertElement(NcsElementDTO dto);

    int updateElement(NcsElementDTO dto);

    int disableUnit(NcsUnitDTO key);

    int disableElements(NcsUnitDTO key);

    int disableElement(NcsElementDTO key);

    int disableMissingElements(@Param("key") NcsUnitDTO key, @Param("elementIds") List<Long> elementIds);
}
