# Day 16. VIEW & 사용자정의 FUNCTION

| 항목 | 내용 |
|---|---|
| 선수학습 | DQL/함수/JOIN/SUBQUERY, DDL(CTAS/제약조건), TCL(자동 커밋 개념) |
| 이번 챕터 | `CREATE VIEW`(단순/복합 뷰, 갱신 가능 여부), `WITH CHECK OPTION`, `CREATE FUNCTION`(사용자정의 함수) |
| 권장 진행 | 1일 |
| DB 기준 | MySQL 8.0 |

## 학습목표
- 뷰가 "저장된 `SELECT`문"이라는 것을 이해하고, `CREATE VIEW`/`DROP VIEW`를 사용할 수
  있다.
- 뷰를 통한 `INSERT`/`UPDATE`가 실제로는 원본 테이블을 바꾼다는 것과, 갱신 불가능한
  뷰의 조건(집계 함수/`GROUP BY`/`DISTINCT`/`JOIN` 등 포함 시)을 설명할 수 있다.
- `WITH CHECK OPTION`으로 뷰의 조건을 벗어나는 데이터 변경을 막을 수 있다.
- `CREATE FUNCTION`으로 나만의 함수를 만들어 `SELECT`절에서 재사용할 수 있다.
- `DELIMITER`를 바꿔야 하는 이유와 `DETERMINISTIC` 선언의 의미를 안다.

---

## 0. 실습 준비 - 사본 테이블

이번 챕터는 뷰를 통해 실제로 데이터를 변경해보므로, `DDL`/`DML`/`TCL` 챕터와 마찬가지로
원본 `EMP`/`DEPT`를 건드리지 않도록 사본에서 진행합니다.

```sql
DROP TABLE IF EXISTS EMP_COPY;
DROP TABLE IF EXISTS DEPT_COPY;

CREATE TABLE DEPT_COPY AS SELECT * FROM DEPT;
CREATE TABLE EMP_COPY  AS SELECT * FROM EMP;
```

---

## 1. VIEW란 - 저장된 SELECT문

**뷰(view)**는 테이블처럼 보이지만 실제로는 데이터를 따로 저장하지 않고, `SELECT`문
자체를 저장해뒀다가 조회할 때마다 그 `SELECT`를 다시 실행하는 객체입니다.

```sql
CREATE VIEW V_D9_EMP AS
SELECT EMP_ID, EMP_NAME, SALARY, HIRE_DATE
FROM EMP_COPY
WHERE DEPT_ID = 'D9';

SELECT * FROM V_D9_EMP ORDER BY EMP_ID;
```

**출력 결과**
```
200 곽상혁 8000000 2013-03-02
201 권진우 6000000 2013-09-15
202 김민혜 3700000 2014-02-10
```

**설명**: `V_D9_EMP`를 조회하면 마치 진짜 테이블처럼 결과가 나오지만, 실제로는 매번
`SELECT ... FROM EMP_COPY WHERE DEPT_ID = 'D9'`를 다시 실행한 것입니다. 뷰를 쓰는
이유는 크게 두 가지입니다: (1) 자주 쓰는 복잡한 쿼리를 이름 붙여 재사용, (2) 원본
테이블의 일부 컬럼/행만 보여줘서 나머지를 감추는 보안(예: `SALARY`를 빼고 보여주는 뷰).

---

## 2. 복합 뷰 (JOIN + 집계 포함)

```sql
CREATE VIEW V_DEPT_AVG_SALARY AS
SELECT D.DEPT_ID, D.DEPT_TITLE, ROUND(AVG(E.SALARY)) AS 평균급여, COUNT(*) AS 인원수
FROM EMP_COPY E
JOIN DEPT_COPY D ON E.DEPT_ID = D.DEPT_ID
GROUP BY D.DEPT_ID, D.DEPT_TITLE;

SELECT * FROM V_DEPT_AVG_SALARY ORDER BY 평균급여 DESC;
```

**출력 결과**
```
총무부      5900000 3
해외영업2부 3650000 2
해외영업1부 2752000 5
인사관리부  2606667 3
기술지원부  2328747 3
회계관리부  2280000 3
```

**설명**: `JOIN`과 `GROUP BY`(집계 함수)를 포함한 이 뷰는 **갱신 불가능한 뷰**입니다.
"이 뷰의 한 행(예: 총무부 평균급여)을 수정했을 때 원본 `EMP_COPY`의 어느 행을 어떻게
바꿔야 할지" 알 수 없기 때문입니다. 뷰는 조회용으로만 쓰고, 데이터 변경은 원본 테이블
또는 이런 조건이 없는 단순 뷰를 통해서만 할 수 있습니다.

---

## 3. 뷰를 통한 UPDATE - 갱신 가능한 뷰

```sql
CREATE VIEW V_D8_EMP AS
SELECT EMP_ID, EMP_NAME, DEPT_ID, SALARY
FROM EMP_COPY
WHERE DEPT_ID = 'D8';

UPDATE V_D8_EMP SET SALARY = SALARY + 100000 WHERE EMP_ID = '210';

SELECT EMP_ID, EMP_NAME, SALARY FROM EMP_COPY WHERE EMP_ID = '210';
```

**출력 결과**
```
210 이광렬 2100000
```

**설명**: 뷰를 통한 `UPDATE`는 실제로는 원본 `EMP_COPY` 테이블의 데이터를 바꿉니다.
뷰는 "원본 테이블을 보는 창(window)"일 뿐, 별도의 저장 공간을 갖지 않기 때문입니다.
`JOIN`/`GROUP BY`/집계 함수/`DISTINCT`가 없는 이런 단순 뷰만 `INSERT`/`UPDATE`가
가능합니다.

---

## 4. WITH CHECK OPTION

```sql
DROP VIEW IF EXISTS V_D8_EMP;

CREATE VIEW V_D8_EMP AS
SELECT EMP_ID, EMP_NAME, DEPT_ID, SALARY
FROM EMP_COPY
WHERE DEPT_ID = 'D8'
WITH CHECK OPTION;

UPDATE V_D8_EMP SET DEPT_ID = 'D1' WHERE EMP_ID = '210';
```

**출력 결과**
```
오류: CHECK OPTION failed 'V_D8_EMP'
```

**설명**: `WITH CHECK OPTION`이 없다면 이 `UPDATE`는 성공하고, 이후
`SELECT * FROM V_D8_EMP;`를 조회하면 방금 부서를 바꾼 이광렬은 더 이상 뷰의 조건
(`DEPT_ID = 'D8'`)에 맞지 않아 화면에서 조용히 "사라지는" 이상한 경험을 하게 됩니다.
`WITH CHECK OPTION`은 뷰의 `WHERE` 조건을 벗어나는 변경 자체를 애초에 막아줍니다.

---

## 5. 갱신 불가능한 뷰의 조건

아래 요소가 하나라도 포함되면 그 뷰는 `INSERT`/`UPDATE`(일부는 `DELETE`도)가 불가능한
읽기 전용 뷰가 됩니다.

| 요소 | 이유 |
|---|---|
| 집계 함수(`SUM`, `AVG`, `COUNT` 등) | 원본의 어느 행을 바꿔야 할지 알 수 없음 |
| `GROUP BY` / `HAVING` | 여러 원본 행이 이미 하나로 합쳐짐 |
| `DISTINCT` | 중복 제거로 원본 행 대응 관계가 깨짐 |
| 여러 테이블을 묶은 `JOIN` | 한 번의 변경이 어느 테이블에 반영돼야 할지 모호함(단, 일부 조건에서는 가능) |
| `UNION` / `UNION ALL` | 여러 조회 결과를 합친 것이라 원본 행 하나로 되돌릴 수 없음 |

---

## 6. DROP VIEW

```sql
DROP VIEW IF EXISTS V_D9_EMP;
DROP VIEW IF EXISTS V_DEPT_AVG_SALARY;
DROP VIEW IF EXISTS V_D8_EMP;
```

---

## 7. 사용자정의 FUNCTION

```sql
DELIMITER $$

CREATE FUNCTION GET_ANNUAL_SALARY(P_SALARY INT, P_BONUS DECIMAL(4,2))
RETURNS INT
DETERMINISTIC
BEGIN
    RETURN (P_SALARY + P_SALARY * IFNULL(P_BONUS, 0)) * 12;
END $$

DELIMITER ;

SELECT EMP_NAME, SALARY, BONUS, GET_ANNUAL_SALARY(SALARY, BONUS) AS 연봉
FROM EMP_COPY
WHERE DEPT_ID = 'D5'
ORDER BY 연봉 DESC;
```

**출력 결과**
```
박지민 3500000 0.15 48300000
윤정주 3760000 (null) 45120000
유제영 2500000 (null) 30000000
염성원 2200000 0.10 29040000
최주호 1800000 (null) 21600000
```

**설명**: `DELIMITER $$`로 구분자를 잠시 바꾸는 이유는, 함수 본문 안에 세미콜론(`;`)이
등장하기 때문입니다(`RETURN` 문 끝). 세미콜론을 문장 종결자로 그대로 두면 MySQL이
함수 정의 중간에서 문장이 끝난 걸로 착각합니다. `$$`로 바꿔두면 함수 전체를 하나의
덩어리로 인식시킬 수 있고, 정의가 끝난 뒤 `DELIMITER ;`로 원래대로 되돌립니다.
`DETERMINISTIC`은 "같은 입력이면 항상 같은 출력을 낸다"는 선언으로, MySQL의 이진
로그(binlog) 설정에 따라 이 선언이 없으면 함수 생성 자체가 거부될 수 있습니다.
만들어진 함수는 일반 내장 함수(`ROUND`, `IFNULL` 등)와 똑같이 `SELECT`절 어디서나
재사용할 수 있습니다.

---

## 8. DROP FUNCTION

```sql
DROP FUNCTION IF EXISTS GET_ANNUAL_SALARY;
```

---

## 자주 하는 실수

- **`GROUP BY`/집계 함수/`DISTINCT`가 포함된 뷰를 통해 `INSERT`/`UPDATE` 시도** →
  오류. 갱신 가능한 뷰인지(5절) 먼저 확인해야 합니다.
- **뷰가 "복사본"이라고 착각하고 뷰를 통한 `UPDATE`가 원본에 영향 없을 거라 생각** →
  실제로는 원본 테이블이 그대로 바뀝니다(3절).
- **`WITH CHECK OPTION` 없이 뷰 조건을 벗어나는 값으로 `UPDATE`** → 방금 바꾼 행이
  뷰 조회 결과에서 조용히 사라져서 "데이터가 없어졌다"고 착각하게 됩니다.
- **`DELIMITER`를 안 바꾸고 함수 본문을 작성** → 본문 안 세미콜론에서 정의가 끊겨
  오류가 납니다.
- **`DETERMINISTIC` 선언을 빠뜨림** → 서버 설정에 따라 함수 생성 자체가 거부될 수
  있습니다.

---

## 핵심 요약

| 항목 | 핵심 내용 |
|---|---|
| `CREATE VIEW` | `SELECT`문을 저장. 조회할 때마다 다시 실행됨 |
| 단순 뷰 | `JOIN`/집계/`GROUP BY`/`DISTINCT` 없음 → `INSERT`/`UPDATE` 가능(원본에 반영) |
| 복합 뷰 | 위 요소 포함 → 읽기 전용 |
| `WITH CHECK OPTION` | 뷰의 `WHERE` 조건을 벗어나는 변경을 차단 |
| `DROP VIEW` | 뷰 삭제(원본 데이터는 그대로 유지) |
| `CREATE FUNCTION` | `DELIMITER` 변경 필요, `RETURNS 타입`, `DETERMINISTIC` 선언, `SELECT`절에서 재사용 |
| `DROP FUNCTION` | 사용자정의 함수 삭제 |
