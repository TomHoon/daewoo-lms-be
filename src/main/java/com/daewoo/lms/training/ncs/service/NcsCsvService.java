package com.daewoo.lms.training.ncs.service;

import com.daewoo.lms.training.ncs.dto.*;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.*;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class NcsCsvService {

    private final NcsRegistrationService registrationService;

    private static final Set<String> HEADERS = Set.of(
        "unitKey",
        "unitName",
        "classificationNo",
        "trainingHours",
        "nonNcsYn",
        "preAssessmentYn",
        "mixedTypeYn",
        "sortOrder",
        "elementName",
        "elementSortOrder"
    );

    public NcsSaveResultDTO register(Long courseId, Long batchId, Long subjectId, MultipartFile file, Long createdBy) {
        if (file.isEmpty() || file.getSize() > 10L * 1024 * 1024)
            throw bad("CSV는 비어 있지 않은 10MB 이하 파일이어야 합니다.");
        String text;
        try {
            text = StandardCharsets.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPORT).decode(ByteBuffer.wrap(file.getBytes())).toString();
        } catch (IOException e) {
            throw bad("UTF-8 인코딩의 CSV 파일을 사용해 주세요.");
        }
        List<CsvRow> records = parse(text);
        if (records.size() < 2)
            throw bad("CSV 헤더와 데이터 행이 필요합니다.");
        List<String> headers = records.getFirst().values().stream().map(String::trim).toList();
        if (headers.size() != new HashSet<>(headers).size() || !HEADERS.containsAll(headers) || !headers.containsAll(List.of("unitKey", "unitName")))
            throw bad("unitKey, unitName을 포함한 올바른 CSV 헤더가 필요합니다.");
        int keyIndex = headers.indexOf("unitKey");
        Map<String, Group> groups = new LinkedHashMap<>();
        List<NcsSaveResultDTO.Item> failures = new ArrayList<>();
        for (CsvRow row : records.subList(1, records.size())) {
            String key = row.values().size() > keyIndex ? row.values().get(keyIndex).trim() : "";
            if (key.isBlank()) {
                failures.add(new NcsSaveResultDTO.Item(
                    -1,
                    false,
                    null,
                    List.of(row.line()),
                    "unitKey가 필요합니다."
                ));
                continue;
            }
            Group group = groups.computeIfAbsent(key, ignored -> new Group());
            group.rows.add(row.line());
            if (groups.size() > 200)
                throw bad("CSV는 한 번에 최대 200개 능력단위까지 등록할 수 있습니다.");
            try {
                if (row.values().size() != headers.size())
                    throw bad("헤더와 열 개수가 다릅니다.");
                Map<String, String> values = new HashMap<>();
                for (int i = 0; i < headers.size(); i++) values.put(headers.get(i), row.values().get(i).trim());
                BeanWrapperImpl target = new BeanWrapperImpl(group.unit);
                for (String name : headers) {
                    if (Set.of("unitKey", "elementName", "elementSortOrder").contains(name))
                        continue;
                    String raw = values.get(name);
                    if (raw.isBlank())
                        continue;
                    Object value = Set.of("trainingHours", "sortOrder").contains(name) ? Integer.valueOf(raw) : raw;
                    Object previous = target.getPropertyValue(name);
                    if (previous != null && !previous.equals(value))
                        throw bad("같은 unitKey의 능력단위 정보가 서로 다릅니다.");
                    target.setPropertyValue(name, value);
                }
                String elementName = values.getOrDefault("elementName", "");
                String sortOrder = values.getOrDefault("elementSortOrder", "");
                if (!elementName.isBlank()) {
                    NcsElementDTO element = new NcsElementDTO();
                    element.setElementName(elementName);
                    element.setSortOrder(sortOrder.isBlank() ? group.elements.size() : Integer.valueOf(sortOrder));
                    element.setCreatedBy(createdBy);
                    group.elements.add(element);
                } else if (!sortOrder.isBlank())
                    throw bad("요소 정렬순서가 있으면 요소명도 필요합니다.");
            } catch (NumberFormatException e) {
                group.error = "훈련시간과 정렬순서는 정수여야 합니다.";
            } catch (ResponseStatusException e) {
                group.error = e.getReason();
            }
        }
        List<NcsSaveResultDTO.Item> results = new ArrayList<>();
        int index = 0;
        for (Group group : groups.values()) {
            if (group.error != null)
                results.add(new NcsSaveResultDTO.Item(
                    index,
                    false,
                    null,
                    List.copyOf(group.rows),
                    group.error
                ));
            else {
                group.unit.setElements(group.elements);
                group.unit.setCreatedBy(createdBy);
                results.add(
                    registrationService.saveOne(
                        courseId,
                        batchId,
                        subjectId,
                        group.unit,
                        index,
                        List.copyOf(group.rows)
                    )
                );
            }
            index++;
        }
        for (NcsSaveResultDTO.Item failure : failures) results.add(new NcsSaveResultDTO.Item(
            index++,
            false,
            null,
            failure.rowNumbers(),
            failure.message()
        ));
        return NcsRegistrationService.summarize(results);
    }

    private static final class Group {

        final NcsUnitDTO unit = new NcsUnitDTO();

        final List<NcsElementDTO> elements = new ArrayList<>();

        final List<Integer> rows = new ArrayList<>();

        String error;
    }

    record CsvRow(int line, List<String> values) {
    }

    static List<CsvRow> parse(String input) {
        if (input.startsWith("\uFEFF"))
            input = input.substring(1);
        List<CsvRow> rows = new ArrayList<>();
        List<String> row = new ArrayList<>();
        StringBuilder field = new StringBuilder();
        boolean quoted = false;
        boolean closed = false;
        int line = 1;
        int startLine = 1;
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (quoted) {
                if (c == '"') {
                    if (i + 1 < input.length() && input.charAt(i + 1) == '"') {
                        field.append('"');
                        i++;
                    } else {
                        quoted = false;
                        closed = true;
                    }
                } else if (c == '\r' || c == '\n') {
                    if (c == '\r' && i + 1 < input.length() && input.charAt(i + 1) == '\n')
                        i++;
                    field.append('\n');
                    line++;
                } else
                    field.append(c);
                continue;
            }
            if (c == ',' || c == '\r' || c == '\n') {
                row.add(field.toString());
                field.setLength(0);
                closed = false;
                if (c != ',') {
                    addRow(rows, row, startLine);
                    row = new ArrayList<>();
                    if (c == '\r' && i + 1 < input.length() && input.charAt(i + 1) == '\n')
                        i++;
                    startLine = ++line;
                }
            } else if (c == '"') {
                if (!field.isEmpty() || closed)
                    throw bad(line + "행의 따옴표 형식이 올바르지 않습니다.");
                quoted = true;
            } else {
                if (closed)
                    throw bad(line + "행의 닫는 따옴표 뒤에 불필요한 문자가 있습니다.");
                field.append(c);
            }
        }
        if (quoted)
            throw bad(startLine + "행의 따옴표가 닫히지 않았습니다.");
        row.add(field.toString());
        addRow(rows, row, startLine);
        return rows;
    }

    private static void addRow(List<CsvRow> rows, List<String> row, int startLine) {
        if (row.size() == 1 && row.getFirst().isBlank())
            return;
        rows.add(new CsvRow(startLine, List.copyOf(row)));
        if (rows.size() > 2001)
            throw bad("CSV 데이터는 최대 2000행까지 등록할 수 있습니다.");
    }

    private static ResponseStatusException bad(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }
}
