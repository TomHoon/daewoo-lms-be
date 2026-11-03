package com.daewoo.lms.training.ncs.service;

import com.daewoo.lms.training.ncs.dto.*;
import com.daewoo.lms.training.ncs.mapper.NcsMapper;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NcsService {

    private final NcsMapper mapper;

    private static final Set<String> IMMUTABLE = Set.of(
        "courseId",
        "batchId",
        "subjectId",
        "unitId",
        "elementId",
        "createdAt",
        "createdBy",
        "updatedAt",
        "elements"
    );

    public NcsContextDTO context(Long courseId, Long batchId) {
        return found(mapper.context(courseId, batchId), "과정·기수를 찾을 수 없습니다.");
    }

    public List<NcsSubjectDTO> subjects(Long courseId, Long batchId) {
        context(courseId, batchId);
        return mapper.subjects(courseId);
    }

    public List<NcsUnitDTO> units(Long courseId, Long batchId, Long subjectId, boolean includeInactive) {
        context(courseId, batchId);
        if (subjectId != null)
            subject(courseId, subjectId);
        List<NcsUnitDTO> result = mapper.units(
            courseId,
            batchId,
            subjectId,
            includeInactive
        );
        for (NcsUnitDTO unit : result) unit.setElements(mapper.elements(unit));
        return result;
    }

    public NcsUnitDTO unit(Long courseId, Long batchId, Long subjectId, Long unitId) {
        NcsUnitDTO unit = found(mapper.unit(key(
            courseId,
            batchId,
            subjectId,
            unitId
        )), "능력단위를 찾을 수 없습니다.");
        unit.setElements(mapper.elements(unit));
        return unit;
    }

    // Called through the Spring proxy once per unit, including its complete element changes.
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public NcsUnitDTO save(Long courseId, Long batchId, Long subjectId, NcsUnitDTO request) {
        check(request != null, "능력단위 입력값이 필요합니다.");
        lockScope(courseId, batchId, subjectId);
        boolean creating = request.getUnitId() == null;
        NcsUnitDTO current = creating ? new NcsUnitDTO() : found(mapper.unit(key(
            courseId,
            batchId,
            subjectId,
            request.getUnitId()
        )), "능력단위를 찾을 수 없습니다.");
        if ("N".equals(request.getUseYn())) {
            check(!creating, "새 능력단위는 비활성 상태로 등록할 수 없습니다.");
            current.setUpdatedBy(request.getUpdatedBy());
            mapper.disableElements(current);
            mapper.disableUnit(current);
            return unit(
                courseId,
                batchId,
                subjectId,
                current.getUnitId()
            );
        }
        if (!creating)
            check("Y".equals(current.getUseYn()), "비활성화된 능력단위는 수정할 수 없습니다.");
        merge(request, current);
        current.setCourseId(courseId);
        current.setBatchId(batchId);
        current.setSubjectId(subjectId);
        if (creating) {
            current.setUnitId(mapper.nextUnitId(current));
            current.setCreatedBy(request.getCreatedBy());
            defaults(current);
        }
        validateUnit(current);
        if (creating)
            mapper.insertUnit(current);
        else
            mapper.updateUnit(current);
        if (request.getElements() != null)
            saveElements(current, request.getElements());
        return unit(
            courseId,
            batchId,
            subjectId,
            current.getUnitId()
        );
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void disable(Long courseId, Long batchId, Long subjectId, Long unitId, Long updatedBy) {
        lockScope(courseId, batchId, subjectId);
        NcsUnitDTO current = found(mapper.unit(key(
            courseId,
            batchId,
            subjectId,
            unitId
        )), "능력단위를 찾을 수 없습니다.");
        current.setUpdatedBy(updatedBy);
        mapper.disableElements(current);
        mapper.disableUnit(current);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public NcsElementDTO saveElement(Long courseId, Long batchId, Long subjectId, Long unitId, NcsElementDTO request) {
        check(request != null, "능력단위 요소 입력값이 필요합니다.");
        lockScope(courseId, batchId, subjectId);
        NcsUnitDTO unit = activeUnit(
            courseId,
            batchId,
            subjectId,
            unitId
        );
        NcsElementDTO saved = prepareElement(unit, request, mapper.nextElementId(unit));
        persistElement(saved, request.getElementId() == null);
        return mapper.element(saved);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void disableElement(Long courseId, Long batchId, Long subjectId, Long unitId, Long elementId, Long updatedBy) {
        lockScope(courseId, batchId, subjectId);
        NcsUnitDTO unit = found(mapper.unit(key(
            courseId,
            batchId,
            subjectId,
            unitId
        )), "능력단위를 찾을 수 없습니다.");
        NcsElementDTO element = new NcsElementDTO();
        elementScope(unit, element);
        element.setElementId(elementId);
        found(mapper.element(element), "능력단위 요소를 찾을 수 없습니다.");
        element.setUpdatedBy(updatedBy);
        mapper.disableElement(element);
    }

    private void saveElements(NcsUnitDTO unit, List<NcsElementDTO> requests) {
        check(requests.size() <= 2000, "능력단위 요소는 한 번에 최대 2000개까지 저장할 수 있습니다.");
        Set<Long> supplied = new HashSet<>();
        List<NcsElementDTO> prepared = new ArrayList<>();
        long nextId = mapper.nextElementId(unit);
        for (NcsElementDTO request : requests) {
            check(request != null, "능력단위 요소 입력값이 비어 있습니다.");
            if (request.getElementId() != null)
                check(supplied.add(request.getElementId()), "같은 요소 ID가 두 번 전달되었습니다.");
            NcsElementDTO element = prepareElement(unit, request, nextId);
            if (request.getElementId() == null)
                nextId++;
            prepared.add(element);
        }
        List<Long> activeIds = new ArrayList<>();
        for (int i = 0; i < prepared.size(); i++) {
            NcsElementDTO element = prepared.get(i);
            persistElement(element, requests.get(i).getElementId() == null);
            if ("Y".equals(element.getUseYn()))
                activeIds.add(element.getElementId());
        }
        // Keep IDs stable; elements removed from a supplied complete list are soft-deleted.
        mapper.disableMissingElements(unit, activeIds);
    }

    private NcsElementDTO prepareElement(NcsUnitDTO unit, NcsElementDTO request, Long nextId) {
        boolean creating = request.getElementId() == null;
        NcsElementDTO scoped = new NcsElementDTO();
        elementScope(unit, scoped);
        scoped.setElementId(request.getElementId());
        NcsElementDTO current = creating ? scoped : found(mapper.element(scoped), "해당 능력단위의 요소를 찾을 수 없습니다.");
        if ("N".equals(request.getUseYn())) {
            check(!creating, "새 요소는 비활성 상태로 등록할 수 없습니다.");
            current.setUseYn("N");
            current.setUpdatedBy(request.getUpdatedBy() != null ? request.getUpdatedBy() : unit.getUpdatedBy());
            return current;
        }
        if (!creating)
            check("Y".equals(current.getUseYn()), "비활성화된 요소는 수정할 수 없습니다.");
        merge(request, current);
        if (creating) {
            current.setElementId(nextId);
            current.setCreatedBy(
                request.getCreatedBy() != null ? request.getCreatedBy() : unit.getUpdatedBy() != null ? unit.getUpdatedBy() : unit.getCreatedBy()
            );
            if (current.getSortOrder() == null)
                current.setSortOrder(0);
            if (current.getUseYn() == null)
                current.setUseYn("Y");
        }
        if (current.getUpdatedBy() == null)
            current.setUpdatedBy(unit.getUpdatedBy());
        text(
            current.getElementName(),
            300,
            true,
            "능력단위 요소명"
        );
        nonnegative(current.getSortOrder(), "요소 정렬순서");
        yn(current.getUseYn(), "요소 사용여부");
        return current;
    }

    private void persistElement(NcsElementDTO element, boolean creating) {
        if ("N".equals(element.getUseYn()))
            mapper.disableElement(element);
        else if (creating)
            mapper.insertElement(element);
        else
            mapper.updateElement(element);
    }

    private NcsUnitDTO activeUnit(Long courseId, Long batchId, Long subjectId, Long unitId) {
        NcsUnitDTO unit = found(mapper.unit(key(
            courseId,
            batchId,
            subjectId,
            unitId
        )), "능력단위를 찾을 수 없습니다.");
        check("Y".equals(unit.getUseYn()), "비활성화된 능력단위에는 요소를 등록할 수 없습니다.");
        return unit;
    }

    private void lockScope(Long courseId, Long batchId, Long subjectId) {
        check(
            courseId != null && courseId > 0 && batchId != null && batchId > 0 && subjectId != null && subjectId > 0,
            "과정·기수·교과목을 선택해 주세요."
        );
        // Serializes ID allocation and all NCS writes within a batch without a schema change.
        found(mapper.lockBatch(courseId, batchId), "과정·기수를 찾을 수 없습니다.");
        NcsSubjectDTO subject = subject(courseId, subjectId);
        check("Y".equals(subject.getUseYn()), "사용 중인 교과목을 선택해 주세요.");
    }

    private NcsSubjectDTO subject(Long courseId, Long subjectId) {
        return found(mapper.subject(courseId, subjectId), "해당 과정의 교과목을 찾을 수 없습니다.");
    }

    private static void defaults(NcsUnitDTO dto) {
        if (dto.getTrainingHours() == null)
            dto.setTrainingHours(0);
        if (dto.getSortOrder() == null)
            dto.setSortOrder(0);
        if (dto.getNonNcsYn() == null)
            dto.setNonNcsYn("N");
        if (dto.getPreAssessmentYn() == null)
            dto.setPreAssessmentYn("N");
        if (dto.getMixedTypeYn() == null)
            dto.setMixedTypeYn("N");
        if (dto.getUseYn() == null)
            dto.setUseYn("Y");
    }

    private static void validateUnit(NcsUnitDTO dto) {
        text(
            dto.getUnitName(),
            250,
            true,
            "능력단위명"
        );
        text(
            dto.getClassificationNo(),
            100,
            false,
            "분류번호"
        );
        nonnegative(dto.getTrainingHours(), "훈련시간");
        nonnegative(dto.getSortOrder(), "정렬순서");
        yn(dto.getNonNcsYn(), "비NCS 여부");
        yn(dto.getPreAssessmentYn(), "사전평가 여부");
        yn(dto.getMixedTypeYn(), "혼합형 여부");
        yn(dto.getUseYn(), "사용여부");
    }

    private static void text(String value, int max, boolean required, String name) {
        check(!required || (value != null && !value.isBlank()), name + "이 필요합니다.");
        check(value == null || value.codePointCount(0, value.length()) <= max, name + "은 최대 " + max + "자입니다.");
    }

    private static void nonnegative(Integer value, String name) {
        check(value != null && value >= 0, name + "은 0 이상이어야 합니다.");
    }

    private static void yn(String value, String name) {
        check(value != null && Set.of("Y", "N").contains(value), name + "는 Y/N이어야 합니다.");
    }

    private static void merge(Object source, Object target) {
        BeanWrapperImpl from = new BeanWrapperImpl(source);
        BeanWrapperImpl to = new BeanWrapperImpl(target);
        for (var property : from.getPropertyDescriptors()) {
            String name = property.getName();
            if (IMMUTABLE.contains(name) || !to.isWritableProperty(name))
                continue;
            Object value = from.getPropertyValue(name);
            if (value != null)
                to.setPropertyValue(name, value);
        }
    }

    private static NcsUnitDTO key(Long courseId, Long batchId, Long subjectId, Long unitId) {
        NcsUnitDTO key = new NcsUnitDTO();
        key.setCourseId(courseId);
        key.setBatchId(batchId);
        key.setSubjectId(subjectId);
        key.setUnitId(unitId);
        return key;
    }

    private static void elementScope(NcsUnitDTO unit, NcsElementDTO element) {
        element.setCourseId(unit.getCourseId());
        element.setBatchId(unit.getBatchId());
        element.setSubjectId(unit.getSubjectId());
        element.setUnitId(unit.getUnitId());
    }

    private static void check(boolean condition, String message) {
        if (!condition)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }

    private static <T> T found(T value, String message) {
        if (value == null)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, message);
        return value;
    }
}
