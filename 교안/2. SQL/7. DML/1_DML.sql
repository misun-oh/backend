-- Day 12. DML — 1_DML.md와 순서가 같은 실습 스크립트
-- INSERT/UPDATE/DELETE는 DDL과 달리 COMMIT 전까지는 ROLLBACK으로 되돌릴 수 있음(TCL 챕터)


-- ================================
-- 1. DML이란 + 실습 준비
-- ================================
-- 모든 글로벌 변수 확인
SHOW GLOBAL VARIABLES;

-- 모든 세션 변수 확인
SHOW SESSION VARIABLES; -- (SESSION 생략 가능)

-- 특정 변수 검색 (예: 캐릭터셋 관련 설정 확인)
SHOW VARIABLES LIKE 'char%';


DROP TABLE IF EXISTS EMP_COPY;
CREATE TABLE EMP_COPY AS SELECT * FROM EMP;

SELECT COUNT(*) FROM EMP_COPY;   -- 21

-- [환경 확인] Safe Updates 모드 (sql_safe_updates)
-- MySQL Workbench는 이 모드를 기본으로 켠다. 켜져 있으면 키(인덱스) 컬럼을 조건으로
-- 쓰지 않는 UPDATE/DELETE를 Error 1175로 거부한다(실수로 전체 행을 바꾸는 사고 방지).
-- EMP_COPY는 CTAS라 PK/인덱스가 없어 WHERE EMP_ID = '...' 조차 "키 아님"으로 막힌다.
SELECT @@SESSION.sql_safe_updates;   -- 1 = 켜짐, 0 = 꺼짐

-- 실습 동안만 해제 (현재 커넥션에서만 유효, 재접속하면 Workbench 기본값으로 복귀)
SET SESSION sql_safe_updates = 0;


-- ================================
-- 2. INSERT INTO - 전체 컬럼
-- ================================
-- 컬럼명 생략 시 CREATE TABLE에 정의된 컬럼 순서 그대로 값을 나열해야 함

INSERT INTO EMP_COPY
VALUES ('221', '홍길동', '000101-3123456', 'hong_gd@company.com', '01012345678',
        'D9', 'J7', 2600000, NULL, '200', '2026-01-05', NULL, 'N');

SELECT EMP_ID, EMP_NAME, DEPT_ID, JOB_CODE, SALARY, HIRE_DATE
FROM EMP_COPY WHERE EMP_ID = '221';


-- ================================
-- 3. INSERT INTO - 컬럼 지정(일부 컬럼)
-- ================================
-- 지정하지 않은 컬럼은 NULL(또는 DEFAULT 제약이 있다면 그 기본값)로 채워짐
-- EMP_COPY는 CTAS로 만든 사본이라 DEFAULT 제약이 복사되지 않아 ENT_YN도 NULL이 됨

INSERT INTO EMP_COPY (EMP_ID, EMP_NAME, DEPT_ID, JOB_CODE, SALARY, HIRE_DATE)
VALUES ('222', '김하나', 'D8', 'J7', 2450000, '2026-02-16');

SELECT * FROM EMP_COPY WHERE EMP_ID = '222';


-- ================================
-- 4. 다중 행 INSERT
-- ================================
-- VALUES 뒤에 괄호를 쉼표로 이어 쓰면 여러 행을 한 번에 추가 가능

INSERT INTO EMP_COPY (EMP_ID, EMP_NAME, DEPT_ID, JOB_CODE, SALARY, HIRE_DATE) VALUES
('223', '이서준', 'D5', 'J7', 2300000, '2026-03-02'),
('224', '박은서', 'D6', 'J7', 2350000, '2026-03-02');

SELECT COUNT(*) FROM EMP_COPY;   -- 25


-- ================================
-- 5. INSERT INTO ... SELECT
-- ================================
-- INSERT INTO의 컬럼 목록과 SELECT절 컬럼의 개수·순서·타입이 일치해야 함

CREATE TABLE HIGH_PAID_EMP AS
SELECT EMP_ID, EMP_NAME, SALARY FROM EMP_COPY WHERE 1=0;   -- 구조만 복사 (DDL 챕터 참고)

INSERT INTO HIGH_PAID_EMP (EMP_ID, EMP_NAME, SALARY)
SELECT EMP_ID, EMP_NAME, SALARY
FROM EMP_COPY
WHERE SALARY >= 5000000;

SELECT * FROM HIGH_PAID_EMP;


-- ================================
-- 6. UPDATE - 단일 행
-- ================================

UPDATE EMP_COPY SET SALARY = 2700000 WHERE EMP_ID = '222';

SELECT EMP_NAME, SALARY FROM EMP_COPY WHERE EMP_ID = '222';


-- ================================
-- 7. UPDATE - 여러 컬럼 동시 수정
-- ================================
-- SET 뒤에 컬럼 = 값을 쉼표로 이어 쓰면 한 문장으로 여러 컬럼을 동시에 변경

UPDATE EMP_COPY
SET DEPT_ID = 'D8', JOB_CODE = 'J6'
WHERE EMP_NAME = '김하나';

SELECT EMP_NAME, DEPT_ID, JOB_CODE FROM EMP_COPY WHERE EMP_NAME = '김하나';


-- ================================
-- 8. UPDATE - 여러 행에 한 번에 적용 (표현식 사용)
-- ================================
-- WHERE 조건에 맞는 행 전부가 수정됨, SET에 컬럼 자신의 값을 활용한 계산식도 가능

UPDATE EMP_COPY
SET SALARY = SALARY * 1.1
WHERE DEPT_ID = 'D8';

SELECT EMP_ID, EMP_NAME, SALARY FROM EMP_COPY WHERE DEPT_ID = 'D8' ORDER BY EMP_ID;


-- ================================
-- 9. WHERE절의 중요성
-- ================================
-- UPDATE/DELETE는 WHERE를 빠뜨리면 테이블의 모든 행에 영향을 줌 (가장 위험한 실수)
-- 위험한 예시(실행하지 마세요): UPDATE EMP_COPY SET SALARY = 0;

-- 안전한 습관: 1) 같은 조건으로 먼저 SELECT해서 대상 확인 -> 2) 확인된 조건으로 실행
SELECT * FROM EMP_COPY WHERE DEPT_ID = 'D2' AND SALARY < 2000000;

UPDATE EMP_COPY SET SALARY = SALARY + 100000
WHERE DEPT_ID = 'D2' AND SALARY < 2000000;


-- ================================
-- 10. DELETE FROM ... WHERE
-- ================================
-- DELETE는 DDL의 TRUNCATE TABLE과 달리 WHERE로 삭제 대상을 골라낼 수 있음

DELETE FROM EMP_COPY WHERE EMP_ID IN ('223', '224');

SELECT COUNT(*) FROM EMP_COPY;   -- 23

-- DELETE(DML, 커밋 전 ROLLBACK 가능) vs TRUNCATE/DROP(DDL, 자동 커밋)은
-- DDL 챕터의 "DROP TABLE vs TRUNCATE TABLE" 절 참고


-- ================================
-- 11. 실습 마무리 - 안전장치 복구
-- ================================
-- 실습하며 해제했던 Safe Updates 모드를 다시 켠다.
SET SESSION sql_safe_updates = 1;
