-- Day 1~2. DQL (SELECT) — 1_SELECT.md와 순서가 같은 실습 스크립트
-- SELECT 컬럼명 [, 컬럼명, ...] FROM 테이블명 WHERE 조건식;


-- ================================
-- 1. SQL과 SELECT문
-- ================================

-- EMP 테이블에서 전 사원의 사번, 이름, 급여 조회
SELECT	EMP_ID, EMP_NAME, SALARY
FROM	EMP;

SELECT * FROM EMP;


-- ================================
-- 2. 컬럼 별칭 (Alias)
-- ================================

-- AS는 생략 가능, 공백/특수문자가 있으면 "큰따옴표" 필수
SELECT	EMP_NAME AS 이름, SALARY * 12 AS 연봉
FROM	EMP;


-- ================================
-- 3. 산술 연산과 리터럴
-- ================================

-- BONUS가 NULL인 사원은 산술 연산 결과도 NULL이 됨(NULL이 섞이면 결과도 NULL)
-- IFNULL(BONUS, 0) : BONUS가 NULL이면 0으로 취급 (함수 챕터에서 자세히 다룸)
SELECT	EMP_NAME AS 이름,
		SALARY * 12 AS 연봉,
		(SALARY + (SALARY * IFNULL(BONUS, 0))) * 12 AS "보너스 포함 연봉"
FROM	EMP;

-- 리터럴 : 테이블에 없는 문자열을 그대로 쓰면 모든 행에 똑같이 반복 출력됨
SELECT	EMP_NAME, SALARY, '원' AS 단위
FROM	EMP;


-- ================================
-- 4. DISTINCT - 중복 제거
-- ================================

-- DISTINCT는 SELECT절에 1번만 사용 가능, 여러 컬럼이면 조합 전체가 같아야 중복 처리
SELECT DISTINCT JOB_CODE
FROM	EMP;


-- ================================
-- 5. WHERE절과 비교 연산자
-- ================================

-- 문자값 비교는 반드시 작은따옴표로 감싸야 함('D1'), 대소문자 구분함
SELECT	*
FROM	EMP
WHERE	DEPT_ID = 'D1';

SELECT	EMP_NAME, SALARY
FROM	EMP
WHERE	SALARY >= 3000000;


-- ================================
-- 6. BETWEEN AND - 범위 조건
-- ================================

-- BETWEEN A AND B : A 이상 B 이하(경계값 포함)
SELECT	EMP_NAME, SALARY
FROM	EMP
WHERE	SALARY BETWEEN 2000000 AND 3000000;

SELECT	EMP_NAME, SALARY
FROM	EMP
WHERE	SALARY NOT BETWEEN 2000000 AND 3000000;


-- ================================
-- 7. LIKE - 문자 패턴 검색
-- ================================

-- % : 0글자 이상, _ : 정확히 1글자
SELECT	EMP_NAME, HIRE_DATE
FROM	EMP
WHERE	EMP_NAME LIKE '김%';

-- 이메일 아이디에 실제 언더바(_)가 포함된 사원 -> 와일드카드 _와 충돌하므로 ESCAPE 필요
SELECT	EMP_NAME, EMAIL
FROM	EMP
WHERE	EMAIL LIKE '%$_%' ESCAPE '$';

SELECT	EMP_NAME
FROM	EMP
WHERE	EMP_NAME NOT LIKE '이%';


-- ================================
-- 8. IN - 여러 값 중 하나와 일치
-- ================================

SELECT	EMP_NAME, DEPT_ID, SALARY
FROM	EMP
WHERE	DEPT_ID IN ('D5', 'D6', 'D8');

-- 위와 완전히 같은 결과를 OR로 작성한 경우 (IN이 훨씬 간결함)
SELECT	EMP_NAME, DEPT_ID, SALARY
FROM	EMP
WHERE	DEPT_ID = 'D5' OR DEPT_ID = 'D6' OR DEPT_ID = 'D8';


-- ================================
-- 9. IS NULL / IS NOT NULL
-- ================================

-- NULL은 '값이 없음'이므로 = NULL로 비교 불가(항상 UNKNOWN) -> 전용 연산자 사용
SELECT	EMP_NAME, DEPT_ID
FROM	EMP
WHERE	DEPT_ID IS NULL;

SELECT	EMP_NAME, MANAGER_ID, DEPT_ID
FROM	EMP
WHERE	MANAGER_ID IS NULL AND DEPT_ID IS NULL;


-- ================================
-- 10. 논리 연산자와 연산자 우선순위
-- ================================
-- 우선순위(높은 순): 산술 > 비교/IS NULL/LIKE/IN > BETWEEN > NOT > AND > OR
-- MySQL 기본모드에서 ||는 문자열 연결이 아니라 OR와 동일하게 동작함(CONCAT() 사용)

SELECT	*
FROM	EMP
WHERE	DEPT_ID = 'D5' AND JOB_CODE = 'J7';

-- AND가 OR보다 먼저 계산되므로 괄호 없이 쓰면 의도와 다르게 해석될 수 있음!
SELECT	*
FROM	EMP
WHERE	(JOB_CODE = 'J7' OR JOB_CODE = 'J6') AND SALARY >= 2000000;

-- NOT을 단독으로 사용
SELECT	*
FROM	EMP
WHERE	NOT (JOB_CODE = 'J7');


-- ================================
-- 11. ORDER BY - 정렬
-- ================================

SELECT	EMP_NAME, SALARY
FROM	EMP
ORDER BY SALARY DESC;

-- 부서코드 오름차순, 같은 부서 안에서는 급여 내림차순
SELECT	EMP_NAME, DEPT_ID, SALARY
FROM	EMP
ORDER BY DEPT_ID ASC, SALARY DESC;

-- 부서가 배정되지 않은(NULL) 행을 맨 뒤로 보내기
-- MySQL은 NULL을 가장 작은 값으로 취급 -> NULLS FIRST/LAST가 없어 (컬럼 IS NULL)로 우회
SELECT	EMP_NAME, DEPT_ID
FROM	EMP
ORDER BY (DEPT_ID IS NULL), DEPT_ID ASC;
