# Day 13. TCL (트랜잭션 제어어)

| 항목 | 내용 |
|---|---|
| 선수학습 | DML(`INSERT`/`UPDATE`/`DELETE`), DDL(자동 커밋 개념) |
| 이번 챕터 | `COMMIT`, `ROLLBACK`, `SAVEPOINT`, `AUTOCOMMIT` |
| 권장 진행 | 1일 |

## 학습목표
- 트랜잭션이 무엇이고 왜 필요한지 ACID로 설명할 수 있다.
- `COMMIT`/`ROLLBACK`으로 트랜잭션을 확정하거나 취소할 수 있다.
- `SAVEPOINT`로 트랜잭션 안의 특정 지점까지만 되돌릴 수 있다.
- MySQL의 `AUTOCOMMIT` 기본 동작과 DDL의 묵시적 커밋을 이해하고, 언제 트랜잭션으로
  묶어야 하는지 판단할 수 있다.

---

## 1. 트랜잭션이란

**트랜잭션(transaction)**은 "논리적으로 하나의 작업 단위로 묶여야 하는 SQL 문의
모음"입니다. 대표적인 예가 계좌 이체입니다. "A계좌에서 10만원을 출금하고 B계좌에
10만원을 입금한다"는 두 개의 `UPDATE`문으로 이루어지지만, 이 둘은 **반드시 함께
성공하거나 함께 실패**해야 합니다. 출금만 되고 입금이 실패하면 돈이 사라지는
셈이기 때문입니다.

트랜잭션이 지켜야 할 4가지 성질을 **ACID**라고 부릅니다.

| 성질 | 의미 |
|---|---|
| 원자성 (Atomicity) | 트랜잭션 안의 작업은 전부 성공하거나 전부 실패한다(부분 성공 없음) |
| 일관성 (Consistency) | 트랜잭션 전후로 데이터베이스가 항상 유효한 규칙(제약조건 등)을 만족한다 |
| 고립성 (Isolation) | 동시에 실행되는 여러 트랜잭션이 서로의 중간 상태에 영향을 주지 않는다 |
| 지속성 (Durability) | 한 번 `COMMIT`된 데이터는 시스템 장애가 나도 사라지지 않는다 |

이번 챕터에서는 이 중 `COMMIT`(원자성 확정)과 `ROLLBACK`(원자성 취소)을 SQL로 직접
다뤄봅니다.

---

## 2. AUTOCOMMIT

```sql
SELECT @@autocommit;
```

**출력 결과**
```
1
```

MySQL은 기본적으로 `autocommit = 1`(켜짐) 상태입니다. 즉, `DML` 챕터에서 실행했던
`INSERT`/`UPDATE`/`DELETE` 문장은 **트랜잭션을 따로 시작하지 않아도 실행되는 즉시
자동으로 `COMMIT`** 됩니다. 여러 문장을 하나의 트랜잭션으로 묶어서 함께
`COMMIT`/`ROLLBACK`하려면 `START TRANSACTION;`으로 명시적으로 트랜잭션을 시작해야
합니다.

```sql
START TRANSACTION;
-- 이 지점부터는 COMMIT/ROLLBACK을 직접 실행하기 전까지 확정되지 않음
```

---

## 3. COMMIT

이번 챕터의 예제도 `DDL`/`DML` 챕터와 마찬가지로 원본을 보존한 사본에서 진행합니다.

```sql
DROP TABLE IF EXISTS EMP_COPY;
CREATE TABLE EMP_COPY AS SELECT * FROM EMP;

START TRANSACTION;
UPDATE EMP_COPY SET SALARY = SALARY * 1.1 WHERE DEPT_ID = 'D8';
COMMIT;

SELECT EMP_ID, EMP_NAME, SALARY FROM EMP_COPY WHERE DEPT_ID = 'D8' ORDER BY EMP_ID;
```

**출력 결과**
```
210 이광렬 2200000
211 이금빈 2805000
212 오미자 2679864
```

**설명**: `COMMIT`을 실행하는 순간 이 트랜잭션에서 바뀐 내용(급여 10% 인상)이
데이터베이스에 **영구히 확정**됩니다. `COMMIT` 이후에는 같은 트랜잭션 안에서
`ROLLBACK`을 실행해도 이 변경은 되돌릴 수 없습니다.

---

## 4. ROLLBACK

```sql
START TRANSACTION;

DELETE FROM EMP_COPY WHERE DEPT_ID = 'D2';
SELECT COUNT(*) FROM EMP_COPY;
```

**출력 결과**
```
18
```

회계관리부(D2) 사원 3명(박홍주, 심재호, 엄용민)이 삭제되어 21행에서 18행이 되었습니다.
그런데 이 삭제가 실수였다는 것을 깨달았다면, 아직 `COMMIT`하지 않았으므로 취소할 수
있습니다.

```sql
ROLLBACK;

SELECT COUNT(*) FROM EMP_COPY;
```

**출력 결과**
```
21
```

**설명**: `ROLLBACK`은 마지막 `COMMIT`(또는 `START TRANSACTION`) 이후의 모든 변경을
취소하고 그 이전 상태로 되돌립니다. `DELETE`가 아직 확정(`COMMIT`)되지 않았기 때문에
완전히 취소되어 21행으로 복구되었습니다.

---

## 5. SAVEPOINT

트랜잭션 안에서 특정 지점까지만 되돌리고 싶을 때는 `SAVEPOINT`로 "책갈피"를 표시해둘 수
있습니다.

```sql
START TRANSACTION;

UPDATE EMP_COPY SET SALARY = SALARY * 1.05 WHERE DEPT_ID = 'D5';
SAVEPOINT SP1;

DELETE FROM EMP_COPY WHERE DEPT_ID = 'D9';
SELECT COUNT(*) FROM EMP_COPY;
```

**출력 결과**
```
18
```

총무부(D9) 사원 3명(곽상혁, 권진우, 김민혜)이 삭제되어 21행에서 18행이 되었습니다. 이제
`DELETE`만 취소하고 그 앞의 `UPDATE`(D5 급여 5% 인상)는 유지하고 싶다면, 트랜잭션
전체가 아니라 `SP1` 지점까지만 되돌립니다.

```sql
ROLLBACK TO SP1;

SELECT COUNT(*) FROM EMP_COPY;
```

**출력 결과**
```
21
```

```sql
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

```sql
COMMIT;
```

**설명**: `ROLLBACK TO SP1`은 `SP1` **이후**의 변경(D9 삭제)만 취소하고, `SP1`
**이전**의 변경(D5 급여 인상)은 그대로 남깁니다. 트랜잭션 전체를 취소하는
`ROLLBACK`(4절)과 달리, `SAVEPOINT`를 활용하면 여러 단계 중 일부만 선택적으로 되돌릴
수 있습니다. 마지막으로 원하는 변경만 남은 상태에서 `COMMIT`으로 확정합니다.

> `ROLLBACK TO SP1` 이후에도 트랜잭션 자체는 계속 진행 중입니다. 최종적으로
> `COMMIT`이나 (세이브포인트가 아닌) `ROLLBACK`을 실행해야 트랜잭션이 종료됩니다.

---

## 6. DDL의 묵시적 커밋

`DDL` 챕터에서 "DDL 문은 실행 즉시 자동 커밋된다"고 배운 이유를 직접 확인해봅시다.

```sql
START TRANSACTION;

UPDATE EMP_COPY SET SALARY = 0 WHERE EMP_ID = '200';

ALTER TABLE EMP_COPY ADD COLUMN MEMO VARCHAR(20);   -- DDL 실행

ROLLBACK;

SELECT SALARY FROM EMP_COPY WHERE EMP_ID = '200';
```

**출력 결과**
```
0
```

**설명**: `ALTER TABLE`(DDL)을 실행하는 순간, 그 앞에서 실행했던 `UPDATE`까지 포함해서
트랜잭션 전체가 **묵시적으로 커밋**됩니다. 그 다음에 실행한 `ROLLBACK`은 이미 확정된
내용을 되돌리지 못하므로, 곽상혁(200)의 급여는 `0`으로 남아 있습니다. 이것이 `DDL`
챕터에서 "DDL 문은 자동 커밋되어 `ROLLBACK`이 통하지 않는다"고 설명했던 이유입니다.
**트랜잭션 중간에 `CREATE`/`ALTER`/`DROP`/`TRUNCATE`를 실행하면 안 됩니다.**

---

## 7. 언제 트랜잭션으로 묶어야 하는가

여러 개의 DML 문이 **"논리적으로 하나의 작업"**일 때 `START TRANSACTION` ~
`COMMIT`/`ROLLBACK`으로 묶습니다.

- 계좌 이체: 출금 `UPDATE` + 입금 `UPDATE`
- 주문 처리: 주문 `INSERT` + 재고 차감 `UPDATE`
- 회원 탈퇴: 회원 정보 `DELETE` + 관련 게시글 `UPDATE`(작성자 처리)

이런 작업을 트랜잭션 없이(자동 커밋 상태로) 하나씩 실행하면, 중간 문장에서 오류가 나도
그 앞의 문장은 이미 커밋되어버려 데이터가 **일부만 반영된 상태**로 남을 수 있습니다.
트랜잭션으로 묶으면 오류 발생 시 `ROLLBACK` 한 번으로 작업 전체를 시작 전 상태로 되돌릴
수 있습니다.

---

## 자주 하는 실수

- **`COMMIT`하지 않으면 다른 세션(다른 접속)에서 변경 내용이 보이지 않음** →
  고립성(Isolation) 때문에 당연한 동작입니다. "분명히 바꿨는데 안 보인다"고 당황하기
  전에 `COMMIT` 여부를 먼저 확인하세요.
- **이미 `COMMIT`된 내용을 `ROLLBACK`으로 되돌리려 함** → `ROLLBACK`은 마지막
  `COMMIT`(또는 트랜잭션 시작) 이후의 변경만 취소할 수 있습니다.
- **트랜잭션 중간에 DDL을 실행하고 이후 `ROLLBACK`을 기대함** → DDL은 실행 즉시 앞
  내용까지 묵시적 커밋시키므로 되돌릴 수 없습니다(6절).
- **존재하지 않는 `SAVEPOINT` 이름으로 `ROLLBACK TO` 시도** → 오류가 납니다. 이름을
  정확히 기억하거나, 세이브포인트를 남발하지 않도록 관리합니다.
- **트랜잭션을 열어놓고 `COMMIT`/`ROLLBACK` 없이 방치** → 변경한 행에 잠금(lock)이
  유지되어 다른 세션의 작업이 오래 대기하게 됩니다. 트랜잭션은 짧게 열고 빨리
  마무리합니다.

---

## 핵심 요약

| 명령어 | 용도 | 비고 |
|---|---|---|
| `START TRANSACTION` | 명시적 트랜잭션 시작 | 이후 문장은 자동 커밋되지 않음 |
| `COMMIT` | 트랜잭션 안의 모든 변경을 확정 | 확정 후에는 `ROLLBACK` 불가 |
| `ROLLBACK` | 트랜잭션 안의 모든 변경을 취소 | 마지막 `COMMIT` 이후 상태로 복원 |
| `SAVEPOINT 이름` | 트랜잭션 안에 되돌릴 지점을 표시 | |
| `ROLLBACK TO 이름` | 지정한 세이브포인트까지만 되돌림 | 트랜잭션 자체는 계속 진행됨 |
| `AUTOCOMMIT` | 문장 단위 자동 커밋 여부 | MySQL 기본값 켜짐(`1`) |
| DDL의 묵시적 커밋 | `CREATE`/`ALTER`/`DROP`/`TRUNCATE` 실행 시 앞 트랜잭션까지 자동 커밋 | 트랜잭션 중 DDL 금지 |
