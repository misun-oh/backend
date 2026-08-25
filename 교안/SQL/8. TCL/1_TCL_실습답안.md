# Day 13. TCL - 실습 답안

---

## 기본

### 문제 1. START TRANSACTION + COMMIT

```sql
START TRANSACTION;
UPDATE EMP_COPY SET SALARY = SALARY * 1.03 WHERE DEPT_ID = 'D9';
COMMIT;

SELECT EMP_ID, EMP_NAME, SALARY FROM EMP_COPY WHERE DEPT_ID = 'D9' ORDER BY EMP_ID;
```

**출력 결과**
```
200 곽상혁 8240000
201 권진우 6180000
202 김민혜 3811000
```

**설명**: 곽상혁(8,000,000 × 1.03 = 8,240,000)을 포함한 총무부 3명 전원의 급여가
3% 인상되었고, `COMMIT`으로 확정했습니다.

---

### 문제 2. START TRANSACTION + ROLLBACK

```sql
START TRANSACTION;
DELETE FROM EMP_COPY WHERE DEPT_ID = 'D6';

SELECT COUNT(*) FROM EMP_COPY;   -- 19

ROLLBACK;

SELECT COUNT(*) FROM EMP_COPY;   -- 21
```

**설명**: 해외영업2부(D6) 사원 2명(김은민, 김태일)이 삭제되어 21행에서 19행이
되었지만, `COMMIT`하지 않은 상태에서 `ROLLBACK`했으므로 삭제가 전부 취소되어 다시
21행으로 복구되었습니다.

---

## 응용

### 문제 3. SAVEPOINT로 일부만 되돌리기

```sql
START TRANSACTION;

UPDATE EMP_COPY SET SALARY = SALARY * 1.02 WHERE DEPT_ID = 'D1';
SAVEPOINT SP1;

DELETE FROM EMP_COPY WHERE DEPT_ID = 'D8';
SELECT COUNT(*) FROM EMP_COPY;   -- 18

ROLLBACK TO SP1;
SELECT COUNT(*) FROM EMP_COPY;   -- 21

COMMIT;

SELECT EMP_ID, EMP_NAME, SALARY FROM EMP_COPY WHERE DEPT_ID = 'D1' ORDER BY EMP_ID;
```

**출력 결과**
```
213 이다현 2835600
214 전태성 3733200
215 한재헌 1407600
```

**설명**: `SAVEPOINT SP1`은 D1 급여 인상 **직후**의 상태를 표시합니다. 이후 D8
삭제로 18행이 되었지만, `ROLLBACK TO SP1`은 `SP1` 이후의 변경(D8 삭제)만 취소하므로
21행으로 복구되고, `SP1` 이전 변경(D1 급여 인상)은 그대로 유지됩니다. 예를 들어
이다현은 `2,780,000 × 1.02 = 2,835,600`입니다.

---

### 문제 4. DDL 실행 시 묵시적 커밋 확인

```sql
START TRANSACTION;

DELETE FROM EMP_COPY WHERE DEPT_ID = 'D2';

CREATE TABLE TEMP_LOG (LOG_ID INT);

ROLLBACK;

SELECT COUNT(*) FROM EMP_COPY;   -- 18
```

**설명**: `CREATE TABLE`(DDL)을 실행하는 순간, 그 앞에서 실행한
`DELETE FROM EMP_COPY WHERE DEPT_ID = 'D2';`까지 포함해 트랜잭션 전체가 묵시적으로
커밋됩니다. 따라서 이후의 `ROLLBACK`은 이미 확정된 삭제를 되돌리지 못하고, 회계관리부
3명이 삭제된 18행 상태가 그대로 유지됩니다.

---

## 도전

### 문제 5. 트랜잭션으로 묶어야 하는 상황 만들기

```sql
START TRANSACTION;

UPDATE EMP_COPY SET SALARY = SALARY - 500000 WHERE EMP_ID = '213';
UPDATE EMP_COPY SET SALARY = SALARY + 500000 WHERE EMP_ID = '214';

COMMIT;

SELECT EMP_ID, SALARY FROM EMP_COPY WHERE EMP_ID IN ('213', '214') ORDER BY EMP_ID;
```

**출력 결과**
```
213 2335600
214 4233200
```

**설명**: 문제 3에서 인상된 이다현(2,835,600)과 전태성(3,733,200)을 기준으로,
이다현에게서 500,000원을 빼고(→ 2,335,600) 전태성에게 500,000원을 더했습니다(→
4,233,200). 두 `UPDATE`를 하나의 트랜잭션으로 묶었기 때문에, 만약 둘 중 하나가 오류로
실패했다면 `ROLLBACK`으로 전체를 취소해 "돈이 한쪽에서만 사라지는" 상황을 막을 수
있습니다.

---

### 문제 6. 커밋 전 확인의 중요성

**1. 어떤 문제가 발생하는가**: `WHERE EMP_ID = '299'`는 `EMP_COPY`에 존재하지 않는
사번이므로 두 번째 `UPDATE`는 **0행에 영향을 주고 조용히 성공**합니다(오류가 나지
않습니다). 그 상태로 `COMMIT`하면 이다현의 급여만 500,000원 줄어들고, 어느 누구의
급여도 늘어나지 않은 채 확정되어버립니다. 즉 500,000원이 시스템 안에서 "증발"한
것처럼 되어, 트랜잭션의 원자성(Atomicity)이 지켜지지 않은 셈입니다.

**2. 커밋 전 확인 절차**: 각 DML 실행 직후 영향받은 행 수(MySQL 클라이언트가 보여주는
`Rows matched`/`Query OK, N rows affected`)를 확인하거나, `COMMIT`하기 전에
`SELECT EMP_ID, SALARY FROM EMP_COPY WHERE EMP_ID IN ('213', '214');`처럼 대상
사번들을 직접 조회해서 "의도한 두 행이 모두 바뀌었는지"를 검증해야 합니다. 하나라도
예상과 다르면 `COMMIT` 대신 `ROLLBACK`을 실행합니다.

**3. 이미 COMMIT까지 실행해버렸다면**: `COMMIT` 이후에는 `ROLLBACK`이 통하지
않으므로, 잘못된 값을 원래대로 되돌리는 **보정용 `UPDATE`**를 새로 실행하고 다시
`COMMIT`해야 합니다.

```sql
START TRANSACTION;
UPDATE EMP_COPY SET SALARY = SALARY + 500000 WHERE EMP_ID = '213';   -- 잘못 차감된 금액 복구
COMMIT;
```
