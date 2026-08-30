# Day 1~2. DQL (SELECT)

| 항목 | 내용 |
|---|---|
| 선수 학습 | `02_ERD읽는법.md`, `01_실습데이터.md` (EMP, DEPT 등 테이블 생성 완료) |
| 이번 챕터 | SELECT 기본 문법, WHERE 조건, 정렬(ORDER BY) |
| 권장 진행 | 2일 (Day1: 1~5절, Day2: 6~11절 + 실습) |

## 학습목표
- SELECT문으로 원하는 컬럼만 골라 조회할 수 있다
- 컬럼에 별칭을 붙이고, 산술 연산·문자열 연결로 값을 가공해서 조회할 수 있다
- `DISTINCT`로 중복을 제거하고 조회할 수 있다
- `WHERE`절과 비교 연산자로 조건에 맞는 행만 걸러낼 수 있다
- `BETWEEN`, `LIKE`, `IN`, `IS NULL` 같은 특수 비교 연산자를 상황에 맞게 사용할 수 있다
- `AND`/`OR`/`NOT`을 조합하고, 연산자 우선순위를 고려해 괄호로 의도를 명확히 할 수 있다
- `ORDER BY`로 조회 결과를 원하는 기준으로 정렬할 수 있다

---

## 1. SQL과 SELECT문

SQL(Structured Query Language)은 관계형 데이터베이스에 저장된 데이터를 조회·조작하기 위한
표준 언어입니다. "어떻게 찾을지"가 아니라 **"무엇을 원하는지"** 조건만 기술하면, 나머지 처리는
데이터베이스가 알아서 수행합니다.

| 분류 | 용도 | 대표 명령어 |
|---|---|---|
| DQL (Data Query Language) | 데이터 검색 | `SELECT` |
| DML (Data Manipulation Language) | 데이터 조작 | `INSERT`, `UPDATE`, `DELETE` |
| DDL (Data Definition Language) | 데이터 정의 | `CREATE`, `ALTER`, `DROP` |
| TCL (Transaction Control Language) | 트랜잭션 제어 | `COMMIT`, `ROLLBACK` |

이 교안은 이 중 **DQL(SELECT)** 을 다룹니다. 이번 챕터부터 4개 챕터 내내 사용할 표는
`01_실습데이터.md`의 `EMP`(사원, 21명)와 `DEPT`(부서, 9개)입니다. 두 테이블의 관계는
`02_ERD읽는법.md`에서 다룬 `DEPT ||--o{ EMP` 그대로입니다(부서 하나에 사원 여러 명).

**기본 문법**
```sql
SELECT 컬럼명 [, 컬럼명, ...]
FROM 테이블명
WHERE 조건식;
```

```sql
-- EMP 테이블에서 전 사원의 사번, 이름, 급여 조회
SELECT EMP_ID, EMP_NAME, SALARY
FROM EMP;
```

**설명**: `SELECT`절에는 조회하려는 컬럼명을 쉼표로 구분해 나열합니다(마지막 컬럼 뒤에는
쉼표를 붙이지 않습니다). `FROM`절에는 그 컬럼들이 들어 있는 테이블명을 씁니다. 모든 컬럼을
보고 싶다면 컬럼명 대신 `*`를 씁니다.

```sql
SELECT * FROM EMP;
```

---

## 2. 컬럼 별칭 (Alias)

산술 연산을 하면 결과 컬럼명이 `SALARY*12`처럼 지저분해집니다. 별칭을 붙이면 결과를 읽기
좋게 정리할 수 있습니다.

```sql
-- 사원 이름과 연봉(급여*12)을 "이름", "연봉"이라는 이름으로 조회
SELECT EMP_NAME AS 이름, SALARY * 12 AS 연봉
FROM EMP;
```

| 표기법 | 예시 |
|---|---|
| `컬럼명 AS 별칭` | `SALARY AS 급여` |
| `컬럼명 별칭` (AS 생략) | `SALARY 급여` |
| `컬럼명 AS "별칭"` (공백/특수문자 포함 시 필수) | `SALARY AS "월 급여"` |

**설명**: `AS`는 생략할 수 있지만, 별칭에 띄어쓰기나 숫자로 시작하는 이름을 쓰고 싶다면
반드시 큰따옴표(`"..."`)로 감싸야 합니다.

---

## 3. 산술 연산과 리터럴

```sql
-- 사원 이름, 연봉(급여*12), 보너스 포함 연봉을 조회
SELECT EMP_NAME AS 이름,
       SALARY * 12 AS 연봉,
       (SALARY + (SALARY * IFNULL(BONUS, 0))) * 12 AS "보너스 포함 연봉"
FROM EMP;
```

**설명**: `BONUS`가 `NULL`인 사원은 이 연산 결과도 통째로 `NULL`이 되는 것이 원칙입니다
(NULL이 섞인 산술 연산의 결과는 무조건 NULL). `IFNULL(BONUS, 0)`은 "`BONUS`가 `NULL`이면 0으로
취급하라"는 함수로, `함수(FUNCTION)` 챕터(Day3~5)에서 자세히 다룹니다. 지금은 "NULL이 섞이면
연산 결과가 깨질 수 있다"는 점만 기억해두면 됩니다.

**리터럴**: 테이블에 없는 임의의 문자열을 SELECT절에 그대로 쓰면, 조회되는 모든 행에 똑같이
반복 출력됩니다.

```sql
SELECT EMP_NAME, SALARY, '원' AS 단위
FROM EMP;
```

---

## 4. DISTINCT — 중복 제거

```sql
-- EMP에 등장하는 직급 코드 종류만 조회 (중복 제거)
SELECT DISTINCT JOB_CODE
FROM EMP;
```

**설명**: `DISTINCT`는 `SELECT`절에 딱 한 번만 쓸 수 있습니다. 여러 컬럼을 함께 쓰면
(`SELECT DISTINCT DEPT_ID, JOB_CODE`) 그 컬럼들의 **조합 전체**가 같은 행만 중복으로 처리됩니다.

---

## 5. WHERE절과 비교 연산자

조회 대상 행을 조건으로 제한할 때 `WHERE`절을 씁니다.

| 연산자 | 의미 |
|---|---|
| `=` | 같다 |
| `>`, `<`, `>=`, `<=` | 크다/작다/크거나 같다/작거나 같다 |
| `!=`, `<>` | 같지 않다 |

```sql
-- 인사관리부(D1) 소속 사원 전체 조회
SELECT *
FROM EMP
WHERE DEPT_ID = 'D1';

-- 급여가 300만원 이상인 사원의 이름, 급여 조회
SELECT EMP_NAME, SALARY
FROM EMP
WHERE SALARY >= 3000000;
```

**설명**: 문자값을 비교할 때는 반드시 작은따옴표(`'D1'`)로 감싸야 합니다. 숫자는 따옴표 없이
그대로 씁니다. 문자열은 대소문자를 구분하므로 `'d1'`으로 쓰면 조회되지 않습니다.

> `DEPT_ID`는 `EMP` 테이블에서는 FK(부서를 참조하는 값), `DEPT` 테이블에서는 PK(부서를
> 구분하는 값)입니다. 같은 컬럼명을 양쪽 테이블에 똑같이 맞춰둔 이유는 JOIN 챕터(Day6~7)에서
> `USING(DEPT_ID)` 구문으로 더 간결하게 연결하기 위해서입니다.

---

## 6. BETWEEN AND — 범위 조건

```sql
-- 급여가 200만원 이상 300만원 이하인 사원 조회
SELECT EMP_NAME, SALARY
FROM EMP
WHERE SALARY BETWEEN 2000000 AND 3000000;

-- 위와 반대: 그 범위에 속하지 않는 사원 조회
SELECT EMP_NAME, SALARY
FROM EMP
WHERE SALARY NOT BETWEEN 2000000 AND 3000000;
```

**설명**: `BETWEEN A AND B`는 **A 이상 B 이하**(경계값 포함)를 의미합니다. 날짜에도 그대로
쓸 수 있습니다(`HIRE_DATE BETWEEN '2015-01-01' AND '2020-12-31'`).

---

## 7. LIKE — 문자 패턴 검색

| 와일드카드 | 의미 | 예시 |
|---|---|---|
| `%` | 0글자 이상 아무 문자 | `'김%'` → 김으로 시작하는 모든 문자열 |
| `_` | 정확히 1글자 | `'_해%'` → 두 번째 글자가 '해'인 문자열 |

```sql
-- 성이 '김'씨인 사원 조회
SELECT EMP_NAME, HIRE_DATE
FROM EMP
WHERE EMP_NAME LIKE '김%';

-- 이메일 아이디 부분에 밑줄(_)이 포함된 사원 조회 (와일드카드 _와 실제 문자 _가 충돌 → ESCAPE 필요)
SELECT EMP_NAME, EMAIL
FROM EMP
WHERE EMAIL LIKE '%$_%' ESCAPE '$';

-- '이'씨가 아닌 사원 조회
SELECT EMP_NAME
FROM EMP
WHERE EMP_NAME NOT LIKE '이%';
```

**설명**: 검색하려는 문자 자체가 `%`나 `_`와 겹치면(예: 이메일의 `_`), 임의의 특수문자를
와일드카드 앞에 붙이고 `ESCAPE '그 특수문자'`로 등록해서 "이건 와일드카드가 아니라 진짜 문자"라고
알려줘야 합니다.

---

## 8. IN — 여러 값 중 하나와 일치

```sql
-- 부서가 D5, D6, D8인 사원의 이름, 부서코드, 급여 조회
SELECT EMP_NAME, DEPT_ID, SALARY
FROM EMP
WHERE DEPT_ID IN ('D5', 'D6', 'D8');

-- 위와 완전히 같은 결과를 OR로 작성한 경우 (IN이 훨씬 간결함)
SELECT EMP_NAME, DEPT_ID, SALARY
FROM EMP
WHERE DEPT_ID = 'D5' OR DEPT_ID = 'D6' OR DEPT_ID = 'D8';
```

---

## 9. IS NULL / IS NOT NULL

```sql
-- 부서가 배정되지 않은 사원 조회
SELECT EMP_NAME, DEPT_ID
FROM EMP
WHERE DEPT_ID IS NULL;

-- 관리자가 없고, 부서도 배정되지 않은 사원 조회
SELECT EMP_NAME, MANAGER_ID, DEPT_ID
FROM EMP
WHERE MANAGER_ID IS NULL AND DEPT_ID IS NULL;
```

**설명**: `NULL`은 "값이 없음"을 뜻하므로 `= NULL`로 비교할 수 없습니다(항상 결과가
`UNKNOWN`이 되어 아무 행도 걸러지지 않습니다). 반드시 전용 연산자 `IS NULL` / `IS NOT NULL`을
사용해야 합니다.

---

## 10. 논리 연산자와 연산자 우선순위

| 연산자 | 의미 |
|---|---|
| `AND` | 모든 조건이 참일 때만 참 |
| `OR` | 조건 중 하나라도 참이면 참 |
| `NOT` | 조건의 결과를 반대로 뒤집음 |

```sql
-- 해외영업1부(D5)이면서 직급이 사원(J7)인 사람
SELECT *
FROM EMP
WHERE DEPT_ID = 'D5' AND JOB_CODE = 'J7';

-- 직급이 사원(J7) 또는 대리(J6)인 사람 중 급여가 200만원 이상인 사람
-- AND가 OR보다 먼저 계산되므로 괄호 없이 쓰면 의도와 다르게 해석될 수 있음!
SELECT *
FROM EMP
WHERE (JOB_CODE = 'J7' OR JOB_CODE = 'J6') AND SALARY >= 2000000;

-- 사원(J7)이 아닌 사람 (NOT을 단독으로 사용)
SELECT *
FROM EMP
WHERE NOT (JOB_CODE = 'J7');
```

**연산자 우선순위** (숫자가 낮을수록 먼저 계산됨)

| 순위 | 연산자 |
|---|---|
| 1 | 산술 연산자 (`*`, `/`, `+`, `-`) |
| 2 | 비교 연산자, `IS NULL`, `LIKE`, `IN` |
| 3 | `BETWEEN AND` |
| 4 | `NOT` |
| 5 | `AND` |
| 6 | `OR` |

**설명**: `AND`가 `OR`보다 먼저 계산되기 때문에, `WHERE A OR B AND C`는 `WHERE A OR (B AND C)`로
해석됩니다. 의도가 "(A 또는 B) 그리고 C"라면 반드시 괄호로 `WHERE (A OR B) AND C`처럼
명시해야 합니다. **우선순위를 외우기보다, 조건이 2개 이상 섞이면 습관적으로 괄호를 쓰는 것을
권장합니다.**

> **주의**: 표준 SQL/Oracle에서는 `||`가 문자열을 이어붙이는 연결 연산자지만, **MySQL은
> 기본 모드(`PIPES_AS_CONCAT` 비활성화 시)에서 `||`를 논리 `OR`와 동일하게 처리**합니다.
> 예를 들어 `SELECT 'a' || 'b';`는 `'ab'`가 아니라 `0`을 반환합니다. MySQL에서 문자열을
> 이어붙일 때는 반드시 `CONCAT()`을 사용하세요(`2. 함수(FUNCTION)/1_함수.md` 1절 참고).

---

## 11. ORDER BY — 정렬

```sql
-- 급여가 높은 순으로 사원 이름, 급여 조회
SELECT EMP_NAME, SALARY
FROM EMP
ORDER BY SALARY DESC;

-- 부서코드 오름차순, 같은 부서 안에서는 급여 내림차순
SELECT EMP_NAME, DEPT_ID, SALARY
FROM EMP
ORDER BY DEPT_ID ASC, SALARY DESC;

-- 부서가 배정되지 않은(NULL) 행을 맨 뒤로 보내기
SELECT EMP_NAME, DEPT_ID
FROM EMP
ORDER BY (DEPT_ID IS NULL), DEPT_ID ASC;
```

**설명**: `ORDER BY`는 SELECT문에서 **가장 마지막에 실행**되는 절입니다. 기본 정렬 방식은
오름차순(`ASC`, 생략 가능)이며, `DESC`를 붙이면 내림차순입니다. MySQL은 `NULL`을 "가장 작은
값"으로 취급하므로, **오름차순(`ASC`) 정렬 시 `NULL`이 기본적으로 맨 앞에**, 내림차순(`DESC`)
정렬 시 맨 뒤에 옵니다(Oracle 등 다른 DB와 기본 동작이 반대이니 주의). MySQL에는
`NULLS FIRST`/`NULLS LAST` 구문이 없기 때문에, `NULL`을 뒤로 보내고 싶다면 위 예시처럼
`(컬럼 IS NULL)`을 정렬 기준 맨 앞에 추가하는 방법을 씁니다. `DEPT_ID IS NULL`은 `NULL`인
행에서만 `1`(참), 나머지는 `0`(거짓)이 되므로, 오름차순 정렬하면 `0`(NULL이 아닌 행)이 먼저,
`1`(NULL인 행)이 나중에 옵니다. 컬럼명 대신 별칭이나 `SELECT`절에 나열한 컬럼의 순번
(예: `ORDER BY 2`)을 써도 됩니다.

---

## 자주 하는 실수

1. **문자값 비교에 따옴표를 빼먹음**
   ```sql
   WHERE DEPT_ID = D1;  -- 에러! D1을 컬럼명으로 오해함
   WHERE DEPT_ID = 'D1'; -- 올바른 표현
   ```

2. **NULL을 `=`로 비교하려는 시도**
   ```sql
   WHERE BONUS = NULL;    -- 항상 결과 없음 (틀린 문법은 아니지만 의도대로 동작 안 함)
   WHERE BONUS IS NULL;   -- 올바른 표현
   ```

3. **AND/OR가 섞였는데 괄호를 생략함**
   ```sql
   -- "D5 부서 또는 D6 부서이면서 급여 300만 이상"을 의도했지만
   WHERE DEPT_ID = 'D5' OR DEPT_ID = 'D6' AND SALARY >= 3000000;
   -- 실제로는 "D5 부서이거나, (D6 부서이면서 급여 300만 이상)"으로 해석됨
   ```

4. **`ORDER BY` 없이 정렬을 기대함**
   ```sql
   SELECT * FROM EMP; -- 정렬 순서가 보장되지 않음(내부 저장 순서에 의존)
   ```
   원하는 순서가 있다면 반드시 `ORDER BY`를 명시해야 합니다.

---

## 핵심 요약

| 항목 | 핵심 내용 |
|---|---|
| SELECT 기본형 | `SELECT 컬럼 FROM 테이블 WHERE 조건 ORDER BY 정렬기준;` |
| 별칭 | `AS 별칭` (생략 가능), 공백/특수문자 포함 시 `"큰따옴표"` 필수 |
| DISTINCT | 중복 제거, SELECT절에 1회만 사용 가능 |
| 비교 연산자 | `=`, `>`, `<`, `>=`, `<=`, `!=`/`<>` |
| BETWEEN AND | 범위(경계값 포함) 조건 |
| LIKE | `%`(0글자 이상), `_`(1글자) 와일드카드, 충돌 시 `ESCAPE` |
| IN | 여러 값 중 하나와 일치하는지 (OR의 축약형) |
| IS NULL | NULL 비교는 `=`가 아니라 전용 연산자로 |
| 연산자 우선순위 | AND가 OR보다 먼저 계산됨 → 헷갈리면 괄호 사용 |
| ORDER BY | SELECT문의 맨 마지막에 실행, 기본은 오름차순(`ASC`) |
