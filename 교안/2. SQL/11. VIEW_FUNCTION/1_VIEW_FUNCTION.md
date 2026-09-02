# Day 16. VIEW & 사용자정의 FUNCTION

| 항목 | 내용 |
|---|---|
| 선수학습 | DQL/함수/JOIN/SUBQUERY, DDL(CTAS/제약조건), TCL(자동 커밋 개념) |
| 이번 챕터 | `CREATE VIEW`(단순/복합 뷰, 갱신 가능 여부), `WITH CHECK OPTION`, `CREATE FUNCTION`(파라미터·`RETURNS`·특성 절·`BEGIN…END`·`DECLARE`·`SET`·`IF`·`RETURN`) |
| 권장 진행 | 1일 |
| DB 기준 | MySQL 8.0 |

## 학습목표
- 뷰가 "저장된 `SELECT`문"이라는 것을 이해하고, `CREATE VIEW`/`DROP VIEW`를 사용할 수
  있다.
- 뷰를 통한 `INSERT`/`UPDATE`가 실제로는 원본 테이블을 바꾼다는 것과, 갱신 불가능한
  뷰의 조건(집계 함수/`GROUP BY`/`DISTINCT`/`JOIN` 등 포함 시)을 설명할 수 있다.
- `WITH CHECK OPTION`으로 뷰의 조건을 벗어나는 데이터 변경을 막을 수 있다.
- `CREATE FUNCTION`으로 나만의 함수를 만들어 `SELECT`절에서 재사용할 수 있다.
- 함수 정의의 각 부분(파라미터, `RETURNS`, 특성 절, `BEGIN…END`, `DECLARE`, `SET`,
  `IF`, `RETURN`)이 어떤 키워드로 어떻게 쓰이는지 안다.
- `DELIMITER`가 복합문을 하나의 객체로 안전하게 정의하도록 경계를 설정한다는 것을 안다.
- 함수의 특성 절(`DETERMINISTIC` 등)은 `log_bin_trust_function_creators` 설정으로 생략할 수
  있고, 설정을 안 한 서버에서는 키워드를 붙여야 한다는 것을 안다.

---

## 0. 실습 준비 - 사본 테이블 + 함수 생성 설정

이번 챕터는 뷰를 통해 실제로 데이터를 변경해보므로, `DDL`/`DML`/`TCL` 챕터와 마찬가지로
원본 `EMP`/`DEPT`를 건드리지 않도록 사본에서 진행합니다.

```sql
DROP TABLE IF EXISTS EMP_COPY;
DROP TABLE IF EXISTS DEPT_COPY;

CREATE TABLE DEPT_COPY AS SELECT * FROM DEPT;
CREATE TABLE EMP_COPY  AS SELECT * FROM EMP;
```

### 사용자정의 함수 생성 허용 (한 번만)

MySQL은 바이너리 로그가 켜져 있으면(기본값), 함수를 만들 때 "이 함수가 결정적인지"를
특성 절(`DETERMINISTIC` 등)로 선언하지 않으면 생성 자체를 거부합니다(`Error 1418`).
`DML` 챕터의 `sql_safe_updates` 처럼, 실습 편의를 위해 서버에 **한 번만** 아래 설정을 해두면
특성 키워드 없이도 함수를 만들 수 있습니다.

```sql
SET GLOBAL log_bin_trust_function_creators = 1;   -- 함수 정의 시 특성 절 생략 허용
```

- `GLOBAL` 이라 서버가 재시작되기 전까지 유효합니다. 영구 적용은 `my.ini`/`my.cnf` 의
  `[mysqld]` 에 `log_bin_trust_function_creators = 1` 을 넣고 서버 재시작.
- 권한이 없어 `SET GLOBAL` 이 막히면(공유 서버 등), **그때만** 함수 정의에
  `DETERMINISTIC`(또는 `NO SQL` / `READS SQL DATA`)을 직접 붙입니다(7.1 ③).
- 이 교재의 함수 예제는 이 설정이 돼 있다고 보고 **특성 절을 생략**합니다.

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

### 사용자정의 함수(Stored Function)란

계산 로직을 **하나의 이름으로 묶어 DB에 저장해 두고**, 내장 함수(`ROUND`, `IFNULL` …)처럼
`SELECT`·`WHERE` 등 **식이 들어갈 자리 어디서나** 불러 쓰는 것입니다.

- 핵심은 **입력(파라미터)을 받아 값 하나를 `RETURN`** 하는 것. 여러 행을 반환하거나
  데이터를 바꾸는 건 함수가 아니라 **프로시저**의 몫입니다(Day 17).
- 같은 계산식(예: 연봉 = (급여+수당)×12, 주민번호 → 나이)이 여러 쿼리에 반복될 때,
  한 곳에 정의해 두고 재사용 → 실수·중복이 줄어듭니다.
- 제약: 함수 안에서는 `COMMIT`/`ROLLBACK` 같은 **트랜잭션 제어를 쓸 수 없고**, 보통
  데이터를 변경하지 않는(조회·계산) 용도로만 씁니다.

| | 뷰(VIEW) | 함수(FUNCTION) | 프로시저(PROCEDURE, Day 17) | 트리거(TRIGGER, Day 17) |
|---|---|---|---|---|
| 정체 | 저장된 `SELECT` | 값 1개를 돌려주는 계산식 | 작업(문장)들의 묶음 | 이벤트에 자동 실행되는 프로시저 |
| 호출 | `FROM 뷰이름` | 식 안에서 `함수(인자)` | `CALL 이름(인자)` | 호출 안 함 (`INSERT`/`UPDATE`/`DELETE`가 방아쇠) |
| 반환 | 결과 집합(표) | `RETURNS` 값 1개 | 없음 (`OUT` 파라미터로 전달) | 없음 |

### 7.1 작성 구조와 키워드

```sql
DELIMITER $$                                                    -- 이제부터 문장의 끝은 $$ 로 본다 (기본 ; 대신)

CREATE FUNCTION 함수이름(파라미터명 타입, 파라미터명 타입, ...)   -- ① 머리
RETURNS 반환타입                                                 -- ② 반환 타입 (필수)
                                                                -- ③ 특성 절: 0절 설정을 했으면 생략.
                                                                --    안 했으면 여기에 DETERMINISTIC / NO SQL / READS SQL DATA
BEGIN                                                            -- ④ 본문 블록 시작
    DECLARE 변수명 타입 DEFAULT 초기값;                           -- ⑤ 지역 변수 선언 (맨 위에서만)
    SET 변수명 = 식;                                             -- ⑥ 값 대입
    IF 조건 THEN ... ELSEIF 조건 THEN ... ELSE ... END IF;       -- ⑦ 제어문
    RETURN 식;                                                   -- ⑧ 결과 반환 후 종료 (필수)
END $$                                                          -- 함수 정의 전체가 여기서 끝 (문장 종결자 $$)

DELIMITER ;                                                     -- 정의 끝. 기본 구분자 ; 로 되돌린다
```

- `DELIMITER $$` : 지금부터 **문장의 끝을 알리는 문자**를 `;` → `$$` 로 바꾼다. 그래야
  본문 안 `;` 들을 문장 끝으로 안 보고, `END $$` 까지를 하나의 `CREATE` 문으로 인식한다.
- `$$` : 문장의 끝을 지정하는 문자(원하면 `//`, `$$$` 등 다른 것도 가능)
- `DELIMITER ;` : 함수 정의가 끝났으니 **다시 본래 기본 구분자인 `;` 로 돌아가기**

| 위치 | 키워드 | 의미·규칙 |
|---|---|---|
| ① | `CREATE FUNCTION 이름(...)` | 파라미터는 `이름 타입` 쌍으로 나열. **입력 전용**이라 `IN`/`OUT` 표시를 붙이지 않음(프로시저와 다른 점). 파라미터가 없어도 괄호 `()`는 씀 |
| ② | `RETURNS 타입` | 돌려줄 값의 타입. **반드시** 파라미터 괄호 다음에 한 번. (본문의 `RETURN`과 다름 — 이건 `S`가 붙은 **선언**) |
| ③ | 특성 절 | **선택** — 0절의 `log_bin_trust_function_creators` 설정을 했으면 아예 생략. (안 했다면 `DETERMINISTIC`·`NO SQL`·`READS SQL DATA` 중 하나는 필수) 그 밖에 `SQL SECURITY DEFINER│INVOKER`, `COMMENT '설명'` 등을 공백으로 나열 |
| ④ | `BEGIN ... END` | 본문에 실행문이 2개 이상이면 이 블록으로 감쌈. 문장이 하나(예: `RETURN ...`만)면 생략 가능 |
| ⑤ | `DECLARE 이름 타입 [DEFAULT 값]` | 지역 변수. **`BEGIN` 바로 다음, 다른 실행문보다 먼저** 선언해야 함 |
| ⑥ | `SET 변수 = 식` | 대입. 쿼리 결과를 넣을 땐 `SELECT 컬럼 INTO 변수 FROM ...` |
| ⑦ | 제어문 | `IF ... THEN ... [ELSEIF ...] [ELSE ...] END IF;` · `CASE` · `WHILE 조건 DO ... END WHILE;` · `REPEAT ... UNTIL 조건 END REPEAT;` · `LOOP ... END LOOP;` |
| ⑧ | `RETURN 식` | 값을 돌려주고 함수를 **즉시 끝냄**. 실행이 지나갈 수 있는 **모든 경로**에서 한 번은 실행돼야 함 |
| 감싸기 | `DELIMITER $$` … `DELIMITER ;` | **여러 문장이 든 복합문(`BEGIN…END`)을 하나의 객체로 안전하게 정의하도록 경계를 설정**한다. 본문 안 `;`를 문장 끝으로 오해하지 않게 종결자를 잠깐 `$$`로 바꾸고, 정의가 끝나면 `;`로 되돌림 |

> **`DELIMITER` 를 더 풀어보면**: MySQL 클라이언트는 `;` 를 만나면 "여기까지가 한 문장"
> 이라 보고 서버로 보냅니다. 그런데 함수·프로시저·트리거의 본문은 `DECLARE …;`,
> `SET …;`, `RETURN …;` 처럼 **`;` 가 여러 번** 나오는 복합문입니다. 종결자가 `;` 그대로면
> 첫 번째 `;` 에서 정의가 잘려 보내집니다. 그래서 정의하는 동안만 종결자를 `$$`(또는
> `//`) 로 바꿔, `END $$` 까지를 **통째로 하나의 `CREATE` 문**으로 서버에 넘깁니다.
> `DELIMITER` 는 SQL 문법이 아니라 **클라이언트 명령**이라 세미콜론을 붙이지 않습니다.

> **특성 절을 생략하는 이유**: 0절에서 `log_bin_trust_function_creators = 1` 설정을 했기
> 때문입니다. 이 설정을 하지 않은 서버라면, 아래 예제들의 `RETURNS ...` 다음 줄에
> `DETERMINISTIC` 을 추가해야 `Error 1418` 없이 생성됩니다.

### 7.2 예제 1 - 한 줄 함수 (연봉 계산)

본문이 `RETURN` 한 문장이라 `BEGIN...END` 없이도 되지만, 형태를 익히려고 블록으로 씁니다.

```sql
DELIMITER $$

CREATE FUNCTION GET_ANNUAL_SALARY(P_SALARY INT, P_BONUS DECIMAL(4,2))
RETURNS INT
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

**설명**: 파라미터 `P_SALARY`, `P_BONUS`를 받아 `RETURNS INT`로 정수 하나를 돌려줍니다.
만들어진 함수는 내장 함수와 똑같이 `SELECT`절·`WHERE`절 어디서나 재사용할 수 있습니다.

**파라미터 타입은 왜 이렇게?**

| 파라미터 | 타입 | 이유 |
|---|---|---|
| `P_SALARY` | `INT` | 급여가 원 단위 정수라서. 소수가 필요 없음 |
| `P_BONUS` | `DECIMAL(4,2)` | 아래 3가지 이유 |

- **`DECIMAL` (FLOAT/DOUBLE 아님)**: 보너스는 급여에 곱하는 **비율**(`0.15` = 15%)이고
  금액 계산에 들어갑니다. `FLOAT`/`DOUBLE`은 이진 근사라 `0.1`도 정확히 못 담아 오차가
  누적됩니다. `DECIMAL`은 10진수 그대로 저장해 **오차가 없습니다**.
- **`(4,2)` 의미**: 정밀도(전체 자릿수) 4, 소수 자릿수 2 → `-99.99 ~ 99.99`, 소수점 이하
  **딱 2자리**. `0.15`, `0.10` 같은 1% 단위 비율을 담기에 맞습니다(`0.155`처럼 더 잘게
  쓰려면 소수 3자리가 필요).
- **호출 컬럼과 맞추기**: `GET_ANNUAL_SALARY(SALARY, BONUS)`로 `EMP_COPY.BONUS` 컬럼을
  그대로 넘깁니다. 파라미터 타입이 컬럼 타입과 어긋나면 암묵적 형변환·정밀도 손실이
  생기므로, **컬럼 타입에 맞춰** 잡습니다.

> `INT * DECIMAL(4,2)` 계산은 `DECIMAL`로 승격됐다가 `RETURNS INT`에서 정수로 잘려
> 반환됩니다. scale를 넘는 값(`0.156`)을 넘기면 대입 시 2자리로 **반올림**(`0.16`)됩니다.

### 7.3 예제 2 - 변수와 제어문을 쓰는 함수 (급여 등급)

```sql
DELIMITER $$

CREATE FUNCTION SALARY_GRADE(P_SALARY INT)
RETURNS VARCHAR(10)
BEGIN
    DECLARE V_GRADE VARCHAR(10);          -- ⑤ 지역 변수

    IF P_SALARY >= 6000000 THEN           -- ⑦ 제어문
        SET V_GRADE = 'A';               -- ⑥ 대입
    ELSEIF P_SALARY >= 4000000 THEN
        SET V_GRADE = 'B';
    ELSEIF P_SALARY >= 2500000 THEN
        SET V_GRADE = 'C';
    ELSE
        SET V_GRADE = 'D';
    END IF;

    RETURN V_GRADE;                       -- ⑧ 반환
END $$

DELIMITER ;

SELECT EMP_NAME, SALARY, SALARY_GRADE(SALARY) AS 급여등급
FROM EMP_COPY
ORDER BY SALARY DESC;
```

**출력 결과 (일부)**
```
곽상혁 8000000 A
권진우 6000000 A
윤정주 3760000 C
유제영 2500000 C
최주호 1800000 D
```

**설명**: `DECLARE`로 변수 하나를 만들고 `IF ... ELSEIF ... ELSE ... END IF`로 값을 채운 뒤
`RETURN`으로 돌려줍니다. 본문에 실행문이 여러 개라 `BEGIN ... END`로 감쌌고, 그래서
`DELIMITER` 변경이 필요합니다.

### 7.4 함수 vs 프로시저 (미리보기)

절 첫머리 4종 비교표에 더해, 파라미터·데이터 변경 관점의 차이입니다.

| | 함수 (FUNCTION) | 프로시저 (PROCEDURE, Day 17) |
|---|---|---|
| 파라미터 | 입력 전용 (`IN` 표시 안 함) | `IN` / `OUT` / `INOUT` |
| 데이터 변경 | 조회·계산 위주 (제약 많음) | `INSERT`/`UPDATE`/`DELETE` 자유 |
| 트랜잭션 제어 | 불가 (`COMMIT`/`ROLLBACK` 사용 시 오류) | 가능 (`START TRANSACTION … COMMIT`) |

---

## 8. DROP FUNCTION

```sql
DROP FUNCTION IF EXISTS GET_ANNUAL_SALARY;
DROP FUNCTION IF EXISTS SALARY_GRADE;
```

수정은 `ALTER FUNCTION`으로 특성(코멘트 등)만 바꿀 수 있고, 본문·파라미터를 고치려면
`DROP` 후 다시 `CREATE` 합니다.

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
- **`log_bin_trust_function_creators` 설정도 안 하고, 특성 절(`DETERMINISTIC` 등)도 안 씀**
  → 함수 생성이 `Error 1418` 로 거부됩니다. 0절 설정을 하거나, `RETURNS` 다음에
  `DETERMINISTIC` 한 줄을 넣으면 됩니다.
- **`DECLARE`를 `SET`·`IF` 뒤에 씀** → 지역 변수 선언은 `BEGIN` 바로 다음, 실행문보다
  먼저 와야 합니다.
- **`IF` 분기 중 `RETURN`이 없는 경로가 있음** → "함수에 RETURN이 없다" 오류. 모든
  경로가 `RETURN`으로 끝나야 합니다(마지막에 기본값 `RETURN`을 두는 방법).
- **`RETURNS`(선언)와 `RETURN`(실행문) 혼동** → 머리에는 `RETURNS 타입`, 본문에는
  `RETURN 값`.

---

## 핵심 요약

| 항목 | 핵심 내용 |
|---|---|
| `CREATE VIEW` | `SELECT`문을 저장. 조회할 때마다 다시 실행됨 |
| 단순 뷰 | `JOIN`/집계/`GROUP BY`/`DISTINCT` 없음 → `INSERT`/`UPDATE` 가능(원본에 반영) |
| 복합 뷰 | 위 요소 포함 → 읽기 전용 |
| `WITH CHECK OPTION` | 뷰의 `WHERE` 조건을 벗어나는 변경을 차단 |
| `DROP VIEW` | 뷰 삭제(원본 데이터는 그대로 유지) |
| `CREATE FUNCTION` | `이름(파라미터 타입…)` → `RETURNS 타입` → `BEGIN … END`. 특성 절(`DETERMINISTIC` 등)은 `log_bin_trust_function_creators=1` 설정 시 생략 |
| 함수 본문 키워드 | `DECLARE`(변수 선언, 맨 위) · `SET`/`SELECT…INTO`(대입) · `IF/CASE/WHILE`(제어) · `RETURN 값`(반환·종료) |
| `DELIMITER $$ … ;` | 복합문(`BEGIN…END`)을 하나의 객체로 안전하게 정의하도록 경계를 설정. 본문 안 `;` 로 정의가 끊기지 않게 종결자를 잠시 변경 |
| `DROP FUNCTION` | 사용자정의 함수 삭제 (본문 수정은 DROP 후 재생성) |
