# Day 17. PROCEDURE & TRIGGER

| 항목 | 내용 |
|---|---|
| 선수학습 | DML(`INSERT`/`UPDATE`/`DELETE`), TCL(`COMMIT`/`ROLLBACK`), VIEW & FUNCTION(`DELIMITER`) |
| 이번 챕터 | `CREATE PROCEDURE`(파라미터, 제어문, 커서, 예외 처리), `CREATE TRIGGER` |
| 권장 진행 | 1~2일 |
| DB 기준 | MySQL 8.0 |

## 학습목표
- 프로시저와 트리거가 각각 **무엇이고 언제 쓰는지** 설명할 수 있다.
- `CREATE PROCEDURE`로 여러 SQL문을 하나의 이름으로 묶어 `CALL`로 재사용할 수 있다.
- `IN`/`OUT` 파라미터를 구분해서 사용할 수 있다.
- `IF`/`CASE` 분기와 `WHILE`/`LOOP`/`REPEAT` 반복문(`LEAVE`/`ITERATE`, 중첩)을 쓸 수 있다.
- 커서(`CURSOR`)로 프로시저 안에서 테이블의 행을 한 건씩 순회 처리할 수 있다.
- `DECLARE ... HANDLER`로 예외 상황(`CONTINUE`/`EXIT`)을 처리할 수 있다.
- 프로시저에서 `COMMIT`/`ROLLBACK`이 필요한 때와 필요 없는 때를 구분할 수 있다.
- `CREATE TRIGGER`로 `INSERT`/`UPDATE` 시점에 자동 실행되는 로직을 만들 수 있다.
- 트리거의 무한 루프 위험을 이해하고 피할 수 있다.

---

## 개념: 프로시저와 트리거란

### 프로시저(Stored Procedure)

여러 SQL 문(주로 `INSERT`/`UPDATE`/`DELETE`)과 제어 로직(`IF`, `WHILE`, 커서)을
**하나의 이름으로 묶어 DB에 저장해 둔 것**입니다. `CALL 이름(인자)` 문장으로 실행합니다.

- 함수(`CREATE FUNCTION`)와 달리 **값을 반환하지 않고**(`RETURN` 없음), `SELECT`절
  안에서 못 씁니다. 결과가 필요하면 `OUT` 파라미터로 돌려줍니다.
- "여러 작업을 한 번의 호출로", "앱 대신 DB 안에서 절차적 처리"가 필요할 때 씁니다.
- 앱↔DB 왕복을 줄이고 로직을 한곳에 모으는 장점 / DB에 로직이 묶여 이식성·버전 관리가
  어려운 단점.

### 트리거(Trigger)

특정 테이블에 `INSERT`/`UPDATE`/`DELETE`가 일어나는 **시점에 자동으로 실행되는
프로시저**입니다. `CALL`로 부르지 않고, 그 이벤트가 방아쇠(trigger) 역할을 합니다.

- `BEFORE`/`AFTER` × `INSERT`/`UPDATE`/`DELETE` = 6가지 시점.
- `OLD.컬럼`(변경 전 값), `NEW.컬럼`(변경 후 값)으로 그 행을 참조.
- 용도: 변경 이력 로깅, 기본값·파생값 자동 채움, 무결성 규칙 강제.
- 눈에 안 보이게 동작하므로 남용하면 원인 추적이 어렵습니다.

### 프로시저와 트랜잭션 - COMMIT/ROLLBACK 없이도 저장되나?

MySQL은 기본이 `autocommit = 1`이라, **프로시저 안의 각 DML은 실행 즉시 자동 커밋**됩니다.
그래서 이 챕터 예제처럼 프로시저에 `COMMIT`을 안 써도 변경은 저장됩니다. 프로시저는
**자기만의 트랜잭션을 새로 열지 않고** 호출한 세션의 문맥에서 실행됩니다.

`COMMIT`/`ROLLBACK`이 필요한 경우 = **여러 DML을 전부 성공 아니면 전부 취소로 묶고 싶을 때**.

```sql
-- (1) 호출 측에서 묶기
SET autocommit = 0;                 -- 또는 START TRANSACTION;
CALL TRANSFER_POINT('A', 'B', 100);
COMMIT;                             -- 문제가 있으면 ROLLBACK;

-- (2) 프로시저 안에서 묶기 + 오류 시 자동 롤백
CREATE PROCEDURE TRANSFER_POINT(...)
BEGIN
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;                   -- 오류를 호출부로 다시 전달
    END;

    START TRANSACTION;
        UPDATE ... ;                -- 출금
        UPDATE ... ;                -- 입금
    COMMIT;
END
```

- **함수(FUNCTION)와 트리거(TRIGGER)에서는 `COMMIT`/`ROLLBACK`/`START TRANSACTION`을
  쓸 수 없습니다**(오류). 트랜잭션 제어는 프로시저에서만 가능합니다.
- 프로시저 안에서 DDL(`CREATE`/`ALTER`/`DROP TABLE` 등)을 실행하면 **암묵적 커밋**이
  일어나 그전 변경까지 확정됩니다.

---

## 0. 실습 준비 - 사본 테이블 + 로그 테이블

```sql
DROP TABLE IF EXISTS EMP_COPY;
CREATE TABLE EMP_COPY AS SELECT * FROM EMP;

DROP TABLE IF EXISTS SALARY_LOG;
CREATE TABLE SALARY_LOG (
    LOG_ID     INT AUTO_INCREMENT PRIMARY KEY,
    EMP_ID     VARCHAR(3),
    OLD_SALARY INT,
    NEW_SALARY INT,
    CHANGED_AT DATETIME DEFAULT NOW()
);
```

이번 챕터는 여러 절이 이어서 같은 `EMP_COPY`의 상태를 바꿔가며 진행합니다. 중간에
다른 예제로 넘어가기 전에 상태를 깨끗하게 되돌리고 싶다면, 위 `EMP_COPY` 재생성
스크립트를 다시 실행하면 됩니다.

---

## 1. PROCEDURE 기본 - IN 파라미터

```sql
DELIMITER $$

CREATE PROCEDURE RAISE_SALARY(IN P_DEPT_ID CHAR(2), IN P_RATE DECIMAL(3,2))
BEGIN
    UPDATE EMP_COPY
    SET SALARY = SALARY * (1 + P_RATE)
    WHERE DEPT_ID = P_DEPT_ID;
END $$

DELIMITER ;

CALL RAISE_SALARY('D8', 0.10);

SELECT EMP_ID, EMP_NAME, SALARY FROM EMP_COPY WHERE DEPT_ID = 'D8' ORDER BY EMP_ID;
```

**출력 결과**
```
210 이광렬 2200000
211 이금빈 2805000
212 오미자 2679864
```

**설명**: `IN` 파라미터는 프로시저를 호출할 때 값을 **넘겨받기만** 합니다.
`CALL RAISE_SALARY('D8', 0.10);`처럼 부서코드와 인상률을 바꿔가며 몇 번이고
재사용할 수 있습니다. 함수(`CREATE FUNCTION`)와 달리 프로시저는 `RETURN` 값이
없고, `SELECT`절 안에서 쓸 수도 없습니다 — 대신 `UPDATE`/`INSERT`/`DELETE`처럼
값을 반환하지 않는 작업의 묶음에 적합합니다.

> 이 프로시저에 `COMMIT`이 없는데도 `UPDATE`가 반영된 이유는 `autocommit=1`(기본)이라
> `UPDATE` 문이 실행 즉시 자동 커밋되기 때문입니다. 여러 DML을 하나로 묶어야 할 때만
> `START TRANSACTION … COMMIT`이 필요합니다 (위 "개념" 절 참고).

---

## 2. OUT 파라미터 - 결과값 반환

```sql
DELIMITER $$

CREATE PROCEDURE GET_DEPT_AVG_SALARY(IN P_DEPT_ID CHAR(2), OUT P_AVG_SALARY INT)
BEGIN
    SELECT ROUND(AVG(SALARY)) INTO P_AVG_SALARY
    FROM EMP_COPY
    WHERE DEPT_ID = P_DEPT_ID;
END $$

DELIMITER ;

CALL GET_DEPT_AVG_SALARY('D9', @avg_sal);
SELECT @avg_sal;
```

**출력 결과**
```
5900000
```

**설명**: `OUT` 파라미터는 프로시저 **안에서 계산한 값을 호출부로 돌려줄 때**
씁니다. `SELECT ... INTO 변수`로 조회 결과를 변수에 담고, 호출할 때는
`@avg_sal`처럼 **사용자 정의 변수**를 넘겨서 결과를 받습니다. 총무부(D9)는 D8 급여
인상과 무관하므로 여전히 5,900,000원입니다.

### MySQL의 변수 세 가지

`@avg_sal`, `DECLARE V_SALARY`, `@@autocommit` 은 생김새도 쓰임도 다릅니다.

| 종류 | 표기 | 범위(살아있는 동안) | 선언 | 대입 | 용도 |
|---|---|---|---|---|---|
| **사용자 정의 변수** | `@이름` | 현재 **세션** 전체 (접속이 끊기면 소멸) | 필요 없음 (첫 사용이 곧 생성, 타입 자동) | `SET @x = 10;` / `SELECT ... INTO @x` / `SELECT @x := 10` | 프로시저 `OUT` 값 받기, 문장과 문장 사이에 값 전달 |
| **지역 변수** | `이름` (`@` 없음) | `BEGIN … END` 블록 안에서만 | `DECLARE V_X INT [DEFAULT ...];` (블록 맨 위) | `SET V_X = 10;` / `SELECT ... INTO V_X` | 프로시저·함수 본문 안 계산용 |
| **시스템 변수** | `@@이름` | 서버 설정값 (`SESSION` 또는 `GLOBAL`) | (서버가 제공) | `SET @@autocommit = 0;` / `SET SESSION ...` / `SET GLOBAL ...` | DB 엔진 동작 제어 (`8. TCL`의 `sql_safe_updates`, `10. INDEX`의 `cte_max_recursion_depth` 등) |

```sql
-- 사용자 정의 변수: 선언 없이 바로
SET @cnt = 0;
SELECT COUNT(*) INTO @cnt FROM EMP_COPY;
SELECT @cnt;                          -- 42

-- 세션이 끝나면 사라짐. 다른 접속에서는 안 보임.
-- 프로시저 밖에서 OUT 값을 받는 "그릇"으로 주로 사용:
CALL GET_DEPT_AVG_SALARY('D9', @avg_sal);
SELECT @avg_sal;
```

- 지역 변수와 이름이 같아 헷갈리면, `@`가 붙었는지로 구분하세요. `@x`(세션)와 `x`(블록
  지역)는 **서로 다른 변수**입니다.
- 사용자 정의 변수는 타입을 미리 못 정하고, `SELECT`문 여러 곳에서 값을 바꾸면 평가
  순서가 보장되지 않으니 계산 로직엔 지역 변수를 쓰는 편이 안전합니다.

---

## 3. IF 제어문

```sql
DELIMITER $$

CREATE PROCEDURE CLASSIFY_SALARY(IN P_EMP_ID VARCHAR(3), OUT P_GRADE VARCHAR(10))
BEGIN
    DECLARE V_SALARY INT;

    SELECT SALARY INTO V_SALARY FROM EMP_COPY WHERE EMP_ID = P_EMP_ID;

    IF V_SALARY >= 5000000 THEN
        SET P_GRADE = '고액';
    ELSEIF V_SALARY >= 2500000 THEN
        SET P_GRADE = '중액';
    ELSE
        SET P_GRADE = '소액';
    END IF;
END $$

DELIMITER ;

CALL CLASSIFY_SALARY('200', @grade);
SELECT @grade;
```

**출력 결과**
```
고액
```

**설명**: 프로시저 본문 안의 `IF ... ELSEIF ... ELSE ... END IF;`는 함수 챕터의
`CASE`/`IF()` **표현식**과 달리, 값을 반환하지 않고 여러 문장(`SET`, `INSERT` 등)을
실행하는 **제어문**입니다. 곽상혁(200)의 급여 8,000,000원은 500만원 이상이므로
`'고액'`이 반환됩니다.

---

## 4. 반복문(LOOP / WHILE)과 커서(CURSOR)

### 4.1 반복문 먼저 - 구구단

커서로 넘어가기 전에, **반복문 자체**를 연습합니다. MySQL 저장 프로그램의 반복문은
세 가지입니다.

| 반복문 | 종료 조건 | 특징 |
|---|---|---|
| `WHILE 조건 DO ... END WHILE` | 조건이 거짓이면 종료 | 조건을 **먼저** 검사 |
| `REPEAT ... UNTIL 조건 END REPEAT` | 조건이 참이면 종료 | 조건을 **끝에서** 검사 (최소 1회 실행) |
| `LOOP ... END LOOP` | **없음** | `IF ... THEN LEAVE 레이블` 로 직접 빠져나와야 함 |

**(a) `WHILE` 로 한 단 출력** — 결과를 문자열로 조립해 반환

```sql
DELIMITER $$

CREATE PROCEDURE GUGUDAN(IN P_DAN INT)
BEGIN
    DECLARE I INT DEFAULT 1;
    DECLARE MSG TEXT DEFAULT '';

    WHILE I <= 9 DO
        SET MSG = CONCAT(MSG, P_DAN, ' x ', I, ' = ', P_DAN * I, '\n');
        SET I = I + 1;
    END WHILE;

    SELECT MSG AS 구구단;
END $$

DELIMITER ;

CALL GUGUDAN(7);
```

**출력 결과**
```
7 x 1 = 7
7 x 2 = 14
...
7 x 9 = 63
```

**(b) 같은 것을 `LOOP` + `IF` + `LEAVE` 로** — `LOOP` 는 종료 조건이 없어 `IF` 로 검사해
`LEAVE` 해야 합니다(안 하면 무한 루프).

```sql
DELIMITER $$

CREATE PROCEDURE GUGUDAN_LOOP(IN P_DAN INT)
BEGIN
    DECLARE I INT DEFAULT 1;
    DECLARE MSG TEXT DEFAULT '';

    DAN_LOOP: LOOP
        IF I > 9 THEN                         -- 종료 조건을 직접 검사
            LEAVE DAN_LOOP;                   -- DAN_LOOP 레이블이 붙은 반복문을 빠져나감
        END IF;

        SET MSG = CONCAT(MSG, P_DAN, ' x ', I, ' = ', P_DAN * I, '\n');
        SET I = I + 1;
    END LOOP;

    SELECT MSG AS 구구단;
END $$

DELIMITER ;

CALL GUGUDAN_LOOP(7);
```

- `LEAVE 레이블` = 그 반복문 탈출(다른 언어의 `break`).
- `ITERATE 레이블` = 이번 회차를 건너뛰고 다음 반복으로(다른 언어의 `continue`).
- 둘 다 **레이블이 반드시 필요**합니다. MySQL 에는 `break`/`continue` 키워드가 없습니다.

**(c) 중첩 반복 - 2~9단 전체** — 바깥 `LOOP`(단) 안에 안쪽 `WHILE`(1~9)

```sql
DELIMITER $$

CREATE PROCEDURE GUGUDAN_ALL()
BEGIN
    DECLARE DAN INT DEFAULT 2;

    DROP TEMPORARY TABLE IF EXISTS TMP_GUGU;
    CREATE TEMPORARY TABLE TMP_GUGU (DAN INT, I INT, RESULT INT);

    DAN_LOOP: LOOP
        IF DAN > 9 THEN LEAVE DAN_LOOP; END IF;

        BEGIN                                    -- 안쪽 블록: I 를 단마다 새로 선언(1로 초기화)
            DECLARE I INT DEFAULT 1;
            WHILE I <= 9 DO
                INSERT INTO TMP_GUGU VALUES (DAN, I, DAN * I);
                SET I = I + 1;
            END WHILE;
        END;

        SET DAN = DAN + 1;
    END LOOP;

    SELECT * FROM TMP_GUGU ORDER BY I, DAN;       -- I 먼저 정렬하면 가로줄로 읽힘
END $$

DELIMITER ;

CALL GUGUDAN_ALL();
```

> 이 절은 **반복문 문법**(`WHILE`/`LOOP`/`LEAVE`/`ITERATE`/중첩) 연습입니다. 아래 커서는
> "반복문 + `FETCH` 로 테이블의 행을 도는" 응용입니다.

### 커서란

`SELECT` 결과가 여러 행일 때, 그 결과를 **한 행씩 순서대로 짚어가며 처리**하는
"읽기 포인터"입니다.

- 보통의 SQL은 `UPDATE ... WHERE ...`처럼 **집합 전체를 한 번에** 처리합니다(그게 빠릅니다).
- 하지만 "행마다 값을 꺼내 조건별로 다른 프로시저를 호출한다", "행마다 계산한 결과를
  다른 형태로 쌓는다"처럼 **행 단위 절차적 처리**가 필요할 때 커서를 씁니다.
- 느리므로 **한 문장(SQL)으로 안 될 때만** 사용합니다. 이 예제(`FLAG_LOW_SALARY`)도
  사실은 `INSERT INTO SALARY_LOG SELECT ... FROM EMP_COPY WHERE SALARY < 2000000` 한
  줄로 됩니다 — 커서 문법을 익히기 위한 예시입니다.

### 커서 사용 5단계

```sql
-- ① 선언 (DECLARE 순서 규칙: 변수 → 커서 → 핸들러)
DECLARE V_DONE INT DEFAULT 0;                             -- 종료 감지용 플래그
DECLARE V_EMP_ID VARCHAR(3);                              -- FETCH한 값을 담을 변수 (SELECT 컬럼 수와 개수·순서 일치)
DECLARE V_SALARY INT;
DECLARE CUR CURSOR FOR SELECT EMP_ID, SALARY FROM EMP_COPY;   -- 순회할 결과 집합을 정의
DECLARE CONTINUE HANDLER FOR NOT FOUND SET V_DONE = 1;    -- ② 더 꺼낼 행이 없을 때(NOT FOUND) 플래그를 1로

OPEN CUR;                                                 -- ③ 커서 열기 (여기서 SELECT가 실행됨)

READ_LOOP: LOOP
    FETCH CUR INTO V_EMP_ID, V_SALARY;                    -- ④ 다음 한 행을 꺼내 변수에 대입
    IF V_DONE = 1 THEN LEAVE READ_LOOP; END IF;           --    행이 없으면 반복 종료
    -- ... V_EMP_ID, V_SALARY 로 원하는 처리 ...
END LOOP;

CLOSE CUR;                                                -- ⑤ 커서 닫기 (리소스 해제)
```

| 단계 | 키워드 | 하는 일 |
|---|---|---|
| ① 선언 | `DECLARE ... CURSOR FOR <SELECT>` | 어떤 결과 집합을 순회할지 이름을 붙여 정의. **아직 실행 안 함** |
| ② 종료 처리 | `DECLARE CONTINUE HANDLER FOR NOT FOUND` | `FETCH`가 마지막 행 다음을 읽으면 `NOT FOUND` 발생 → 플래그를 세팅. **이게 없으면 오류(1329)로 죽음** |
| ③ 열기 | `OPEN 커서명` | 정의된 `SELECT`를 실제로 실행해 첫 행 앞에 포인터를 놓음 |
| ④ 꺼내기 | `FETCH 커서명 INTO 변수들` | 현재 행의 값을 변수에 담고 포인터를 다음 행으로. 루프 안에서 반복 |
| ⑤ 닫기 | `CLOSE 커서명` | 커서 리소스 해제 (프로시저가 끝나면 자동으로 닫히지만 명시하는 게 관례) |

> **선언 순서**: MySQL은 프로시저 본문에서 `DECLARE 변수` → `DECLARE ... CURSOR` →
> `DECLARE ... HANDLER` 순서를 요구합니다. 순서가 틀리면 정의 자체가 오류입니다.

```sql
-- 0절의 EMP_COPY 재생성 스크립트를 다시 실행해 상태를 초기화한 뒤 진행합니다.

DELIMITER $$

CREATE PROCEDURE FLAG_LOW_SALARY()
BEGIN
    DECLARE V_DONE INT DEFAULT 0;
    DECLARE V_EMP_ID VARCHAR(3);
    DECLARE V_SALARY INT;

    DECLARE CUR CURSOR FOR
        SELECT EMP_ID, SALARY FROM EMP_COPY;
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET V_DONE = 1;

    OPEN CUR;

    READ_LOOP: LOOP
        FETCH CUR INTO V_EMP_ID, V_SALARY;
        IF V_DONE = 1 THEN
            LEAVE READ_LOOP;
        END IF;

        IF V_SALARY < 2000000 THEN
            INSERT INTO SALARY_LOG (EMP_ID, OLD_SALARY, NEW_SALARY)
            VALUES (V_EMP_ID, V_SALARY, V_SALARY);
        END IF;
    END LOOP;

    CLOSE CUR;
END $$

DELIMITER ;

CALL FLAG_LOW_SALARY();

SELECT EMP_ID, OLD_SALARY FROM SALARY_LOG ORDER BY EMP_ID;
```

**출력 결과**
```
209 1800000
215 1380000
217 1550000
```

**설명**: **커서(CURSOR)**는 `SELECT` 결과를 한 행씩 순회할 수 있게 해줍니다.
`DECLARE CONTINUE HANDLER FOR NOT FOUND SET V_DONE = 1;`은 "더 이상 가져올 행이
없으면(`NOT FOUND`) `V_DONE`을 1로 만들고 계속 진행하라"는 예외 처리이며,
`LOOP`~`END LOOP;` 안에서 매번 `V_DONE`을 확인해 반복을 끝냅니다(`LEAVE`는
반복문을 빠져나가는 명령입니다). 급여 200만원 미만인 최주호(209)·한재헌(215)·
심재호(217) 세 명만 조건을 만족해 `SALARY_LOG`에 기록됩니다.

---

## 5. 예외 처리 - HANDLER

프로시저 실행 중 오류가 나면 기본적으로 **그 자리에서 멈추고 호출부로 오류가 전달**됩니다.
`DECLARE ... HANDLER` 는 "이런 상황이 생기면 이렇게 대응하라"를 미리 등록해 두는 장치입니다.

### 5.1 문법

```sql
DECLARE  {CONTINUE | EXIT}  HANDLER
FOR      <조건>[, <조건> ...]
         <동작>;                       -- 문장 1개, 또는 BEGIN ... END 블록
```

**① 동작 방식 (`CONTINUE` vs `EXIT`)**

| | 핸들러 실행 후 |
|---|---|
| `CONTINUE HANDLER` | 오류가 난 문장 **다음 문장부터 계속** 실행 |
| `EXIT HANDLER` | 핸들러가 선언된 `BEGIN ... END` 블록을 **즉시 빠져나감** (그 블록이 프로시저 본문이면 프로시저 종료) |

- 커서를 **끝까지 돌려야** 하면 `CONTINUE` (4절 — `NOT FOUND` 나도 루프는 계속).
- 오류가 나면 **더 진행하면 안 되는** 작업이면 `EXIT` (이체·정산 도중 실패 → 중단하고 정리).

**② 조건 (`FOR` 뒤)** — 어떤 상황에 반응할지

| 조건 | 언제 |
|---|---|
| `SQLEXCEPTION` | SQLSTATE 가 `00`(성공)·`01`(경고)·`02`(NOT FOUND) 로 시작하지 **않는** 모든 오류 = 사실상 "진짜 오류 전부" |
| `NOT FOUND` | 커서가 마지막 행 다음을 `FETCH` 하거나, `SELECT ... INTO` 결과가 **0행**일 때 (SQLSTATE `02000`) |
| `SQLWARNING` | 경고(SQLSTATE `01`) |
| `SQLSTATE '23000'` | 특정 SQLSTATE 지정 (`23000` = 제약조건 위반: PK/UNIQUE/FK/NOT NULL) |
| `1062` | 특정 **MySQL 오류번호** 지정 (`1062` = 중복 키) |
| 이름 붙인 조건 | `DECLARE dup CONDITION FOR SQLSTATE '23000';` 로 이름을 만들어 `FOR dup` |

**③ 선언 위치** — `BEGIN` 바로 다음, `DECLARE 변수` → `DECLARE 커서` → `DECLARE 핸들러` 순.

> 어떤 SQLSTATE·오류번호가 있는지, `SIGNAL`/`RESIGNAL`/`GET DIAGNOSTICS` 사용법은
> [SQLSTATE_참고표.md](SQLSTATE_참고표.md) 참고.

### 5.2 예제 1 - NOT FOUND 를 EXIT 로 처리 (안전한 조회)

```sql
DELIMITER $$

CREATE PROCEDURE GET_SALARY_SAFE(IN P_EMP_ID VARCHAR(3), OUT P_SALARY INT)
BEGIN
    DECLARE EXIT HANDLER FOR NOT FOUND
        SET P_SALARY = -1;                              -- 결과가 없으면 -1 을 돌려주고 프로시저 종료

    SELECT SALARY INTO P_SALARY FROM EMP_COPY WHERE EMP_ID = P_EMP_ID;
END $$

DELIMITER ;

CALL GET_SALARY_SAFE('999', @sal);
SELECT @sal;
```

**출력 결과**
```
-1
```

**설명**: `SELECT ... INTO` 는 결과가 0행이면 `NOT FOUND`(SQLSTATE `02000`) 가 됩니다.
핸들러가 없으면 그대로 오류가 나지만, `EXIT HANDLER FOR NOT FOUND` 를 걸어 두면 존재하지
않는 사번(`'999'`)을 조회해도 죽지 않고 `-1` 을 돌려주고 끝냅니다. 여기서는 뒤에 실행할
문장이 없으므로 `EXIT` 든 `CONTINUE` 든 결과는 같지만, "이후 로직을 진행하면 안 된다"는
의도를 드러내려 `EXIT` 를 씁니다.

### 5.3 예제 2 - SQLEXCEPTION + 트랜잭션 롤백 (실무 패턴)

여러 DML 을 묶을 때, 도중에 오류가 나면 **전부 취소**해야 합니다.

```sql
DELIMITER $$

CREATE PROCEDURE TRANSFER_SALARY(IN P_FROM VARCHAR(3), IN P_TO VARCHAR(3), IN P_AMT INT)
BEGIN
    -- 어떤 오류든(SQLEXCEPTION) 나면: 롤백하고, 오류를 호출부로 다시 던지고, 종료
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;                                       -- 원래 오류를 그대로 호출부에 전달
    END;

    START TRANSACTION;
        UPDATE EMP_COPY SET SALARY = SALARY - P_AMT WHERE EMP_ID = P_FROM;   -- 출금
        UPDATE EMP_COPY SET SALARY = SALARY + P_AMT WHERE EMP_ID = P_TO;     -- 입금
        -- 여기서 CHECK 위반·데드락 등 어떤 오류가 나도 위 핸들러가 ROLLBACK
    COMMIT;
END $$

DELIMITER ;
```

**설명**:
- 핸들러 동작이 **두 문장 이상**(`ROLLBACK;` + `RESIGNAL;`)이라 `BEGIN ... END` 블록으로 감쌌습니다.
- `EXIT` 라서 오류 발생 즉시 `COMMIT` 을 건너뛰고 프로시저를 빠져나갑니다.
- `RESIGNAL` 은 삼켰던 오류를 **다시 발생**시켜 호출부(앱)가 실패를 알 수 있게 합니다. 이게
  없으면 "롤백은 됐는데 호출부는 성공한 줄 아는" 상태가 됩니다.
- 함수·트리거에서는 `START TRANSACTION`/`COMMIT`/`ROLLBACK` 이 금지라 이 패턴은 **프로시저 전용**입니다.

### 5.4 CONTINUE 로 특정 오류만 무시

```sql
-- 중복 키(1062)면 무시하고 계속 (있으면 넘어가는 "insert if not exists" 흉내)
DECLARE CONTINUE HANDLER FOR 1062 BEGIN END;

INSERT INTO SALARY_LOG (EMP_ID, OLD_SALARY, NEW_SALARY) VALUES ('200', 0, 0);
-- 위가 중복이면 아무 일 없이 다음 문장으로
```

> 핸들러가 넓을수록(`SQLEXCEPTION` 전체를 `CONTINUE` 로) 진짜 버그까지 삼켜 원인 추적이
> 어려워집니다. **꼭 예상되는 조건만 좁게** 잡고, `EXIT` + `RESIGNAL` 을 기본으로 삼으세요.

---

## 6. DROP PROCEDURE

```sql
DROP PROCEDURE IF EXISTS RAISE_SALARY;
DROP PROCEDURE IF EXISTS GET_DEPT_AVG_SALARY;
DROP PROCEDURE IF EXISTS CLASSIFY_SALARY;
DROP PROCEDURE IF EXISTS FLAG_LOW_SALARY;
DROP PROCEDURE IF EXISTS GET_SALARY_SAFE;
```

---

## 7. TRIGGER 기본 - AFTER UPDATE

```sql
-- 0절의 EMP_COPY 재생성 스크립트를 다시 실행해 상태를 초기화한 뒤 진행합니다.
DELETE FROM SALARY_LOG;

DELIMITER $$

CREATE TRIGGER TRG_EMP_SALARY_LOG
AFTER UPDATE ON EMP_COPY
FOR EACH ROW
BEGIN
    IF OLD.SALARY <> NEW.SALARY THEN
        INSERT INTO SALARY_LOG (EMP_ID, OLD_SALARY, NEW_SALARY)
        VALUES (NEW.EMP_ID, OLD.SALARY, NEW.SALARY);
    END IF;
END $$

DELIMITER ;

UPDATE EMP_COPY SET SALARY = SALARY + 200000 WHERE EMP_ID = '209';

SELECT EMP_ID, OLD_SALARY, NEW_SALARY FROM SALARY_LOG WHERE EMP_ID = '209';
```

**출력 결과**
```
209 1800000 2000000
```

**설명**: `AFTER UPDATE` 트리거는 `UPDATE`가 **성공적으로 반영된 직후** 자동으로
실행됩니다. `OLD.컬럼`은 변경 전 값, `NEW.컬럼`은 변경 후 값을 가리킵니다. 실제로
급여가 바뀐 경우에만(`OLD.SALARY <> NEW.SALARY`) 로그를 남기도록 조건을 걸었는데,
이 조건이 없으면 급여와 무관한 컬럼만 바뀐 `UPDATE`에도 매번 로그가 쌓이게 됩니다.

---

## 8. TRIGGER - BEFORE INSERT

```sql
DELIMITER $$

CREATE TRIGGER TRG_EMP_DEFAULT_ENTYN
BEFORE INSERT ON EMP_COPY
FOR EACH ROW
BEGIN
    IF NEW.ENT_YN IS NULL THEN
        SET NEW.ENT_YN = 'N';
    END IF;
END $$

DELIMITER ;

INSERT INTO EMP_COPY (EMP_ID, EMP_NAME, DEPT_ID, JOB_CODE, SALARY, HIRE_DATE, ENT_YN)
VALUES ('221', '테스트', 'D9', 'J7', 2000000, '2026-08-19', NULL);

SELECT EMP_ID, EMP_NAME, ENT_YN FROM EMP_COPY WHERE EMP_ID = '221';
```

**출력 결과**
```
221 테스트 N
```

**설명**: `BEFORE INSERT` 트리거는 실제로 행이 저장되기 **전**에 실행되므로,
`SET NEW.컬럼 = 값`으로 저장될 값 자체를 바꿔치기할 수 있습니다(`AFTER` 트리거에서는
`NEW` 값을 바꿔도 이미 저장이 끝난 뒤라 반영되지 않습니다). `ENT_YN`을 명시하지
않고 `INSERT`해도, 트리거가 자동으로 `'N'`을 채워 넣습니다.

---

## 9. 트리거의 무한 루프 위험

```sql
-- 절대 실행하지 마세요 (무한 루프 예시, 주석 처리된 상태로만 확인)
-- CREATE TRIGGER TRG_INFINITE
-- AFTER UPDATE ON EMP_COPY
-- FOR EACH ROW
-- BEGIN
--     UPDATE EMP_COPY SET SALARY = SALARY WHERE EMP_ID = NEW.EMP_ID;
-- END;
```

**설명**: `AFTER UPDATE ON EMP_COPY` 트리거 본문 안에서 다시 `EMP_COPY`를
`UPDATE`하면, 그 `UPDATE`가 같은 트리거를 다시 실행시키고, 그 트리거가 또
`UPDATE`를 실행하는 **무한 루프**에 빠집니다(MySQL은 기본적으로 트리거 재귀 호출
깊이를 제한하지만, 결국 `Trigger recursion too deep` 오류로 죽습니다). 트리거
안에서 로그를 남기고 싶다면 **자기 자신이 아닌 다른 테이블**(7절의 `SALARY_LOG`
처럼)에 기록해야 합니다.

---

## 10. DROP TRIGGER

```sql
DROP TRIGGER IF EXISTS TRG_EMP_SALARY_LOG;
DROP TRIGGER IF EXISTS TRG_EMP_DEFAULT_ENTYN;
```

---

## 자주 하는 실수

- **`DELIMITER`를 안 바꾸고 프로시저/트리거 본문 작성** → 본문 안 세미콜론에서
  정의가 끊겨 오류가 납니다.
- **`CONTINUE HANDLER`와 `EXIT HANDLER`를 혼동** → 커서로 끝까지 순회해야 하면
  `CONTINUE`, 예외 발생 즉시 중단하고 안전한 값을 돌려주려면 `EXIT`를 씁니다.
- **`SQLEXCEPTION` 전체를 `CONTINUE` 로 삼킴** → 진짜 버그까지 조용히 무시돼 원인 추적
  불가. 예상되는 조건만 좁게 잡습니다.
- **롤백 핸들러에서 `RESIGNAL` 을 빠뜨림** → 롤백은 됐는데 호출부(앱)는 성공한 줄 앎.
  `EXIT HANDLER FOR SQLEXCEPTION BEGIN ROLLBACK; RESIGNAL; END;` 형태로.
- **핸들러 동작이 여러 문장인데 `BEGIN ... END` 로 안 감쌈** → 문법 오류. 한 문장이면
  블록 없이도 됩니다.
- **커서를 `OPEN`만 하고 `CLOSE`를 안 함** → 리소스가 계속 열린 채로 남습니다.
- **트리거 안에서 자기 테이블을 다시 `UPDATE`/`INSERT`** → 무한 루프(9절).
- **`AFTER` 트리거에서 `NEW` 값을 바꾸면 반영될 거라 착각** → `BEFORE`
  트리거에서만 `NEW` 값 변경이 실제 저장값에 반영됩니다.
- **프로시저의 `OUT` 파라미터에 값을 채우지 않고 끝냄** → 호출부에서 `NULL`을
  받게 됩니다.
- **프로시저에 `COMMIT`이 없어서 저장이 안 될까 걱정** → `autocommit=1`(기본)이면 각
  DML이 자동 커밋됩니다. 여러 DML을 묶을 때만 트랜잭션 제어가 필요합니다.
- **함수·트리거 안에 `COMMIT`/`ROLLBACK`을 씀** → 오류. 트랜잭션 제어는 프로시저에서만.
- **지역 변수 `x`와 사용자 정의 변수 `@x`를 같은 것으로 착각** → `@`가 붙으면 세션
  변수, 안 붙으면 블록 지역 변수로 서로 다릅니다.

---

## 핵심 요약

| 항목 | 핵심 내용 |
|---|---|
| 프로시저 | 여러 SQL·제어 로직을 이름으로 묶어 저장, `CALL`로 실행. 값 반환 없음 |
| 트리거 | 테이블의 `INSERT`/`UPDATE`/`DELETE` 시점에 자동 실행되는 프로시저. `OLD`/`NEW` 참조 |
| `CREATE PROCEDURE` | `CALL`로 호출, `RETURN` 없음, `SELECT`절에서 사용 불가 |
| `IN` / `OUT` 파라미터 | `IN`은 값 전달만, `OUT`은 결과를 호출부의 사용자 정의 변수(`@변수`)로 반환 |
| 변수 3종 | `@x`(세션 사용자 변수) / `DECLARE x`(블록 지역 변수) / `@@x`(시스템 변수) |
| 프로시저와 트랜잭션 | `autocommit=1`이면 각 DML 자동 커밋. 묶으려면 `START TRANSACTION … COMMIT`(+오류 시 `ROLLBACK` 핸들러). 함수·트리거는 트랜잭션 제어 불가 |
| `IF ... ELSEIF ... END IF` | 값이 아닌 여러 문장을 실행하는 분기문 |
| 반복문 | `WHILE 조건 DO`(선검사) / `REPEAT ... UNTIL`(후검사) / `LOOP`(조건 없음, `IF+LEAVE` 필수). `LEAVE 레이블`=break, `ITERATE 레이블`=continue (레이블 필수) |
| 커서(CURSOR) | `SELECT` 결과를 한 행씩 순회하는 포인터. `DECLARE`(변수→커서→핸들러) → `OPEN` → `FETCH ... INTO` → `CLOSE` 5단계. `NOT FOUND` 핸들러 필수. 한 문장으로 되면 커서 안 씀 |
| `DECLARE ... HANDLER FOR 조건 동작` | 동작: `CONTINUE`(다음 문장부터 계속) / `EXIT`(선언된 블록 즉시 종료). 조건: `SQLEXCEPTION`·`NOT FOUND`·`SQLWARNING`·특정 `SQLSTATE`·에러번호. 여러 문장이면 동작을 `BEGIN..END` 로 |
| 트랜잭션 안전 패턴 | `DECLARE EXIT HANDLER FOR SQLEXCEPTION BEGIN ROLLBACK; RESIGNAL; END;` (프로시저 전용) |
| SQLSTATE·오류번호 | 목록·`SIGNAL`/`RESIGNAL`/`GET DIAGNOSTICS` → [SQLSTATE_참고표.md](SQLSTATE_참고표.md) |
| `CREATE TRIGGER` | `BEFORE`/`AFTER` × `INSERT`/`UPDATE`/`DELETE`, `OLD`/`NEW`로 값 참조 |
| `BEFORE` 트리거 | `NEW` 값을 바꿔 실제 저장값을 변경 가능 |
| 트리거 무한 루프 | 트리거 안에서 자기 테이블을 다시 변경하면 발생. 다른 테이블에 기록해서 회피 |
| `DROP PROCEDURE`/`DROP TRIGGER` | 정리 |
