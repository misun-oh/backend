-- MANAGER_ID : 매니져의 사번
-- 최상위노드 (ROOT) - 매니져ID
-- 가상의 테이블을 생성
WITH RECURSIVE EmpTree AS (
    -- 1. Root 노드 선택 (최상위 매니저: 주로 MANAGER_ID가 NULL인 사원)
    SELECT 
        EMP_ID, 
        EMP_NAME, 
        MANAGER_ID, 
        1 AS LEVEL,                         -- 트리의 깊이(레벨)
        CAST(EMP_NAME AS CHAR(200)) AS PATH -- 정렬을 위한 경로 저장
    FROM EMP
    WHERE MANAGER_ID IS NULL
    
    UNION ALL -- 집합연산자 - 쿼리 결과의 합집합 (컬럼의 갯수가 일치)
    
    -- 2번째 쿼리를 반복적으로 실행하여 가상테이블에 추가
    -- 2. 자식 노드들을 재귀적으로 결합
    SELECT 
        e.EMP_ID, 
        e.EMP_NAME, 
        e.MANAGER_ID, 
        t.LEVEL + 1 AS LEVEL,
        CONCAT(t.PATH, ' -> ', e.EMP_NAME) AS PATH
    FROM EMP e
    INNER JOIN EmpTree t ON e.MANAGER_ID = t.EMP_ID
)
-- 3. 트리 형태로 정렬 및 시각화 출력
SELECT 
    EMP_ID,
    -- LEVEL 수만큼 앞공간에 공백(들여쓰기)과 기호를 넣어 트리 구조 시각화
    CONCAT(REPEAT('    ', LEVEL - 1), '┗ ', EMP_NAME) AS '조직도',
    MANAGER_ID,
    LEVEL,
    PATH AS '전체 경로'
FROM EmpTree
ORDER BY PATH; -- 경로순으로 정렬하면 트리 구조대로 출력됩니다.




SELECT 
        EMP_ID, 
        EMP_NAME, 
        MANAGER_ID, 
        1 AS LEVEL,                         -- 트리의 깊이(레벨)
        CAST(EMP_NAME AS CHAR(200)) AS PATH -- 정렬을 위한 경로 저장
    FROM EMP
    WHERE MANAGER_ID IS NULL;
    
    
    
    