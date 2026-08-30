-- Day 11. DDL — 1_DDL.md와 순서가 같은 실습 스크립트
-- DDL(CREATE/ALTER/DROP/TRUNCATE)은 실행 즉시 자동 커밋되어 ROLLBACK이 통하지 않음
-- 원본 EMP/DEPT는 건드리지 않고, 사본 테이블(EMP_COPY/DEPT_COPY)에서 실습한다


-- ================================
-- 2. CREATE TABLE과 제약조건
-- ================================
-- (참고용 예시 — 실제 실습은 3절의 CTAS로 진행)

CREATE TABLE DEPT_COPY (
	DEPT_ID     CHAR(2) PRIMARY KEY,
	DEPT_TITLE  VARCHAR(35) NOT NULL,
	LOCATION_ID CHAR(2)
);

CREATE TABLE EMP_COPY (
	EMP_ID     VARCHAR(3) PRIMARY KEY,
	EMP_NAME   VARCHAR(20) NOT NULL,
	EMAIL      VARCHAR(25) UNIQUE,
	DEPT_ID    CHAR(2),
	JOB_CODE   CHAR(2),
	SALARY     INT DEFAULT 0,
	MANAGER_ID VARCHAR(3),
	HIRE_DATE  DATE NOT NULL,
	ENT_YN     CHAR(1) DEFAULT 'N' CHECK (ENT_YN IN ('Y', 'N')),
	CONSTRAINT FK_EMPCOPY_DEPT    FOREIGN KEY (DEPT_ID)    REFERENCES DEPT_COPY(DEPT_ID),
	CONSTRAINT FK_EMPCOPY_MANAGER FOREIGN KEY (MANAGER_ID) REFERENCES EMP_COPY(EMP_ID)
);
-- PRIMARY KEY : 유일 식별(NOT NULL + UNIQUE)
-- FOREIGN KEY : 다른 테이블(또는 자기 자신)의 PK를 참조, 참조 대상엔 PK/UNIQUE 필수
-- NOT NULL / UNIQUE / DEFAULT / CHECK


-- ================================
-- 3. 기존 테이블 복사 — CREATE TABLE ... AS SELECT (CTAS)
-- ================================
-- CTAS는 데이터와 컬럼 정의(타입)만 복사, 제약조건/인덱스/AUTO_INCREMENT는 복사 안 됨

DROP TABLE IF EXISTS EMP_COPY;
DROP TABLE IF EXISTS DEPT_COPY;

CREATE TABLE DEPT_COPY AS SELECT * FROM DEPT;
CREATE TABLE EMP_COPY  AS SELECT * FROM EMP;

DESC EMP_COPY;   -- Key 컬럼이 모두 비어 있음(제약조건 없음)


-- ================================
-- 4. ALTER TABLE
-- ================================

-- 4.1 제약조건 추가 — ADD CONSTRAINT
-- 외래키는 NULL 값 자체는 허용(참조 검사 대상 아님) -> DEPT_ID가 NULL인 2명이 있어도 성공
ALTER TABLE DEPT_COPY ADD CONSTRAINT PK_DEPT_COPY PRIMARY KEY (DEPT_ID);
ALTER TABLE EMP_COPY  ADD CONSTRAINT PK_EMP_COPY  PRIMARY KEY (EMP_ID);

ALTER TABLE EMP_COPY
	ADD CONSTRAINT FK_EMPCOPY_DEPT FOREIGN KEY (DEPT_ID) REFERENCES DEPT_COPY(DEPT_ID);

-- 4.2 컬럼 추가 — ADD COLUMN (기존 행은 전부 NULL로 채워짐)
ALTER TABLE EMP_COPY ADD COLUMN REMARK VARCHAR(50);

-- 4.3 컬럼 변경 — MODIFY COLUMN (이름은 그대로, 정의만 변경)
ALTER TABLE EMP_COPY MODIFY COLUMN REMARK VARCHAR(100);

-- 4.4 컬럼 삭제 — DROP COLUMN
ALTER TABLE EMP_COPY DROP COLUMN REMARK;

-- 4.5 이름 변경 — RENAME COLUMN / RENAME TABLE
ALTER TABLE EMP_COPY RENAME COLUMN EMP_NO TO RESIDENT_NO;
ALTER TABLE EMP_COPY RENAME TO STAFF;
ALTER TABLE STAFF RENAME TO EMP_COPY;   -- 원래 이름으로 되돌림


-- ================================
-- 5. DROP TABLE vs TRUNCATE TABLE
-- ================================
-- TRUNCATE : 데이터만 삭제(구조 유지), DROP : 구조+데이터 모두 삭제
-- 둘 다 DDL이라 자동 커밋되며 ROLLBACK 불가 (DELETE는 DML이라 커밋 전엔 되돌릴 수 있음)

TRUNCATE TABLE EMP_COPY;   -- 데이터만 전부 삭제, 테이블 구조는 남음

CREATE TABLE SCRATCH_TEST (ID INT);
DROP TABLE SCRATCH_TEST;   -- 테이블 구조 + 데이터 모두 삭제

-- 주의 — 부모 테이블 먼저 DROP:
-- EMP_COPY.DEPT_ID가 DEPT_COPY.DEPT_ID를 참조하는 FK가 걸린 상태에서
-- DROP TABLE DEPT_COPY;를 실행하면 "다른 테이블이 참조 중"이라는 오류가 남
-- (자식의 FK를 먼저 제거하거나, 자식 테이블을 먼저 삭제해야 함 — 실습 문제 7 참고)

-- 데이터 복구 (다음 챕터 실습을 위해 다시 채워둠)
INSERT INTO EMP_COPY SELECT * FROM EMP;
