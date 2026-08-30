-- Day 8~9. SUBQUERY — 1_SUBQUERY.md와 순서가 같은 실습 스크립트
-- 서브쿼리가 반환하는 값의 개수(단일행/다중행/다중컬럼)에 따라 쓸 수 있는 연산자가 다름


-- ================================
-- 1. 서브쿼리란
-- ================================

-- 괄호 안이 서브쿼리 : 비교 기준 자체를 쿼리 결과로만 구할 수 있을 때 사용
SELECT	EMP_NAME, SALARY
FROM	EMP
WHERE	SALARY > (SELECT AVG(SALARY) FROM EMP);


-- ================================
-- 2. 단일행 서브쿼리
-- ================================

-- 서브쿼리가 값 1개만 반환하면 일반 비교 연산자(=, >, < 등) 그대로 사용
SELECT	EMP_NAME, SALARY
FROM	EMP
WHERE	SALARY > (SELECT AVG(SALARY) FROM EMP)
ORDER BY SALARY DESC;

-- 주의: 서브쿼리가 실제로 값을 2개 이상 반환하면
-- 'Subquery returns more than 1 row' 오류가 남


-- ================================
-- 3. 다중행 서브쿼리 - IN
-- ================================

-- 서브쿼리가 여러 행을 반환하면 = 대신 IN 사용
-- 대리(J6)가 한 명이라도 있는 부서(D8, D1) 전체 사원 조회
SELECT	EMP_ID, EMP_NAME, DEPT_ID
FROM	EMP
WHERE	DEPT_ID IN (
	SELECT DEPT_ID FROM EMP WHERE JOB_CODE = 'J6'
)
ORDER BY EMP_ID;


-- ================================
-- 4. 다중행 서브쿼리 - ANY / ALL
-- ================================
-- ANY(SOME) : 목록 중 하나라도 만족하면 참, ALL : 목록 전부를 만족해야 참
-- > ALL(목록) = > MAX(목록),  < ANY(목록) = < MAX(목록)
-- > ANY(목록) = > MIN(목록),  < ALL(목록) = < MIN(목록)

-- ALL - 인사관리부(D1) 급여 전부(최댓값 3,660,000)보다 커야 함
SELECT	EMP_NAME, SALARY
FROM	EMP
WHERE	SALARY > ALL (
	SELECT SALARY FROM EMP WHERE DEPT_ID = 'D1'
)
ORDER BY SALARY DESC;

-- ANY - 기술지원부(D8) 급여 중 하나(최댓값 2,550,000)보다만 작으면 됨
SELECT	EMP_NAME, SALARY
FROM	EMP
WHERE	SALARY < ANY (
	SELECT SALARY FROM EMP WHERE DEPT_ID = 'D8'
)
ORDER BY SALARY ASC;


-- ================================
-- 5. 다중 컬럼 서브쿼리
-- ================================

-- (DEPT_ID, JOB_CODE) = (서브쿼리) : 두 컬럼을 한 쌍으로 묶어서 비교
-- 이광렬(D8, J6)과 부서·직급이 같은 다른 사원 조회 (본인 제외)
SELECT	EMP_NAME, DEPT_ID, JOB_CODE
FROM	EMP
WHERE	(DEPT_ID, JOB_CODE) = (
	SELECT DEPT_ID, JOB_CODE FROM EMP WHERE EMP_NAME = '이광렬'
)
AND EMP_NAME != '이광렬';


-- ================================
-- 6. 상관 서브쿼리 (Correlated Subquery) + EXISTS
-- ================================

-- 바깥 쿼리의 각 행마다 안쪽 서브쿼리가 그 행의 값을 참조하며 반복 실행됨
-- EXISTS는 서브쿼리가 행을 1개 이상 반환하면 참(값 자체는 보지 않음)
-- 부하 직원이 있는 사원(관리자 역할을 하는 7명) 조회
SELECT	E.EMP_NAME
FROM	EMP E
WHERE	EXISTS (
	SELECT 1 FROM EMP M WHERE M.MANAGER_ID = E.EMP_ID
)
ORDER BY E.EMP_ID;

-- 참고: NOT IN은 목록에 NULL이 섞이면 결과가 통째로 비어버릴 수 있음
-- -> 그럴 가능성이 있으면 NOT IN 대신 NOT EXISTS 사용

-- 상관 서브쿼리 - 비교 연산자 활용 (EXISTS 없이)
-- 비교 기준(자기 부서 평균)이 바깥 행마다 달라지는 경우
-- 부서가 없는 사원(NULL)은 서브쿼리가 빈 결과 -> SALARY > NULL은 UNKNOWN -> 자동 제외
SELECT	E.EMP_NAME, E.DEPT_ID, E.SALARY
FROM	EMP E
WHERE	E.SALARY > (
	SELECT AVG(E2.SALARY)
	FROM EMP E2
	WHERE E2.DEPT_ID = E.DEPT_ID
)
ORDER BY E.EMP_ID;


-- ================================
-- 7. FROM절 서브쿼리 (인라인 뷰)
-- ================================

-- FROM절에 쓰인 서브쿼리(T)를 임시 테이블처럼 다룰 수 있음
-- 부서별 평균 급여를 먼저 집계 -> 그 결과를 DEPT와 조인
SELECT	D.DEPT_TITLE, T.평균급여
FROM (
	SELECT DEPT_ID, ROUND(AVG(SALARY)) AS 평균급여
	FROM EMP
	GROUP BY DEPT_ID
) T
JOIN	DEPT D ON T.DEPT_ID = D.DEPT_ID
WHERE	T.평균급여 >= 3000000;


-- ================================
-- 8. WITH (공통 테이블 표현식, CTE)
-- ================================

-- 서브쿼리에 이름을 붙여두고 본문에서 테이블처럼 참조 (위 인라인 뷰와 결과 동일)
WITH DEPT_AVG AS (
	SELECT DEPT_ID, ROUND(AVG(SALARY)) AS 평균급여
	FROM EMP
	GROUP BY DEPT_ID
)
SELECT	D.DEPT_TITLE, DA.평균급여
FROM	DEPT_AVG DA
JOIN	DEPT D ON DA.DEPT_ID = D.DEPT_ID
WHERE	DA.평균급여 >= 3000000;

-- 여러 개의 CTE를 한 번에 정의 (콤마로 이어씀, 뒤 CTE가 앞 CTE를 참조 가능)
WITH DEPT_AVG AS (
	SELECT DEPT_ID, ROUND(AVG(SALARY)) AS 평균급여
	FROM EMP
	GROUP BY DEPT_ID
),
HIGH_AVG_DEPT AS (
	SELECT DEPT_ID, 평균급여
	FROM DEPT_AVG
	WHERE 평균급여 >= 3000000
)
SELECT	D.DEPT_TITLE, H.평균급여
FROM	HIGH_AVG_DEPT H
JOIN	DEPT D ON H.DEPT_ID = D.DEPT_ID;


-- ================================
-- 9. LIMIT을 이용한 Top-N 분석
-- ================================
-- MySQL에는 Oracle의 ROWNUM이 없음 -> ORDER BY ... LIMIT n으로 상위 n개를 자름

SELECT	EMP_NAME, SALARY
FROM	EMP
ORDER BY SALARY DESC
LIMIT 3;

-- LIMIT 오프셋, 개수 : 오프셋은 "건너뛸 행 개수"(0부터 셈), "시작 위치"가 아님
-- (= LIMIT 3 OFFSET 3) 3개를 건너뛴 다음 행(=4등)부터 3개(4~6등) 조회
SELECT	EMP_NAME, SALARY
FROM	EMP
ORDER BY SALARY DESC
LIMIT 3, 3;

-- 페이지네이션: OFFSET = (페이지번호 - 1) x 페이지당개수
-- 2페이지, 페이지당 5명 -> OFFSET = (2-1) x 5 = 5
SELECT	EMP_NAME, SALARY
FROM	EMP
ORDER BY SALARY DESC
LIMIT 5, 5;
