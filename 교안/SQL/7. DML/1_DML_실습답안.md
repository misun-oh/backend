# Day 12. DML - 실습 답안

---

## 기본

### 문제 1. INSERT - 전체 컬럼

```sql
INSERT INTO EMP_COPY
VALUES ('225', '최유진', '030815-4889900', 'choi_yj@company.com', '01055512345',
        'D3', 'J7', 2400000, NULL, NULL, '2026-04-01', NULL, 'N');
```

**설명**: 컬럼명을 생략했으므로 `CREATE TABLE EMP` 정의 순서(`EMP_ID, EMP_NAME, EMP_NO,
EMAIL, PHONE, DEPT_ID, JOB_CODE, SALARY, BONUS, MANAGER_ID, HIRE_DATE, ENT_DATE,
ENT_YN`) 그대로 13개 값을 나열했습니다. `BONUS`, `MANAGER_ID`, `ENT_DATE`는 값이 없으므로
`NULL`을 명시했습니다.

---

### 문제 2. INSERT - 일부 컬럼, 다중 행

```sql
INSERT INTO EMP_COPY (EMP_ID, EMP_NAME, DEPT_ID, JOB_CODE, SALARY, HIRE_DATE) VALUES
('226', '정다은', 'D4', 'J7', 2350000, '2026-04-02'),
('227', '서준혁', 'D4', 'J6', 2600000, '2026-04-02');
```

**설명**: 21(원본) + 1(225) + 2(226, 227) = 24행이 됩니다. 지정하지 않은 `EMP_NO`,
`EMAIL` 등은 모두 `NULL`로 채워집니다.

---

## 응용

### 문제 3. UPDATE - 여러 행 일괄 수정

```sql
UPDATE EMP_COPY SET SALARY = SALARY * 1.05 WHERE DEPT_ID = 'D5';

SELECT EMP_ID, EMP_NAME, SALARY FROM EMP_COPY WHERE DEPT_ID = 'D5' ORDER BY EMP_ID;
```

**출력 결과**
```
205 박지민 3675000
206 염성원 2310000
207 유제영 2625000
208 윤정주 3948000
209 최주호 1890000
```

**설명**: `WHERE DEPT_ID = 'D5'`에 해당하는 5명(박지민~최주호)의 급여가 모두 5%씩
인상되었습니다. 예를 들어 박지민은 `3,500,000 × 1.05 = 3,675,000`입니다.

---

### 문제 4. UPDATE - 여러 컬럼 동시 수정

```sql
UPDATE EMP_COPY
SET DEPT_ID = 'D5', JOB_CODE = 'J6'
WHERE EMP_ID = '226';

SELECT DEPT_ID, JOB_CODE FROM EMP_COPY WHERE EMP_ID = '226';
```

**출력 결과**
```
D5 J6
```

**설명**: 이 시점부터 국내영업부(`D4`)에는 서준혁(227) 한 명만 남고, 해외영업1부(`D5`)에는
기존 5명 + 정다은(226) 총 6명이 됩니다.

---

### 문제 5. DELETE - 조건에 맞는 행 삭제

```sql
DELETE FROM EMP_COPY WHERE ENT_YN = 'Y';

SELECT COUNT(*) FROM EMP_COPY;
```

**출력 결과**
```
23
```

**설명**: `ENT_YN = 'Y'`인 사원은 오미자(212, 기술지원부) 한 명뿐이므로, 24행에서
23행이 됩니다.

---

## 도전

### 문제 6. INSERT ... SELECT - 집계 결과를 새 테이블에 저장

```sql
CREATE TABLE DEPT_AVG_SALARY (
    DEPT_ID    CHAR(2),
    AVG_SALARY INT
);

INSERT INTO DEPT_AVG_SALARY (DEPT_ID, AVG_SALARY)
SELECT DEPT_ID, ROUND(AVG(SALARY))
FROM EMP_COPY
WHERE DEPT_ID IS NOT NULL
GROUP BY DEPT_ID;

SELECT * FROM DEPT_AVG_SALARY ORDER BY DEPT_ID;
```

**출력 결과**
```
D1 2606667
D2 2280000
D3 2400000
D4 2600000
D5 2799667
D6 3650000
D8 2275000
D9 5900000
```

**설명**: 문제 5까지 진행한 상태의 `EMP_COPY`를 `GROUP BY DEPT_ID`로 묶어 부서별 평균
급여를 계산했습니다. 예를 들어 D5는 문제 3에서 인상된 급여 5명분(3,675,000 + 2,310,000
+ 2,625,000 + 3,948,000 + 1,890,000)에 문제 4에서 옮겨온 정다은(2,350,000)을 더한 6명의
평균 `16,798,000 / 6 ≈ 2,799,666.67`을 반올림한 값입니다. 소속 사원이 아예 없는
해외영업3부(D7)는 `GROUP BY` 결과에 나타나지 않고, 부서가 없는 사원(219, 220)은
`WHERE DEPT_ID IS NOT NULL`로 애초에 집계에서 빠집니다.

---

### 문제 7. WHERE 없는 UPDATE, 복구는 어떻게?

**설명**: MySQL은 기본적으로 `autocommit = 1`(켜짐)입니다. 즉, `START TRANSACTION`으로
트랜잭션을 직접 시작하지 않은 이상 `UPDATE`/`DELETE` 같은 DML 문장 하나하나가 실행되는
즉시 자동으로 커밋(확정)됩니다. `WHERE`가 빠진 `UPDATE EMP_COPY SET SALARY = 0;`을
실행하면 23명 전원의 급여가 즉시 0으로 바뀌고, 그 순간 이미 커밋되어 버렸기 때문에
`ROLLBACK`으로 되돌릴 수 없습니다. 이 경우 SQL만으로는 복구할 수 없고, 원본 `EMP`가
훼손되지 않았다는 전제 하에 `DROP TABLE EMP_COPY; CREATE TABLE EMP_COPY AS SELECT *
FROM EMP;`로 처음부터 다시 만드는 수밖에 없습니다(단, 이 챕터에서 진행한 INSERT/UPDATE/
DELETE 내역은 모두 사라집니다).

이런 사고를 예방하려면, 위험한 `UPDATE`/`DELETE`를 실행하기 전에 `START TRANSACTION;`으로
명시적 트랜잭션을 시작해두는 습관이 필요합니다.

```sql
START TRANSACTION;
UPDATE EMP_COPY SET SALARY = 0;   -- 실수로 WHERE를 빠뜨렸다고 가정
-- 결과가 이상하다는 것을 확인했다면
ROLLBACK;                         -- 아직 COMMIT하지 않았으므로 되돌릴 수 있음
```

`START TRANSACTION`으로 시작한 트랜잭션은 명시적으로 `COMMIT`을 실행하기 전까지는
확정되지 않으므로, 실수를 발견한 즉시 `ROLLBACK`으로 되돌릴 수 있습니다. 이 내용은 다음
`TCL` 챕터에서 자세히 다룹니다.
