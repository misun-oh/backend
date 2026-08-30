# Day 11. DDL - 실습

> 아래 문제는 원본 `EMP`, `DEPT` 테이블(`01_실습데이터.md`)이 이미 만들어져 있다는
> 전제로 진행합니다. 원본은 건드리지 않고, 매 문제마다 사본 테이블에서 작업합니다.

---

## 기본

### 문제 1. CTAS로 사본 테이블 만들기
`DEPT`, `EMP`를 각각 `DEPT_COPY`, `EMP_COPY`라는 이름으로 복사하세요. (CTAS 사용)
그 후 `DESC EMP_COPY;`로 제약조건이 없는 상태(모든 `Key` 컬럼이 비어 있음)를 확인하세요.

---

### 문제 2. PRIMARY KEY 추가
`DEPT_COPY.DEPT_ID`와 `EMP_COPY.EMP_ID`에 각각 `PRIMARY KEY` 제약조건을 추가하세요.
(`ALTER TABLE ... ADD CONSTRAINT` 사용)

**확인할 것**: `DESC EMP_COPY;`의 `EMP_ID` 행 `Key` 컬럼에 `PRI`가 표시됩니다.

---

## 응용

### 문제 3. FOREIGN KEY 추가
`EMP_COPY.DEPT_ID`가 `DEPT_COPY.DEPT_ID`를 참조하도록 외래키를 추가하세요.

**확인할 것**: 부서가 없는 사원(`DEPT_ID IS NULL`, 2명)이 있어도 오류 없이 성공해야
합니다.

---

### 문제 4. 자기 참조 FOREIGN KEY
`EMP_COPY.MANAGER_ID`가 `EMP_COPY.EMP_ID`(같은 테이블)를 참조하도록 외래키를
추가하세요.

---

### 문제 5. 컬럼 추가·변경·삭제
`EMP_COPY`에 `VARCHAR(50)` 타입의 `REMARK` 컬럼을 추가한 뒤, 길이를 `VARCHAR(100)`으로
늘리고, 마지막에 다시 삭제하세요. (`ADD COLUMN` → `MODIFY COLUMN` → `DROP COLUMN` 순서)

---

## 도전

### 문제 6. TRUNCATE 후 구조 확인
`EMP_COPY`를 `TRUNCATE`한 뒤 `SELECT COUNT(*) FROM EMP_COPY;`와 `DESC EMP_COPY;`를 각각
실행해서 "데이터는 없지만 구조는 남아 있음"을 확인하세요. 그 후
`INSERT INTO EMP_COPY SELECT * FROM EMP;`로 데이터를 다시 채워 넣으세요.

**확인할 것**: `TRUNCATE` 직후 `COUNT(*)`는 `0`, 복구 후 `COUNT(*)`는 `21`입니다.

---

### 문제 7. 참조 무결성 때문에 DROP이 실패하는 경우
`EMP_COPY.DEPT_ID`가 `DEPT_COPY.DEPT_ID`를 참조하는 외래키가 걸린 상태(문제 3)에서
`DROP TABLE DEPT_COPY;`를 실행해보고 어떤 오류가 나는지 확인하세요. 그 다음, 오류 없이
`DEPT_COPY`를 삭제하려면 어떤 순서로 명령을 실행해야 하는지 SQL로 작성하세요.
