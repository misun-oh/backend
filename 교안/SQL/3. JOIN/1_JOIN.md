# Day 6~7. JOIN

| 선수학습 | 이번 챕터 | 권장 진행 |
|---|---|---|
| DQL(SELECT), 함수(FUNCTION) | INNER/OUTER/SELF/비동등 JOIN | 2일 (Day 6~7) |

## 학습목표
- 여러 테이블에 나뉘어 있는 데이터를 하나의 결과로 합쳐서 조회할 수 있다.
- `INNER JOIN`과 `OUTER JOIN(LEFT/RIGHT)`의 차이를 결과 행 수로 설명할 수 있다.
- MySQL에 없는 `FULL OUTER JOIN`을 `UNION`으로 대체할 수 있다.
- 같은 테이블을 자기 자신과 조인하는 `SELF JOIN`을 이해하고 활용할 수 있다.
- 등호가 아닌 범위 조건으로 연결하는 `비동등 조인(NON-EQUI JOIN)`을 이해한다.

> `02_ERD읽는법.md`에서 이미 EMP-DEPT, EMP-JOB, EMP-EMP(자기참조), DEPT-LOCATION,
> LOCATION-NATIONAL 관계를 해석해봤습니다. 이번 챕터는 그 관계를 실제 SQL로 옮기는
> 과정입니다.

---

## 1. JOIN이 필요한 이유

`EMP` 테이블에는 `DEPT_ID`만 있고 부서 이름(`DEPT_TITLE`)은 없습니다. 부서 이름은
`DEPT` 테이블에 있습니다. 사원 이름과 부서 이름을 **함께** 보려면 두 테이블을 연결해야
합니다.

두 테이블을 조건 없이 그냥 나열하면 어떻게 될까요?

```sql
SELECT E.EMP_NAME, D.DEPT_TITLE
FROM EMP E, DEPT D;
```

이 방식(콤마로 테이블을 나열하는 옛날 문법)은 두 테이블의 **모든 행의 조합**
(카티전 곱, cartesian product)을 만들어 버립니다. `EMP` 21행 × `DEPT` 9행 = **189행**이
나오는데, 대부분은 사원과 아무 관계 없는 부서가 잘못 짝지어진 쓰레기 데이터입니다.

이 문제를 막으려면 "어떤 조건으로 두 테이블을 연결할지"를 명시해야 합니다. 이 교재에서는
아래처럼 **ANSI 표준 JOIN 문법**(`JOIN ... ON`)만 사용합니다.

```sql
SELECT E.EMP_NAME, D.DEPT_TITLE
FROM EMP E
JOIN DEPT D ON E.DEPT_ID = D.DEPT_ID;
```

> Oracle에는 `WHERE E.DEPT_ID = D.DEPT_ID(+)`처럼 괄호와 `+`로 외부 조인을 표현하는
> 전용 문법이 있었지만, **MySQL은 이 문법을 지원하지 않습니다.** 반드시 `LEFT JOIN`/
> `RIGHT JOIN` 키워드를 사용해야 합니다.

---

## 2. INNER JOIN (내부 조인)

`INNER JOIN`(또는 그냥 `JOIN`)은 **양쪽 테이블에 모두 값이 존재해서 조건이 맞아떨어지는
행만** 결과에 포함합니다.

```sql
SELECT E.EMP_NAME, D.DEPT_TITLE
FROM EMP E
JOIN DEPT D ON E.DEPT_ID = D.DEPT_ID
WHERE D.DEPT_ID = 'D9';
```

**출력 결과**
```
곽상혁 총무부
권진우 총무부
김민혜 총무부
```

**주의**: `EMP`는 총 21명이지만, `DEPT_ID`가 `NULL`인 조정원(219)·한규원(220)은
`E.DEPT_ID = D.DEPT_ID` 조건 자체가 성립하지 않아(`NULL`은 무엇과 비교해도 참이 될 수
없음) `WHERE` 조건 없이 전체를 조회해도 **19행**만 나옵니다. `INNER JOIN`은 "조건에
맞지 않으면 통째로 빠진다"는 점을 항상 기억해야 합니다.

### USING - 조인 컬럼명이 같을 때

`EMP.DEPT_ID`와 `DEPT.DEPT_ID`처럼 두 테이블의 연결 컬럼 이름이 완전히 같으면
`ON` 대신 `USING`을 쓸 수 있습니다.

```sql
SELECT E.EMP_NAME, D.DEPT_TITLE
FROM EMP E
JOIN DEPT D USING (DEPT_ID);
```

`ON E.DEPT_ID = D.DEPT_ID`와 결과는 완전히 같지만 더 짧습니다. (컬럼 이름을 처음
설계할 때 `EMP` 쪽을 `DEPT_CODE`가 아니라 `DEPT_ID`로 맞춘 이유가 바로 이것입니다 -
이름이 다르면 `USING`을 쓸 수 없습니다.)

---

## 3. OUTER JOIN (외부 조인)

`INNER JOIN`은 조건이 안 맞는 행을 버리지만, `OUTER JOIN`은 **한쪽 테이블의 행을
무조건 살리고**, 상대편에 짝이 없으면 그 자리를 `NULL`로 채웁니다.

### LEFT JOIN - 왼쪽(FROM 바로 뒤) 테이블 기준으로 전부 살리기

```sql
SELECT E.EMP_NAME, E.DEPT_ID, D.DEPT_TITLE
FROM EMP E
LEFT JOIN DEPT D ON E.DEPT_ID = D.DEPT_ID
WHERE E.DEPT_ID IS NULL;
```

**출력 결과**
```
조정원 (null) (null)
한규원 (null) (null)
```

`INNER JOIN`이었다면 이 두 사람은 아예 결과에서 사라졌겠지만, `LEFT JOIN`은 `EMP` 쪽
행을 무조건 살리기 때문에 부서가 없어도(`DEPT_ID IS NULL`) 조회됩니다. 단지
`D.DEPT_TITLE` 자리가 `NULL`로 채워질 뿐입니다.

### RIGHT JOIN - 오른쪽 테이블 기준으로 전부 살리기

```sql
SELECT D.DEPT_ID, D.DEPT_TITLE, COUNT(E.EMP_ID) AS 인원수
FROM EMP E
RIGHT JOIN DEPT D ON E.DEPT_ID = D.DEPT_ID
GROUP BY D.DEPT_ID, D.DEPT_TITLE
ORDER BY D.DEPT_ID;
```

**출력 결과**
```
D1 인사관리부   3
D2 회계관리부   3
D3 마케팅부     0
D4 국내영업부   0
D5 해외영업1부  5
D6 해외영업2부  2
D7 해외영업3부  0
D8 기술지원부   3
D9 총무부       3
```

`RIGHT JOIN`은 `DEPT`(오른쪽 테이블)의 9개 부서를 전부 살립니다. 그래서 사원이 한 명도
배정되지 않은 마케팅부(D3), 국내영업부(D4), 해외영업3부(D7)도 `인원수 0`으로
결과에 나타납니다. `COUNT(E.EMP_ID)`는 `EMP_ID`가 `NULL`인 행을 세지 않으므로
사원이 없는 부서는 정확히 0이 됩니다.

> 실무에서는 `RIGHT JOIN`보다 테이블 순서를 바꾼 `LEFT JOIN`을 더 많이 씁니다.
> `FROM DEPT D LEFT JOIN EMP E ON ...`으로 쓰면 위와 완전히 같은 결과를 얻으면서
> "어느 쪽을 기준으로 전부 살릴지"가 `FROM`절 순서만 봐도 더 직관적으로 읽힙니다.

### FULL OUTER JOIN - MySQL에는 없다

Oracle/표준 SQL의 `FULL OUTER JOIN`(양쪽 테이블을 모두 살리고, 짝이 없는 자리는 각각
`NULL`)을 MySQL은 지원하지 않습니다. 대신 `LEFT JOIN` 결과와 `RIGHT JOIN` 결과를
`UNION`으로 합쳐서 흉내 냅니다.

```sql
SELECT E.EMP_NAME, D.DEPT_TITLE
FROM EMP E
LEFT JOIN DEPT D ON E.DEPT_ID = D.DEPT_ID
UNION
SELECT E.EMP_NAME, D.DEPT_TITLE
FROM EMP E
RIGHT JOIN DEPT D ON E.DEPT_ID = D.DEPT_ID;
```

**결과 행 수 확인**: `LEFT JOIN` 결과는 21행(매칭 19행 + 조정원·한규원 2행),
`RIGHT JOIN` 결과는 22행(매칭 19행 + 사원 없는 부서 3행)입니다. 두 결과를 `UNION`으로
합치면 겹치는 19행은 한 번만 남고(`UNION`은 기본적으로 완전히 같은 행을 중복 제거합니다),
`LEFT JOIN`에만 있던 2행 + `RIGHT JOIN`에만 있던 3행이 더해져서 **총 24행**이 됩니다.
(중복을 제거하지 않으려면 `UNION ALL`을 쓰면 되지만, `FULL OUTER JOIN` 흉내를 낼
때는 매칭된 행이 두 번 나오면 안 되므로 반드시 `UNION`을 써야 합니다.)

---

## 4. 다중 테이블 JOIN

`JOIN`은 두 테이블뿐 아니라 세 개 이상도 이어붙일 수 있습니다.

```sql
SELECT E.EMP_NAME, D.DEPT_TITLE, J.JOB_NAME
FROM EMP E
JOIN DEPT D ON E.DEPT_ID = D.DEPT_ID
JOIN JOB J ON E.JOB_CODE = J.JOB_CODE
WHERE E.DEPT_ID = 'D5';
```

**출력 결과**
```
박지민 해외영업1부 부장
염성원 해외영업1부 과장
유제영 해외영업1부 과장
윤정주 해외영업1부 과장
최주호 해외영업1부 사원
```

테이블이 늘어나도 원리는 같습니다: 매번 "이 테이블을 어떤 조건으로 붙일지" `ON`을
하나씩 추가할 뿐입니다. `DEPT`→`LOCATION`→`NATIONAL`처럼 FK가 체인으로 이어진 경우도
동일합니다.

```sql
SELECT E.EMP_NAME, D.DEPT_TITLE, L.LOCAL_NAME, N.NATIONAL_NAME
FROM EMP E
JOIN DEPT D ON E.DEPT_ID = D.DEPT_ID
JOIN LOCATION L ON D.LOCATION_ID = L.LOCAL_CODE
JOIN NATIONAL N ON L.NATIONAL_CODE = N.NATIONAL_CODE
WHERE E.DEPT_ID = 'D9';
```

**출력 결과**
```
곽상혁 총무부 ASIA1 한국
권진우 총무부 ASIA1 한국
김민혜 총무부 ASIA1 한국
```

`DEPT.LOCATION_ID`는 `LOCATION.LOCAL_CODE`를 참조하고, `LOCATION.NATIONAL_CODE`는
다시 `NATIONAL.NATIONAL_CODE`를 참조합니다. 이렇게 FK가 연쇄적으로 이어져 있으면
`JOIN`도 그 체인을 따라 하나씩 이어 붙이면 됩니다.

---

## 5. SELF JOIN (자기 자신과의 조인)

`EMP.MANAGER_ID`는 다른 테이블이 아니라 **같은 `EMP` 테이블의 `EMP_ID`**를 가리키는
FK입니다(`02_ERD읽는법.md`에서 본 자기참조 관계). 이런 경우에는 같은 테이블을
서로 다른 별칭으로 두 번 등장시켜서 조인합니다.

```sql
SELECT E.EMP_NAME AS 사원, IFNULL(M.EMP_NAME, '(관리자 없음)') AS 관리자
FROM EMP E
LEFT JOIN EMP M ON E.MANAGER_ID = M.EMP_ID
WHERE E.EMP_ID IN ('200', '213', '214', '215');
```

**출력 결과**
```
곽상혁 (관리자 없음)
이다현 곽상혁
전태성 이다현
한재헌 이다현
```

`E`는 "사원 입장의 EMP", `M`은 "관리자 입장의 EMP"로 같은 테이블에 서로 다른 이름표를
붙인 것뿐입니다. 대표(곽상혁, 200)는 `MANAGER_ID`가 `NULL`이라 `INNER JOIN`이었다면
아예 결과에서 빠졌을 것입니다. 관리자가 없는 사람도 보여줘야 하므로 `LEFT JOIN` +
`IFNULL`을 함께 씁니다(함수 챕터에서 배운 `IFNULL`이 여기서도 그대로 쓰입니다).

---

## 6. 비동등 조인 (NON-EQUI JOIN)

지금까지의 `ON` 조건은 전부 `=`(등호)였습니다. 하지만 `SAL_GRADE` 테이블은 다른
테이블과 FK로 연결되어 있지 않고, `EMP.SALARY`가 `MIN_SAL`~`MAX_SAL` **구간**
안에 들어가는지로만 연결됩니다. 이럴 때는 `BETWEEN`처럼 등호가 아닌 조건으로 조인합니다.

```sql
SELECT E.EMP_NAME, E.SALARY, S.SAL_LEVEL
FROM EMP E
JOIN SAL_GRADE S ON E.SALARY BETWEEN S.MIN_SAL AND S.MAX_SAL
WHERE E.DEPT_ID = 'D9';
```

**출력 결과**
```
곽상혁 8000000 S1
권진우 6000000 S1
김민혜 3700000 S4
```

권진우의 급여 6,000,000은 `S1`(6,000,000~10,000,000)의 최솟값과 정확히 같습니다.
`BETWEEN`은 양쪽 경계값을 포함하므로 `S2`(5,000,000~5,999,999)가 아니라 `S1`에
속합니다. 이런 실수를 피하려면 구간 테이블을 설계/조회할 때 항상 경계값이 어느 쪽에
포함되는지 먼저 확인해야 합니다.

---

## 자주 하는 실수

- **`ON` 조건을 빼먹고 콤마로만 테이블을 나열** → 카티전 곱이 발생해 행 수가 폭발적으로
  늘어납니다. 항상 `JOIN ... ON`(또는 `USING`)을 명시하세요.
- **OUTER JOIN인데 `WHERE`에 오른쪽 테이블 조건을 그냥 씀** → `LEFT JOIN ... WHERE
  D.DEPT_TITLE = '총무부'`처럼 쓰면 `NULL`을 채운 행까지 그 조건에서 걸러지면서
  사실상 `INNER JOIN`과 같아져 버립니다. 왼쪽 테이블을 전부 살리고 싶다면 오른쪽 테이블
  조건은 `ON`절 안에 넣어야 합니다.
- **MySQL에 `FULL OUTER JOIN` 키워드가 있다고 착각** → 문법 오류가 납니다.
  `LEFT JOIN UNION RIGHT JOIN`으로 대체해야 합니다.
- **SELF JOIN에서 별칭을 안 붙이거나 같은 이름을 씀** → "어느 테이블의 EMP_ID인지"가
  모호해져 오류가 납니다. 반드시 서로 다른 별칭(`E`, `M`)을 붙이세요.
- **비동등 조인에서 경계값 포함 여부를 착각** → `BETWEEN`은 양 끝 값을 포함합니다.
  `MAX_SAL`이 `5999999`가 아니라 `6000000`이었다면 6,000,000인 사람이 `S1`과 `S2`
  양쪽에 다 걸려서 중복 조회될 수 있습니다.

---

## 핵심 요약

| 구분 | 특징 | 이 교재의 예시 |
|---|---|---|
| INNER JOIN | 양쪽 조건이 맞는 행만 | EMP - DEPT (부서 배정된 19명) |
| LEFT JOIN | 왼쪽 테이블 전부 + 짝 없으면 NULL | EMP - DEPT (부서 없는 2명 포함) |
| RIGHT JOIN | 오른쪽 테이블 전부 + 짝 없으면 NULL | DEPT - EMP (사원 없는 부서 포함) |
| FULL OUTER JOIN | 양쪽 다 전부 (MySQL은 UNION으로 흉내) | LEFT JOIN UNION RIGHT JOIN |
| SELF JOIN | 같은 테이블을 별칭 두 개로 조인 | EMP(사원) - EMP(관리자) |
| NON-EQUI JOIN | `=`가 아닌 조건(BETWEEN 등)으로 조인 | EMP - SAL_GRADE |
