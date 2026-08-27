# 5_계층형쿼리_조직도와사이트맵.pdf 본문 실습 스크립트
# DB 기준: MySQL 8.0+ (WITH RECURSIVE는 8.0부터)
# 1~3번: 공통 실습 데이터의 EMP 사용 / 4번: 이 스크립트에서 MENU 생성

# =====================================================================
# 1. 계층형 데이터 확인 - EMP.MANAGER_ID (자기참조)
# =====================================================================
SELECT EMP_ID, EMP_NAME, MANAGER_ID FROM EMP ORDER BY EMP_ID;


# =====================================================================
# 2. 재귀 없이 조직도 만들기 - 레벨별 조회 + UNION
# =====================================================================

# 2.1 1레벨 - 관리자가 없는 직원 (최상위)
SELECT EMP_ID, EMP_NAME, MANAGER_ID
FROM EMP
WHERE MANAGER_ID IS NULL;

# 2.2 2레벨 - 1레벨 사번을 MANAGER_ID로 가진 직원
SELECT EMP_ID, EMP_NAME, MANAGER_ID
FROM EMP
WHERE MANAGER_ID IN (
    SELECT EMP_ID FROM EMP WHERE MANAGER_ID IS NULL
);

# 2.3 UNION으로 두 레벨을 합치기 = 2단계 조직도
SELECT EMP_ID, EMP_NAME, MANAGER_ID, 1 AS LVL
FROM EMP
WHERE MANAGER_ID IS NULL

UNION ALL

SELECT EMP_ID, EMP_NAME, MANAGER_ID, 2 AS LVL
FROM EMP
WHERE MANAGER_ID IN (
    SELECT EMP_ID FROM EMP WHERE MANAGER_ID IS NULL
)

ORDER BY LVL, MANAGER_ID, EMP_ID;
# 한계: 3레벨은 UNION ALL 조각 + 서브쿼리 중첩을 하나 더 붙여야 한다. 깊이만큼 반복.

# 2.4 다른 각도 - 집계 함수로 "관리자별 요약"
# MANAGER_ID로 GROUP BY 후 COUNT(부하 수) + GROUP_CONCAT(부하 명단)
SELECT m.EMP_NAME AS 관리자,
       COUNT(*) AS 직속부하수,
       GROUP_CONCAT(e.EMP_NAME ORDER BY e.EMP_ID SEPARATOR ', ') AS 직속부하
FROM EMP e
JOIN EMP m ON e.MANAGER_ID = m.EMP_ID     -- e = 부하, m = 그 관리자
GROUP BY e.MANAGER_ID, m.EMP_NAME
ORDER BY 직속부하수 DESC;
# -> 곽상혁 5 / 박지민 3 / 이광렬 2 / 이다현 2 / 권진우·김은민·염성원 각 1
# 집계는 "관리자 -> 직속 부하 한 단계" 요약. 손자 세대는 못 내려가고 GROUP BY는 계층을 없앤다.


# =====================================================================
# 3. WITH RECURSIVE - 재귀 쿼리로 전체 조직도
# =====================================================================

# 3.3 조직도 완성 쿼리 (LVL + PATH + 들여쓰기)
WITH RECURSIVE EmpTree AS (
    -- (1) 앵커: 관리자가 없는 최상위 (2.1의 쿼리와 동일)
    SELECT EMP_ID, EMP_NAME, MANAGER_ID,
           1 AS LVL,
           CAST(EMP_NAME AS CHAR(200)) AS PATH   -- 경로는 넉넉한 길이로
    FROM EMP
    WHERE MANAGER_ID IS NULL

    UNION ALL

    -- (3) 재귀: 직전 결과(t)의 사원을 관리자로 둔 부하(e)를 붙인다
    SELECT e.EMP_ID, e.EMP_NAME, e.MANAGER_ID,
           t.LVL + 1,
           CONCAT(t.PATH, ' > ', e.EMP_NAME)
    FROM EMP e
    JOIN EmpTree t ON e.MANAGER_ID = t.EMP_ID
)
-- (4) 완성된 가상 테이블을 경로순 정렬 + 깊이만큼 들여쓰기
SELECT EMP_ID,
       CONCAT(REPEAT('    ', LVL - 1), '└ ', EMP_NAME) AS 조직도,
       MANAGER_ID, LVL, PATH AS 전체경로
FROM EmpTree
ORDER BY PATH;

# 응용: 특정 부서(박지민 205)만 - 앵커의 WHERE만 교체
WITH RECURSIVE EmpTree AS (
    SELECT EMP_ID, EMP_NAME, MANAGER_ID,
           1 AS LVL, CAST(EMP_NAME AS CHAR(200)) AS PATH
    FROM EMP
    WHERE EMP_ID = '205'
    UNION ALL
    SELECT e.EMP_ID, e.EMP_NAME, e.MANAGER_ID,
           t.LVL + 1, CONCAT(t.PATH, ' > ', e.EMP_NAME)
    FROM EMP e
    JOIN EmpTree t ON e.MANAGER_ID = t.EMP_ID
)
SELECT EMP_ID,
       CONCAT(REPEAT('    ', LVL - 1), '└ ', EMP_NAME) AS 조직도, LVL
FROM EmpTree
ORDER BY PATH;


# =====================================================================
# 4. 메뉴 테이블로 사이트맵 완성
# =====================================================================

# 4.1 자기참조 PARENT_ID를 가진 MENU 테이블
DROP TABLE IF EXISTS MENU;
CREATE TABLE MENU (
    MENU_ID    INT          PRIMARY KEY,
    MENU_NAME  VARCHAR(50)  NOT NULL,
    PARENT_ID  INT,                              -- 상위 메뉴의 MENU_ID (대메뉴는 NULL)
    SORT_ORDER INT          NOT NULL DEFAULT 0,  -- 같은 상위 안에서의 표시 순서
    URL        VARCHAR(100),
    CONSTRAINT FK_MENU_PARENT FOREIGN KEY (PARENT_ID) REFERENCES MENU (MENU_ID)
);

# 4.2 데이터 입력 - 대메뉴 -> 서브메뉴 -> 하위메뉴 순서 (PARENT_ID 지정)
INSERT INTO MENU VALUES
 (1, '회원관리', NULL, 1, NULL),
 (2, '상품관리', NULL, 2, NULL),
 (3, '주문관리', NULL, 3, NULL);

INSERT INTO MENU VALUES
 (11, '회원목록',   1, 1, '/admin/members'),
 (12, '등급관리',   1, 2, '/admin/members/grade'),
 (21, '상품목록',   2, 1, '/admin/products'),
 (22, '카테고리',   2, 2, NULL),
 (31, '주문목록',   3, 1, '/admin/orders'),
 (32, '배송관리',   3, 2, '/admin/orders/ship');

INSERT INTO MENU VALUES
 (221, '대분류', 22, 1, '/admin/products/cat/1'),
 (222, '중분류', 22, 2, '/admin/products/cat/2'),
 (223, '소분류', 22, 3, '/admin/products/cat/3');

SELECT * FROM MENU;

# 4.3 재귀 쿼리로 사이트맵 출력 (SORT_ORDER 정렬)
WITH RECURSIVE SiteMap AS (
    -- (1) 앵커: 대메뉴 (상위가 없는 메뉴)
    SELECT MENU_ID, MENU_NAME, PARENT_ID,
           1 AS DEPTH,
           CAST(LPAD(SORT_ORDER, 3, '0') AS CHAR(200)) AS SORT_PATH
    FROM MENU
    WHERE PARENT_ID IS NULL

    UNION ALL

    -- (3) 재귀: 직전 결과(s)를 상위로 둔 하위 메뉴(m)를 붙인다
    SELECT m.MENU_ID, m.MENU_NAME, m.PARENT_ID,
           s.DEPTH + 1,
           CONCAT(s.SORT_PATH, '-', LPAD(m.SORT_ORDER, 3, '0'))
    FROM MENU m
    JOIN SiteMap s ON m.PARENT_ID = s.MENU_ID
)
SELECT MENU_ID,
       CONCAT(REPEAT('    ', DEPTH - 1), MENU_NAME) AS 사이트맵,
       PARENT_ID, DEPTH
FROM SiteMap
ORDER BY SORT_PATH;

# 4.4 응용 - 특정 대메뉴('상품관리' 2) 하위만: 앵커 조건만 교체
WITH RECURSIVE SiteMap AS (
    SELECT MENU_ID, MENU_NAME, PARENT_ID,
           1 AS DEPTH,
           CAST(LPAD(SORT_ORDER, 3, '0') AS CHAR(200)) AS SORT_PATH
    FROM MENU
    WHERE MENU_ID = 2
    UNION ALL
    SELECT m.MENU_ID, m.MENU_NAME, m.PARENT_ID,
           s.DEPTH + 1,
           CONCAT(s.SORT_PATH, '-', LPAD(m.SORT_ORDER, 3, '0'))
    FROM MENU m
    JOIN SiteMap s ON m.PARENT_ID = s.MENU_ID
)
SELECT MENU_ID,
       CONCAT(REPEAT('    ', DEPTH - 1), MENU_NAME) AS 사이트맵,
       PARENT_ID, DEPTH
FROM SiteMap
ORDER BY SORT_PATH;
