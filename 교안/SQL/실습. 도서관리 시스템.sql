-- DDL
-- DBMS에서 제공해주는 여러가지 객체를 생성(CREATE), 수정(ALTER), 삭제(DROP)
-- CREATE TABLE 테이블이름 (
-- 		속성이름 타입 [제약조건],
--		속성이름 타입 [제약조건]
-- );
-- AUTO_INCREMENT : 식별자, 값을 입력하지 않아도 1부터 시작하는 1씩 증가하는 일련번호가 입력됨
-- PRIMARY KEY : 식별자, 중복이 될수 없고 NULL을 입력 할수 없다 --- 하나의 행을 유일무이하게 구분
-- NOT NULL : 필수입력
CREATE TABLE MEMBER (
    MEMBER_ID INT          AUTO_INCREMENT PRIMARY KEY,
    NAME      VARCHAR(20)  NOT NULL,
    PHONE     VARCHAR(20)  NOT NULL,
    ADDRESS   VARCHAR(100)
);
CREATE TABLE BOOK (
    BOOK_ID INT           AUTO_INCREMENT PRIMARY KEY,
    TITLE   VARCHAR(100)  NOT NULL,
    AUTHOR  VARCHAR(50)
);
CREATE TABLE RENTAL_STATUS_CODE (
    STATUS_CODE VARCHAR(10) PRIMARY KEY,
    STATUS_NAME VARCHAR(20) NOT NULL
);

-- 외래키 제약조건을 적용하는 경우, 참조테이블이 미리 생성되어져 있어야 한다!!!!
CREATE TABLE RENTAL (
    RENTAL_ID    INT          AUTO_INCREMENT PRIMARY KEY,
    MEMBER_ID    INT          NOT NULL,
    BOOK_ID      INT          NOT NULL,
    RENTAL_DATE  DATE         NOT NULL,
    RETURN_DATE  DATE,
    STATUS_CODE  VARCHAR(10)  NOT NULL,
    -- CONSTRAINT 제약조건이름 FOREIGN KEY (컬럼명) REFERENCES 참조테이블명 (컬럼명)
    -- 데이터의 무결성 유지
    -- 사원테이블의 사원ID 참조하면 등록되지 않은 사원ID를 입력시 오류가 발생
    CONSTRAINT FK_RENTAL_MEMBER FOREIGN KEY (MEMBER_ID) REFERENCES MEMBER (MEMBER_ID),
    CONSTRAINT FK_RENTAL_BOOK   FOREIGN KEY (BOOK_ID)   REFERENCES BOOK (BOOK_ID),
    CONSTRAINT FK_RENTAL_STATUS FOREIGN KEY (STATUS_CODE) REFERENCES RENTAL_STATUS_CODE (STATUS_CODE)
);


-- 테이블 삭제
DROP TABLE RENTAL;
DROP TABLE RENTAL_STATUS_CODE;
DROP TABLE MEMBER;
DROP TABLE BOOK;

-- EMP_COPY 테이블 만들기
-- EMP_ID   	INT   PRIMARY KEY AUTO_INCREMENT
-- EMP_NAME 	VARCHAR(20)
-- EMP_NO 		CHAR(14)
-- JOB_CODE		CHAR(2)		FK(JOB테이블의 JOB_CODEF를 참조하고 이름은 FK_EMPCOPY_JOB)
CREATE TABLE EMP_COPY (
    EMP_ID   	INT      AUTO_INCREMENT PRIMARY KEY,
    EMP_NAME    VARCHAR(20)		,
    BOOK_ID     CHAR(14)        ,
    JOB_CODE  	CHAR(2)         , 
    -- 참조키 - 다른테이블의 기본키를 참조
    -- JOB 테이블의 기본키를 먼저 설정
    CONSTRAINT FK_EMPCOPY_JOB FOREIGN KEY (JOB_CODE) REFERENCES JOB (JOB_CODE)
);
-- 테이블의 구조를 변경 - 기본키 지정
ALTER TABLE JOB ADD PRIMARY KEY (JOB_CODE);
-- 여러문장을 스크립트로 작성후 실행 -> 오류가 발생하면 멈춤
-- IF EXISTS -> 경고만 출력후 다음명령문을 실행
DROP TABLE IF EXISTS EMP_COPY;

-- 테이블을 복사해서 만들기
-- SELECT문의 결과집합을 테이블로 생성 -> 결과집합의 데이터가 있다면 데이터까지 입력해줌
-- CREATE TABLE 테이블이름 AS SELECT문;
CREATE TABLE EMP_COPY AS SELECT EMP_ID, EMP_NAME FROM EMP;
-- 테이블의 구조만 복사하는 방법
SELECT EMP_ID, EMP_NAME FROM EMP WHERE 1=0; -- 데이터가 한건도 조회되지 않는다!!!
CREATE TABLE EMP_COPY AS SELECT EMP_ID, EMP_NAME FROM EMP WHERE 1=0;


SELECT * FROM EMP_COPY;

-- EMP테이블의 구조와 모든 데이터가 복사
-- 제약조건은 복사 되지 않는다!
CREATE TABLE EMP_BK AS SELECT * FROM EMP;

-- VIEW를 이용해서 테이블 만들기
CREATE TABLE VIEW_BK AS SELECT * FROM emp_master;


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
    -- DEPT(DEPT_ID) 기본키로 설정
    CONSTRAINT FK_EMPCOPY_DEPT    FOREIGN KEY (DEPT_ID)    REFERENCES DEPT(DEPT_ID),
    CONSTRAINT FK_EMPCOPY_MANAGER FOREIGN KEY (MANAGER_ID) REFERENCES EMP_COPY(EMP_ID)
);

DROP TABLE EMP_COPY;
SELECT * FROM EMP_COPY;

-- 테이블의 구조를 확인
DESC EMP_COPY;

-- DML - 데이터 삽입, 삭제, 수정
-- 데이터 삽입
-- 모든 컬럼에 데이터를 입력시 컬럼을 지정하지 않고 데이터만 입력
-- INSERT INTO 테이블 이름 VALUES (컬럼에 입력할 값, .....);
-- INSERT INTO EMP_COPY (입력할 컬럼, ...) VALUES (입력할 값, ...);
-- SYSDATE() : 현재 날자와 시간을 반환하는 기본 내장함수
-- CURDATE() : 날자만 반환
INSERT INTO EMP_COPY (EMP_ID, EMP_NAME, HIRE_DATE, DEPT_ID) VALUES ('002', '오미자', CURDATE(), 'D1');
SELECT * FROM EMP_COPY;
SELECT * FROM DEPT;

-- 날자를 입력할때 문자 형식으로 입력해도 자동형변환에 의해서 날자타입으로 변환되서 입력되어짐
INSERT INTO EMP_COPY (EMP_ID, EMP_NAME, HIRE_DATE, DEPT_ID) VALUES ('003', '오미자', '2026-08-01', 'D1');














INSERT INTO EMP_COPY (EMP_ID, EMP_NAME, HIRE_DATE, ENT_YN) VALUES ('001', '오미자', SYSDATE(), 'D');



