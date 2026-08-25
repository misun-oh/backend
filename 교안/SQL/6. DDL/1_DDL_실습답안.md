# Day 11. DDL - 실습 답안

---

## 기본

### 문제 1. CTAS로 사본 테이블 만들기

```sql
DROP TABLE IF EXISTS EMP_COPY;
DROP TABLE IF EXISTS DEPT_COPY;

CREATE TABLE DEPT_COPY AS SELECT * FROM DEPT;
CREATE TABLE EMP_COPY  AS SELECT * FROM EMP;

DESC EMP_COPY;
```

**설명**: CTAS는 원본의 컬럼 정의(타입)와 데이터만 그대로 복사합니다. `PRIMARY
KEY`/`FOREIGN KEY` 같은 제약조건은 옮겨지지 않으므로 `DESC` 결과의 `Key` 컬럼이 모두
비어 있습니다.

---

### 문제 2. PRIMARY KEY 추가

```sql
ALTER TABLE DEPT_COPY ADD CONSTRAINT PK_DEPT_COPY PRIMARY KEY (DEPT_ID);
ALTER TABLE EMP_COPY  ADD CONSTRAINT PK_EMP_COPY  PRIMARY KEY (EMP_ID);

DESC EMP_COPY;
```

**설명**: `EMP_ID`는 `'200'`~`'220'`까지 21개 값이 모두 유일하므로 `PRIMARY KEY` 추가가
문제없이 성공합니다. `DESC` 결과에서 `EMP_ID` 행의 `Key` 컬럼에 `PRI`가 표시됩니다.

---

## 응용

### 문제 3. FOREIGN KEY 추가

```sql
ALTER TABLE EMP_COPY
    ADD CONSTRAINT FK_EMPCOPY_DEPT FOREIGN KEY (DEPT_ID) REFERENCES DEPT_COPY(DEPT_ID);
```

**설명**: 외래키는 `NULL` 값을 검사 대상에서 제외합니다. 조정원(219)·한규원(220)의
`DEPT_ID`가 `NULL`이어도 "참조 대상에 없는 값을 저장하려는 것"이 아니므로 오류 없이
성공합니다. 반대로 `DEPT_ID`에 `'D1'`~`'D9'`가 아닌 값(예: `'D99'`)을 넣으려 하면
`DEPT_COPY`에 그런 값이 없으므로 거부됩니다.

---

### 문제 4. 자기 참조 FOREIGN KEY

```sql
ALTER TABLE EMP_COPY
    ADD CONSTRAINT FK_EMPCOPY_MANAGER FOREIGN KEY (MANAGER_ID) REFERENCES EMP_COPY(EMP_ID);
```

**설명**: `MANAGER_ID`는 `EMP_COPY` 자기 자신의 `EMP_ID`를 참조합니다. 관리자가 없는
사원(곽상혁 등 6명)은 `MANAGER_ID`가 `NULL`이라 문제 3과 같은 이유로 오류 없이
통과합니다.

---

### 문제 5. 컬럼 추가·변경·삭제

```sql
ALTER TABLE EMP_COPY ADD COLUMN REMARK VARCHAR(50);
ALTER TABLE EMP_COPY MODIFY COLUMN REMARK VARCHAR(100);
ALTER TABLE EMP_COPY DROP COLUMN REMARK;
```

**설명**: `ADD COLUMN`으로 만든 `REMARK`는 기존 21개 행에 대해 값이 전부 `NULL`로
채워집니다. `MODIFY COLUMN`은 이름은 그대로 두고 길이 정의만 `VARCHAR(50)` →
`VARCHAR(100)`으로 넓혔고, 마지막 `DROP COLUMN`으로 컬럼 자체를 제거했습니다.

---

## 도전

### 문제 6. TRUNCATE 후 구조 확인

```sql
TRUNCATE TABLE EMP_COPY;

SELECT COUNT(*) FROM EMP_COPY;   -- 0
DESC EMP_COPY;                   -- 컬럼 정의는 그대로 남아 있음

INSERT INTO EMP_COPY SELECT * FROM EMP;

SELECT COUNT(*) FROM EMP_COPY;   -- 21
```

**설명**: `TRUNCATE TABLE`은 데이터만 지우고 테이블 구조(컬럼 정의, 제약조건)는 그대로
남깁니다. `DROP TABLE` 후 다시 만드는 것과 달리, `TRUNCATE` 후에는 `CREATE TABLE`을 다시
쓸 필요 없이 바로 `INSERT`로 데이터를 채울 수 있습니다.

---

### 문제 7. 참조 무결성 때문에 DROP이 실패하는 경우

```sql
DROP TABLE DEPT_COPY;
-- ERROR 3730 (HY000): Cannot drop table 'DEPT_COPY' referenced by a foreign key
-- constraint 'FK_EMPCOPY_DEPT' on table 'EMP_COPY'.
```

**설명**: `EMP_COPY.DEPT_ID`가 `DEPT_COPY.DEPT_ID`를 참조하는 외래키가 걸려 있는 한,
부모 테이블인 `DEPT_COPY`를 먼저 지울 수 없습니다. 오류 없이 지우려면 자식의 외래키를
먼저 제거하거나, 자식 테이블을 먼저 삭제해야 합니다.

```sql
-- 방법 1: 자식의 FK 제약만 제거하고 DEPT_COPY는 남김
ALTER TABLE EMP_COPY DROP FOREIGN KEY FK_EMPCOPY_DEPT;
DROP TABLE DEPT_COPY;

-- 방법 2: 자식 테이블(EMP_COPY)을 먼저 통째로 삭제
DROP TABLE EMP_COPY;
DROP TABLE DEPT_COPY;
```
