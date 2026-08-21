-- 단일행 함수 - 행 마다 함수를 적용
-- 그룹함수 - 여러개의 행을 하나로 묶어서 처리
-- 집계함수 
-- COUNT, SUM, AVG, MAX, MIN
SELECT COUNT(*) FROM EMP;		-- 조건에 맞는 전체 행의 갯수
SELECT COUNT(EMP_ID) FROM EMP;
SELECT COUNT(BONUS) FROM EMP; 	-- 컬럼에 값이 NULL이 아닌 컬럼의 갯수를 세어줌

-- 부서가 배정된 사원의 수를 세어봅시다
SELECT * FROM EMP WHERE DEPT_ID IS NULL;
SELECT COUNT(DEPT_ID) FROM EMP; 	

-- 총 급여의 합
SELECT SUM(SALARY), '원' FROM EMP;

-- 금여가 가장높은사람, 가장 낮은사람
SELECT MAX(SALARY) 최고급여, MIN(SALARY) 최저급여 FROM EMP;

SELECT MIN(SALARY) 최저급여 FROM EMP;

-- 서브쿼리를 이용해서 최저 급여를 조회 하고 조건절에서 사용
-- 메인쿼리
-- 서브쿼리 - 쿼리안에 쿼리를 작성
-- 최저급여를 받는 사원
SELECT 	EMP_NAME 사원명, SALARY '월 급여'
FROM 	EMP
WHERE 	SALARY > (SELECT avg(SALARY) 최저급여 FROM EMP);



SELECT SUM(SALARY),              -- 65616240
	   AVG(SALARY),       -- 3124583
       -- 소수점이하 반올림
       ROUND(AVG(SALARY)),       -- 3124583
       MAX(SALARY),              -- 8000000
       MIN(SALARY),              -- 1380000
       COUNT(*),                 -- 21  (전체 사원 수)
       COUNT(BONUS),             -- 9   (보너스를 받는 사원 수)
       COUNT(DISTINCT DEPT_ID),  -- 6   (사원이 배치된 부서 종류 수)
       COUNT(DISTINCT JOB_CODE)  -- 7   (등장하는 직급 종류 수)
FROM EMP;

-- 반올림, 버림, 천단위절삭, 
SELECT 	AVG(SALARY), 
		ROUND(AVG(SALARY)), -- 소수점이하 반올림
        ROUND(AVG(SALARY), 1), -- 소수점이하 몇번째 까지 보여주는지
        ROUND(AVG(SALARY), -1), -- 원단위 절삭
        ROUND(AVG(SALARY), -3) -- 천단위 절삭
FROM 	EMP
-- 그룹으로 묶어서 조회
-- 부서별 급여의 평균
GROUP BY DEPT_ID
;

-- GROUP BY절을 사용하면 집계함수와 GROUP BY절에 사용된 컬럼만 조회가 가능
SELECT 	DEPT_ID
		, COUNT(*) '부서별 사원의 수'
        -- 버림(자릿수 지정 필수)
        , TRUNCATE(AVG(SALARY), -3) '부서별 급여의 평균'
        , CONCAT(TRUNCATE(AVG(SALARY)/10000, 0), '만원')
FROM 	EMP
-- SELECT 절에 올수 있는 컬럼이 제한
GROUP BY DEPT_ID;

-- 조인 - 여러 테이블에 나뉘어 있는 데이터를 하나의 결과로 합쳐서 조회
-- 사원명, 부서명을 조회
-- 조건을 주지 않으면 22 * 9
SELECT 	*
FROM	EMP, DEPT -- 여러개의 테이블 나열;
WHERE 	EMP.DEPT_ID = DEPT.DEPT_ID -- 테이블의 데이터를 연결 시켜주는 조건
									-- 부서를 배정받지 못한 사원
;

-- 일치하는 데이터만 조회
-- EMP테이블과 DEPT테이블은 같은 컬럼을 가지고 있다
-- 테이블의 컬럼이 동일한 이름인 경우 테이블의 이름을 써줘야함
SELECT 	EMP_NAME, EMP.DEPT_ID, DEPT_TITLE
FROM 	EMP
-- JOIN	DEPT USING (DEPT_ID) -- 컬럼이름이 같을때, 컬럼에 접근할때 테이블이름을 명시하지 않아도 됨 
JOIN	DEPT ON EMP.DEPT_ID = DEPT.DEPT_ID	-- 컬럼이름이 다를때
;

SELECT 	EMP_NAME, DEPT_ID, DEPT_TITLE
FROM 	EMP 
-- JOIN을 기준으로 왼쪽에 있는 테이블의 데이터는 모두 조회
LEFT JOIN	DEPT USING (DEPT_ID) -- 컬럼이름이 같을때, 컬럼에 접근할때 테이블이름을 명시하지 않아도 됨 
;

-- 사원이름, 직급코드, 직급이름
-- EMP, JOB
-- 단, 모든 사원이 출력되도록 한다
SELECT EMP_NAME, JOB_CODE, JOB_NAME 
FROM EMP
-- OUBER JOIN
-- LEFT : 왼쪽 테이블에 있는 조건이 일치하지 않는 데이터도 모두 조회
-- RIGHT : 오른쪽 테이블에 있는 조건이 일치하지 않는 데이터도 모두 조회
LEFT JOIN JOB USING (JOB_CODE);

-- USING을 사용해서 EMP와 DEPT를 연결하고, 회계관리부('D2') 사원의 이름과 부서명을 조회하세요.
SELECT 	EMP_NAME, DEPT_TITLE
FROM	EMP
-- JOIN 테이블 USING (컬럼명)
LEFT JOIN	DEPT USING (DEPT_ID)
WHERE 	DEPT_ID = 'D2';
-- WHERE 	DEPT_TITLE LIKE '%회계%';

-- 부서코드, 부서명, 부서별 사원수
-- GROUP BY절이 사용된 경우, SELECT절에 올수 있는 컬럼은 GROUP BY절에 사용된 컬럼으로 제한!!!!
SELECT 	DEPT_ID, DEPT_TITLE, CONCAT(COUNT(*), '명')
FROM	EMP
-- JOIN 테이블 USING (컬럼명)
LEFT JOIN	DEPT USING (DEPT_ID)
GROUP BY DEPT_ID, DEPT_TITLE
;




SELECT COUNT(*) FROM DEPT;
SELECT COUNT(*) FROM EMP;





