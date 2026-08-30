-- Day 6~7. JOIN — 1_JOIN.md와 순서가 같은 실습 스크립트
-- ANSI 표준 JOIN 문법(JOIN ... ON)만 사용 (콤마로 나열하는 옛날 문법은 카티전 곱 위험)


-- ================================
-- 1. JOIN이 필요한 이유
-- ================================

-- 조건 없이 나열 -> 카티전 곱(EMP 21 x DEPT 9 = 189행), 실무에서 쓰면 안 됨
SELECT	E.EMP_NAME, D.DEPT_TITLE
FROM	EMP E, DEPT D;

-- 연결 조건을 명시 (MySQL은 Oracle의 (+) 외부조인 문법을 지원하지 않음)
SELECT	E.EMP_NAME, D.DEPT_TITLE
FROM	EMP E
JOIN	DEPT D ON E.DEPT_ID = D.DEPT_ID;


-- ================================
-- 2. INNER JOIN (내부 조인)
-- ================================

-- 양쪽 테이블에 모두 값이 존재해서 조건이 맞아떨어지는 행만 결과에 포함
SELECT	E.EMP_NAME, D.DEPT_TITLE
FROM	EMP E
JOIN	DEPT D ON E.DEPT_ID = D.DEPT_ID
WHERE	D.DEPT_ID = 'D9';

-- 주의: DEPT_ID가 NULL인 조정원(219)/한규원(220)은 조건 자체가 성립 안 해서
-- WHERE 없이 전체 조회해도 21명이 아니라 19명만 나옴
SELECT	E.EMP_NAME, D.DEPT_TITLE
FROM	EMP E
JOIN	DEPT D ON E.DEPT_ID = D.DEPT_ID;

-- USING : 조인 컬럼명이 두 테이블에서 완전히 같을 때 ON 대신 사용 (결과는 동일)
SELECT	E.EMP_NAME, D.DEPT_TITLE
FROM	EMP E
JOIN	DEPT D USING (DEPT_ID);


-- ================================
-- 3. OUTER JOIN (외부 조인)
-- ================================

-- LEFT JOIN : 왼쪽(FROM 바로 뒤) 테이블 기준으로 전부 살리고, 짝 없으면 NULL
SELECT	E.EMP_NAME, E.DEPT_ID, D.DEPT_TITLE
FROM	EMP E
LEFT JOIN DEPT D ON E.DEPT_ID = D.DEPT_ID
WHERE	E.DEPT_ID IS NULL;

-- RIGHT JOIN : 오른쪽 테이블 기준으로 전부 살리고, 짝 없으면 NULL
-- COUNT(E.EMP_ID)는 NULL을 세지 않으므로 사원 없는 부서는 정확히 0으로 집계됨
SELECT	D.DEPT_ID, D.DEPT_TITLE, COUNT(E.EMP_ID) AS 인원수
FROM	EMP E
RIGHT JOIN DEPT D ON E.DEPT_ID = D.DEPT_ID
GROUP BY D.DEPT_ID, D.DEPT_TITLE
ORDER BY D.DEPT_ID;

-- FULL OUTER JOIN : MySQL에는 없음 -> LEFT JOIN + RIGHT JOIN을 UNION으로 흉내
-- (UNION은 완전히 같은 행을 자동으로 중복 제거함, UNION ALL을 쓰면 안 됨)
SELECT	E.EMP_NAME, D.DEPT_TITLE
FROM	EMP E
LEFT JOIN DEPT D ON E.DEPT_ID = D.DEPT_ID
UNION
SELECT	E.EMP_NAME, D.DEPT_TITLE
FROM	EMP E
RIGHT JOIN DEPT D ON E.DEPT_ID = D.DEPT_ID;


-- ================================
-- 4. 다중 테이블 JOIN
-- ================================

SELECT	E.EMP_NAME, D.DEPT_TITLE, J.JOB_NAME
FROM	EMP E
JOIN	DEPT D ON E.DEPT_ID = D.DEPT_ID
JOIN	JOB J ON E.JOB_CODE = J.JOB_CODE
WHERE	E.DEPT_ID = 'D5';

-- DEPT -> LOCATION -> NATIONAL 처럼 FK가 체인으로 이어진 경우도 하나씩 ON을 추가
SELECT	E.EMP_NAME, D.DEPT_TITLE, L.LOCAL_NAME, N.NATIONAL_NAME
FROM	EMP E
JOIN	DEPT D ON E.DEPT_ID = D.DEPT_ID
JOIN	LOCATION L ON D.LOCATION_ID = L.LOCAL_CODE
JOIN	NATIONAL N ON L.NATIONAL_CODE = N.NATIONAL_CODE
WHERE	E.DEPT_ID = 'D9';


-- ================================
-- 5. SELF JOIN (자기 자신과의 조인)
-- ================================

-- EMP.MANAGER_ID는 같은 EMP 테이블의 EMP_ID를 가리키는 FK(자기참조)
-- 같은 테이블을 서로 다른 별칭(E, M)으로 두 번 등장시켜 조인
-- 대표(200)는 MANAGER_ID가 NULL -> INNER JOIN이면 빠지므로 LEFT JOIN + IFNULL 사용
SELECT	E.EMP_NAME AS 사원, IFNULL(M.EMP_NAME, '(관리자 없음)') AS 관리자
FROM	EMP E
LEFT JOIN EMP M ON E.MANAGER_ID = M.EMP_ID
WHERE	E.EMP_ID IN ('200', '213', '214', '215');


-- ================================
-- 6. 비동등 조인 (NON-EQUI JOIN)
-- ================================

-- SAL_GRADE는 FK로 연결되어 있지 않고, SALARY가 MIN_SAL~MAX_SAL 구간에 속하는지로만 연결
-- BETWEEN은 양쪽 경계값을 포함 -> 6,000,000은 S2가 아니라 S1(6,000,000~10,000,000)
SELECT	E.EMP_NAME, E.SALARY, S.SAL_LEVEL
FROM	EMP E
JOIN	SAL_GRADE S ON E.SALARY BETWEEN S.MIN_SAL AND S.MAX_SAL
WHERE	E.DEPT_ID = 'D9';
