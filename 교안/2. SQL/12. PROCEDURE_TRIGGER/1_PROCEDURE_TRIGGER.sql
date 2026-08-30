# 1_PROCEDURE_TRIGGER.md 본문 실습 진행 스크립트 (Day 17)

# 0. 실습 준비 - 사본 테이블 + 로그 테이블
DROP TABLE IF EXISTS EMP_COPY;                       -- 이전 실습에서 만든 사본이 있으면 제거
CREATE TABLE EMP_COPY AS SELECT * FROM EMP;          -- 원본 EMP를 건드리지 않도록 사본 생성 (구조+데이터 복사, 제약조건은 복사 안 됨)

DROP TABLE IF EXISTS SALARY_LOG;                     -- 급여 변경 이력 로그 테이블 초기화
CREATE TABLE SALARY_LOG (
    LOG_ID     INT AUTO_INCREMENT PRIMARY KEY,       -- 로그 일련번호 (자동 증가)
    EMP_ID     VARCHAR(3),                           -- 어떤 사원의
    OLD_SALARY INT,                                  -- 변경 전 급여
    NEW_SALARY INT,                                  -- 변경 후 급여
    CHANGED_AT DATETIME DEFAULT NOW()                -- 기록 시각 (미지정 시 현재 시각)
);

# 1. PROCEDURE 기본 - IN 파라미터
DELIMITER $$                                         -- 본문 안 ; 때문에 정의가 끊기지 않도록 문장 종결자를 $$ 로 변경

CREATE PROCEDURE RAISE_SALARY(IN P_DEPT_ID CHAR(2), IN P_RATE DECIMAL(3,2))   -- 부서코드와 인상률을 입력(IN)으로 받는 프로시저
BEGIN
    UPDATE EMP_COPY                                  -- 해당 부서 사원들의
    SET SALARY = SALARY * (1 + P_RATE)              -- 급여를 (1 + 인상률)배로 올림
    WHERE DEPT_ID = P_DEPT_ID;
END $$

DELIMITER ;                                          -- 정의 끝. 문장 종결자를 기본값 ; 로 복원

CALL RAISE_SALARY('D8', 0.10);                       -- D8 부서 급여 10% 인상 실행 (autocommit=1이면 UPDATE가 즉시 커밋)

SELECT EMP_ID, EMP_NAME, SALARY FROM EMP_COPY WHERE DEPT_ID = 'D8' ORDER BY EMP_ID;   -- 결과 확인

# 2. OUT 파라미터 - 결과값 반환
DELIMITER $$

CREATE PROCEDURE GET_DEPT_AVG_SALARY(IN P_DEPT_ID CHAR(2), OUT P_AVG_SALARY INT)   -- 부서코드 입력(IN), 평균급여를 출력(OUT)으로 돌려줌
BEGIN
    SELECT ROUND(AVG(SALARY)) INTO P_AVG_SALARY     -- 조회 결과(1행 1값)를 OUT 파라미터에 담음
    FROM EMP_COPY
    WHERE DEPT_ID = P_DEPT_ID;
END $$

DELIMITER ;

CALL GET_DEPT_AVG_SALARY('D9', @avg_sal);            -- @avg_sal = 세션 사용자 변수. OUT 값을 받는 "그릇"으로 넘김
SELECT @avg_sal;                                     -- 돌려받은 값 확인

# 3. IF 제어문
DELIMITER $$

CREATE PROCEDURE CLASSIFY_SALARY(IN P_EMP_ID VARCHAR(3), OUT P_GRADE VARCHAR(10))   -- 사번을 받아 급여 등급 문자열을 OUT으로 반환
BEGIN
    DECLARE V_SALARY INT;                            -- 블록 지역 변수 선언 (BEGIN 바로 다음, @ 없음)

    SELECT SALARY INTO V_SALARY FROM EMP_COPY WHERE EMP_ID = P_EMP_ID;   -- 그 사원의 급여를 지역 변수에 담음

    IF V_SALARY >= 5000000 THEN                      -- 조건 분기 (값이 아니라 문장을 실행하는 제어문)
        SET P_GRADE = '고액';
    ELSEIF V_SALARY >= 2500000 THEN
        SET P_GRADE = '중액';
    ELSE
        SET P_GRADE = '소액';
    END IF;
END $$

DELIMITER ;

CALL CLASSIFY_SALARY('200', @grade);                 -- 곽상혁(200) 등급 계산
SELECT @grade;

# 4.1 반복문 연습 - 구구단 (WHILE / LOOP / 중첩)
#   WHILE 조건 DO ... END WHILE     : 조건을 먼저 검사
#   LOOP ... END LOOP              : 종료 조건 없음 -> IF ... THEN LEAVE 레이블 로 직접 탈출
#   LEAVE 레이블 = break,  ITERATE 레이블 = continue  (레이블 필수, break/continue 키워드 없음)
DELIMITER $$

CREATE PROCEDURE GUGUDAN(IN P_DAN INT)               -- (a) WHILE 로 한 단 출력
BEGIN
    DECLARE I INT DEFAULT 1;
    DECLARE MSG TEXT DEFAULT '';
    WHILE I <= 9 DO                                  -- I 가 9 이하인 동안 반복
        SET MSG = CONCAT(MSG, P_DAN, ' x ', I, ' = ', P_DAN * I, '\n');
        SET I = I + 1;                               -- 증가시키지 않으면 무한 루프
    END WHILE;
    SELECT MSG AS 구구단;
END $$

CREATE PROCEDURE GUGUDAN_LOOP(IN P_DAN INT)          -- (b) 같은 것을 LOOP + IF + LEAVE 로
BEGIN
    DECLARE I INT DEFAULT 1;
    DECLARE MSG TEXT DEFAULT '';
    DAN_LOOP: LOOP                                   -- 레이블(DAN_LOOP) 필요
        IF I > 9 THEN LEAVE DAN_LOOP; END IF;        -- 종료 조건을 직접 검사해 탈출
        SET MSG = CONCAT(MSG, P_DAN, ' x ', I, ' = ', P_DAN * I, '\n');
        SET I = I + 1;
    END LOOP;
    SELECT MSG AS 구구단;
END $$

CREATE PROCEDURE GUGUDAN_ALL()                       -- (c) 중첩: 바깥 LOOP(단) + 안쪽 WHILE(1~9)
BEGIN
    DECLARE DAN INT DEFAULT 2;
    DROP TEMPORARY TABLE IF EXISTS TMP_GUGU;
    CREATE TEMPORARY TABLE TMP_GUGU (DAN INT, I INT, RESULT INT);
    DAN_LOOP: LOOP
        IF DAN > 9 THEN LEAVE DAN_LOOP; END IF;
        BEGIN                                        -- 안쪽 블록: 단마다 I 를 1로 새로 선언
            DECLARE I INT DEFAULT 1;
            WHILE I <= 9 DO
                INSERT INTO TMP_GUGU VALUES (DAN, I, DAN * I);
                SET I = I + 1;
            END WHILE;
        END;
        SET DAN = DAN + 1;
    END LOOP;
    SELECT * FROM TMP_GUGU ORDER BY I, DAN;          -- I 먼저 정렬 -> 가로줄로 읽힘
END $$

DELIMITER ;

CALL GUGUDAN(7);
CALL GUGUDAN_LOOP(7);
CALL GUGUDAN_ALL();

# 4.2 커서(CURSOR) - 반복문 + FETCH 로 테이블 행 순회
# ---------------------------------------------------------------------
# 커서 = SELECT 결과를 한 행씩 순회하는 읽기 포인터. 앞으로만 이동, 되감기 불가.
# 한 문장(UPDATE ... WHERE / INSERT ... SELECT)으로 안 될 때만 쓴다.
#
# [문법 5단계]  선언 순서: 변수 -> 커서 -> 핸들러
#   ① DECLARE 커서명 CURSOR FOR <SELECT>;                 -- 순회할 결과 정의 (아직 실행 안 함)
#   ② DECLARE CONTINUE HANDLER FOR NOT FOUND SET done=1;  -- 마지막 행 다음을 FETCH하면 NOT FOUND -> 플래그 세움 (없으면 오류 1329)
#   ③ OPEN  커서명;                                       -- 여기서 SELECT 실행, 포인터를 첫 행 앞에 둠
#   ④ FETCH 커서명 INTO 변수들;                           -- 현재 행 값을 변수에 담고 다음 행으로. SELECT 컬럼 개수/순서와 변수 목록 일치
#      LOOP 안에서 ④ 반복, 매번 done 확인해 LEAVE 로 탈출
#   ⑤ CLOSE 커서명;                                       -- 리소스 해제 (프로시저 끝나면 자동이지만 명시하는 게 관례)
#
# 관용구:  OPEN -> LOOP { FETCH -> IF done LEAVE -> 처리 } -> CLOSE
# ---------------------------------------------------------------------
DROP TABLE IF EXISTS EMP_COPY;                       -- 1절에서 급여가 바뀐 상태이므로 사본을 다시 만들어 초기화
CREATE TABLE EMP_COPY AS SELECT * FROM EMP;

DELIMITER $$

CREATE PROCEDURE FLAG_LOW_SALARY()                   -- 파라미터 없이, 저급여 사원을 로그에 기록하는 프로시저
BEGIN
    DECLARE V_DONE INT DEFAULT 0;                    -- 커서 순회 종료 플래그 (0=계속, 1=끝)
    DECLARE V_EMP_ID VARCHAR(3);                     -- FETCH로 꺼낸 한 행의 값을 담을 지역 변수
    DECLARE V_SALARY INT;

    DECLARE CUR CURSOR FOR                           -- 커서 선언: 순회할 SELECT 결과를 정의
        SELECT EMP_ID, SALARY FROM EMP_COPY;
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET V_DONE = 1;   -- 더 가져올 행이 없으면(NOT FOUND) 플래그를 1로 하고 계속 진행

    OPEN CUR;                                        -- 커서 열기 (SELECT 실행)

    READ_LOOP: LOOP                                  -- 이름표(READ_LOOP)를 붙인 반복문
        FETCH CUR INTO V_EMP_ID, V_SALARY;          -- 다음 한 행을 꺼내 지역 변수에 대입
        IF V_DONE = 1 THEN
            LEAVE READ_LOOP;                         -- 행이 없으면 반복문 탈출
        END IF;

        IF V_SALARY < 2000000 THEN                   -- 급여 200만원 미만이면
            INSERT INTO SALARY_LOG (EMP_ID, OLD_SALARY, NEW_SALARY)
            VALUES (V_EMP_ID, V_SALARY, V_SALARY);   -- 로그 테이블에 기록
        END IF;
    END LOOP;

    CLOSE CUR;                                       -- 커서 닫기 (리소스 해제)
END $$

DELIMITER ;

CALL FLAG_LOW_SALARY();                              -- 실행

SELECT EMP_ID, OLD_SALARY FROM SALARY_LOG ORDER BY EMP_ID;   -- 기록된 저급여 사원 확인

# 5. 예외 처리 - HANDLER
# 5.2 NOT FOUND 를 EXIT 로: 없는 사번을 조회해도 죽지 않고 -1 반환
DELIMITER $$

CREATE PROCEDURE GET_SALARY_SAFE(IN P_EMP_ID VARCHAR(3), OUT P_SALARY INT)
BEGIN
    DECLARE EXIT HANDLER FOR NOT FOUND               -- NOT FOUND(결과 0행) 발생 시: 핸들러 실행 후 블록 즉시 종료
        SET P_SALARY = -1;                           -- 결과가 없으면 -1을 돌려줌

    SELECT SALARY INTO P_SALARY FROM EMP_COPY WHERE EMP_ID = P_EMP_ID;   -- 0행이면 NOT FOUND -> 위 핸들러 발동
END $$

DELIMITER ;

CALL GET_SALARY_SAFE('999', @sal);                   -- 존재하지 않는 사번
SELECT @sal;                                         -- -1

# 5.3 SQLEXCEPTION + 트랜잭션 롤백 (실무 패턴): 도중 오류 나면 전부 취소 + 오류 재전달
DELIMITER $$

CREATE PROCEDURE TRANSFER_SALARY(IN P_FROM VARCHAR(3), IN P_TO VARCHAR(3), IN P_AMT INT)
BEGIN
    DECLARE EXIT HANDLER FOR SQLEXCEPTION            -- 어떤 오류든(경고/NOT FOUND 제외 전부) 발생 시
    BEGIN                                            -- 동작이 두 문장 이상이라 BEGIN..END 로 묶음
        ROLLBACK;                                    -- 지금까지의 변경 전부 취소
        RESIGNAL;                                    -- 원래 오류를 호출부(앱)로 그대로 다시 전달
    END;

    START TRANSACTION;
        UPDATE EMP_COPY SET SALARY = SALARY - P_AMT WHERE EMP_ID = P_FROM;   -- 출금
        UPDATE EMP_COPY SET SALARY = SALARY + P_AMT WHERE EMP_ID = P_TO;     -- 입금 (여기서 오류 나면 위 핸들러가 ROLLBACK)
    COMMIT;                                          -- 둘 다 성공해야 확정
END $$

DELIMITER ;

CALL TRANSFER_SALARY('205', '206', 100000);          -- 정상: 두 UPDATE 후 COMMIT
SELECT EMP_ID, SALARY FROM EMP_COPY WHERE EMP_ID IN ('205','206');

# 5.4 CONTINUE 로 특정 오류(중복 키 1062)만 무시하고 계속
DELETE FROM SALARY_LOG WHERE LOG_ID = 999;           -- 데모용 ID 자리 비우기

DELIMITER $$

CREATE PROCEDURE INSERT_LOG_IGNORE_DUP(IN P_EMP_ID VARCHAR(3))
BEGIN
    DECLARE CONTINUE HANDLER FOR 1062 BEGIN END;     -- 중복 키(1062)면 아무 것도 안 하고 다음 문장으로

    INSERT INTO SALARY_LOG (LOG_ID, EMP_ID, OLD_SALARY, NEW_SALARY) VALUES (999, P_EMP_ID, 0, 0);
    INSERT INTO SALARY_LOG (LOG_ID, EMP_ID, OLD_SALARY, NEW_SALARY) VALUES (999, P_EMP_ID, 0, 0);   -- 두 번째는 PK 중복 -> 핸들러가 삼킴
END $$

DELIMITER ;

CALL INSERT_LOG_IGNORE_DUP('200');
SELECT COUNT(*) FROM SALARY_LOG WHERE LOG_ID = 999;   -- 1 (두 번째 INSERT는 무시됨, 프로시저는 정상 종료)

# 6. DROP PROCEDURE
DROP PROCEDURE IF EXISTS GUGUDAN;
DROP PROCEDURE IF EXISTS GUGUDAN_LOOP;
DROP PROCEDURE IF EXISTS GUGUDAN_ALL;
DROP PROCEDURE IF EXISTS RAISE_SALARY;               -- 실습에서 만든 프로시저 정리
DROP PROCEDURE IF EXISTS GET_DEPT_AVG_SALARY;
DROP PROCEDURE IF EXISTS CLASSIFY_SALARY;
DROP PROCEDURE IF EXISTS FLAG_LOW_SALARY;
DROP PROCEDURE IF EXISTS GET_SALARY_SAFE;
DROP PROCEDURE IF EXISTS TRANSFER_SALARY;
DROP PROCEDURE IF EXISTS INSERT_LOG_IGNORE_DUP;

# 7. TRIGGER 기본 - AFTER UPDATE
DROP TABLE IF EXISTS EMP_COPY;                       -- 사본 초기화
CREATE TABLE EMP_COPY AS SELECT * FROM EMP;
DELETE FROM SALARY_LOG;                              -- 로그 비우기

DELIMITER $$

CREATE TRIGGER TRG_EMP_SALARY_LOG                    -- 트리거 이름
AFTER UPDATE ON EMP_COPY                             -- EMP_COPY에 UPDATE가 반영된 "직후" 실행
FOR EACH ROW                                         -- 영향받은 행마다 1번씩
BEGIN
    IF OLD.SALARY <> NEW.SALARY THEN                 -- OLD=변경 전 값, NEW=변경 후 값. 급여가 실제로 바뀐 경우만
        INSERT INTO SALARY_LOG (EMP_ID, OLD_SALARY, NEW_SALARY)
        VALUES (NEW.EMP_ID, OLD.SALARY, NEW.SALARY); -- 다른 테이블(SALARY_LOG)에 이력 기록
    END IF;
END $$

DELIMITER ;

UPDATE EMP_COPY SET SALARY = SALARY + 200000 WHERE EMP_ID = '209';   -- 이 UPDATE가 트리거를 자동 발동시킴

SELECT EMP_ID, OLD_SALARY, NEW_SALARY FROM SALARY_LOG WHERE EMP_ID = '209';   -- 트리거가 남긴 로그 확인

# 8. TRIGGER - BEFORE INSERT
DELIMITER $$

CREATE TRIGGER TRG_EMP_DEFAULT_ENTYN
BEFORE INSERT ON EMP_COPY                            -- 행이 저장되기 "전"에 실행
FOR EACH ROW
BEGIN
    IF NEW.ENT_YN IS NULL THEN                       -- 입력값에 ENT_YN이 없으면
        SET NEW.ENT_YN = 'N';                        -- 저장될 값 자체를 'N'으로 바꿔치기 (BEFORE에서만 가능)
    END IF;
END $$

DELIMITER ;

INSERT INTO EMP_COPY (EMP_ID, EMP_NAME, DEPT_ID, JOB_CODE, SALARY, HIRE_DATE, ENT_YN)
VALUES ('221', '테스트', 'D9', 'J7', 2000000, '2026-08-19', NULL);   -- ENT_YN을 NULL로 넣어봄

SELECT EMP_ID, EMP_NAME, ENT_YN FROM EMP_COPY WHERE EMP_ID = '221';   -- 트리거가 'N'으로 채웠는지 확인

# 9. 트리거의 무한 루프 위험 (절대 실행하지 마세요)
-- AFTER UPDATE 트리거 본문에서 같은 테이블(EMP_COPY)을 다시 UPDATE하면
-- 그 UPDATE가 같은 트리거를 또 부르는 무한 루프에 빠져 "Trigger recursion too deep" 오류로 죽습니다.
-- CREATE TRIGGER TRG_INFINITE
-- AFTER UPDATE ON EMP_COPY
-- FOR EACH ROW
-- BEGIN
--     UPDATE EMP_COPY SET SALARY = SALARY WHERE EMP_ID = NEW.EMP_ID;
-- END;

# 10. DROP TRIGGER
DROP TRIGGER IF EXISTS TRG_EMP_SALARY_LOG;           -- 실습에서 만든 트리거 정리
DROP TRIGGER IF EXISTS TRG_EMP_DEFAULT_ENTYN;
