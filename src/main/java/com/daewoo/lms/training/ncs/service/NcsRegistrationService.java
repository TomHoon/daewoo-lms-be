package com.daewoo.lms.training.ncs.service;

import com.daewoo.lms.training.ncs.dto.*;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionException;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class NcsRegistrationService {

    private final NcsService unitService;

    public NcsSaveResultDTO save(Long courseId, Long batchId, Long subjectId, List<NcsUnitDTO> units) {
        if (units == null || units.size() > 200)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "units 배열이 필요하며 한 번에 최대 200개까지 저장할 수 있습니다.");
        List<NcsSaveResultDTO.Item> results = new ArrayList<>();
        Set<Long> seen = new HashSet<>();
        for (int i = 0; i < units.size(); i++) {
            NcsUnitDTO unit = units.get(i);
            Long unitId = unit == null ? null : unit.getUnitId();
            if (unitId != null && !seen.add(unitId)) {
                results.add(new NcsSaveResultDTO.Item(
                    i,
                    false,
                    unitId,
                    List.of(),
                    "같은 능력단위 ID가 두 번 전달되었습니다."
                ));
                continue;
            }
            results.add(saveOne(
                courseId,
                batchId,
                subjectId,
                unit,
                i,
                List.of()
            ));
        }
        return summarize(results);
    }

    NcsSaveResultDTO.Item saveOne(Long courseId, Long batchId, Long subjectId, NcsUnitDTO unit, int index, List<Integer> rows) {
        try {
            NcsUnitDTO result = unitService.save(
                courseId,
                batchId,
                subjectId,
                unit
            );
            return new NcsSaveResultDTO.Item(
                index,
                true,
                result.getUnitId(),
                rows,
                null
            );
        } catch (ResponseStatusException e) {
            return new NcsSaveResultDTO.Item(
                index,
                false,
                unit == null ? null : unit.getUnitId(),
                rows,
                e.getReason()
            );
        } catch (DataAccessException | TransactionException e) {
            LoggerFactory.getLogger(NcsRegistrationService.class).warn("NCS 능력단위 저장 실패: index={}", index, e);
            return new NcsSaveResultDTO.Item(
                index,
                false,
                unit == null ? null : unit.getUnitId(),
                rows,
                "데이터 저장에 실패했습니다."
            );
        }
    }

    static NcsSaveResultDTO summarize(List<NcsSaveResultDTO.Item> results) {
        int success = (int) results.stream().filter(NcsSaveResultDTO.Item::success).count();
        return new NcsSaveResultDTO(
            results.size(),
            success,
            results.size() - success,
            List.copyOf(results)
        );
    }
}
