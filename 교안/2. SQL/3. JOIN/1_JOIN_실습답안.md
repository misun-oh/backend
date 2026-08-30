# Day 6~7. JOIN - 실습 답안

---

## 기본

### 문제 1. INNER JOIN

```sql
SELECT E.EMP_NAME, D.DEPT_TITLE, J.JOB_NAME
FROM EMP E
JOIN DEPT D ON E.DEPT_ID = D.DEPT_ID
JOIN JOB J ON E.JOB_CODE = J.JOB_CODE
WHERE D.DEPT_ID = 'D8';
```

**출력 결과**
```
이광렬 기술지원부 대리
이금빈 기술지원부 대리
오미자 기술지원부 사원
```

**설명**: `EMP`-`DEPT`, `EMP`-`JOB` 두 개의 `ON` 조건을 각각 걸어 세 테이블을
연결했습니다. `INNER JOIN`이므로 세 테이블 모두 조건이 맞는 행만 남습니다.

---

### 문제 2. USING

```sql
SELECT E.EMP_NAME, D.DEPT_TITLE
FROM EMP E
JOIN DEPT D USING (DEPT_ID)
WHERE DEPT_ID = 'D2';
```

**출력 결과**
```
박홍주 회계관리부
심재호 회계관리부
엄용민 회계관리부
```

**설명**: `USING (DEPT_ID)`는 두 테이블에 동일하게 존재하는 `DEPT_ID` 컬럼으로 자동
연결합니다. `USING`을 쓴 컬럼은 `E.DEPT_ID`처럼 테이블 별칭을 붙이지 않고 그냥
`DEPT_ID`로만 참조해야 합니다.

---

## 응용

### 문제 3. LEFT JOIN + IFNULL

```sql
SELECT E.EMP_NAME, IFNULL(D.DEPT_TITLE, '부서없음') AS 부서명
FROM EMP E
LEFT JOIN DEPT D ON E.DEPT_ID = D.DEPT_ID;
```

**출력 결과 (일부)**
```
...
조정원 부서없음
한규원 부서없음
```

**설명**: `LEFT JOIN`은 `EMP`(왼쪽 테이블) 21행을 전부 살립니다. 부서가 없는 조정원,
한규원은 `D.DEPT_TITLE`이 `NULL`로 채워지는데, 여기에 `IFNULL`을 씌워 `'부서없음'`이라는
문자열로 바꿔줍니다. `INNER JOIN`으로 썼다면 이 두 사람은 결과에서 아예 사라져 19행만
나왔을 것입니다.

---

### 문제 4. 다중 JOIN

```sql
SELECT E.EMP_NAME, D.DEPT_TITLE, L.LOCAL_NAME, N.NATIONAL_NAME
FROM EMP E
JOIN DEPT D ON E.DEPT_ID = D.DEPT_ID
JOIN LOCATION L ON D.LOCATION_ID = L.LOCAL_CODE
JOIN NATIONAL N ON L.NATIONAL_CODE = N.NATIONAL_CODE
WHERE D.DEPT_ID = 'D1';
```

**출력 결과**
```
이다현 인사관리부 ASIA1 한국
전태성 인사관리부 ASIA1 한국
한재헌 인사관리부 ASIA1 한국
```

**설명**: `DEPT.LOCATION_ID`(`'L1'`) → `LOCATION.LOCAL_CODE`(`'L1'`, 지역명
`'ASIA1'`) → `LOCATION.NATIONAL_CODE`(`'KO'`) → `NATIONAL.NATIONAL_CODE`(`'KO'`,
국가명 `'한국'`) 순서로 FK 체인을 따라 3번 조인했습니다.

---

### 문제 5. SELF JOIN

```sql
SELECT E.EMP_NAME, E.SALARY
FROM EMP E
JOIN EMP M ON E.MANAGER_ID = M.EMP_ID
WHERE M.EMP_NAME = '이광렬';
```

**출력 결과**
```
이금빈 2550000
오미자 2436240
```

**설명**: `M`은 "관리자 입장의 EMP"입니다. `M.EMP_NAME = '이광렬'`로 관리자를 먼저
지정한 뒤, `E.MANAGER_ID = M.EMP_ID`로 그 관리자를 상사로 둔 사원(`E`)을 찾습니다.
이광렬(사번 210)을 관리자로 둔 사원은 이금빈(211), 오미자(212) 두 명입니다.

---

## 도전

### 문제 6. RIGHT JOIN + GROUP BY

```sql
SELECT D.DEPT_ID, D.DEPT_TITLE, ROUND(AVG(E.SALARY)) AS 평균급여
FROM EMP E
RIGHT JOIN DEPT D ON E.DEPT_ID = D.DEPT_ID
GROUP BY D.DEPT_ID, D.DEPT_TITLE
ORDER BY D.DEPT_ID;
```

**출력 결과**
```
D1 인사관리부   2606667
D2 회계관리부   2280000
D3 마케팅부     (null)
D4 국내영업부   (null)
D5 해외영업1부  2752000
D6 해외영업2부  3650000
D7 해외영업3부  (null)
D8 기술지원부   2328747
D9 총무부       5900000
```

**설명**: `RIGHT JOIN`은 `DEPT`(오른쪽 테이블) 9개 부서를 전부 살립니다. 마케팅부(D3),
국내영업부(D4), 해외영업3부(D7)는 배정된 사원이 한 명도 없어 `E.SALARY`가 매칭되는 행
자체가 없으므로, `AVG(SALARY)`는 "0"이 아니라 **집계할 값이 없다는 뜻의 `NULL`**을
반환합니다. `AVG`는 `NULL`을 만들어내는 것이지 0으로 계산하는 것이 아니라는 점에
주의하세요.

---

### 문제 7. 비동등 조인 + 정렬

```sql
SELECT E.EMP_NAME, E.SALARY, S.SAL_LEVEL
FROM EMP E
JOIN SAL_GRADE S ON E.SALARY BETWEEN S.MIN_SAL AND S.MAX_SAL
WHERE S.SAL_LEVEL IN ('S1', 'S6')
ORDER BY S.SAL_LEVEL ASC, E.SALARY DESC;
```

**출력 결과**
```
곽상혁 8000000 S1
권진우 6000000 S1
최주호 1800000 S6
심재호 1550000 S6
한재헌 1380000 S6
```

**설명**: `ON` 조건이 `=`가 아니라 `BETWEEN`인 비동등 조인입니다. `ORDER BY`에 두 개의
정렬 기준(`S.SAL_LEVEL ASC`, `E.SALARY DESC`)을 순서대로 나열하면, 먼저 등급으로 크게
묶은 뒤 같은 등급 안에서는 급여 내림차순으로 다시 정렬됩니다.
