# 교육과정 기수 API

기존 `trainingJobCategory`, `educationCourseCategory`와 동일한 계층 구조로
`com.daewoo.lms.training.course` 패키지와 `resources/mapper/course/course.xml`을 추가했습니다.
테이블과 컬럼은 `doc/lms_mariadb_ddl_v3.sql`을 기준으로 작성했습니다.

## DB 준비

기존 6개 테이블 외에 **`doc/course_registration_draft.sql`을 한 번 적용해야 합니다.**
애플리케이션이 해당 SQL을 자동 실행하지 않습니다.
임시저장의 미완성 입력과 공통 과정 수정본은 `COURSE_REGISTRATION_DRAFT.PAYLOAD`에 JSON으로 보관합니다.
임시저장에서는 `COURSE`, `COURSE_BATCH`, 요일·강사·추가정보를 변경하지 않습니다.
`POST /api/v1/courses/create`는 선택한 두 분류 아래 새 과정과 첫 기수를 함께 생성합니다.
기존 과정에 기수를 추가할 때에는 `POST /api/v1/courses/{courseId}/batches`를 사용합니다.
기존 임시저장 API는 기존 과정의 ID가 필요합니다.

## API 목록

기본 경로: `/api/v1/courses`

| Method | 경로 | 기능 |
|---|---|---|
| POST | `/create` | 훈련직종·교육과정 분류를 선택해 신규 과정과 첫 기수를 함께 등록, 201 |
| GET | `/options` | 훈련직종, 교육과정 분류, 캠퍼스, 강사 선택 목록 |
| GET | `?trainingJobCategoryId=1&educationCategoryId=2` | 기존 과정 선택 목록, 필터는 선택 사항 |
| GET | `/{courseId}` | 과정 공통정보 |
| GET | `/{courseId}/batches` | 과정별 기수 목록 |
| GET | `/{courseId}/batches/{batchId}` | 공통정보, 기수, 요일, 강사, 추가정보, 대표이미지 메타데이터 |
| POST | `/{courseId}/batches` | 기수 신규 등록, 공통정보 동시 반영, 201 |
| PUT | `/{courseId}/batches/{batchId}` | 기수 및 공통정보 수정 |
| DELETE | `/{courseId}/batches/{batchId}` | 기수와 연결된 요일·강사·추가정보·기수 수정 임시저장 실제 삭제, 204 |
| GET | `/drafts?courseId=1` | 임시저장 목록, 필터는 선택 사항 |
| POST | `/drafts` | 임시저장 생성, 201 |
| GET | `/drafts/{draftId}` | 임시저장 입력 데이터 불러오기 |
| PUT | `/drafts/{draftId}` | 임시저장 내용 교체 |
| POST | `/drafts/{draftId}/publish` | 임시저장 정식 반영 후 임시저장 삭제 |
| DELETE | `/drafts/{draftId}` | 임시저장 삭제, 204 |
| POST | `/files` | multipart 이미지 업로드 또는 JSON 파일 메타데이터 등록, 201 |
| GET | `/files/{fileId}` | 파일 메타데이터 조회 |
| GET | `/files/{fileId}/content` | 이 API로 업로드한 이미지 다운로드 |

기수 ID는 `(COURSE_ID, BATCH_ID)` 복합키입니다. 서버에서 과정 행을 `FOR UPDATE`로 잠근 후
해당 과정의 다음 기수 ID를 할당합니다. 클라이언트가 신규 기수 ID를 생성할 필요가 없습니다.
추가정보 ID는 요청 배열 순서에 따라 기수 내부에서 생성합니다.

## 정식 등록 / 수정 요청

```json
{
  "course": {
    "trainingJobCategoryId": 1,
    "educationCategoryId": 2,
    "courseName": "웹 개발 실무",
    "courseDivision": "취업 과정",
    "courseColor": "#3366ff",
    "introduction": "<p>과정소개</p>",
    "qualification": "<p>지원자격</p>",
    "tuitionRefundInfo": "<p>교육비 및 환급 안내</p>",
    "updatedBy": 100
  },
  "batch": {
    "batchNo": 1,
    "batchName": "1기",
    "campusId": 1,
    "trainingCode": "TRAINING-001",
    "status": "RECRUITING",
    "recruitType": "FIXED",
    "startDate": "2026-11-10",
    "endDate": "2027-02-10",
    "tuitionAmount": 1000000,
    "tuitionVisibleYn": "N",
    "govSupportAmount": 800000,
    "selfPaymentDesc": "본인부담금 안내",
    "allowanceType": "MONTHLY",
    "allowanceAmount": 200000,
    "allowanceDesc": "훈련수당 안내",
    "representativeFileId": 10,
    "classStartTime": "09:00:00",
    "classEndTime": "18:00:00",
    "dailyHours": 8,
    "approvalCount": 20,
    "trainingDays": 60,
    "trainingPlace": "1강의실",
    "hrdLink": "https://www.work24.go.kr/",
    "createdBy": 100,
    "updatedBy": 100
  },
  "days": ["MON", "TUE", "WED", "THU", "FRI"],
  "instructors": [{"instructorId": 1, "mainYn": "Y"}, {"instructorId": 2, "mainYn": "N"}],
  "extraInfos": [{"itemName": "준비물", "itemValue": "노트북", "sortOrder": 1}]
}
```

- 경로의 과정·기수 ID를 사용합니다. 내부 DTO에 다른 ID를 넣어도 경로 ID를 우선합니다.
- 공통정보는 전달한 값만 변경합니다. 같은 과정의 다른 기수에도 반영됩니다.
- 기수의 생략 또는 `null`인 필드는 기존 값을 유지합니다. 신규 등록에서는 DB 기본값을 사용합니다.
- `days`, `instructors`, `extraInfos`는 생략/`null`이면 기존 목록 유지, `[]`이면 전체 삭제,
  배열을 전달하면 전체 교체합니다. 생성 시 생략하면 빈 목록입니다.
- 대표이미지 선택은 `batch.representativeFileId`, 대표이미지 연결 해제는 최상위 `removeRepresentativeImage: true`를 사용합니다.
  `representativeFile`은 상세 응답용 필드이며 저장 시 파일 생성/수정에 사용하지 않습니다.
- 문자열을 비우려면 `""`를 전달합니다. `null`은 기존 값 유지 의미입니다.
- `status`, `recruitType`, `allowanceType`은 예시 코드입니다. 실제 프런트와 업무 코드를 맞춰 사용해야 합니다.
  `DRAFT`와 `임시등록`은 정식 등록 상태로 사용할 수 없습니다.
- 현재 정식 등록 검증은 과정명·사용여부·두 분류·캠퍼스·상태·모집분류, 날짜/시간의 선후관계,
  음수 금액/인원, Y/N 값, 중복 요일/강사, 최대 강사 10명, 담임 최대 1명, 추가정보 항목명을 포함합니다.
  기간 미확정 모집도 고려해 교육기간은 입력한 경우 선후관계를 검증합니다.
- 파일을 업로드한 뒤 반환받은 `fileId`를 저장 요청에 사용합니다.
- 기본설명 출력 체크박스는 요청/DB 필드가 없습니다.

## 임시저장

최소 요청:

```json
{"courseId": 1}
```

상세 입력은 위 정식 요청과 같은 구조로 전달합니다. 기존 기수를 수정하는 임시저장은
최상위 `batchId`도 전달합니다. 임시저장 수정 시에는 생성할 때의 `courseId`와 `batchId`를 유지하고,
입력 내용 전체를 전달해야 합니다. 목록에서 얻은 `draftId`로 내용을 불러오고 정식 반영합니다.
정식 반영에 실패하면 임시저장은 남습니다. 정식 반영과 관련 테이블 저장은 하나의 트랜잭션입니다.

## 대표이미지 업로드

`POST /api/v1/courses/files`에 `multipart/form-data`로 `file`과 선택 사항인 `createdBy`를 전달합니다.
PNG/JPEG/GIF/WebP 파일 시그니처를 검사하고 `file.upload-dir` 아래 `course` 폴더에 UUID 이름으로 저장합니다.
최대 크기는 기존 multipart 설정(파일당 10MB)을 사용합니다.
파일 DB 저장 트랜잭션이 롤백되면 생성한 실물 파일도 정리합니다.

이미 별도 업로더가 있는 경우 같은 경로에 `application/json`으로 `CourseFileDTO`를 전달해
메타데이터만 등록할 수도 있습니다. 이 경우 원본 파일명과 저장 경로가 필요하며 실물 파일을 만들지 않습니다.
다운로드 API는 설정된 업로드 폴더의 `course` 하위 파일만 제공합니다.

기수 삭제나 대표이미지 연결 해제는 `FILES` 메타데이터와 실물 파일을 삭제하지 않습니다.
같은 파일을 다른 기수에서 사용할 수 있으므로 독립적인 미사용 파일 정리는 별도 정책 대상입니다.

## 검증

```powershell
.\gradlew.bat test --tests "com.daewoo.lms.training.course.*"
```

테스트는 외부 DB에 접속하지 않습니다. 임시저장의 공통정보 격리, 정식 반영,
잘못된 입력 차단, 컬렉션 교체, 이미지 연결 해제, 자식 우선 삭제와 MyBatis XML 파싱/바인딩을 확인합니다.
실제 MariaDB CRUD 통합 검증은 DB 준비 후 별도로 필요합니다.
