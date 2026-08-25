# Day 17. PROCEDURE & TRIGGER

| 항목 | 내용 |
|---|---|
| 선수학습 | DML(`INSERT`/`UPDATE`/`DELETE`), TCL(`COMMIT`/`ROLLBACK`), VIEW & FUNCTION(`DELIMITER`) |
| 이번 챕터 | `CREATE PROCEDURE`(파라미터, 제어문, 커서, 예외 처리), `CREATE TRIGGER` |
| 권장 진행 | 1~2일 |
| DB 기준 | MySQL 8.0 |

## 학습목표
- `CREATE PROCEDURE`로 여러 SQL문을 하나의 이름으로 묶어 `CALL`로 재사용할 수 있다.
- `IN`/`OUT` 파라미터를 구분해서 사용할 수 있다.
- `IF`/`WHILE` 제어문과 커서(`CURSOR`)로 프로시저 안에서 행 단위 반복 처리를 할 수
  있다.
- `DECLARE ... HANDLER`로 예외 상황(`CONTINUE`/`EXIT`)을 처리할 수 있다.
- `CREATE TRIGGER`로 `INSERT`/`UPDATE` 시점에 자동 실행되는 로직을 만들 수 있다.
- 트리거의 무한 루프 위험을 이해하고 피할 수 있다.

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
`@avg_sal`처럼 세션 사용자 변수(`8_TCL`에서 다룬 `@@시스템변수`와는 다른, `@`
하나짜리 사용자 정의 변수)를 넘겨서 결과를 받습니다. 총무부(D9)는 D8 급여
인상과 무관하므로 여전히 5,900,000원입니다.

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

## 4. WHILE / 커서(CURSOR)

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

## 5. 예외 처리 - EXIT HANDLER

```sql
DELIMITER $$

CREATE PROCEDURE GET_SALARY_SAFE(IN P_EMP_ID VARCHAR(3), OUT P_SALARY INT)
BEGIN
    DECLARE EXIT HANDLER FOR NOT FOUND
        SET P_SALARY = -1;

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

**설명**: `EXIT HANDLER`는 예외가 발생하는 즉시 프로시저 실행을 멈추고 핸들러 코드만
실행한 뒤 종료합니다(반대로 4절의 `CONTINUE HANDLER`는 예외 발생 지점 다음 문장부터
계속 실행합니다 - 커서를 끝까지 순회해야 했으므로 `CONTINUE`를 썼습니다).
`SELECT ... INTO`는 결과가 0행이면 `NOT FOUND` 상황이 되므로, 존재하지 않는 사번
(`'999'`)을 조회해도 오류로 죽지 않고 `-1`이라는 안전한 값을 돌려줍니다.

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
- **커서를 `OPEN`만 하고 `CLOSE`를 안 함** → 리소스가 계속 열린 채로 남습니다.
- **트리거 안에서 자기 테이블을 다시 `UPDATE`/`INSERT`** → 무한 루프(9절).
- **`AFTER` 트리거에서 `NEW` 값을 바꾸면 반영될 거라 착각** → `BEFORE`
  트리거에서만 `NEW` 값 변경이 실제 저장값에 반영됩니다.
- **프로시저의 `OUT` 파라미터에 값을 채우지 않고 끝냄** → 호출부에서 `NULL`을
  받게 됩니다.

---

## 핵심 요약

| 항목 | 핵심 내용 |
|---|---|
| `CREATE PROCEDURE` | `CALL`로 호출, `RETURN` 없음, `SELECT`절에서 사용 불가 |
| `IN` / `OUT` 파라미터 | `IN`은 값 전달만, `OUT`은 결과를 호출부 변수(`@변수`)로 반환 |
| `IF ... ELSEIF ... END IF` | 값이 아닌 여러 문장을 실행하는 제어문 |
| `WHILE` / `LOOP` + 커서 | `SELECT` 결과를 한 행씩 순회. `FETCH`로 값을 꺼냄 |
| `DECLARE ... HANDLER` | `CONTINUE`(계속 진행) / `EXIT`(즉시 종료) 두 가지 |
| `CREATE TRIGGER` | `BEFORE`/`AFTER` × `INSERT`/`UPDATE`/`DELETE`, `OLD`/`NEW`로 값 참조 |
| `BEFORE` 트리거 | `NEW` 값을 바꿔 실제 저장값을 변경 가능 |
| 트리거 무한 루프 | 트리거 안에서 자기 테이블을 다시 변경하면 발생. 다른 테이블에 기록해서 회피 |
| `DROP PROCEDURE`/`DROP TRIGGER` | 정리 |
