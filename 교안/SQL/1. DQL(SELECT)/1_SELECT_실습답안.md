# Day 1~2. DQL(SELECT) - 실습 답안

---

## 기본

### 문제 1. 급여 내림차순 전체 조회

```sql
SELECT EMP_ID, EMP_NAME, SALARY
FROM EMP
ORDER BY SALARY DESC;
```

**출력 결과**
```
200 곽상혁 8000000
201 권진우 6000000
204 김태일 3900000
208 윤정주 3760000
202 김민혜 3700000
214 전태성 3660000
205 박지민 3500000
203 김은민 3400000
220 한규원 2890000
216 박홍주 2800000
213 이다현 2780000
211 이금빈 2550000
207 유제영 2500000
218 엄용민 2490000
212 오미자 2436240
219 조정원 2320000
206 염성원 2200000
210 이광렬 2000000
209 최주호 1800000
217 심재호 1550000
215 한재헌 1380000
```

**설명**: `ORDER BY SALARY DESC`는 급여를 기준으로 큰 값부터 작은 값 순서로 정렬합니다.
`ORDER BY`를 쓰지 않으면 정렬 순서가 보장되지 않으므로, "높은 순으로"라는 요구사항이 있다면
반드시 명시해야 합니다.

---

### 문제 2. 특정 부서 조회

```sql
SELECT EMP_NAME, SALARY
FROM EMP
WHERE DEPT_ID = 'D5';
```

**출력 결과**
```
박지민 3500000
염성원 2200000
유제영 2500000
윤정주 3760000
최주호 1800000
```

**설명**: 문자값 비교는 반드시 작은따옴표로 감싸야 합니다(`'D5'`). `DEPT_ID`는 `CHAR(2)`
타입이라 대소문자와 자릿수까지 정확히 일치해야 조회됩니다.

---

## 응용

### 문제 3. 범위 조건 + 정렬

```sql
SELECT EMP_NAME, SALARY, DEPT_ID
FROM EMP
WHERE SALARY BETWEEN 2000000 AND 3000000
ORDER BY SALARY ASC;
```

**출력 결과**
```
이광렬 2000000 D8
염성원 2200000 D5
조정원 2320000 (null)
오미자 2436240 D8
엄용민 2490000 D2
유제영 2500000 D5
이금빈 2550000 D8
이다현 2780000 D1
박홍주 2800000 D2
한규원 2890000 (null)
```

**설명**: `BETWEEN 2000000 AND 3000000`은 200만 이상 300만 **이하**(경계값 포함)를 의미합니다.
이 조건에 걸리는 사원 10명은 모두 급여가 서로 달라서 동점 처리(2차 정렬 기준)가 필요 없지만,
실무에서는 값이 같은 행이 있을 수 있으므로 `ORDER BY SALARY ASC, EMP_ID ASC`처럼 2차 정렬
기준을 함께 써두는 습관을 들이는 것이 좋습니다.

---

### 문제 4. 문자 패턴 + NULL 조건

```sql
SELECT EMP_NAME, BONUS
FROM EMP
WHERE EMP_NAME NOT LIKE '이%'
  AND BONUS IS NOT NULL;
```

**출력 결과**
```
곽상혁 0.30
김은민 0.20
박지민 0.15
염성원 0.10
오미자 0.35
전태성 0.30
조정원 0.10
```

**설명**: 보너스를 받는 사원은 총 9명(곽상혁, 김은민, 박지민, 염성원, 이금빈, 오미자, 이다현,
전태성, 조정원)인데, 그중 `'이금빈'`, `'이다현'`이 `'이%'` 패턴에 걸려 제외되어 7명이 남습니다.

---

### 문제 5. IN + AND 조합

```sql
SELECT EMP_ID, EMP_NAME, DEPT_ID, JOB_CODE
FROM EMP
WHERE DEPT_ID IN ('D1', 'D5', 'D8')
  AND JOB_CODE = 'J6';
```

**출력 결과**
```
210 이광렬 D8 J6
211 이금빈 D8 J6
213 이다현 D1 J6
214 전태성 D1 J6
```

**설명**: 직급이 대리(`J6`)인 사원은 210, 211, 213, 214, 219 총 5명입니다. 이 중 219(조정원)는
`DEPT_ID`가 `NULL`이라 `IN ('D1','D5','D8')` 조건에 해당하지 않아 제외됩니다.
(`NULL`은 어떤 목록과 비교해도 참이 될 수 없습니다.)

---

## 도전

### 문제 6. LIKE + ESCAPE

```sql
SELECT EMP_ID, EMP_NAME, EMAIL
FROM EMP
WHERE EMAIL LIKE '___$_%' ESCAPE '$'
ORDER BY EMP_ID ASC;
```

**출력 결과**
```
202 김민혜 kim_mh@company.com
203 김은민 kim_em@company.com
204 김태일 kim_ti@company.com
207 유제영 yoo_jy@company.com
210 이광렬 lee_gr@company.com
211 이금빈 lee_gb@company.com
213 이다현 lee_dh@company.com
215 한재헌 han_jh@company.com
217 심재호 sim_jh@company.com
218 엄용민 eom_ym@company.com
220 한규원 han_gw@company.com
```

**설명**: 패턴 `'___$_%'`은 "아무 문자 3개 + (이스케이프 처리된) 밑줄 1개 + 그 뒤 아무거나"를
의미합니다. `$`를 이스케이프 문자로 등록했기 때문에 `$_`는 와일드카드가 아니라 **문자 그대로의
밑줄**로 해석됩니다. `kwak_sh`처럼 밑줄 앞이 4글자(`kwak`)인 경우는 3번째 자리가 `'_'`가 아니라
`'k'`이므로 패턴에 맞지 않아 제외됩니다.

---

### 문제 7. 종합 - NULL 조건 여러 개 결합

```sql
SELECT EMP_ID, EMP_NAME, DEPT_ID, MANAGER_ID
FROM EMP
WHERE DEPT_ID IS NULL
   OR MANAGER_ID IS NULL
ORDER BY EMP_ID ASC;
```

**출력 결과**
```
200 곽상혁 D9   (null)
216 박홍주 D2   (null)
217 심재호 D2   (null)
218 엄용민 D2   (null)
219 조정원 (null) (null)
220 한규원 (null) (null)
```

**설명**: `DEPT_ID IS NULL`을 만족하는 사원은 219, 220 두 명뿐이지만, `MANAGER_ID IS NULL`까지
`OR`로 묶으면 대표(200, 애초에 관리자가 있을 수 없음)와 회계관리부(D2)에서 중간관리자 없이
바로 대표에게만 보고하지 않는 차장급 사원들(216, 217, 218)까지 함께 조회됩니다. `OR`는 두
조건 중 **하나만** 참이어도 결과에 포함시킨다는 점에 주의하세요.
