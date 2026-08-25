# Day 12. DML (데이터 조작어)

| 항목 | 내용 |
|---|---|
| 선수학습 | DDL(`EMP_COPY` 등 사본 테이블 생성 방법 이해) |
| 이번 챕터 | `INSERT`, `UPDATE`, `DELETE` |
| 권장 진행 | 1~2일 |

## 학습목표
- `INSERT INTO`로 새 행을 추가할 수 있다(전체 컬럼/일부 컬럼/다중 행/`INSERT ... SELECT`).
- `UPDATE ... SET ... WHERE`로 기존 데이터를 원하는 조건에 맞춰 수정할 수 있다.
- `DELETE FROM ... WHERE`로 원하는 행만 골라 삭제할 수 있다.
- `WHERE`절을 빠뜨렸을 때 어떤 위험이 있는지 설명하고, 안전하게 실행하는 습관을 갖는다.

---

## 1. DML이란 + 실습 준비

DML(`INSERT`/`UPDATE`/`DELETE`)은 테이블 **구조**는 그대로 둔 채 **데이터**를
추가·수정·삭제하는 명령어입니다. `DDL`과 달리 `COMMIT`하기 전까지는 `ROLLBACK`으로
되돌릴 수 있습니다(자세한 내용은 `TCL` 챕터에서 다룹니다).

이번 챕터도 원본 `EMP`는 건드리지 않고, `DDL` 챕터에서 배운 CTAS로 깨끗한 사본을 다시
만들어 시작합니다.

```sql
DROP TABLE IF EXISTS EMP_COPY;
CREATE TABLE EMP_COPY AS SELECT * FROM EMP;

SELECT COUNT(*) FROM EMP_COPY;
```

**출력 결과**
```
21
```

---

## 2. INSERT INTO - 전체 컬럼

컬럼명을 생략하면 `CREATE TABLE`에 정의된 **컬럼 순서 그대로** 값을 나열해야 합니다.

```sql
INSERT INTO EMP_COPY
VALUES ('221', '홍길동', '000101-3123456', 'hong_gd@company.com', '01012345678',
        'D9', 'J7', 2600000, NULL, '200', '2026-01-05', NULL, 'N');
```

```sql
SELECT EMP_ID, EMP_NAME, DEPT_ID, JOB_CODE, SALARY, HIRE_DATE
FROM EMP_COPY WHERE EMP_ID = '221';
```

**출력 결과**
```
221 홍길동 D9 J7 2600000 2026-01-05
```

> **주의**: 값의 개수와 순서가 컬럼 정의와 정확히 일치해야 합니다. 하나라도 빠지거나
> 순서가 바뀌면 `Column count doesn't match value count` 오류가 나거나, 엉뚱한 컬럼에
> 값이 들어갑니다.

---

## 3. INSERT INTO - 컬럼 지정(일부 컬럼)

컬럼명을 직접 지정하면 순서를 자유롭게 정할 수 있고, 지정하지 않은 컬럼은 `NULL`(또는
`DEFAULT` 제약이 있다면 그 기본값)로 채워집니다.

```sql
INSERT INTO EMP_COPY (EMP_ID, EMP_NAME, DEPT_ID, JOB_CODE, SALARY, HIRE_DATE)
VALUES ('222', '김하나', 'D8', 'J7', 2450000, '2026-02-16');
```

```sql
SELECT * FROM EMP_COPY WHERE EMP_ID = '222';
```

**출력 결과**
```
222 김하나 NULL NULL NULL D8 J7 2450000 NULL NULL 2026-02-16 NULL NULL
```

**설명**: `EMP_NO`, `EMAIL`, `PHONE`, `BONUS`, `MANAGER_ID`, `ENT_DATE`, `ENT_YN`처럼
값을 지정하지 않은 컬럼은 전부 `NULL`입니다. 특히 `ENT_YN`은 원본 `EMP`에서는
`DEFAULT 'N'` 제약이 있어 자동으로 `'N'`이 채워졌겠지만, `EMP_COPY`는 `DDL` 챕터에서
배운 것처럼 **CTAS로 만든 사본이라 DEFAULT 제약이 복사되지 않았기 때문에** `NULL`로
남습니다.

---

## 4. 다중 행 INSERT

`VALUES` 뒤에 괄호를 쉼표로 이어 쓰면 여러 행을 한 번에 추가할 수 있습니다.

```sql
INSERT INTO EMP_COPY (EMP_ID, EMP_NAME, DEPT_ID, JOB_CODE, SALARY, HIRE_DATE) VALUES
('223', '이서준', 'D5', 'J7', 2300000, '2026-03-02'),
('224', '박은서', 'D6', 'J7', 2350000, '2026-03-02');

SELECT COUNT(*) FROM EMP_COPY;
```

**출력 결과**
```
25
```

**설명**: `INSERT`문 하나로 두 행이 한 번에 추가되어 21(원본) + 1(221) + 1(222) +
2(223, 224) = 25행이 됩니다. 문장을 4번 나눠 쓰는 것보다 네트워크 왕복이 줄어들어 더
효율적입니다.

---

## 5. INSERT INTO ... SELECT

다른 테이블(또는 같은 테이블의 조회 결과)을 그대로 삽입할 수도 있습니다. 이때 값을
직접 나열하지 않고 `SELECT`문을 씁니다.

```sql
CREATE TABLE HIGH_PAID_EMP AS
SELECT EMP_ID, EMP_NAME, SALARY FROM EMP_COPY WHERE 1=0;   -- 구조만 복사 (DDL 챕터 참고)

INSERT INTO HIGH_PAID_EMP (EMP_ID, EMP_NAME, SALARY)
SELECT EMP_ID, EMP_NAME, SALARY
FROM EMP_COPY
WHERE SALARY >= 5000000;

SELECT * FROM HIGH_PAID_EMP;
```

**출력 결과**
```
200 곽상혁 8000000
201 권진우 6000000
```

**설명**: `WHERE 1=0`은 항상 거짓이므로 `HIGH_PAID_EMP`는 `EMP_COPY`와 같은 컬럼 구조만
갖고 데이터는 하나도 없이 만들어집니다. 그 다음 `INSERT INTO ... SELECT`로 급여
5,000,000원 이상인 사원만 옮겨 담았습니다. `INSERT ... SELECT`의 `SELECT`절 컬럼
개수·순서·타입은 `INSERT INTO`의 컬럼 목록과 일치해야 합니다.

---

## 6. UPDATE - 단일 행

```sql
UPDATE EMP_COPY SET SALARY = 2700000 WHERE EMP_ID = '222';

SELECT EMP_NAME, SALARY FROM EMP_COPY WHERE EMP_ID = '222';
```

**출력 결과**
```
김하나 2700000
```

---

## 7. UPDATE - 여러 컬럼 동시 수정

`SET` 뒤에 `컬럼 = 값`을 쉼표로 이어 쓰면 한 문장으로 여러 컬럼을 동시에 바꿀 수
있습니다.

```sql
UPDATE EMP_COPY
SET DEPT_ID = 'D8', JOB_CODE = 'J6'
WHERE EMP_NAME = '김하나';

SELECT EMP_NAME, DEPT_ID, JOB_CODE FROM EMP_COPY WHERE EMP_NAME = '김하나';
```

**출력 결과**
```
김하나 D8 J6
```

---

## 8. UPDATE - 여러 행에 한 번에 적용 (표현식 사용)

`WHERE` 조건에 맞는 행이 여러 개면 `UPDATE` 한 문장으로 그 행 **전부**가 수정됩니다.
`SET`에는 컬럼 자신의 값을 활용한 계산식도 쓸 수 있습니다.

```sql
UPDATE EMP_COPY
SET SALARY = SALARY * 1.1
WHERE DEPT_ID = 'D8';

SELECT EMP_ID, EMP_NAME, SALARY FROM EMP_COPY WHERE DEPT_ID = 'D8' ORDER BY EMP_ID;
```

**출력 결과**
```
210 이광렬 2200000
211 이금빈 2805000
212 오미자 2679864
222 김하나 2970000
```

**설명**: `SET SALARY = SALARY * 1.1`은 "현재 급여에 10%를 더한 값으로 바꿔라"는
뜻입니다. `WHERE DEPT_ID = 'D8'`에 해당하는 4명(이광렬, 이금빈, 오미자, 그리고 7절에서
`D8`로 부서를 옮긴 김하나) 전원의 급여가 한 번에 10% 인상되었습니다.

---

## 9. WHERE절의 중요성

`UPDATE`와 `DELETE`는 `WHERE`절을 빠뜨리면 **테이블의 모든 행**에 영향을 줍니다.

```sql
-- 위험한 예시(실행하지 마세요): WHERE가 없으므로 EMP_COPY의 모든 사원 급여가 0이 됩니다.
UPDATE EMP_COPY SET SALARY = 0;
```

`SELECT`는 조건이 잘못돼도 "잘못된 결과를 보여줄 뿐"이지만, `UPDATE`/`DELETE`는 조건이
잘못되면 **데이터 자체가 잘못 바뀌거나 사라집니다**. 그래서 실무에서는 다음 순서로
작업하는 습관을 들입니다.

1. 먼저 같은 `WHERE` 조건으로 `SELECT`를 실행해서 "이 조건에 걸리는 행이 내가 예상한
   행이 맞는지" 확인한다.
2. 확인된 조건 그대로 `UPDATE`/`DELETE`로 바꿔서 실행한다.

```sql
-- 1) 먼저 확인
SELECT * FROM EMP_COPY WHERE DEPT_ID = 'D2' AND SALARY < 2000000;

-- 2) 확인된 조건으로 실행
UPDATE EMP_COPY SET SALARY = SALARY + 100000
WHERE DEPT_ID = 'D2' AND SALARY < 2000000;
```

---

## 10. DELETE FROM ... WHERE

```sql
DELETE FROM EMP_COPY WHERE EMP_ID IN ('223', '224');

SELECT COUNT(*) FROM EMP_COPY;
```

**출력 결과**
```
23
```

**설명**: 4절에서 추가했던 이서준(223), 박은서(224) 2명이 삭제되어 25행에서 23행이
되었습니다. `DELETE`는 `DDL`의 `TRUNCATE TABLE`과 달리 `WHERE`로 삭제 대상을 골라낼 수
있습니다.

---

## DELETE vs TRUNCATE vs DROP

`DDL` 챕터의 `TRUNCATE TABLE`/`DROP TABLE`과 이번 챕터의 `DELETE FROM`을 비교하면
다음과 같습니다.

| 구분 | 분류 | 삭제 범위 | `WHERE` | 되돌리기 |
|---|---|---|---|---|
| `DELETE FROM` | DML | 조건에 맞는 행만 (전체도 가능) | 사용 가능 | `COMMIT` 전이면 `ROLLBACK` 가능 |
| `TRUNCATE TABLE` | DDL | 데이터 전체(구조는 유지) | 사용 불가 | 불가(자동 커밋) |
| `DROP TABLE` | DDL | 구조 + 데이터 전체 | 사용 불가 | 불가(자동 커밋) |

---

## 자주 하는 실수

- **`INSERT`에서 컬럼 개수와 값 개수 불일치** → `Column count doesn't match value
  count` 오류. 컬럼을 생략했다면 `CREATE TABLE`에 정의된 순서 그대로 값을 나열해야
  합니다.
- **문자열/날짜 값에 따옴표 누락** → `'2026-01-05'`처럼 문자/날짜 값은 반드시 작은
  따옴표로 감싸야 합니다.
- **`UPDATE`/`DELETE`에 `WHERE`를 빠뜨림** → 테이블 전체가 수정·삭제되는, 가장 위험한
  실수입니다. 실행 전 같은 조건으로 `SELECT`를 먼저 해보는 습관을 들이세요.
- **`INSERT ... SELECT` 시 컬럼 개수·순서·타입 불일치** → `INSERT INTO`의 컬럼 목록과
  `SELECT`절의 컬럼 목록이 개수와 순서가 맞아야 합니다.
- **외래키(FK)가 걸린 상태에서 참조되는 부모 행을 먼저 삭제** → 참조 무결성 오류가
  납니다(`DDL` 챕터 4.1절 참고). 자식 행을 먼저 삭제하거나 FK 제약을 확인해야 합니다.

---

## 핵심 요약

| 명령어 | 용도 | 비고 |
|---|---|---|
| `INSERT INTO ... VALUES` | 값을 직접 나열해 행 추가 | 컬럼 생략 시 정의 순서 그대로 |
| `INSERT INTO (컬럼목록) VALUES` | 일부 컬럼만 지정해 행 추가 | 나머지는 `NULL`/`DEFAULT` |
| `INSERT INTO ... VALUES (...), (...)` | 다중 행 추가 | 한 문장으로 여러 행 |
| `INSERT INTO ... SELECT` | 조회 결과를 그대로 삽입 | 컬럼 개수·순서·타입 일치 필요 |
| `UPDATE ... SET ... WHERE` | 조건에 맞는 행의 값 수정 | `WHERE` 없으면 전체 행 수정 |
| `DELETE FROM ... WHERE` | 조건에 맞는 행 삭제 | `WHERE` 없으면 전체 행 삭제 |
