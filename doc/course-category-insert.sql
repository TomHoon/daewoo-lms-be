-- 과정분류 관리 화면의 초기 분류 데이터 (MariaDB)
-- TRAINING_JOB_CATEGORY_ID, EDUCATION_CATEGORY_ID는 AUTO_INCREMENT로 생성합니다.
-- 재실행하면 동일한 분류가 추가됩니다. 초기 등록 시 한 번 실행하세요.
-- 화면의 등록과정수는 COURSE 연결 데이터의 집계값이므로 저장하지 않습니다.

SET NAMES utf8mb4;

START TRANSACTION;

INSERT INTO TRAINING_JOB_CATEGORY (
    CATEGORY_NAME, SORT_ORDER, INCUMBENT_YN, HIDDEN_YN, USE_YN, CREATED_AT
) VALUES
    ('국가기간전략산업',       0, 'N', 'N', 'Y', NOW()),
    ('내일배움카드(실업자)',   0, 'N', 'N', 'Y', NOW()),
    ('내일배움카드(재직자)',   0, 'Y', 'N', 'Y', NOW()),
    ('일반과정',              0, 'N', 'N', 'Y', NOW());

INSERT INTO EDUCATION_COURSE_CATEGORY (
    CATEGORY_NAME, SORT_ORDER, HIDDEN_YN, USE_YN, CREATED_AT
) VALUES
    ('컴퓨터활용',    1, 'N', 'Y', NOW()),
    ('UI/UX디자인',   2, 'N', 'Y', NOW()),
    ('프로그래밍',    3, 'N', 'Y', NOW()),
    ('바리스타',      4, 'N', 'Y', NOW()),
    ('한식 조리사',   5, 'N', 'Y', NOW());

COMMIT;
