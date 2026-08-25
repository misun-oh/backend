-- Day 3~5. 함수(FUNCTION) — 1_함수.md와 순서가 같은 실습 스크립트
-- 단일행 함수 - 행 마다 함수를 적용
-- 그룹함수 - 여러개의 행을 하나로 묶어서 처리, 그룹당 결과 1개


-- ================================
-- 1. 문자 함수
-- ================================

-- CHAR_LENGTH : 글자 수, LENGTH : 바이트 수
-- utf8mb4에서 한글 1글자 = 3바이트 (영문/숫자는 1글자 = 1바이트)
SELECT	EMP_NAME, CHAR_LENGTH(EMP_NAME), LENGTH(EMP_NAME)
FROM	EMP
WHERE	EMP_ID = '200';		-- 곽상혁, 3, 9

-- INSTR(문자열, 찾을문자) : 찾을 문자가 처음 등장하는 위치(1부터 시작)
SELECT	EMAIL, INSTR(EMAIL, '@') 위치
FROM	EMP
WHERE	EMP_ID = '200';		-- @는 8번째 글자

-- LPAD/RPAD(문자열, 길이, 채울문자) : 왼쪽/오른쪽을 지정한 문자로 채워서 길이를 맞춤
SELECT	LPAD(EMAIL, 25, '*') 왼쪽채움,
		RPAD(EMAIL, 25, '*') 오른쪽채움
FROM	EMP
WHERE	EMP_ID = '200';

-- TRIM : 앞/뒤/양쪽의 지정한 문자를 제거 (옵션 생략시 BOTH가 기본값)
-- LEADING : 앞쪽만, TRAILING : 뒤쪽만, BOTH : 양쪽
SELECT	TRIM('   Hello   ')					양쪽공백제거,		-- 'Hello'
		TRIM(LEADING '0' FROM '000123')	앞쪽0제거,			-- '123'
		TRIM(TRAILING '0' FROM '123000')	뒤쪽0제거,			-- '123'
		TRIM(BOTH 'z' FROM 'zzHELLOzz')	양쪽문자제거;		-- 'HELLO'

-- LTRIM/RTRIM : MySQL은 공백만 제거 가능(Oracle과 달리 지정문자 인자 없음)
SELECT	LTRIM('   SQL')	왼쪽공백만,		-- 'SQL'
		RTRIM('SQL   ')	오른쪽공백만;	-- 'SQL'
-- SELECT LTRIM('000123', '0'); -- 에러! MySQL의 LTRIM은 인자 1개만 받음
-- 지정 문자를 제거하려면 TRIM(LEADING|TRAILING|BOTH 문자 FROM 문자열) 사용

-- SUBSTRING(문자열, 시작위치, 길이)
-- EMP_NO(주민등록번호 형식)에서 성별을 나타내는 8번째 글자만 추출
SELECT	EMP_NAME, EMP_NO, SUBSTRING(EMP_NO, 8, 1) 성별코드
FROM	EMP
WHERE	EMP_ID = '200';

-- LOWER/UPPER : 소문자/대문자 변환
-- CONCAT : 여러 문자열을 이어붙임 (MySQL은 2개 이상 자유롭게 가능)
-- REPLACE : 특정 문자를 다른 문자로 치환
SELECT	UPPER('company.com')							대문자,
		CONCAT(EMP_NAME, '(', EMP_ID, ')')				이름사번,
		REPLACE(EMAIL, 'company.com', 'work.co.kr')	새이메일
FROM	EMP
WHERE	EMP_ID = '200';


-- ================================
-- 2. 숫자 함수
-- ================================

-- ABS(절댓값), MOD(나머지, % 연산자와 동일)
SELECT	ABS(-15.5),		-- 15.5
		MOD(10, 3);		-- 1

-- ROUND(반올림) vs TRUNCATE(반올림 없이 그냥 자름) vs CEIL(올림)/FLOOR(버림)
-- 자리수가 양수면 소수점 아래 자릿수, 음수면 소수점 위(10의자리, 100의자리...) 기준
SELECT	ROUND(123.456, 1),		-- 123.5
		ROUND(123.456, -1),	-- 120   (10의 자리에서 반올림)
		TRUNCATE(123.456, 1),	-- 123.4 (반올림 없이 자름)
		TRUNCATE(123.456, -1),	-- 120
		CEIL(123.1),			-- 124
		FLOOR(123.9),			-- 123
		FLOOR(-123.1);			-- -124  (버림은 항상 더 작은 정수 쪽으로)


-- ================================
-- 3. 날짜 함수
-- ================================

-- 날짜 단위(INTERVAL, EXTRACT, TIMESTAMPDIFF가 공통으로 사용)
-- YEAR, QUARTER, MONTH, WEEK, DAY, HOUR, MINUTE, SECOND

-- NOW() : 현재 날짜+시각, CURDATE() : 현재 날짜만
SELECT	NOW(), CURDATE();

-- DATE_ADD(날짜, INTERVAL n 단위) : 날짜에 기간을 더함
-- 입사일에 6개월을 더한 날짜 (수습 기간 종료일 계산 등에 활용)
SELECT	HIRE_DATE, DATE_ADD(HIRE_DATE, INTERVAL 6 MONTH) 수습종료일
FROM	EMP
WHERE	EMP_ID = '200';		-- 2013-03-02 -> 2013-09-02

-- DATE_SUB(날짜, INTERVAL n 단위) : 날짜에서 기간을 뺌 (DATE_ADD의 반대)
SELECT	CURDATE(), DATE_SUB(CURDATE(), INTERVAL 7 DAY) 일주일전;

-- TIMESTAMPDIFF(단위, 날짜1, 날짜2) : 두 날짜 사이의 완전한 간격만 계산
SELECT	TIMESTAMPDIFF(MONTH, '2020-01-15', '2024-03-20');		-- 50

-- 재직 기간(개월) : 오늘 날짜(CURDATE()) 기준으로 계산 -> 실행 시점마다 결과가 달라짐
SELECT	EMP_NAME, TIMESTAMPDIFF(MONTH, HIRE_DATE, CURDATE()) 재직개월
FROM	EMP;

-- LAST_DAY(날짜) : 그 날짜가 속한 달의 마지막 날짜
SELECT	LAST_DAY('2026-02-15');	-- 2026-02-28 (평년)

-- EXTRACT(단위 FROM 날짜) : 날짜에서 연/월/일 등 특정 요소만 추출
SELECT	EMP_NAME,
		EXTRACT(YEAR FROM HIRE_DATE)	입사년,
		EXTRACT(MONTH FROM HIRE_DATE)	입사월,
		EXTRACT(DAY FROM HIRE_DATE)	입사일
FROM	EMP
WHERE	EMP_ID = '200';


-- ================================
-- 4. 형변환 함수
-- ================================

-- DATE_FORMAT(날짜, 포맷) : 날짜 -> 원하는 형식의 문자열
-- %Y 4자리연도, %m 2자리월, %d 2자리일, %H:%i:%s 시:분:초
SELECT	EMP_NAME, DATE_FORMAT(HIRE_DATE, '%Y년 %m월 %d일') 입사일
FROM	EMP
WHERE	EMP_ID = '200';

-- STR_TO_DATE(문자열, 포맷) : 문자열 -> 날짜
SELECT	STR_TO_DATE('2026-08-19', '%Y-%m-%d');

-- CAST(값 AS 타입) : 명시적 형변환
-- 대상 타입은 CHAR/SIGNED/UNSIGNED/DECIMAL/DATE/DATETIME/TIME/BINARY/JSON 등으로 한정됨
-- 주의: CREATE TABLE에서 쓰던 VARCHAR/INT는 CAST에 쓸 수 없음(반드시 CHAR/SIGNED)
-- SELECT CAST(SALARY AS VARCHAR(10)); -- 에러!
-- SELECT CAST('123' AS INT);          -- 에러!
SELECT	CAST(SALARY AS CHAR)		문자로,
		CAST('12345' AS SIGNED)	숫자로
FROM	EMP
WHERE	EMP_ID = '200';

-- 형변환이 잘 되었는지 확인하는 방법: CTAS로 담아서 DESC로 실제 컬럼 타입 확인
CREATE TABLE TEMP_CAST_CHECK AS
SELECT	SALARY,
		CAST(SALARY AS CHAR)    문자로,
		CAST('12345' AS SIGNED) 숫자로
FROM	EMP
WHERE	EMP_ID = '200';

DESC TEMP_CAST_CHECK;

DROP TABLE TEMP_CAST_CHECK;


-- ================================
-- 5. NULL 처리 함수
-- ================================

-- IFNULL(값, 대체값) : 값이 NULL이면 대체값을, 아니면 값을 그대로 반환 (인자 2개 고정)
-- SELECT NVL(BONUS, 0) FROM EMP; -- 에러! MySQL에는 NVL이 없음(Oracle 함수)
SELECT	EMP_NAME, BONUS,
		IFNULL(BONUS, 0)								보너스율,
		(SALARY + (SALARY * IFNULL(BONUS, 0))) * 12	연봉
FROM	EMP
WHERE	EMP_ID IN ('200', '201');

-- COALESCE(값1, 값2, ...) : 여러 값 중 NULL이 아닌 첫번째 값 (3개 이상도 가능)
SELECT	EMP_NAME, COALESCE(BONUS, 0) 보너스율
FROM	EMP;

-- NULLIF(값1, 값2) : 두 값이 같으면 NULL, 다르면 값1을 반환
SELECT	NULLIF(10, 10),		-- NULL
		NULLIF(10, 20);		-- 10


-- ================================
-- 6. CASE / IF - 조건에 따른 값 선택
-- ================================

-- CASE WHEN 조건 THEN 결과 ... ELSE 결과 END (= if / else if / else)
-- 급여를 3단계 등급으로 분류
SELECT	EMP_NAME, SALARY,
		CASE	WHEN SALARY >= 5000000 THEN '고액'
				WHEN SALARY >= 2500000 THEN '중액'
				ELSE '소액'
		END 등급
FROM	EMP
WHERE	EMP_ID IN ('200', '205', '210');

-- CASE 표현식 WHEN 값 THEN 결과 ... END (표현식이 그 값과 같은지만 비교)
-- MySQL에는 Oracle의 DECODE가 없어서 단순 값 비교도 CASE로 처리
SELECT	EMP_NAME,
		CASE SUBSTRING(EMP_NO, 8, 1)
			WHEN '1' THEN '남'
			WHEN '2' THEN '여'
			WHEN '3' THEN '남'
			WHEN '4' THEN '여'
		END 성별
FROM	EMP
WHERE	EMP_ID = '200';

-- IF(조건, 참일때값, 거짓일때값) : 두 갈래(참/거짓)만 필요할 때 CASE보다 간단
-- CASE WHEN SALARY >= 3000000 THEN '고액' ELSE '일반' END 와 완전히 동일한 결과
SELECT	EMP_NAME, SALARY,
		IF(SALARY >= 3000000, '고액', '일반') 급여구분
FROM	EMP
WHERE	EMP_ID IN ('200', '210');


-- ================================
-- 7. 그룹 함수(집계함수) - COUNT, SUM, AVG, MAX, MIN
-- ================================

SELECT COUNT(*) FROM EMP;		-- 조건에 맞는 전체 행의 갯수
SELECT COUNT(EMP_ID) FROM EMP;
SELECT COUNT(BONUS) FROM EMP; 	-- 컬럼의 값이 NULL이 아닌 행의 갯수만 세어줌

-- 부서가 배정된 사원의 수를 세어봅시다
SELECT * FROM EMP WHERE DEPT_ID IS NULL;
SELECT COUNT(DEPT_ID) FROM EMP;

-- 총 급여의 합
SELECT SUM(SALARY), '원' FROM EMP;

-- 급여가 가장높은사람, 가장 낮은사람
SELECT MAX(SALARY) 최고급여, MIN(SALARY) 최저급여 FROM EMP;

-- 서브쿼리를 이용해서 평균 급여를 먼저 조회하고, 그 결과를 조건절에서 사용
-- 메인쿼리 / 서브쿼리 - 쿼리안에 쿼리를 작성
-- 평균급여보다 많이 받는 사원
SELECT	EMP_NAME 사원명, SALARY '월 급여'
FROM	EMP
WHERE	SALARY > (SELECT AVG(SALARY) FROM EMP);

SELECT SUM(SALARY),              -- 65616240
	   AVG(SALARY),
       ROUND(AVG(SALARY)),       -- 소수점이하 반올림, 3124583
       MAX(SALARY),              -- 8000000
       MIN(SALARY),              -- 1380000
       COUNT(*),                 -- 21  (전체 사원 수)
       COUNT(BONUS),             -- 9   (보너스를 받는 사원 수)
       COUNT(DISTINCT DEPT_ID),  -- 6   (사원이 배치된 부서 종류 수)
       COUNT(DISTINCT JOB_CODE)  -- 7   (등장하는 직급 종류 수)
FROM EMP;

-- ROUND는 반올림(TRUNCATE와 달리 버리지 않음), 자리수는 아래에서 정리
SELECT	AVG(SALARY),
		ROUND(AVG(SALARY)), 		-- 소수점이하 반올림
        ROUND(AVG(SALARY), 1), 	-- 소수점이하 몇번째 까지 보여주는지
        ROUND(AVG(SALARY), -1), 	-- 십의 자리에서 반올림
        ROUND(AVG(SALARY), -3) 	-- 천의 자리에서 반올림
FROM	EMP;


-- ================================
-- 8. GROUP BY / HAVING
-- ================================
-- WHERE  : 그룹으로 묶이기 전, 개별 행에 대한 조건
-- HAVING : 그룹으로 묶인 후, 집계 결과에 대한 조건

-- 부서별 급여 합계, 인원수
SELECT	DEPT_ID, SUM(SALARY) 급여합계, COUNT(*) 인원수
FROM	EMP
GROUP BY DEPT_ID
ORDER BY DEPT_ID;

-- GROUP BY절이 사용된 경우, SELECT절에 올 수 있는 컬럼은
-- GROUP BY절에 사용된 컬럼 + 그룹함수로 제한!!!!
SELECT	DEPT_ID
		, COUNT(*) '부서별 사원의 수'
		, TRUNCATE(AVG(SALARY), -3) '부서별 급여의 평균'	-- 버림(자릿수 지정 필수)
		, CONCAT(TRUNCATE(AVG(SALARY)/10000, 0), '만원')
FROM	EMP
GROUP BY DEPT_ID;

-- 부서별 급여 합계가 1000만원 이상인 부서만 조회
-- 집계 결과에 대한 조건은 WHERE가 아니라 HAVING에 작성!
SELECT	DEPT_ID, SUM(SALARY) 급여합계
FROM	EMP
GROUP BY DEPT_ID
HAVING	SUM(SALARY) >= 10000000
ORDER BY DEPT_ID;

-- 실행순서 : FROM -> WHERE -> GROUP BY -> HAVING -> SELECT -> ORDER BY
