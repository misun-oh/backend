# Day 8~9. SUBQUERY - 실습 답안

---

## 기본

### 문제 1. 단일행 서브쿼리

```sql
SELECT EMP_NAME, SALARY
FROM EMP
WHERE SALARY > (
    SELECT SALARY FROM EMP WHERE EMP_NAME = '김민혜'
)
ORDER BY SALARY DESC;
```

**출력 결과**
```
곽상혁 8000000
권진우 6000000
김태일 3900000
윤정주 3760000
```

**설명**: 안쪽 서브쿼리는 김민혜의 급여(3,700,000) 딱 하나만 반환하므로 `=`/`>` 같은
단일행 비교 연산자를 그대로 쓸 수 있습니다. 3,700,000을 초과하는 사원 4명이
조회됩니다.

---

### 문제 2. 다중행 서브쿼리 (IN)

```sql
SELECT EMP_ID, EMP_NAME, DEPT_ID
FROM EMP
WHERE DEPT_ID IN (
    SELECT DEPT_ID FROM EMP WHERE JOB_CODE = 'J3'
)
ORDER BY EMP_ID;
```

**출력 결과**
```
203 김은민 D6
204 김태일 D6
205 박지민 D5
206 염성원 D5
207 유제영 D5
208 윤정주 D5
209 최주호 D5
```

**설명**: 부장(`'J3'`)인 사원은 김은민(203, D6), 김태일(204, D6), 박지민(205, D5)
세 명입니다. 서브쿼리는 이들의 부서코드 목록(`D6, D6, D5`, 중복 포함)을 반환하고,
바깥 쿼리는 `DEPT_ID`가 D6 또는 D5인 사원 전체(7명)를 조회합니다.

---

## 응용

### 문제 3. ALL

```sql
SELECT EMP_NAME, SALARY
FROM EMP
WHERE SALARY < ALL (
    SELECT SALARY FROM EMP WHERE DEPT_ID = 'D2'
);
```

**출력 결과**
```
한재헌 1380000
```

**설명**: 회계관리부(D2) 사원 급여는 `2,800,000 / 1,550,000 / 2,490,000`이며 최솟값은
1,550,000입니다. `< ALL(목록)`은 "목록의 최솟값보다 작아야 한다"는 뜻과 같으므로,
1,550,000 미만인 한재헌(1,380,000) 한 명만 조회됩니다.

---

### 문제 4. ANY

```sql
SELECT EMP_NAME, SALARY
FROM EMP
WHERE SALARY > ANY (
    SELECT SALARY FROM EMP WHERE DEPT_ID = 'D6'
)
ORDER BY SALARY DESC;
```

**출력 결과**
```
곽상혁 8000000
권진우 6000000
김태일 3900000
윤정주 3760000
김민혜 3700000
전태성 3660000
박지민 3500000
```

**설명**: 해외영업2부(D6) 사원 급여는 `3,400,000 / 3,900,000`이며 최솟값은
3,400,000입니다. `> ANY(목록)`은 "목록의 최솟값보다 크면 된다"는 뜻과 같으므로,
3,400,000을 초과하는 사원 7명이 조회됩니다. 김태일(204) 본인도 D6 소속이지만
목록에는 자신보다 작은 값(3,400,000, 김은민)이 있어 조건을 만족합니다.

---

### 문제 5. 다중 컬럼 서브쿼리

```sql
SELECT EMP_NAME, DEPT_ID, JOB_CODE
FROM EMP
WHERE (DEPT_ID, JOB_CODE) = (
    SELECT DEPT_ID, JOB_CODE FROM EMP WHERE EMP_NAME = '권진우'
)
AND EMP_NAME != '권진우';
```

**출력 결과**
```
김민혜 D9 J2
```

**설명**: 권진우는 (D9, J2)입니다. 같은 부서·같은 직급 조합을 가진 사원은 김민혜뿐이며,
본인이 다시 조회되지 않도록 `EMP_NAME != '권진우'` 조건을 추가했습니다.

---

## 도전

### 문제 6. 상관 서브쿼리 (NOT EXISTS)

```sql
SELECT E.EMP_NAME
FROM EMP E
WHERE NOT EXISTS (
    SELECT 1 FROM EMP M WHERE M.MANAGER_ID = E.EMP_ID
)
ORDER BY E.EMP_ID;
```

**출력 결과**
```
김민혜
김태일
유제영
윤정주
최주호
이금빈
오미자
전태성
한재헌
박홍주
심재호
엄용민
조정원
한규원
```

**설명**: `1_함수.md`~`1_SUBQUERY.md`에서 확인한 관리자 7명(곽상혁, 권진우, 김은민,
박지민, 염성원, 이광렬, 이다현)을 제외한 나머지 14명은 아무도 자신을 `MANAGER_ID`로
지정하지 않았습니다. 전체 21명에서 관리자 7명을 뺀 14명이 정확히 일치합니다.

---

### 문제 7. 종합 - 인라인 뷰 + LIMIT

```sql
SELECT D.DEPT_TITLE, T.평균급여
FROM (
    SELECT DEPT_ID, ROUND(AVG(SALARY)) AS 평균급여
    FROM EMP
    GROUP BY DEPT_ID
) T
JOIN DEPT D ON T.DEPT_ID = D.DEPT_ID
ORDER BY T.평균급여 DESC
LIMIT 1;
```

**출력 결과**
```
총무부 5900000
```

**설명**: 안쪽 인라인 뷰(`T`)가 부서별 평균 급여를 먼저 계산하고, 바깥 쿼리는 그
결과를 `DEPT`와 조인해서 부서명을 붙인 뒤 `ORDER BY ... DESC LIMIT 1`로 평균 급여가
가장 높은 한 행만 남깁니다. 총무부(D9)의 평균 급여 5,900,000원이 9개 부서 중
가장 높습니다.
