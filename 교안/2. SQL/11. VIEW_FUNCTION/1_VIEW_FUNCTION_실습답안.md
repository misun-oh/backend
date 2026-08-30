# Day 16. VIEW & 사용자정의 FUNCTION - 실습 답안

---

## 기본

### 문제 1. 단순 뷰

```sql
CREATE VIEW V_D2_EMP AS
SELECT EMP_NAME, SALARY
FROM EMP_COPY
WHERE DEPT_ID = 'D2';

SELECT * FROM V_D2_EMP ORDER BY SALARY DESC;
```

**출력 결과**
```
박홍주 2800000
엄용민 2490000
심재호 1550000
```

**설명**: `JOIN`/집계 함수/`GROUP BY`가 없는 단순 뷰이므로, 이 뷰를 통해
`INSERT`/`UPDATE`도 가능합니다(6절 문제에서 직접 확인).

---

### 문제 2. 사용자정의 함수 - 급여 등급

```sql
DELIMITER $$

CREATE FUNCTION GET_GRADE(P_SALARY INT)
RETURNS VARCHAR(10)
DETERMINISTIC
BEGIN
    DECLARE V_GRADE VARCHAR(10);

    IF P_SALARY >= 5000000 THEN
        SET V_GRADE = '고액';
    ELSEIF P_SALARY >= 2500000 THEN
        SET V_GRADE = '중액';
    ELSE
        SET V_GRADE = '소액';
    END IF;

    RETURN V_GRADE;
END $$

DELIMITER ;

SELECT EMP_NAME, SALARY, GET_GRADE(SALARY) AS 등급
FROM EMP_COPY
WHERE DEPT_ID = 'D6';
```

**출력 결과**
```
김은민 3400000 중액
김태일 3900000 중액
```

**설명**: 함수 본문 안에서는 `DECLARE`로 지역 변수를 선언하고 `IF ... ELSEIF ... ELSE
... END IF;`로 분기할 수 있습니다. 함수 챕터의 `CASE`/`IF()` 표현식과 달리, 여기서는
`RETURN` 전에 값을 계산하는 절차형 로직을 자유롭게 쓸 수 있습니다.

---

## 응용

### 문제 3. 복합 뷰 - 부서별 요약

```sql
CREATE VIEW V_DEPT_SUMMARY AS
SELECT D.DEPT_ID, D.DEPT_TITLE, COUNT(*) AS 인원수, SUM(E.SALARY) AS 급여합계
FROM EMP_COPY E
JOIN DEPT_COPY D ON E.DEPT_ID = D.DEPT_ID
GROUP BY D.DEPT_ID, D.DEPT_TITLE;

SELECT DEPT_TITLE, 인원수, 급여합계
FROM V_DEPT_SUMMARY
WHERE 인원수 >= 3
ORDER BY 급여합계 DESC;
```

**출력 결과**
```
총무부      3 17700000
해외영업1부 5 13760000
인사관리부  3 7820000
기술지원부  3 6986240
회계관리부  3 6840000
```

**설명**: 해외영업2부(D6)는 인원수가 2명이라 조건(`인원수 >= 3`)에서 제외됩니다.
이 뷰는 `JOIN`+`GROUP BY`를 포함하므로 읽기 전용(갱신 불가능) 뷰입니다.

---

### 문제 4. WITH CHECK OPTION 확인

```sql
CREATE VIEW V_D5_EMP AS
SELECT EMP_ID, EMP_NAME, DEPT_ID, SALARY
FROM EMP_COPY
WHERE DEPT_ID = 'D5'
WITH CHECK OPTION;

UPDATE V_D5_EMP SET SALARY = 3000000 WHERE EMP_ID = '211';
```

**출력 결과**
```
오류: CHECK OPTION failed 'V_D5_EMP'
```

**설명**: 이금빈(211)은 `DEPT_ID`가 `'D8'`이라 애초에 `V_D5_EMP`(`WHERE DEPT_ID =
'D5'`)의 조회 결과에 존재하지 않는 행입니다. `WITH CHECK OPTION`이 걸린 뷰를 통한
`UPDATE`는 "변경 후에도 뷰의 `WHERE` 조건을 만족하는 행"만 대상으로 삼을 수 있는데,
애초에 조건에 맞지 않는 행을 지정했으므로 실행 자체가 거부됩니다.

---

### 문제 5. 사용자정의 함수 - 근속연수

```sql
DELIMITER $$

CREATE FUNCTION GET_TENURE_YEARS(P_HIRE_DATE DATE)
RETURNS INT
DETERMINISTIC
BEGIN
    RETURN TIMESTAMPDIFF(YEAR, P_HIRE_DATE, '2026-08-19');
END $$

DELIMITER ;

SELECT EMP_NAME, GET_TENURE_YEARS(HIRE_DATE) AS 근속연수
FROM EMP_COPY
WHERE DEPT_ID = 'D1';
```

**출력 결과**
```
이다현 5
전태성 5
한재헌 2
```

**설명**: 함수 안에서 `TIMESTAMPDIFF`처럼 이미 배운 내장 함수를 그대로 활용할 수
있습니다. 기준일(`'2026-08-19'`)을 함수 안에 고정해두면, 호출부에서는 입사일만
넘기면 되므로 재사용성이 높아집니다.

---

## 도전

### 문제 6. 뷰를 통한 UPDATE 확인

```sql
CREATE VIEW V_D8_EMP AS
SELECT EMP_ID, EMP_NAME, SALARY
FROM EMP_COPY
WHERE DEPT_ID = 'D8';

UPDATE V_D8_EMP SET SALARY = SALARY * 1.1 WHERE EMP_ID = '212';

SELECT EMP_ID, EMP_NAME, SALARY FROM EMP_COPY WHERE EMP_ID = '212';
```

**출력 결과**
```
212 오미자 2679864
```

**설명**: 오미자의 원래 급여 2,436,240원에 10%를 인상하면 2,679,864원입니다. 뷰를
통한 `UPDATE`가 실제로 원본 `EMP_COPY`에 반영된 것을 `EMP_COPY`를 직접 조회해서
확인할 수 있습니다.

---

### 문제 7. 종합 - 함수 + 뷰 + LIMIT

```sql
DELIMITER $$

CREATE FUNCTION GET_ANNUAL_SALARY(P_SALARY INT, P_BONUS DECIMAL(4,2))
RETURNS INT
DETERMINISTIC
BEGIN
    RETURN (P_SALARY + P_SALARY * IFNULL(P_BONUS, 0)) * 12;
END $$

DELIMITER ;

CREATE VIEW V_DEPT_ANNUAL_AVG AS
SELECT D.DEPT_ID, D.DEPT_TITLE,
       ROUND(AVG(GET_ANNUAL_SALARY(E.SALARY, E.BONUS))) AS 평균연봉
FROM EMP_COPY E
JOIN DEPT_COPY D ON E.DEPT_ID = D.DEPT_ID
GROUP BY D.DEPT_ID, D.DEPT_TITLE;

SELECT DEPT_TITLE, 평균연봉
FROM V_DEPT_ANNUAL_AVG
ORDER BY 평균연봉 DESC
LIMIT 1;
```

**출력 결과**
```
총무부 80400000
```

**설명**: 총무부(D9)는 곽상혁(보너스 30%, 연봉 124,800,000원)의 영향으로 평균 연봉
80,400,000원이 되어, 2위인 해외영업2부(D6, 47,880,000원)를 크게 앞섭니다. 사용자정의
함수는 이렇게 뷰의 `SELECT`절이나 `GROUP BY`의 집계 대상 안에서도 일반 내장 함수와
동일하게 사용할 수 있습니다.
