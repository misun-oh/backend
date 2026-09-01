select * from emp;
-- 1. auto commit : false
-- DDL문장은 트랜젝션처리와 무관 - 실행하는 순간 커밋됨!!
-- DML문장은 COMMIT 하지 않으면 저장되지 않는다
use haksa;
use hr;
-- 2. 다른 스키마의 데이터를 조회 하는 경우 스키마이름.테이블이름 (스키마이름을 명시)
-- 다른스키마에 접근 하기 위해서는 권한이 필요하다!!
-- 권한 불충분시 접근불가능
select * from haksa.tb_class;

-- 실습테이블 준비
DROP TABLE IF EXISTS EMP_COPY;
-- 테이블의 구조와 데이터를 복사 -> 제약조건, 키는 복사되지 않는다!
CREATE TABLE EMP_COPY AS SELECT * FROM EMP;

-- 언제나 참인 조건, 언제나 거짓인 조건은 가끔 사용 될 수 있음
SELECT * FROM EMP WHERE 1 != 1
-- 조건이 있을수도 있고 없을수도 있을때
AND EMP_NAME LIKE '박%'
AND BONUS IS NOT NULL;

-- -----------------------
-- DML - 데이터 조작어
-- 데이터 삽입, 수정, 삭제
-- -----------------------
-- 1. 전체 컬럼에 데이터를 입력
DESC EMP_COPY;

-- 외래키 제약조건, 기본키 제약조건이 걸리면 입력에 제한
-- 기본키 제약조건 NULL입력불가, 중복값 입력 불가
INSERT INTO EMP_COPY VALUES (NULL, '이미자', '111111-2222222', 'aaa@bbb.com', '01022223333'
								, 'D8', 'J7', 3000000, 0.3, 200, NOW(), NULL, 'N');
INSERT INTO EMP_COPY VALUES ('223', '이미자', '111111-2222222', 'aaa@bbb.com', '01022223333'
								, 'D8', 'J7', 3000000, 0.3, 200, CURRENT_DATE(), NULL, 'N');                                
-- 외래키 제약조건 - NULL 입력 가능, 값이 입력 된다면 참조 테이틀에 등록된 값인지 확인
-- 무결성 지키기 위해서 (쓸모없는값이나 쓰레기 데이터가 들어오지 못하게 막는 역할)
-- NULL과 ''(빈문자열)을 다른다!
-- 0	101	11:44:50	INSERT INTO EMP_COPY VALUES ('225', '이미자', '111111-2222222', 'aaa@bbb.com', '01022223333'
--         , 'D0', 'J7', 3000000, 0.3, 200, '2026-07-21', NULL, 'N')	
-- Error Code: 1452. Cannot add or update a child row: a foreign key constraint fails 
-- (`hr`.`emp_copy`, CONSTRAINT `FK_EMP_DEPT` FOREIGN KEY (`DEPT_ID`) REFERENCES `dept` (`DEPT_ID`))	
INSERT INTO EMP_COPY VALUES ('225', '이미자', '111111-2222222', 'aaa@bbb.com', '01022223333'
								, 'D2', 'J7', 3000000, 0.3, 200, '2026-07-21', NULL, 'N');                                  
SELECT * FROM DEPT;
-- 날자 + 시간
SELECT NOW(), CURRENT_DATE();

-- 2. 일부 컬럼에 데이터를 입력
DESC EMP_COPY;
-- 테이블 이름 뒤에 컬럼을 명시
INSERT INTO EMP_COPY (EMP_ID, EMP_NAME) VALUES ('226', '아직자');
-- 하나의 SQL문으로 다중행 입력하기
INSERT INTO EMP_COPY (EMP_ID, EMP_NAME) VALUES 
('230', '일이삼'),
('231', '이삼사'),
('232', '오육칠'),
('233', '팔구십');




SELECT * FROM EMP_COPY ORDER BY EMP_ID DESC;

ROLLBACK;

-- 시스템 변수를 통해 현재 설정을 확인
-- FALSE : 0, TRUE : 1
SELECT @@autocommit;
-- 변수값 설정 SET 변수명 = 값;
SET AUTOCOMMIT = 0;

-- 모든 글로벌 변수 확인
SHOW GLOBAL VARIABLES;

-- 모든 세션 변수 확인
SHOW SESSION VARIABLES; -- (SESSION 생략 가능)

-- 특정 변수 검색 (예: 캐릭터셋 관련 설정 확인)
SHOW VARIABLES LIKE 'char%';


-- 데이터 수정
-- 사번 225번의 주민번호를 업데이트
-- SET문장에는 기존 컬럼, 함수를 사용할 수 있다
-- Safe Updates 모드 : 실수로 테이블 전체를 바꾸는 사고를 막으려고 Safe Updates 모드가 활성화
SELECT @@SESSION.sql_safe_updates;   -- 1이면 켜져 있음, 0이면 꺼져 있음
SET SESSION sql_safe_updates = 1;    -- 이 커넥션에서만 해제 (재접속하면 원상복구)

-- 기본키를 조건으로 주면 무조건 1개의 행만 업데이트가 됨
UPDATE EMP_COPY SET EMP_NO = '111111-*******', HIRE_DATE = CURRENT_DATE() WHERE EMP_ID = '20';
SELECT * FROM EMP_COPY;

-- 보너스 업데이트 
-- 모든 사원의 보너스를 0.3 일괄 업데이트
-- SAFE MODE 활성화 시 오류 발생
UPDATE EMP_COPY SET BONUS = 0.3; -- 모든 행이 영향을 받는다
-- IFNULL함수를 이용해서 NULL을 치환
UPDATE EMP_COPY SET BONUS = IFNULL(BONUS, 0) + 0.3; -- 모든 행이 영향을 받는다
ROLLBACK;
COMMIT;
-- NULL값은 연산의 대상이 아님... 
SELECT IFNULL(BONUS, 0) FROM EMP;

-- 200번 사원의 주민번호(123456-1234567)와 보너스(기본보너스 * 1.1) 업데이트
SELECT * FROM EMP;
UPDATE EMP_COPY SET EMP_NO = '123456-1234567', BONUS = BONUS * 1.1 WHERE EMP_ID='200';
COMMIT;

-- 데이터 삭제
DELETE FROM EMP_COPY; -- 조건절을 생략하면 모든 데이터를 삭제
ROLLBACK;
-- 2024년 이후 입사자 제거
-- 날자를 이용해서 조건을 주는경우
DELETE FROM EMP_COPY WHERE HIRE_DATE > '2024-01-01';
SELECT * FROM EMP_COPY;
ROLLBACK;

-- 기본키 추가
ALTER TABLE EMP_COPY ADD PRIMARY KEY (EMP_ID);
DESC EMP_COPY;
-- 외래키 추가 - 참조테이블의 컬럼에 등록된 값만 사용
ALTER TABLE EMP_COPY ADD CONSTRAINT FK_EMP_DEPT FOREIGN KEY (DEPT_ID) REFERENCES DEPT(DEPT_ID);






















