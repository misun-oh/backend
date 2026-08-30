# Day 17. PROCEDURE & TRIGGER - 실습 답안

---

## 기본

### 문제 1. OUT 파라미터

```sql
DELIMITER $$

CREATE PROCEDURE GET_DEPT_COUNT(IN P_DEPT_ID CHAR(2), OUT P_COUNT INT)
BEGIN
    SELECT COUNT(*) INTO P_COUNT
    FROM EMP_COPY
    WHERE DEPT_ID = P_DEPT_ID;
END $$

DELIMITER ;

CALL GET_DEPT_COUNT('D5', @cnt);
SELECT @cnt;
```

**출력 결과**
```
5
```

**설명**: `SELECT COUNT(*) INTO P_COUNT`처럼 집계 함수 결과를 `OUT` 파라미터에 바로
담을 수 있습니다. 해외영업1부(D5)는 5명입니다.

---

### 문제 2. IN 파라미터로 급여 인상

```sql
DELIMITER $$

CREATE PROCEDURE RAISE_SALARY(IN P_DEPT_ID CHAR(2), IN P_RATE DECIMAL(3,2))
BEGIN
    UPDATE EMP_COPY
    SET SALARY = SALARY * (1 + P_RATE)
    WHERE DEPT_ID = P_DEPT_ID;
END $$

DELIMITER ;

CALL RAISE_SALARY('D1', 0.05);

SELECT EMP_NAME, SALARY FROM EMP_COPY WHERE DEPT_ID = 'D1' ORDER BY EMP_ID;
```

**출력 결과**
```
이다현 2919000
전태성 3843000
한재헌 1449000
```

**설명**: 이다현 2,780,000 × 1.05 = 2,919,000, 전태성 3,660,000 × 1.05 = 3,843,000,
한재헌 1,380,000 × 1.05 = 1,449,000원입니다.

---

## 응용

### 문제 3. IF 제어문

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

CALL CLASSIFY_SALARY('208', @grade);
SELECT @grade;
```

**출력 결과**
```
중액
```

**설명**: 윤정주(208)의 급여 3,760,000원은 250만원 이상 500만원 미만이므로
`'중액'`입니다.

---

### 문제 3-1. 반복문(WHILE / LOOP) - 구구단

```sql
DELIMITER $$

CREATE PROCEDURE PRINT_DAN(IN P_DAN INT, OUT P_RESULT TEXT)
BEGIN
    DECLARE I INT DEFAULT 1;
    SET P_RESULT = '';
    WHILE I <= 9 DO                                   -- 조건을 먼저 검사
        SET P_RESULT = CONCAT(P_RESULT, P_DAN, ' x ', I, ' = ', P_DAN * I,
                              IF(I < 9, '\n', ''));
        SET I = I + 1;                                -- 빠뜨리면 무한 루프
    END WHILE;
END $$

CREATE PROCEDURE PRINT_DAN_LOOP(IN P_DAN INT, OUT P_RESULT TEXT)
BEGIN
    DECLARE I INT DEFAULT 1;
    SET P_RESULT = '';
    DAN_LOOP: LOOP                                    -- 레이블 필요 (종료 조건이 문법에 없음)
        IF I > 9 THEN LEAVE DAN_LOOP; END IF;         -- 직접 검사해서 LEAVE
        SET P_RESULT = CONCAT(P_RESULT, P_DAN, ' x ', I, ' = ', P_DAN * I,
                              IF(I < 9, '\n', ''));
        SET I = I + 1;
    END LOOP;
END $$

DELIMITER ;

CALL PRINT_DAN(3, @r);       SELECT @r;
CALL PRINT_DAN_LOOP(3, @r);  SELECT @r;
```

**출력 결과**
```
3 x 1 = 3
3 x 2 = 6
...
3 x 9 = 27
```

**설명**: `WHILE` 은 조건(`I <= 9`)을 먼저 확인하고 반복하며, `LOOP` 은 종료 조건이 없어
`IF I > 9 THEN LEAVE DAN_LOOP` 로 직접 빠져나와야 합니다(`DAN_LOOP` 은 반복문에 붙인
레이블). 두 방식의 결과는 동일합니다. `SET I = I + 1` 을 빠뜨리면 둘 다 무한 루프입니다.

---

### 문제 4. AFTER UPDATE 트리거

```sql
DELIMITER $$

CREATE TRIGGER TRG_SALARY_LOG
AFTER UPDATE ON EMP_COPY
FOR EACH ROW
BEGIN
    IF OLD.SALARY <> NEW.SALARY THEN
        INSERT INTO SALARY_LOG (EMP_ID, OLD_SALARY, NEW_SALARY)
        VALUES (NEW.EMP_ID, OLD.SALARY, NEW.SALARY);
    END IF;
END $$

DELIMITER ;

UPDATE EMP_COPY SET SALARY = SALARY + 300000 WHERE EMP_ID = '209';

SELECT EMP_ID, OLD_SALARY, NEW_SALARY FROM SALARY_LOG WHERE EMP_ID = '209';
```

**출력 결과**
```
209 1800000 2100000
```

**설명**: 최주호(209)의 원래 급여 1,800,000원에 300,000원을 더한 2,100,000원으로
바뀌는 순간, 트리거가 자동으로 `SALARY_LOG`에 변경 전/후 값을 기록합니다.

---

### 문제 5. 커서로 조건에 맞는 행 세기

```sql
DELIMITER $$

CREATE PROCEDURE SNAPSHOT_HIGH_SALARY()
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

        IF V_SALARY >= 3000000 THEN
            INSERT INTO SALARY_LOG (EMP_ID, OLD_SALARY, NEW_SALARY)
            VALUES (V_EMP_ID, V_SALARY, V_SALARY);
        END IF;
    END LOOP;

    CLOSE CUR;
END $$

DELIMITER ;

CALL SNAPSHOT_HIGH_SALARY();

SELECT COUNT(*) FROM SALARY_LOG;
```

**출력 결과**
```
8
```

**설명**: 급여 300만원 이상인 사원은 곽상혁(800만)·권진우(600만)·김민혜(370만)·
김은민(340만)·김태일(390만)·박지민(350만)·윤정주(376만)·전태성(366만) 8명입니다.
(4번 문제에서 최주호가 이미 210만원으로 바뀌었더라도 300만원 미만이므로 영향 없음.)

---

## 도전

### 문제 6. EXIT HANDLER로 예외 처리

```sql
DELIMITER $$

CREATE PROCEDURE GET_SALARY_SAFE(IN P_EMP_ID VARCHAR(3), OUT P_SALARY INT)
BEGIN
    DECLARE EXIT HANDLER FOR NOT FOUND
        SET P_SALARY = -1;

    SELECT SALARY INTO P_SALARY FROM EMP_COPY WHERE EMP_ID = P_EMP_ID;
END $$

DELIMITER ;

CALL GET_SALARY_SAFE('999', @sal1);
SELECT @sal1;

CALL GET_SALARY_SAFE('200', @sal2);
SELECT @sal2;
```

**출력 결과**
```
'999' 호출 결과: -1
'200' 호출 결과: 8000000
```

**설명**: 사번 `'999'`는 `EMP_COPY`에 존재하지 않으므로 `SELECT ... INTO`가
`NOT FOUND` 상황을 만들고, `EXIT HANDLER`가 개입해 `-1`을 설정한 뒤 프로시저를
즉시 종료합니다. 사번 `'200'`(곽상혁)은 정상적으로 급여 8,000,000원을 반환합니다.

---

### 문제 7. 종합 - BEFORE INSERT 트리거

```sql
DELIMITER $$

CREATE TRIGGER TRG_DEFAULT_ENTYN
BEFORE INSERT ON EMP_COPY
FOR EACH ROW
BEGIN
    IF NEW.ENT_YN IS NULL THEN
        SET NEW.ENT_YN = 'N';
    END IF;
END $$

DELIMITER ;

INSERT INTO EMP_COPY (EMP_ID, EMP_NAME, DEPT_ID, JOB_CODE, SALARY, HIRE_DATE, ENT_YN)
VALUES ('222', '신입사원', 'D9', 'J7', 2500000, '2026-08-19', NULL);

SELECT ENT_YN FROM EMP_COPY WHERE EMP_ID = '222';
```

**출력 결과**
```
N
```

**설명**: `INSERT`문에서 `ENT_YN`을 `NULL`로 넘겼지만, `BEFORE INSERT` 트리거가
실제 저장 직전에 `NEW.ENT_YN`을 `'N'`으로 바꿔치기했기 때문에 최종 저장된 값은
`'N'`입니다.
