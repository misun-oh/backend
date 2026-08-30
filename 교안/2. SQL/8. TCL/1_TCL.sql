-- Day 13. TCL — 1_TCL.md와 순서가 같은 실습 스크립트
-- ACID: 원자성/일관성/고립성/지속성. 이 챕터는 COMMIT(확정)과 ROLLBACK(취소)을 다룸


-- ================================
-- 2. AUTOCOMMIT
-- ================================

SELECT @@autocommit;   -- 1 (MySQL 기본값: 켜짐)

-- START TRANSACTION 이후부터는 COMMIT/ROLLBACK을 직접 실행하기 전까지 확정되지 않음
-- START TRANSACTION;


-- ================================
-- 3. COMMIT
-- ================================

DROP TABLE IF EXISTS EMP_COPY;
CREATE TABLE EMP_COPY AS SELECT * FROM EMP;

START TRANSACTION;
UPDATE EMP_COPY SET SALARY = SALARY * 1.1 WHERE DEPT_ID = 'D8';
COMMIT;

SELECT EMP_ID, EMP_NAME, SALARY FROM EMP_COPY WHERE DEPT_ID = 'D8' ORDER BY EMP_ID;
-- COMMIT 이후에는 같은 트랜잭션 안에서 ROLLBACK을 실행해도 되돌릴 수 없음


-- ================================
-- 4. ROLLBACK
-- ================================

START TRANSACTION;

DELETE FROM EMP_COPY WHERE DEPT_ID = 'D2';
SELECT COUNT(*) FROM EMP_COPY;   -- 18

ROLLBACK;

SELECT COUNT(*) FROM EMP_COPY;   -- 21 (COMMIT 전이었으므로 완전히 복구)


-- ================================
-- 5. SAVEPOINT
-- ================================
-- SAVEPOINT는 트랜잭션 안의 "책갈피", ROLLBACK TO는 그 지점 이후 변경만 취소

START TRANSACTION;

UPDATE EMP_COPY SET SALARY = SALARY * 1.05 WHERE DEPT_ID = 'D5';
SAVEPOINT SP1;

DELETE FROM EMP_COPY WHERE DEPT_ID = 'D9';
SELECT COUNT(*) FROM EMP_COPY;   -- 18

ROLLBACK TO SP1;
SELECT COUNT(*) FROM EMP_COPY;   -- 21 (D9 삭제만 취소, D5 급여 인상은 유지)

SELECT EMP_ID, EMP_NAME, SALARY FROM EMP_COPY WHERE DEPT_ID = 'D5' ORDER BY EMP_ID;

COMMIT;   -- ROLLBACK TO 이후에도 트랜잭션은 계속 진행 중 -> 최종 COMMIT/ROLLBACK 필요


-- ================================
-- 6. DDL의 묵시적 커밋
-- ================================
-- ALTER/CREATE/DROP/TRUNCATE 실행 시 그 앞의 트랜잭션까지 통째로 자동 커밋됨
-- -> 트랜잭션 중간에 DDL을 실행하면 안 됨

START TRANSACTION;

UPDATE EMP_COPY SET SALARY = 0 WHERE EMP_ID = '200';

ALTER TABLE EMP_COPY ADD COLUMN MEMO VARCHAR(20);   -- DDL 실행 -> 여기서 묵시적 커밋

ROLLBACK;   -- 이미 늦음, 위 UPDATE는 되돌아가지 않음

SELECT SALARY FROM EMP_COPY WHERE EMP_ID = '200';   -- 0


-- ================================
-- 7. 언제 트랜잭션으로 묶어야 하는가
-- ================================
-- 여러 DML이 "논리적으로 하나의 작업"일 때 START TRANSACTION ~ COMMIT/ROLLBACK으로 묶는다
-- 예: 계좌 이체(출금 UPDATE + 입금 UPDATE), 주문 처리(주문 INSERT + 재고 차감 UPDATE)
