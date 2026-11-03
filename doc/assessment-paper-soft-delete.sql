-- Apply once before using assessment paper APIs (latest diagram: question keys do not include SHEET_ID).
ALTER TABLE QUESTION ADD COLUMN DELETED_YN CHAR(1) NOT NULL DEFAULT 'N' COMMENT '삭제 여부';
ALTER TABLE QUESTION_OPTION ADD COLUMN DELETED_YN CHAR(1) NOT NULL DEFAULT 'N' COMMENT '삭제 여부';
