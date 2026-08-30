# Day 12. DML - 실습

> 아래 문제는 순서대로 이어서 진행합니다(앞 문제의 결과가 뒤 문제에 영향을 줍니다).
> 먼저 `EMP_COPY`를 원본 `EMP`에서 새로 복사한 뒤 시작하세요.
>
> ```sql
> DROP TABLE IF EXISTS EMP_COPY;
> CREATE TABLE EMP_COPY AS SELECT * FROM EMP;
> ```

---

## 기본

### 문제 1. INSERT - 전체 컬럼
마케팅부(`'D3'`, 현재 소속 사원 0명)에 신입사원을 추가하세요.
`EMP_ID='225'`, `EMP_NAME='최유진'`, `EMP_NO='030815-4889900'`,
`EMAIL='choi_yj@company.com'`, `PHONE='01055512345'`, `DEPT_ID='D3'`, `JOB_CODE='J7'`,
`SALARY=2400000`, `BONUS`/`MANAGER_ID`는 없음, `HIRE_DATE='2026-04-01'`,
`ENT_DATE`는 없음, `ENT_YN='N'`.

**확인할 것**: `SELECT * FROM EMP_COPY WHERE EMP_ID = '225';`로 1행이 조회됩니다.

---

### 문제 2. INSERT - 일부 컬럼, 다중 행
국내영업부(`'D4'`, 현재 소속 사원 0명)에 아래 두 명을 한 번의 `INSERT`문으로
추가하세요. (`EMP_ID`, `EMP_NAME`, `DEPT_ID`, `JOB_CODE`, `SALARY`, `HIRE_DATE`만 지정)

| EMP_ID | EMP_NAME | DEPT_ID | JOB_CODE | SALARY | HIRE_DATE |
|---|---|---|---|---|---|
| 226 | 정다은 | D4 | J7 | 2350000 | 2026-04-02 |
| 227 | 서준혁 | D4 | J6 | 2600000 | 2026-04-02 |

**확인할 것**: `SELECT COUNT(*) FROM EMP_COPY;`가 `24`가 됩니다.

---

## 응용

### 문제 3. UPDATE - 여러 행 일괄 수정
해외영업1부(`'D5'`) 전체 사원의 급여를 5% 인상하세요.

**Output** (`EMP_ID, EMP_NAME, SALARY`, `EMP_ID` 오름차순)
```
205 박지민 3675000
206 염성원 2310000
207 유제영 2625000
208 윤정주 3948000
209 최주호 1890000
```

---

### 문제 4. UPDATE - 여러 컬럼 동시 수정
정다은(226)의 소속을 국내영업부(`D4`)에서 해외영업1부(`D5`)로, 직급을 사원(`J7`)에서
대리(`J6`)로 동시에 변경하세요.

**확인할 것**: `SELECT DEPT_ID, JOB_CODE FROM EMP_COPY WHERE EMP_ID='226';` 결과가
`D5 J6`입니다.

---

### 문제 5. DELETE - 조건에 맞는 행 삭제
퇴사한 사원(`ENT_YN = 'Y'`)을 삭제하세요.

**확인할 것**: `SELECT COUNT(*) FROM EMP_COPY;`가 `23`이 됩니다.

---

## 도전

### 문제 6. INSERT ... SELECT - 집계 결과를 새 테이블에 저장
`DEPT_AVG_SALARY(DEPT_ID, AVG_SALARY)` 테이블을 만들고, 부서가 배정된(`DEPT_ID IS NOT
NULL`) 사원만 대상으로 부서별 평균 급여(반올림)를 계산해서 저장하세요.

**Output** (`DEPT_ID` 오름차순)
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

> 국내영업3부(D7)는 소속 사원이 없어 결과에 나타나지 않습니다. 부서가 없는 사원
> (219, 220)은 `WHERE DEPT_ID IS NOT NULL` 조건으로 제외됩니다.

---

### 문제 7. WHERE 없는 UPDATE, 복구는 어떻게?
문제 6까지 마친 상태에서, 실수로 아래 문장을 실행했다고 가정합니다.

```sql
UPDATE EMP_COPY SET SALARY = 0;
```

MySQL의 기본 설정(`autocommit`)에서는 이 문장이 실행되는 즉시 어떤 일이 벌어지는지,
그리고 이 실수를 예방하려면 평소 어떤 습관을 들여야 하는지 서술하세요. (SQL 명령을
함께 제시해도 좋습니다)
