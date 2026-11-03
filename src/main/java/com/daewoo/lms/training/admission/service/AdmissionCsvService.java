package com.daewoo.lms.training.admission.service;

import com.daewoo.lms.training.admission.dto.*;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.*;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
public class AdmissionCsvService {

    private final AdmissionService admissionService;

    private final ObjectMapper objectMapper;

    private static final int MAX_ROWS = 2000;

    private static final Set<String> HEADERS = Set.of(
        "applicantName",
        "birthDate",
        "gender",
        "nationalityType",
        "phone",
        "extraPhone",
        "email",
        "zipCode",
        "region",
        "address",
        "addressDetail",
        "photoFileId",
        "lastSchoolName",
        "educationLevel",
        "graduationType",
        "applyRoute",
        "receptionMethod",
        "applicantMessage",
        "employmentSupportType",
        "employmentSupportCenter",
        "employmentSupportManager",
        "employmentSupportPhone",
        "preTestYn",
        "applicationDate"
    );

    // No enclosing transaction: every call to AdmissionService.create runs through its transactional proxy.
    public AdmissionCsvResultDTO register(Long courseId, Long batchId, MultipartFile file, Long createdBy) {
        if (file.isEmpty() || file.getSize() > 10L * 1024 * 1024)
            throw bad("CSV 파일은 비어 있지 않은 10MB 이하 파일이어야 합니다.");
        String text;
        try {
            text = StandardCharsets.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPORT).onUnmappableCharacter(CodingErrorAction.REPORT).decode(ByteBuffer.wrap(file.getBytes())).toString();
        } catch (IOException e) {
            throw bad("UTF-8 인코딩의 CSV 파일을 사용해 주세요.");
        }
        List<CsvRecord> records = parse(text);
        if (records.size() < 2)
            throw bad("CSV 헤더와 데이터 행이 필요합니다.");
        List<String> headers = records.getFirst().values().stream().map(String::trim).toList();
        if (headers.size() != new HashSet<>(headers).size() || !HEADERS.containsAll(headers))
            throw bad("중복되거나 지원하지 않는 CSV 헤더입니다.");
        if (!headers.containsAll(List.of("applicantName", "birthDate", "phone")))
            throw bad("applicantName, birthDate, phone 헤더가 필요합니다.");
        List<AdmissionCsvResultDTO.RowResult> results = new ArrayList<>();
        int success = 0;
        for (CsvRecord record : records.subList(1, records.size())) {
            try {
                if (record.values().size() != headers.size())
                    throw bad("헤더와 열 개수가 다릅니다.");
                Map<String, String> values = new LinkedHashMap<>();
                for (int i = 0; i < headers.size(); i++) {
                    String value = record.values().get(i).trim();
                    if (!value.isEmpty())
                        values.put(headers.get(i), value);
                }
                AdmissionApplicationDTO request;
                try {
                    request = objectMapper.convertValue(values, AdmissionApplicationDTO.class);
                } catch (IllegalArgumentException e) {
                    throw bad("날짜(YYYY-MM-DD) 또는 숫자 형식이 올바르지 않습니다.");
                }
                request.setInputType("CSV");
                request.setCreatedBy(createdBy);
                AdmissionDetailDTO result = admissionService.create(courseId, batchId, request);
                success++;
                results.add(
                    new AdmissionCsvResultDTO.RowResult(
                        record.rowNumber(),
                        true,
                        result.getApplication().getApplicationId(),
                        null
                    )
                );
            } catch (ResponseStatusException e) {
                results.add(new AdmissionCsvResultDTO.RowResult(
                    record.rowNumber(),
                    false,
                    null,
                    e.getReason()
                ));
            } catch (DataAccessException | TransactionException e) {
                results.add(new AdmissionCsvResultDTO.RowResult(
                    record.rowNumber(),
                    false,
                    null,
                    "데이터 저장에 실패했습니다."
                ));
                LoggerFactory.getLogger(AdmissionCsvService.class).warn("CSV 행 {} 저장 실패", record.rowNumber(), e);
            }
        }
        return new AdmissionCsvResultDTO(
            results.size(),
            success,
            results.size() - success,
            results
        );
    }

    public record CsvRecord(int rowNumber, List<String> values) {
    }

    static List<CsvRecord> parse(String input) {
        if (input.startsWith("\uFEFF"))
            input = input.substring(1);
        List<CsvRecord> records = new ArrayList<>();
        List<String> row = new ArrayList<>();
        StringBuilder field = new StringBuilder();
        boolean quoted = false;
        boolean closedQuote = false;
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
                        closedQuote = true;
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
                closedQuote = false;
                if (c != ',') {
                    addRecord(records, row, startLine);
                    row = new ArrayList<>();
                    if (c == '\r' && i + 1 < input.length() && input.charAt(i + 1) == '\n')
                        i++;
                    startLine = ++line;
                }
            } else if (c == '"') {
                if (!field.isEmpty() || closedQuote)
                    throw bad(line + "행의 따옴표 형식이 올바르지 않습니다.");
                quoted = true;
            } else {
                if (closedQuote)
                    throw bad(line + "행의 닫는 따옴표 뒤에 불필요한 문자가 있습니다.");
                field.append(c);
            }
        }
        if (quoted)
            throw bad(startLine + "행의 따옴표가 닫히지 않았습니다.");
        row.add(field.toString());
        addRecord(records, row, startLine);
        return records;
    }

    private static void addRecord(List<CsvRecord> records, List<String> row, int startLine) {
        if (row.size() == 1 && row.getFirst().isBlank())
            return;
        records.add(new CsvRecord(startLine, List.copyOf(row)));
        if (records.size() > MAX_ROWS + 1)
            throw bad("CSV는 한 번에 최대 2000명까지 등록할 수 있습니다.");
    }

    private static ResponseStatusException bad(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }
}
