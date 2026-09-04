-- =====================================================================
-- 10. PL/SQL 입문 : 사용자 정의 함수(FUNCTION) & 프로시저(PROCEDURE)
-- =====================================================================
--  ▷ 읽는 순서 : 모든 루틴은 아래 5칸으로 정리돼 있다.
--      [상황]      왜 필요한가 (해결하려는 문제)
--      [만들 것]   무엇을 만드는가 (이름 · 종류)
--      [파라미터]  각 인자가 무슨 값인가 (방향 IN/OUT · 타입 · 의미)
--      코드        CREATE ... 정의
--      [실행/결과] 호출 방법과 예상 결과
--  ▷ 대상 DB : hr   (EMP, DEPT 테이블)
-- =====================================================================
USE hr;

-- 함수 본문에서 NOW()·RAND() 같은 비결정적 함수를 쓰면 복제(bin log) 안전성 때문에
-- 생성이 막힐 수 있다. 실습 편의를 위해 허용으로 켜 둔다.
SET GLOBAL log_bin_trust_function_creators = 1;

-- UPDATE/DELETE 시 인덱스 없는 컬럼 조건도 허용 (프로시저 실습에서 사용)
SET SESSION sql_safe_updates = 0;


-- =====================================================================
-- PART 1. 사용자 정의 함수 (FUNCTION)
-- ---------------------------------------------------------------------
--  · 값 하나(RETURNS 타입)를 돌려준다  →  SELECT · WHERE 안에서 호출 가능
--  · 본문에서 COMMIT / ROLLBACK 불가
--  · 정의 끝에 "특성"을 반드시 하나 붙인다
--      DETERMINISTIC      입력이 같으면 결과도 100% 같음 (순수 계산)
--      NOT DETERMINISTIC  NOW()·RAND() 등으로 실행마다 결과가 달라질 수 있음
--      READS SQL DATA     본문에서 SELECT 로 데이터를 읽음
--      MODIFIES SQL DATA  본문에서 INSERT/UPDATE/DELETE 함
--      NO SQL             SQL 문을 아예 쓰지 않음
--  · DELIMITER : 본문 안의 ; 때문에 정의가 중간에 끊기지 않도록 종결자를 잠시 바꾼다
-- =====================================================================


-- ---------------------------------------------------------------------
-- [함수 1] FN_GET_ANNUAL_SALARY  ―  월급·보너스율로 "연봉"을 계산
-- ---------------------------------------------------------------------
-- [상황]    EMP를 조회할 때마다  (급여 + 급여 × 보너스율) × 12  를 손으로 쓴다.
--           이 계산식을 이름 하나로 재사용하고 싶다.
-- [만들 것] 사용자 정의 함수  FN_GET_ANNUAL_SALARY
-- [파라미터]
--     P_SALARY   IN   INT           - 월 급여 (EMP.SALARY)
--     P_BONUS    IN   DECIMAL(4,2)  - 보너스율 0~1 사이, NULL 가능 (EMP.BONUS)
--   반환 : INT  (연간 총 급여)
--   특성 : DETERMINISTIC  (입력만으로 결과 결정, DB 를 읽지 않음)
-- ---------------------------------------------------------------------
DROP FUNCTION IF EXISTS FN_GET_ANNUAL_SALARY;
DELIMITER $$
CREATE FUNCTION FN_GET_ANNUAL_SALARY(P_SALARY INT, P_BONUS DECIMAL(4,2))
    RETURNS INT
    DETERMINISTIC
BEGIN
    -- 보너스가 NULL이면 0으로 취급 (NULL이 섞이면 산술 결과가 통째로 NULL)
    RETURN (P_SALARY + P_SALARY * IFNULL(P_BONUS, 0)) * 12;
END $$
DELIMITER ;

-- [실행/결과]
SELECT FN_GET_ANNUAL_SALARY(2000000, 0.3);    -- (2,000,000 + 600,000) × 12 = 31,200,000
SELECT FN_GET_ANNUAL_SALARY(2000000, NULL);   -- 2,000,000 × 12 = 24,000,000

SELECT EMP_NAME, SALARY, BONUS,
       FN_GET_ANNUAL_SALARY(SALARY, BONUS) AS 연봉
FROM   EMP
ORDER  BY 연봉 DESC;


-- ---------------------------------------------------------------------
-- [함수 2] FN_GET_AGE  ―  주민등록번호로 "만 나이"를 계산
-- ---------------------------------------------------------------------
-- [상황]    EMP에는 생년월일 컬럼이 없고 주민번호(EMP_NO)만 있다.
--           조회할 때 만 나이를 바로 보고 싶다.
-- [만들 것] 사용자 정의 함수  FN_GET_AGE
-- [파라미터]
--     P_EMP_NO   IN   CHAR(14)  - '800514-1234567' 형식의 주민등록번호
--   반환 : INT  (오늘 기준 만 나이)
--   특성 : NOT DETERMINISTIC  (NOW()를 쓰므로 실행 시점마다 값이 달라질 수 있음)
--   로직 : 8번째 글자(성별코드)로 세기 판별  →  1·2 = 1900년대, 3·4 = 2000년대
--          'YYYYMMDD' 문자열을 DATE 로 바꿔 오늘과의 연수 차이를 구함
-- ---------------------------------------------------------------------
DROP FUNCTION IF EXISTS FN_GET_AGE;
DELIMITER $$
CREATE FUNCTION FN_GET_AGE(P_EMP_NO CHAR(14))
    RETURNS INT
    NOT DETERMINISTIC
BEGIN
    DECLARE V_BIRTH DATE;

    SET V_BIRTH = STR_TO_DATE(
        CASE SUBSTRING(P_EMP_NO, 8, 1)
            WHEN '1' THEN CONCAT('19', SUBSTRING(P_EMP_NO, 1, 6))   -- 1900년대 남
            WHEN '2' THEN CONCAT('19', SUBSTRING(P_EMP_NO, 1, 6))   -- 1900년대 여
            WHEN '3' THEN CONCAT('20', SUBSTRING(P_EMP_NO, 1, 6))   -- 2000년대 남
            WHEN '4' THEN CONCAT('20', SUBSTRING(P_EMP_NO, 1, 6))   -- 2000년대 여
        END, '%Y%m%d');

    RETURN TIMESTAMPDIFF(YEAR, V_BIRTH, NOW());   -- 두 날짜 사이의 만(滿) 연수
END $$
DELIMITER ;

-- [실행/결과]
SELECT FN_GET_AGE('800514-1234567');   -- 1980-05-14 출생 → 오늘 기준 만 나이

SELECT EMP_NAME, EMP_NO, FN_GET_AGE(EMP_NO) AS 나이
FROM   EMP
ORDER  BY 나이 DESC;

-- 만든 함수 목록/정의 확인
SHOW FUNCTION STATUS WHERE Db = 'hr';
-- SHOW CREATE FUNCTION FN_GET_AGE;


-- =====================================================================
-- PART 2. 프로시저 (STORED PROCEDURE)
-- ---------------------------------------------------------------------
--  함수 vs 프로시저
--     호출        함수: SELECT FN(...)      프로시저: CALL SP(...)
--     반환        함수: RETURNS 로 값 1개   프로시저: 반환문 없음 → OUT 파라미터로
--     식 안에서   함수: 가능                프로시저: 불가
--     COMMIT 등   함수: 불가                프로시저: 가능
--     용도        함수: 값 계산             프로시저: 여러 DML · 흐름 처리
--
--  파라미터 방향
--     IN    (기본) 호출자 → 프로시저.  안에서 바꿔도 밖에 영향 없음
--     OUT          프로시저 → 호출자.  결과를 담아 내보냄 (들어올 땐 NULL)
--     INOUT        양방향
--  OUT 값 받기 :  CALL SP(..., @변수);   →   SELECT @변수;
-- =====================================================================


-- ---------------------------------------------------------------------
-- 실습 준비 : 원본 EMP를 건드리지 않도록 사본 / 로그 테이블을 만든다
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS EMP_COPY;
CREATE TABLE EMP_COPY AS SELECT * FROM EMP;   -- 구조 + 데이터만 복사 (제약조건은 복사 안 됨)

DROP TABLE IF EXISTS SALARY_LOG;
CREATE TABLE SALARY_LOG (
    LOG_ID     INT AUTO_INCREMENT PRIMARY KEY,   -- 로그 일련번호 (자동 증가)
    EMP_ID     VARCHAR(3),                       -- 어떤 사원
    OLD_SALARY INT,                              -- 변경 전 급여
    NEW_SALARY INT,                              -- 변경 후 급여
    CHANGED_AT DATETIME DEFAULT NOW()            -- 기록 시각
);


-- ---------------------------------------------------------------------
-- [프로시저 1] RAISE_SALARY  ―  부서 급여 일괄 인상 + 인상 후 평균 반환
-- ---------------------------------------------------------------------
-- [상황]    특정 부서 전 직원의 급여를 X% 올리고,
--           인상 뒤 그 부서 평균급여를 곧바로 알고 싶다.
-- [만들 것] 프로시저  RAISE_SALARY   (IN 2개 + OUT 1개)
-- [파라미터]
--     P_DEPT_ID     IN    CHAR(2)       - 대상 부서코드 (예: 'D1')
--     P_RATE        IN    DECIMAL(3,2)  - 인상률 (0.10 = 10%)
--     P_AVG_SALARY  OUT   INT           - 인상 후 그 부서의 평균급여를 담아 돌려줌
--   동작 : ① 부서 급여 UPDATE   ② 평균급여를 SELECT ... INTO OUT   ③ COMMIT
-- ---------------------------------------------------------------------
DROP PROCEDURE IF EXISTS RAISE_SALARY;
DELIMITER $$
CREATE PROCEDURE RAISE_SALARY(IN  P_DEPT_ID    CHAR(2),
                              IN  P_RATE       DECIMAL(3,2),
                              OUT P_AVG_SALARY INT)
BEGIN
    UPDATE EMP_COPY
    SET    SALARY = SALARY * (1 + P_RATE)
    WHERE  DEPT_ID = P_DEPT_ID;

    -- SELECT ... INTO 는 결과가 단일 행일 때만 가능 (여러 행이면 오류)
    SELECT AVG(SALARY) INTO P_AVG_SALARY
    FROM   EMP_COPY
    WHERE  DEPT_ID = P_DEPT_ID;

    COMMIT;
END $$
DELIMITER ;

-- [실행/결과]
CALL RAISE_SALARY('D1', 0.1, @AVG_SAL);   -- D1 부서 급여 10% 인상, 평균을 @AVG_SAL 로 받음
SELECT @AVG_SAL AS D1_평균급여;
SELECT EMP_ID, SALARY FROM EMP_COPY WHERE DEPT_ID = 'D1';


-- ---------------------------------------------------------------------
-- [프로시저 2] GUGUDAN  ―  반복문(LOOP)으로 구구단 표 채우기
-- ---------------------------------------------------------------------
-- [상황]    반복문 문법(LOOP · LEAVE · 레이블)을 연습한다.
--           2~9단 결과를 표 형태로 한 테이블에 쌓는다.
-- [만들 것] 프로시저  GUGUDAN   (파라미터 없음)
--   문법 : LOOP ... END LOOP 는 종료 조건이 없다
--          →  IF 조건 THEN LEAVE 레이블;  로 직접 빠져나온다
--          (LEAVE = break,  ITERATE = continue,  레이블 필수)
--   출력 : 임시 테이블 TMP_GUGU(DAN, I, RESULT) 에 INSERT
-- ---------------------------------------------------------------------
DROP TEMPORARY TABLE IF EXISTS TMP_GUGU;
CREATE TEMPORARY TABLE TMP_GUGU (DAN INT, I INT, RESULT INT);

DROP PROCEDURE IF EXISTS GUGUDAN;
DELIMITER $$
CREATE PROCEDURE GUGUDAN()
BEGIN
    DECLARE V_DAN INT DEFAULT 2;   -- 바깥 반복 : 단 (2 → 9)

    DAN_LOOP: LOOP
        IF V_DAN > 9 THEN LEAVE DAN_LOOP; END IF;

        BEGIN
            DECLARE V_I INT DEFAULT 1;   -- 안쪽 반복 : 곱하는 수 (1 → 9)
            I_LOOP: LOOP
                IF V_I > 9 THEN LEAVE I_LOOP; END IF;
                INSERT INTO TMP_GUGU VALUES (V_DAN, V_I, V_DAN * V_I);
                SET V_I = V_I + 1;   -- 안 늘리면 무한 루프
            END LOOP;
        END;

        SET V_DAN = V_DAN + 1;
    END LOOP;
END $$
DELIMITER ;

-- [실행/결과]
CALL GUGUDAN();
SELECT * FROM TMP_GUGU ORDER BY I, DAN;   -- 8단 × 9행 = 72행 (2단~9단)


-- ---------------------------------------------------------------------
-- [프로시저 3] FLAG_LOW_SALARY  ―  커서로 행을 하나씩 순회하며 로그 기록
-- ---------------------------------------------------------------------
-- [상황]    "급여 200만원 미만 사원만 골라 이력 테이블에 남긴다."
--           한 문장(INSERT ... SELECT)으로도 되지만, 여기서는
--           행마다 판단이 필요한 경우의 정석인 커서를 연습한다.
-- [만들 것] 프로시저  FLAG_LOW_SALARY   (파라미터 없음)
--   커서 4단계 (선언 순서 고정 : 변수 → 커서 → 핸들러)
--     ① DECLARE 변수                          FETCH 결과를 담을 그릇
--     ② DECLARE CUR CURSOR FOR <SELECT>       순회할 결과 정의 (아직 실행 안 함)
--     ③ DECLARE CONTINUE HANDLER FOR NOT FOUND SET done=1;
--                                             더 읽을 행이 없을 때 탈출 플래그 (없으면 오류)
--     ④ OPEN  →  LOOP(FETCH → 처리)  →  CLOSE
-- ---------------------------------------------------------------------
DROP PROCEDURE IF EXISTS FLAG_LOW_SALARY;
DELIMITER $$
CREATE PROCEDURE FLAG_LOW_SALARY()
BEGIN
    DECLARE V_DONE   INT DEFAULT 0;      -- ① 순회 종료 플래그 (0=계속, 1=끝)
    DECLARE V_EMP_ID VARCHAR(3);
    DECLARE V_SALARY INT;

    DECLARE CUR CURSOR FOR               -- ② 순회할 결과
        SELECT EMP_ID, SALARY FROM EMP_COPY;
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET V_DONE = 1;   -- ③

    OPEN CUR;                            -- ④ 여기서 SELECT 실행
    READ_LOOP: LOOP
        FETCH CUR INTO V_EMP_ID, V_SALARY;      -- 다음 한 행 → 변수
        IF V_DONE = 1 THEN LEAVE READ_LOOP; END IF;

        IF V_SALARY < 2000000 THEN
            INSERT INTO SALARY_LOG (EMP_ID, OLD_SALARY, NEW_SALARY)
            VALUES (V_EMP_ID, V_SALARY, V_SALARY);
        END IF;
    END LOOP;
    CLOSE CUR;
END $$
DELIMITER ;

-- [실행/결과]
CALL FLAG_LOW_SALARY();
SELECT EMP_ID, OLD_SALARY FROM SALARY_LOG ORDER BY EMP_ID;
--  급여 200만 미만 : 209 최주호(1,800,000), 210 이광렬(2,000,000 → 제외),
--                    215 한재헌(1,380,000), 217 심재호(1,550,000)


-- =====================================================================
-- 정리 : 만든 객체 확인 & 삭제
-- =====================================================================
SHOW FUNCTION  STATUS WHERE Db = 'hr';
SHOW PROCEDURE STATUS WHERE Db = 'hr';

DROP FUNCTION  IF EXISTS FN_GET_ANNUAL_SALARY;
DROP FUNCTION  IF EXISTS FN_GET_AGE;
DROP PROCEDURE IF EXISTS RAISE_SALARY;
DROP PROCEDURE IF EXISTS GUGUDAN;
DROP PROCEDURE IF EXISTS FLAG_LOW_SALARY;
-- DROP TABLE IF EXISTS EMP_COPY;
-- DROP TABLE IF EXISTS SALARY_LOG;
