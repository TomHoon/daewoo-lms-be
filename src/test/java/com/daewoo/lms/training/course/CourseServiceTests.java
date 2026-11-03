package com.daewoo.lms.training.course;

import com.daewoo.lms.training.course.dto.*;
import com.daewoo.lms.training.course.mapper.CourseMapper;
import com.daewoo.lms.training.course.service.CourseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.json.JsonMapper;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class CourseServiceTests {
    private CourseMapper mapper;
    private CourseService service;

    @BeforeEach
    void setup() {
        mapper = mock(CourseMapper.class);
        service = new CourseService(mapper, JsonMapper.builder().build());
    }

    @Test
    void draftNeedsOnlyExistingCourseAndDoesNotWriteSharedOrBatchData() {
        when(mapper.lockCourse(1L)).thenReturn(new CourseDTO());
        CourseSaveDTO request = new CourseSaveDTO();
        request.setCourseId(1L);
        CourseDTO change = new CourseDTO();
        change.setCourseName("임시 수정 이름");
        request.setCourse(change);
        service.createDraft(request);
        ArgumentCaptor<CourseDraftDTO> saved = ArgumentCaptor.forClass(CourseDraftDTO.class);
        verify(mapper).insertDraft(saved.capture());
        CourseSaveDTO payload = JsonMapper.builder().build().readValue(saved.getValue().getPayload(), CourseSaveDTO.class);
        assertEquals("임시 수정 이름", payload.getCourse().getCourseName());
        assertNull(payload.getBatch());
        verify(mapper, never()).updateCourse(any());
        verify(mapper, never()).insertBatch(any());
    }

    @Test
    void publishWritesSharedCourseAndBatchThenConsumesDraft() {
        CourseSaveDTO request = validRequest();
        request.getBatch().setStartDate(LocalDate.of(2026, 11, 3));
        request.getBatch().setClassStartTime(LocalTime.of(9, 0));
        CourseDTO changed = new CourseDTO();
        changed.setCourseName("정식 수정 이름");
        request.setCourse(changed);
        CourseDraftDTO draft = new CourseDraftDTO();
        draft.setPayload(JsonMapper.builder().build().writeValueAsString(request));
        when(mapper.lockDraft(10L)).thenReturn(draft);
        prepareSave();
        service.publishDraft(10L);
        ArgumentCaptor<CourseDTO> course = ArgumentCaptor.forClass(CourseDTO.class);
        verify(mapper).updateCourse(course.capture());
        assertEquals("정식 수정 이름", course.getValue().getCourseName());
        ArgumentCaptor<CourseBatchDTO> batch = ArgumentCaptor.forClass(CourseBatchDTO.class);
        verify(mapper).insertBatch(batch.capture());
        assertEquals(2L, batch.getValue().getBatchId());
        assertEquals(LocalDate.of(2026, 11, 3), batch.getValue().getStartDate());
        assertEquals(LocalTime.of(9, 0), batch.getValue().getClassStartTime());
        verify(mapper).deleteDraft(10L);
    }

    @Test
    void failedPublishKeepsDraft() {
        CourseSaveDTO request = validRequest();
        request.getBatch().setStatus("DRAFT");
        CourseDraftDTO draft = new CourseDraftDTO();
        draft.setPayload(JsonMapper.builder().build().writeValueAsString(request));
        when(mapper.lockDraft(10L)).thenReturn(draft);
        prepareSave();
        assertThrows(ResponseStatusException.class, () -> service.publishDraft(10L));
        verify(mapper, never()).deleteDraft(anyLong());
        verify(mapper, never()).updateCourse(any());
    }

    @Test
    void duplicateDaysRejectedBeforeWrites() {
        prepareSave();
        CourseSaveDTO request = validRequest();
        request.setDays(List.of("MON", "MON"));
        assertThrows(ResponseStatusException.class, () -> service.create(1L, request));
        verify(mapper, never()).updateCourse(any());
        verify(mapper, never()).insertBatch(any());
    }

    @Test
    void emptyChildrenClearCollectionsWhileOmittedCollectionsRemain() {
        prepareSave();
        CourseSaveDTO request = validRequest();
        request.setDays(List.of());
        service.update(1L, 2L, request);
        verify(mapper).deleteDays(1L, 2L);
        verify(mapper, never()).deleteInstructors(anyLong(), anyLong());
        verify(mapper, never()).deleteExtraInfos(anyLong(), anyLong());
    }

    @Test
    void removingRepresentativeImageExplicitlyClearsForeignKey() {
        prepareSave();
        CourseBatchDTO previous = new CourseBatchDTO();
        previous.setRepresentativeFileId(9L);
        when(mapper.batch(1L, 2L)).thenReturn(previous);
        CourseSaveDTO request = validRequest();
        request.setRemoveRepresentativeImage(true);
        service.update(1L, 2L, request);
        verify(mapper).clearRepresentativeFile(1L, 2L);
        verify(mapper, never()).file(9L);
    }

    @Test
    void deleteRemovesChildrenBeforeBatchAndPreservesCommonCourseAndFiles() {
        when(mapper.lockCourse(1L)).thenReturn(new CourseDTO());
        when(mapper.batch(1L, 2L)).thenReturn(new CourseBatchDTO());
        service.delete(1L, 2L);
        var order = inOrder(mapper);
        order.verify(mapper).lockCourse(1L);
        order.verify(mapper).batch(1L, 2L);
        order.verify(mapper).deleteBatchDrafts(1L, 2L);
        order.verify(mapper).deleteDays(1L, 2L);
        order.verify(mapper).deleteInstructors(1L, 2L);
        order.verify(mapper).deleteExtraInfos(1L, 2L);
        order.verify(mapper).deleteBatch(1L, 2L);
        verifyNoMoreInteractions(mapper);
    }

    @Test
    void newCourseAndFirstBatchAreCreatedUsingSelectedCategories() {
        CourseSaveDTO request = validRequest();
        CourseDTO course = new CourseDTO();
        course.setCourseName("신규 JAVA 과정");
        course.setTrainingJobCategoryId(1L);
        course.setEducationCategoryId(2L);
        request.setCourse(course);
        when(mapper.existsReference(anyString(), anyLong())).thenReturn(1);
        doAnswer(invocation -> {
            CourseDTO inserted = invocation.getArgument(0);
            inserted.setCourseId(10L);
            return 1;
        }).when(mapper).insertCourse(any());
        when(mapper.lockCourse(10L)).thenReturn(course);
        when(mapper.course(10L)).thenReturn(course);
        when(mapper.nextBatchId(10L)).thenReturn(1L);
        when(mapper.batch(10L, 1L)).thenReturn(new CourseBatchDTO());

        CourseSaveDTO result = service.createCourse(request);

        assertEquals(10L, result.getCourseId());
        assertEquals(1L, result.getBatchId());
        assertEquals("Y", course.getUseYn());
        assertEquals("#000000", course.getCourseColor());
        var order = inOrder(mapper);
        order.verify(mapper).insertCourse(course);
        order.verify(mapper).lockCourse(10L);
        verify(mapper).insertBatch(argThat(batch -> batch.getCourseId().equals(10L)
                && batch.getBatchId().equals(1L)));
    }

    @Test
    void newCourseRequiresBothCategoriesBeforeInsertingAnything() {
        CourseSaveDTO request = validRequest();
        CourseDTO course = new CourseDTO();
        course.setCourseName("분류 누락 과정");
        request.setCourse(course);

        assertThrows(ResponseStatusException.class, () -> service.createCourse(request));
        verify(mapper, never()).insertCourse(any());
        verify(mapper, never()).insertBatch(any());
    }

    private void prepareSave() {
        CourseDTO course = new CourseDTO();
        course.setCourseId(1L);
        course.setCourseName("기존 과정");
        course.setTrainingJobCategoryId(1L);
        course.setEducationCategoryId(2L);
        course.setUseYn("Y");
        when(mapper.lockCourse(1L)).thenReturn(course);
        when(mapper.course(1L)).thenReturn(course);
        when(mapper.nextBatchId(1L)).thenReturn(2L);
        when(mapper.existsReference(anyString(), anyLong())).thenReturn(1);
        when(mapper.batch(1L, 2L)).thenReturn(new CourseBatchDTO());
    }

    private CourseSaveDTO validRequest() {
        CourseSaveDTO request = new CourseSaveDTO();
        request.setCourseId(1L);
        CourseBatchDTO batch = new CourseBatchDTO();
        batch.setCampusId(3L);
        batch.setStatus("RECRUITING");
        batch.setRecruitType("FIXED");
        request.setBatch(batch);
        return request;
    }
}
