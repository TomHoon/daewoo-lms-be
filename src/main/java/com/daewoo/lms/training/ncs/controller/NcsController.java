package com.daewoo.lms.training.ncs.controller;

import com.daewoo.lms.training.ncs.dto.*;
import com.daewoo.lms.training.ncs.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "05. NCS 평가항목", description = "기존 교과목 아래 기수별 능력단위와 요소를 관리합니다.")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/ncs/{courseId}/{batchId}")
public class NcsController {

    private final NcsService service;

    private final NcsRegistrationService registrationService;

    private final NcsCsvService csvService;

    @Operation(
        summary = "NCS 과정·기수 정보",
        description = "화면 상단에 표시할 과정명, 기수명, 교육기간, 상태를 조회합니다."
    )
    @GetMapping("/context")
    public NcsContextDTO context(
        @Parameter(description = "훈련과정 ID. 기존 과정 조회에서 확인합니다.", example = "1")
        @PathVariable
        Long courseId,
        @Parameter(description = "훈련기수 ID. courseId와 함께 기수를 식별합니다.", example = "1")
        @PathVariable
        Long batchId
    ) {
        return service.context(courseId, batchId);
    }

    @Operation(
        summary = "기존 교과목 선택 목록",
        description = "과정 공통으로 등록된 사용 중인 교과목을 조회합니다. 이 화면에서는 교과목을 새로 만들지 않습니다."
    )
    @GetMapping("/subjects")
    public List<NcsSubjectDTO> subjects(
        @Parameter(description = "훈련과정 ID. 기존 과정 조회에서 확인합니다.", example = "1")
        @PathVariable
        Long courseId,
        @Parameter(description = "훈련기수 ID. courseId와 함께 기수를 식별합니다.", example = "1")
        @PathVariable
        Long batchId
    ) {
        return service.subjects(courseId, batchId);
    }

    @Operation(
        summary = "기수 전체 평가항목 목록",
        description = "해당 과정·기수의 모든 교과목에 등록된 능력단위와 활성 요소를 조회합니다."
    )
    @GetMapping("/units")
    public List<NcsUnitDTO> batchUnits(
        @PathVariable Long courseId,
        @PathVariable Long batchId,
        @Parameter(description = "비활성 능력단위 포함 여부. 하위 요소는 활성 요소만 반환합니다.")
        @RequestParam(defaultValue = "false") boolean includeInactive
    ) {
        return service.units(courseId, batchId, null, includeInactive);
    }

    @Operation(
        summary = "능력단위·요소 목록",
        description = "해당 과정·기수·교과목의 능력단위와 활성 요소를 조회합니다. includeInactive=true이면 비활성 능력단위도 포함합니다."
    )
    @GetMapping("/subjects/{subjectId}/units")
    public List<NcsUnitDTO> units(
        @Parameter(description = "훈련과정 ID. 기존 과정 조회에서 확인합니다.", example = "1")
        @PathVariable
        Long courseId,
        @Parameter(description = "훈련기수 ID. courseId와 함께 기수를 식별합니다.", example = "1")
        @PathVariable
        Long batchId,
        @Parameter(description = "과정 공통 교과목 ID. 교과목 선택 목록에서 확인합니다.", example = "1")
        @PathVariable
        Long subjectId,
        @Parameter(description = "true이면 비활성 능력단위도 포함합니다. 하위 요소는 활성 요소만 반환합니다.", example = "false")
        @RequestParam(defaultValue = "false")
        boolean includeInactive
    ) {
        return service.units(
            courseId,
            batchId,
            subjectId,
            includeInactive
        );
    }

    @Operation(
        summary = "능력단위 상세",
        description = "해당 능력단위와 사용 중인 하위 요소를 조회합니다."
    )
    @GetMapping("/subjects/{subjectId}/units/{unitId}")
    public NcsUnitDTO unit(
        @Parameter(description = "훈련과정 ID. 기존 과정 조회에서 확인합니다.", example = "1")
        @PathVariable
        Long courseId,
        @Parameter(description = "훈련기수 ID. courseId와 함께 기수를 식별합니다.", example = "1")
        @PathVariable
        Long batchId,
        @Parameter(description = "과정 공통 교과목 ID. 교과목 선택 목록에서 확인합니다.", example = "1")
        @PathVariable
        Long subjectId,
        @Parameter(description = "해당 기수·교과목의 능력단위 ID.", example = "1")
        @PathVariable
        Long unitId
    ) {
        return service.unit(
            courseId,
            batchId,
            subjectId,
            unitId
        );
    }

    @Operation(
        summary = "능력단위 등록",
        description = "새 능력단위와 요소를 한 트랜잭션으로 저장합니다. ID는 서버에서 생성합니다. 분류번호 중복을 허용합니다."
    )
    @PostMapping("/subjects/{subjectId}/units")
    @ResponseStatus(HttpStatus.CREATED)
    public NcsUnitDTO create(
        @Parameter(description = "훈련과정 ID. 기존 과정 조회에서 확인합니다.", example = "1")
        @PathVariable
        Long courseId,
        @Parameter(description = "훈련기수 ID. courseId와 함께 기수를 식별합니다.", example = "1")
        @PathVariable
        Long batchId,
        @Parameter(description = "과정 공통 교과목 ID. 교과목 선택 목록에서 확인합니다.", example = "1")
        @PathVariable
        Long subjectId,
        @RequestBody
        NcsUnitDTO request
    ) {
        request.setUnitId(null);
        return service.save(
            courseId,
            batchId,
            subjectId,
            request
        );
    }

    @Operation(
        summary = "능력단위·요소 수정",
        description = "일반 필드 null은 유지합니다. elements 생략은 유지, []는 전체 비활성화, 배열은 목록 교체입니다. " + "useYn=N이면 능력단위와 요소를 모두 비활성화합니다."
    )
    @PutMapping("/subjects/{subjectId}/units/{unitId}")
    public NcsUnitDTO update(
        @Parameter(description = "훈련과정 ID. 기존 과정 조회에서 확인합니다.", example = "1")
        @PathVariable
        Long courseId,
        @Parameter(description = "훈련기수 ID. courseId와 함께 기수를 식별합니다.", example = "1")
        @PathVariable
        Long batchId,
        @Parameter(description = "과정 공통 교과목 ID. 교과목 선택 목록에서 확인합니다.", example = "1")
        @PathVariable
        Long subjectId,
        @Parameter(description = "해당 기수·교과목의 능력단위 ID.", example = "1")
        @PathVariable
        Long unitId,
        @RequestBody
        NcsUnitDTO request
    ) {
        request.setUnitId(unitId);
        return service.save(
            courseId,
            batchId,
            subjectId,
            request
        );
    }

    @Operation(
        summary = "NCS 평가항목 일괄 저장",
        description = "units 배열을 능력단위별 독립 트랜잭션으로 저장합니다. 한 단위의 오류는 해당 단위와 요소만 롤백하며 성공/실패와 사유를 " + "반환합니다. 최대 200개입니다."
    )
    @PostMapping("/subjects/{subjectId}/save")
    public NcsSaveResultDTO save(
        @Parameter(description = "훈련과정 ID. 기존 과정 조회에서 확인합니다.", example = "1")
        @PathVariable
        Long courseId,
        @Parameter(description = "훈련기수 ID. courseId와 함께 기수를 식별합니다.", example = "1")
        @PathVariable
        Long batchId,
        @Parameter(description = "과정 공통 교과목 ID. 교과목 선택 목록에서 확인합니다.", example = "1")
        @PathVariable
        Long subjectId,
        @RequestBody
        NcsSaveRequestDTO request
    ) {
        return registrationService.save(
            courseId,
            batchId,
            subjectId,
            request.getUnits()
        );
    }

    @Operation(
        summary = "능력단위 비활성화",
        description = "능력단위와 하위 요소의 USE_YN을 N으로 변경하고 데이터는 유지합니다."
    )
    @DeleteMapping("/subjects/{subjectId}/units/{unitId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void disable(
        @Parameter(description = "훈련과정 ID. 기존 과정 조회에서 확인합니다.", example = "1")
        @PathVariable
        Long courseId,
        @Parameter(description = "훈련기수 ID. courseId와 함께 기수를 식별합니다.", example = "1")
        @PathVariable
        Long batchId,
        @Parameter(description = "과정 공통 교과목 ID. 교과목 선택 목록에서 확인합니다.", example = "1")
        @PathVariable
        Long subjectId,
        @Parameter(description = "해당 기수·교과목의 능력단위 ID.", example = "1")
        @PathVariable
        Long unitId,
        @Parameter(description = "수정자 사용자 ID. 현재 인증 도입 전 수동 전달하는 선택값.", example = "1")
        @RequestParam(required = false)
        Long updatedBy
    ) {
        service.disable(
            courseId,
            batchId,
            subjectId,
            unitId,
            updatedBy
        );
    }

    @Operation(
        summary = "능력단위 요소 추가",
        description = "사용 중인 능력단위에 요소 하나를 추가합니다."
    )
    @PostMapping("/subjects/{subjectId}/units/{unitId}/elements")
    @ResponseStatus(HttpStatus.CREATED)
    public NcsElementDTO createElement(
        @Parameter(description = "훈련과정 ID. 기존 과정 조회에서 확인합니다.", example = "1")
        @PathVariable
        Long courseId,
        @Parameter(description = "훈련기수 ID. courseId와 함께 기수를 식별합니다.", example = "1")
        @PathVariable
        Long batchId,
        @Parameter(description = "과정 공통 교과목 ID. 교과목 선택 목록에서 확인합니다.", example = "1")
        @PathVariable
        Long subjectId,
        @Parameter(description = "해당 기수·교과목의 능력단위 ID.", example = "1")
        @PathVariable
        Long unitId,
        @RequestBody
        NcsElementDTO request
    ) {
        request.setElementId(null);
        return service.saveElement(
            courseId,
            batchId,
            subjectId,
            unitId,
            request
        );
    }

    @Operation(
        summary = "능력단위 요소 수정",
        description = "해당 능력단위에 속한 요소만 수정할 수 있습니다. 생략 필드는 유지합니다."
    )
    @PutMapping("/subjects/{subjectId}/units/{unitId}/elements/{elementId}")
    public NcsElementDTO updateElement(
        @Parameter(description = "훈련과정 ID. 기존 과정 조회에서 확인합니다.", example = "1")
        @PathVariable
        Long courseId,
        @Parameter(description = "훈련기수 ID. courseId와 함께 기수를 식별합니다.", example = "1")
        @PathVariable
        Long batchId,
        @Parameter(description = "과정 공통 교과목 ID. 교과목 선택 목록에서 확인합니다.", example = "1")
        @PathVariable
        Long subjectId,
        @Parameter(description = "해당 기수·교과목의 능력단위 ID.", example = "1")
        @PathVariable
        Long unitId,
        @Parameter(description = "해당 능력단위의 요소 ID.", example = "1")
        @PathVariable
        Long elementId,
        @RequestBody
        NcsElementDTO request
    ) {
        request.setElementId(elementId);
        return service.saveElement(
            courseId,
            batchId,
            subjectId,
            unitId,
            request
        );
    }

    @Operation(
        summary = "능력단위 요소 비활성화",
        description = "해당 요소의 USE_YN을 N으로 변경합니다."
    )
    @DeleteMapping("/subjects/{subjectId}/units/{unitId}/elements/{elementId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void disableElement(
        @Parameter(description = "훈련과정 ID. 기존 과정 조회에서 확인합니다.", example = "1")
        @PathVariable
        Long courseId,
        @Parameter(description = "훈련기수 ID. courseId와 함께 기수를 식별합니다.", example = "1")
        @PathVariable
        Long batchId,
        @Parameter(description = "과정 공통 교과목 ID. 교과목 선택 목록에서 확인합니다.", example = "1")
        @PathVariable
        Long subjectId,
        @Parameter(description = "해당 기수·교과목의 능력단위 ID.", example = "1")
        @PathVariable
        Long unitId,
        @Parameter(description = "해당 능력단위의 요소 ID.", example = "1")
        @PathVariable
        Long elementId,
        @Parameter(description = "수정자 사용자 ID. 현재 인증 도입 전 수동 전달하는 선택값.", example = "1")
        @RequestParam(required = false)
        Long updatedBy
    ) {
        service.disableElement(
            courseId,
            batchId,
            subjectId,
            unitId,
            elementId,
            updatedBy
        );
    }

    @Operation(
        summary = "CSV NCS 항목 추가 등록",
        description = "UTF-8 CSV의 unitKey로 같은 능력단위의 행을 묶습니다. 필수 헤더는 unitKey,unitName이며 " + "elementName 등은 선택입니다. 기존 항목을 유지하고 신규 추가합니다. 최대 200단위/2000행/10MB이며 " + "능력단위별 성공/실패를 반환합니다."
    )
    @PostMapping(value = "/subjects/{subjectId}/csv", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public NcsSaveResultDTO csv(
        @Parameter(description = "훈련과정 ID. 기존 과정 조회에서 확인합니다.", example = "1")
        @PathVariable
        Long courseId,
        @Parameter(description = "훈련기수 ID. courseId와 함께 기수를 식별합니다.", example = "1")
        @PathVariable
        Long batchId,
        @Parameter(description = "과정 공통 교과목 ID. 교과목 선택 목록에서 확인합니다.", example = "1")
        @PathVariable
        Long subjectId,
        @Parameter(description = "multipart/form-data로 전달할 파일. 이미지 최대 10MB, CSV는 UTF-8 형식.")
        @RequestPart("file")
        MultipartFile file,
        @Parameter(description = "등록자 사용자 ID. 현재 인증 도입 전 수동 전달하는 선택값.", example = "1")
        @RequestParam(required = false)
        Long createdBy
    ) {
        return csvService.register(
            courseId,
            batchId,
            subjectId,
            file,
            createdBy
        );
    }
}
