# Day 13. TCL - 실습

> 아래 문제는 순서대로 이어서 진행합니다(앞 문제의 결과가 뒤 문제에 영향을 줍니다).
> 먼저 `EMP_COPY`를 원본 `EMP`에서 새로 복사한 뒤 시작하세요.
>
> ```sql
> DROP TABLE IF EXISTS EMP_COPY;
> CREATE TABLE EMP_COPY AS SELECT * FROM EMP;
> ```

---

## 기본

### 문제 1. START TRANSACTION + COMMIT
총무부(`'D9'`) 전체 사원의 급여를 3% 인상하고 `COMMIT`으로 확정하세요. (`START
TRANSACTION`으로 시작)

**Output** (`EMP_ID, EMP_NAME, SALARY`, `EMP_ID` 오름차순)
```
200 곽상혁 8240000
201 권진우 6180000
202 김민혜 3811000
```

---

### 문제 2. START TRANSACTION + ROLLBACK
해외영업2부(`'D6'`) 사원을 전부 삭제했다가, 실수임을 깨닫고 `ROLLBACK`으로
되돌리세요.

**확인할 것**: `DELETE` 직후 `SELECT COUNT(*) FROM EMP_COPY;`는 `19`,
`ROLLBACK` 이후 다시 `21`이 됩니다.

---

## 응용

### 문제 3. SAVEPOINT로 일부만 되돌리기
다음 순서로 실행하세요.
1. `START TRANSACTION`
2. 인사관리부(`'D1'`) 전체 사원 급여 2% 인상
3. `SAVEPOINT SP1`
4. 기술지원부(`'D8'`) 전체 사원 삭제
5. `ROLLBACK TO SP1` (4번만 취소)
6. `COMMIT`

**확인할 것**: 최종적으로 `SELECT COUNT(*) FROM EMP_COPY;`는 `21`(D8 삭제가
취소됨)이고, D1 사원의 급여는 인상된 값 그대로 유지됩니다.

**Output** (`EMP_ID, EMP_NAME, SALARY`, D1 사원, `EMP_ID` 오름차순)
```
213 이다현 2835600
214 전태성 3733200
215 한재헌 1407600
```

---

### 문제 4. DDL 실행 시 묵시적 커밋 확인
다음 순서로 실행하고 결과를 확인하세요.
1. `START TRANSACTION`
2. 회계관리부(`'D2'`) 전체 사원 삭제
3. `LOG_ID INT` 컬럼 하나를 가진 `TEMP_LOG` 테이블을 `CREATE TABLE`로 생성 (DDL)
4. `ROLLBACK`
5. `SELECT COUNT(*) FROM EMP_COPY;`

**확인할 것**: 5번 결과가 `18`로, `ROLLBACK`을 실행했는데도 삭제가 되돌아가지
않습니다. 왜 그런지 한 문장으로 설명하세요.

---

## 도전

### 문제 5. 트랜잭션으로 묶어야 하는 상황 만들기
이다현(213)의 급여에서 500,000원을 차감하고, 그만큼을 전태성(214)의 급여에 더하는
"급여 이체"를 하나의 트랜잭션으로 작성하고 `COMMIT`하세요. (문제 3에서 이미 인상된
급여를 기준으로 진행합니다)

**Output** (`EMP_ID, SALARY`, `EMP_ID` 오름차순)
```
213 2335600
214 4233200
```

---

### 문제 6. 커밋 전 확인의 중요성
문제 5의 트랜잭션에서, 두 번째 `UPDATE`문의 `WHERE EMP_ID = '214'`를 실수로
`WHERE EMP_ID = '299'`(존재하지 않는 사번)로 잘못 입력했다고 가정합니다.

1. 이 상태로 `COMMIT`을 실행하면 어떤 문제가 발생하는지 설명하세요.
2. `COMMIT`하기 **전에** 이 실수를 발견하려면 어떤 절차를 거쳐야 하는지 설명하세요.
3. 만약 이미 `COMMIT`까지 실행해버렸다면, 어떻게 데이터를 바로잡아야 하는지 SQL과
   함께 서술하세요.
