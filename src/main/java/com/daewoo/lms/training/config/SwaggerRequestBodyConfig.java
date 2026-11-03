package com.daewoo.lms.training.config;

import io.swagger.v3.oas.models.PathItem.HttpMethod;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.parameters.RequestBody;
import java.util.List;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import static io.swagger.v3.oas.models.PathItem.HttpMethod.*;
import tools.jackson.databind.ObjectMapper;

/**
 * Keeps request examples separate from Spring's RequestBody binding annotation.
 */
@Configuration(proxyBeanMethods = false)
public class SwaggerRequestBodyConfig {

    private static final String BODY_DESCRIPTION = "입력 예시입니다. ID는 실제 조회한 값으로 바꾸세요. 수정 시 유지할 문항/보기/요소에는 기존 ID를 전달하세요. 필수 조건과 배열 처리 방식은 이 API의 상세 설명을 확인하세요.";

    private static final String EXAMPLE_1 = """
        {
          "inputType": "VISITOR",
          "applicantName": "홍길동",
          "birthDate": "1998-06-12",
          "gender": "M",
          "nationalityType": "DOMESTIC",
          "phone": "01012345678",
          "email": "hong@example.com",
          "receptionMethod": "VISIT"
        }
        """;

    private static final String EXAMPLE_2 = """
        {
          "assessment": {
            "subjectId": 1,
            "unitId": 1,
            "targetLevel": "UNIT",
            "assessmentName": "커피 원두 선택 최종평가",
            "assessmentType": "FINAL",
            "toolType": "ESSAY",
            "sheetType": "ESSAY",
            "authorInstructorId": 1,
            "assessmentDate": "2026-11-11",
            "examMinutes": 60,
            "totalScore": 100,
            "passScore": 60
          },
          "traineeIds": [
            1
          ]
        }
        """;

    private static final String EXAMPLE_3 = """
        {
          "assessment": {
            "subjectId": 1,
            "unitId": 1
          }
        }
        """;

    private static final String EXAMPLE_4 = """
        {
          "draft": true,
          "overrides": {
            "assessmentName": "커피 원두 선택 재시험"
          },
          "traineeIds": [
            1
          ]
        }
        """;

    private static final String EXAMPLE_5 = """
        {
          "assessment": {
            "assessmentName": "커피 원두 선택 최종평가",
            "totalScore": 10,
            "passScore": 6
          },
          "totalQuestionCount": 2,
          "questions": [
            {
              "questionNo": 1,
              "questionType": "OBJECTIVE",
              "questionText": "가장 적절한 원두 선택 기준은?",
              "score": 5,
              "correctAnswer": "2",
              "options": [
                {
                  "optionNo": 1,
                  "optionText": "포장 색상만 확인한다."
                },
                {
                  "optionNo": 2,
                  "optionText": "신선도와 향을 확인한다."
                }
              ]
            },
            {
              "questionNo": 2,
              "questionType": "ESSAY",
              "questionText": "원두 품질 확인 기준을 설명하세요.",
              "score": 5,
              "correctAnswer": "신선도, 향, 보관 상태를 종합하여 확인한다."
            }
          ],
          "levels": [
            {
              "minScore": 6,
              "description": "원두 품질 확인 기준을 이해하고 적용할 수 있다.",
              "sortOrder": 0
            }
          ]
        }
        """;

    private static final String EXAMPLE_6 = """
        {
          "assessment": {
            "totalScore": 0,
            "passScore": 0
          },
          "totalQuestionCount": 0,
          "levels": []
        }
        """;

    private static final String EXAMPLE_7 = """
        {
          "course": {
            "courseName": "JAVA 개발 실무",
            "trainingJobCategoryId": 1,
            "educationCategoryId": 1
          },
          "batch": {
            "batchName": "1기",
            "campusId": 1,
            "status": "RECRUITING",
            "recruitType": "FIXED",
            "startDate": "2026-11-10",
            "endDate": "2027-02-10"
          },
          "days": [
            "MON",
            "TUE",
            "WED",
            "THU",
            "FRI"
          ]
        }
        """;

    private static final String EXAMPLE_8 = """
        {
          "courseId": 1
        }
        """;

    private static final String EXAMPLE_9 = """
        {
          "originalFileName": "photo.png",
          "filePath": "uploads/course/photo.png",
          "mimeType": "image/png"
        }
        """;

    private static final String EXAMPLE_10 = """
        {
          "categoryName": "프로그래밍",
          "sortOrder": 1,
          "hiddenYn": "N",
          "useYn": "Y"
        }
        """;

    private static final String EXAMPLE_11 = """
        {
          "unitName": "커피 원두 선택",
          "classificationNo": "1601020101",
          "trainingHours": 20,
          "nonNcsYn": "N",
          "preAssessmentYn": "Y",
          "elements": [
            {
              "elementName": "원두 특성 확인",
              "sortOrder": 0
            }
          ]
        }
        """;

    private static final String EXAMPLE_12 = """
        {
          "units": [
            {
              "unitName": "커피 원두 선택",
              "classificationNo": "1601020101",
              "trainingHours": 20,
              "nonNcsYn": "N",
              "preAssessmentYn": "Y",
              "elements": [
                {
                  "elementName": "원두 특성 확인",
                  "sortOrder": 0
                }
              ]
            }
          ]
        }
        """;

    private static final String EXAMPLE_13 = """
        {
          "elementName": "원두 특성 확인",
          "sortOrder": 0
        }
        """;

    private static final String EXAMPLE_14 = """
        {
          "categoryName": "내일배움카드(재직자)",
          "sortOrder": 1,
          "incumbentYn": "Y",
          "hiddenYn": "N",
          "useYn": "Y"
        }
        """;

    private static final String EXAMPLE_15 = """
        {
          "trainingJobCategoryId": 1,
          "categoryName": "내일배움카드(재직자)",
          "sortOrder": 1,
          "incumbentYn": "Y",
          "hiddenYn": "N",
          "useYn": "Y"
        }
        """;

    private static final String TRAINING_CATEGORY_UPDATE_EXAMPLE = """
        [
          {"trainingJobCategoryId": 1, "categoryName": "????????", "sortOrder": 0, "incumbentYn": "N", "hiddenYn": "N"},
          {"trainingJobCategoryId": 2, "categoryName": "??????(???)", "sortOrder": 1, "incumbentYn": "N", "hiddenYn": "N"}
        ]
        """;

    private static final String EDUCATION_CATEGORY_UPDATE_EXAMPLE = """
        [
          {"educationCategoryId": 1, "categoryName": "?????", "sortOrder": 1, "hiddenYn": "N"},
          {"educationCategoryId": 2, "categoryName": "UI/UX???", "sortOrder": 2, "hiddenYn": "N"}
        ]
        """;

    @Bean
    public OpenApiCustomizer requestBodyExamples(ObjectMapper objectMapper) {
        List<BodyDocumentation> documentation = List.of(
            new BodyDocumentation("/api/v1/admissions/{courseId}/{batchId}", POST, EXAMPLE_1),
            new BodyDocumentation("/api/v1/admissions/{courseId}/{batchId}/{applicationId}", PUT, EXAMPLE_1),
            new BodyDocumentation("/api/v1/assessments/{courseId}/{batchId}", POST, EXAMPLE_2),
            new BodyDocumentation("/api/v1/assessments/{courseId}/{batchId}/drafts", POST, EXAMPLE_3),
            new BodyDocumentation("/api/v1/assessments/{courseId}/{batchId}/{assessmentId}", PUT, EXAMPLE_2),
            new BodyDocumentation("/api/v1/assessments/{courseId}/{batchId}/{assessmentId}/draft", PUT, EXAMPLE_3),
            new BodyDocumentation("/api/v1/assessments/{courseId}/{batchId}/{assessmentId}/retests", POST, EXAMPLE_4),
            new BodyDocumentation("/api/v1/assessments/{courseId}/{batchId}/{assessmentId}/paper", POST, EXAMPLE_5),
            new BodyDocumentation("/api/v1/assessments/{courseId}/{batchId}/{assessmentId}/paper", PUT, EXAMPLE_5),
            new BodyDocumentation(
                "/api/v1/assessments/{courseId}/{batchId}/{assessmentId}/paper/questions/{questionId}",
                DELETE,
                EXAMPLE_6
            ),
            new BodyDocumentation("/api/v1/courses/create", POST, EXAMPLE_7),
            new BodyDocumentation("/api/v1/courses/{courseId}/batches", POST, EXAMPLE_7),
            new BodyDocumentation("/api/v1/courses/{courseId}/batches/{batchId}", PUT, EXAMPLE_7),
            new BodyDocumentation("/api/v1/courses/drafts", POST, EXAMPLE_8),
            new BodyDocumentation("/api/v1/courses/drafts/{draftId}", PUT, EXAMPLE_8),
            new BodyDocumentation("/api/v1/courses/files", POST, EXAMPLE_9),
            new BodyDocumentation("/api/v1/educationCategories", POST, EXAMPLE_10),
            new BodyDocumentation("/api/v1/educationCategories", PUT, EDUCATION_CATEGORY_UPDATE_EXAMPLE),
            new BodyDocumentation("/api/v1/ncs/{courseId}/{batchId}/subjects/{subjectId}/units", POST, EXAMPLE_11),
            new BodyDocumentation("/api/v1/ncs/{courseId}/{batchId}/subjects/{subjectId}/units/{unitId}", PUT, EXAMPLE_11),
            new BodyDocumentation("/api/v1/ncs/{courseId}/{batchId}/subjects/{subjectId}/save", POST, EXAMPLE_12),
            new BodyDocumentation("/api/v1/ncs/{courseId}/{batchId}/subjects/{subjectId}/units/{unitId}/elements", POST, EXAMPLE_13),
            new BodyDocumentation(
                "/api/v1/ncs/{courseId}/{batchId}/subjects/{subjectId}/units/{unitId}/elements/{elementId}",
                PUT,
                EXAMPLE_13
            ),
            new BodyDocumentation("/api/v1/trainingJobCategories", POST, EXAMPLE_14),
            new BodyDocumentation("/api/v1/trainingJobCategories", PUT, TRAINING_CATEGORY_UPDATE_EXAMPLE),
            new BodyDocumentation("/api/v1/trainingJobCategories", DELETE, EXAMPLE_15)
        );
        return openApi -> {
            if (openApi.getPaths() == null) {
                return;
            }
            for (BodyDocumentation entry : documentation) {
                var path = openApi.getPaths().get(entry.path());
                if (path == null) {
                    continue;
                }
                var operation = path.readOperationsMap().get(entry.method());
                if (operation == null) {
                    continue;
                }
                RequestBody body = operation.getRequestBody();
                if (body == null) {
                    continue;
                }
                body.setDescription(BODY_DESCRIPTION);
                if (body.getContent() != null) {
                    body.getContent().forEach(
                        (mediaType, content) -> {
                            if (mediaType.equals("application/json")) {
                                content.addExamples("입력 예시", new Example().value(objectMapper.readValue(entry.example(), Object.class)));
                            }
                        }
                    );
                }
            }
        };
    }

    private record BodyDocumentation(String path, HttpMethod method, String example) {
    }
}
