-- 부서별 급여의 평균
-- GROUP BY : 행을 압축 -> 그룹당 하나의 행이 출력
-- 조회 할수 있는 컬럼에 제약
SELECT 	DEPT_ID, AVG(SALARY)
FROM 	EMP
GROUP BY DEPT_ID;
D9	5900000.0000
D6	3650000.0000
D5	2752000.0000
D8	2328746.6667
D1	2606666.6667
D2	2280000.0000
	2605000.0000;

SELECT 	EMP_NAME, DEPT_ID, SALARY,  AVG(SALARY) OVER (PARTITION BY DEPT_ID) '부서별 급여의 평균'
FROM 	EMP;

SELECT 	EMP_NAME, DEPT_ID, SALARY,  AVG(SALARY) OVER (PARTITION BY JOB_CODE) '직급별 급여의 평균'
FROM 	EMP;

-- 순위함수
-- 동일순위(같은값)을 처리하는 방식이 다르다
-- ROW_NUMBER / RANK / DENSE_RANK
SELECT -- SALARY, ROW_NUMBER() OVER (ORDER BY SALARY)
		-- 입사년도가 빠른순서대로 순번을 출력
		EXTRACT(YEAR FROM HIRE_DATE),
        -- YEAR(HIRE_DATE),
        ROW_NUMBER() 	OVER (ORDER BY EXTRACT(YEAR FROM HIRE_DATE)) 순번,
        RANK() 			OVER (ORDER BY EXTRACT(YEAR FROM HIRE_DATE)) 순위,
        DENSE_RANK() 	OVER (ORDER BY EXTRACT(YEAR FROM HIRE_DATE)) 밀집순위
FROM 	EMP;

-- 부서별 급여의 순위
SELECT *
FROM 	-- 서브쿼리를 테이블로 사용(인라인뷰)
		-- 서브 쿼리를 ()묶어서 별칭
        -- 테이블 이름을 별칭으로 주지 않으면 오류가 발생!
		(SELECT  DEPT_ID, SALARY,
				RANK() OVER (PARTITION BY DEPT_ID ORDER BY SALARY DESC) 부서별급여순위
		FROM 	EMP) T
WHERE	부서별급여순위 < 4;
-- 윈도우 함수는 WHERE절에서 직접사용이 불가능 함 (SQL 실행순서에 의해서 HAVING절 다음에 실행)
-- 윈도우 함수의 조건을 주고싶은 경우, 서브쿼를 이용해서 조건을 준다!
-- 공통테이블표현식(CTE)
WITH T AS (
		SELECT  DEPT_ID, SALARY,
				RANK() OVER (PARTITION BY DEPT_ID ORDER BY SALARY DESC) 부서별급여순위
		FROM 	EMP
)
SELECT * FROM T
WHERE 부서별급여순위 = 1
;

-- 급여가 높은사람 5명을 조회
WITH T AS (
	SELECT 	SALARY, RANK() OVER (ORDER BY SALARY DESC) 순위
	FROM 	EMP
)
SELECT * FROM T
WHERE 순위 <= 5
;

SELECT SALARY
FROM EMP
ORDER BY SALARY DESC
LIMIT 5;






-- 누적합계
-- 집계함수를 사용할때 윈도우 함수의 ORDER BY절을 추가할 경우 누적집계를 구할 수 있다
SELECT 	DEPT_ID, EMP_NAME, SALARY, 
		SUM(SALARY) OVER (PARTITION BY DEPT_ID ORDER BY SALARY DESC) '급여의 누적합계'
FROM 	EMP;




