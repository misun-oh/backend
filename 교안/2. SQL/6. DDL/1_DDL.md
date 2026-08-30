# Day 11. DDL (데이터 정의어)

| 항목 | 내용 |
|---|---|
| 선수학습 | DQL(SELECT), 함수(FUNCTION), JOIN, SUBQUERY (EMP/DEPT 테이블 구조 이해) |
| 이번 챕터 | CREATE TABLE과 제약조건, ALTER TABLE, DROP/TRUNCATE TABLE |
| 권장 진행 | 1~2일 |

## 학습목표
- DDL(`CREATE`/`ALTER`/`DROP`/`TRUNCATE`)이 DQL/DML과 어떻게 다른지 설명할 수 있다.
- `PRIMARY KEY`, `FOREIGN KEY`, `NOT NULL`, `UNIQUE`, `DEFAULT`, `CHECK` 제약조건을 목적에
  맞게 지정할 수 있다.
- `ALTER TABLE`로 기존 테이블의 컬럼/제약조건을 추가·수정·삭제할 수 있다.
- `DROP TABLE`과 `TRUNCATE TABLE`의 차이를 설명하고 상황에 맞게 선택할 수 있다.

---

## 1. DDL이란

`1_SELECT.md`에서 SQL 명령어를 4가지로 분류했던 표를 다시 봅시다.

| 분류 | 용도 | 대표 명령어 |
|---|---|---|
| DQL (Data Query Language) | 데이터 검색 | `SELECT` |
| DML (Data Manipulation Language) | 데이터 조작 | `INSERT`, `UPDATE`, `DELETE` |
| DDL (Data Definition Language) | 데이터 정의 | `CREATE`, `ALTER`, `DROP`, `TRUNCATE` |
| TCL (Transaction Control Language) | 트랜잭션 제어 | `COMMIT`, `ROLLBACK` |

지금까지는 이미 만들어진 `EMP`/`DEPT` 테이블을 **조회**만 했습니다. 이번 챕터부터는 테이블의
**구조 자체를 정의·변경·삭제**하는 DDL을 다룹니다.

> **중요**: DDL 문은 실행하는 즉시 자동으로 커밋(확정)됩니다. `ROLLBACK`으로 되돌릴 수
> 없다는 뜻입니다. 왜 그런지는 `TCL` 챕터의 "DDL의 묵시적 커밋"에서 자세히 다룹니다.

앞으로 이 챕터와 다음 `DML`/`TCL` 챕터에서는 원본 `EMP`/`DEPT`를 건드리지 않도록, 아래처럼
**사본 테이블**을 만들어 실습합니다.

```sql
CREATE TABLE DEPT_COPY AS SELECT * FROM DEPT;
CREATE TABLE EMP_COPY  AS SELECT * FROM EMP;
```

`CREATE TABLE ... AS SELECT`(줄여서 **CTAS**)는 기존 테이블을 조회한 결과 그대로 새 테이블을
만듭니다. 이 문법은 3절에서 다시 다룹니다.

---

## 2. CREATE TABLE과 제약조건

`CREATE TABLE`은 이미 `0. 환경구성/01_실습데이터.md`에서 컬럼과 데이터 타입만 지정하는
형태로 한 번 사용했습니다. 이번에는 **제약조건(constraint)**을 추가해서, "이 컬럼에는 이런
값만 들어올 수 있다"는 규칙을 테이블 자체에 걸어봅니다.

```sql
CREATE TABLE DEPT_COPY (
    DEPT_ID     CHAR(2) PRIMARY KEY,
    DEPT_TITLE  VARCHAR(35) NOT NULL,
    LOCATION_ID CHAR(2)
);

CREATE TABLE EMP_COPY (
    EMP_ID     VARCHAR(3) PRIMARY KEY,
    EMP_NAME   VARCHAR(20) NOT NULL,
    EMAIL      VARCHAR(25) UNIQUE,
    DEPT_ID    CHAR(2),
    JOB_CODE   CHAR(2),
    SALARY     INT DEFAULT 0,
    MANAGER_ID VARCHAR(3),
    HIRE_DATE  DATE NOT NULL,
    ENT_YN     CHAR(1) DEFAULT 'N' CHECK (ENT_YN IN ('Y', 'N')),
    CONSTRAINT FK_EMPCOPY_DEPT    FOREIGN KEY (DEPT_ID)    REFERENCES DEPT_COPY(DEPT_ID),
    CONSTRAINT FK_EMPCOPY_MANAGER FOREIGN KEY (MANAGER_ID) REFERENCES EMP_COPY(EMP_ID)
);
```

| 제약조건 | 의미 | 위 예시 |
|---|---|---|
| `PRIMARY KEY` | 각 행을 유일하게 식별하는 값. `NOT NULL` + `UNIQUE`를 동시에 만족 | `DEPT_ID`, `EMP_ID` |
| `FOREIGN KEY` | 다른 테이블(또는 같은 테이블)의 PK를 참조. 참조 대상에 없는 값은 저장 불가 | `EMP_COPY.DEPT_ID` → `DEPT_COPY.DEPT_ID` |
| `NOT NULL` | `NULL` 저장 금지(반드시 값이 있어야 함) | `DEPT_TITLE`, `EMP_NAME`, `HIRE_DATE` |
| `UNIQUE` | 중복 값 저장 금지(단, `NULL`은 여러 번 허용) | `EMAIL` |
| `DEFAULT` | 값을 지정하지 않고 `INSERT`하면 자동으로 채워지는 값 | `SALARY DEFAULT 0`, `ENT_YN DEFAULT 'N'` |
| `CHECK` | 지정한 조건을 만족하는 값만 저장 허용 | `ENT_YN`은 `'Y'` 또는 `'N'`만 허용 |

**설명**: `EMP_COPY.MANAGER_ID`가 `EMP_COPY.EMP_ID`를 참조하는 것처럼, 외래키는 **같은
테이블 자신**을 참조할 수도 있습니다(자기 참조 FK). 실제로 `EMP`의 `관리자-부하` 관계가
바로 이 구조입니다.

> **주의**: `FOREIGN KEY`가 참조하는 컬럼(`DEPT_COPY.DEPT_ID`)은 반드시 그 테이블에서
> `PRIMARY KEY`나 `UNIQUE`로 지정되어 있어야 합니다. 그렇지 않으면 `CREATE TABLE` 자체가
> 오류로 실패합니다.

---

## 3. 기존 테이블 복사 — CREATE TABLE ... AS SELECT

매번 제약조건까지 손으로 다 쓰기보다, 실습에서는 원본 `EMP`/`DEPT`의 **데이터**를 그대로
복사해서 시작하는 경우가 많습니다. 이때 쓰는 문법이 CTAS입니다.

```sql
DROP TABLE IF EXISTS EMP_COPY;
DROP TABLE IF EXISTS DEPT_COPY;

CREATE TABLE DEPT_COPY AS SELECT * FROM DEPT;
CREATE TABLE EMP_COPY  AS SELECT * FROM EMP;
```

```sql
DESC EMP_COPY;
```

**출력 결과(일부)**
```
Field       Type          Null  Key  Default  Extra
EMP_ID      varchar(3)    YES        NULL
EMP_NAME    varchar(20)   YES        NULL
...
```

**설명**: `DESC`(=`DESCRIBE`) 결과의 `Key` 컬럼이 모두 비어 있습니다. CTAS는 **컬럼과
데이터만** 복사할 뿐, `PRIMARY KEY`/`FOREIGN KEY`/`NOT NULL`/`AUTO_INCREMENT` 같은
제약조건이나 인덱스는 복사하지 않습니다. 원본 `EMP`/`DEPT`도 애초에 제약조건 없이
만들어졌으므로(`01_실습데이터.md` 참고), 지금부터 `ALTER TABLE`로 제약조건을 직접
추가해보겠습니다.

> **참고**: `WHERE` 절을 추가해 `CREATE TABLE ... AS SELECT ... WHERE 1=0;`처럼 쓰면
> 데이터 없이 **구조만** 복사할 수도 있습니다(`1=0`은 항상 거짓이라 행이 하나도 안
> 옮겨짐). `DML` 챕터에서 다시 사용합니다.

---

## 4. ALTER TABLE

`ALTER TABLE`은 이미 만들어진 테이블의 구조를 바꿉니다.

### 4.1 제약조건 추가 — ADD CONSTRAINT

```sql
ALTER TABLE DEPT_COPY ADD CONSTRAINT PK_DEPT_COPY PRIMARY KEY (DEPT_ID);
ALTER TABLE EMP_COPY  ADD CONSTRAINT PK_EMP_COPY  PRIMARY KEY (EMP_ID);

ALTER TABLE EMP_COPY
    ADD CONSTRAINT FK_EMPCOPY_DEPT FOREIGN KEY (DEPT_ID) REFERENCES DEPT_COPY(DEPT_ID);
```

**설명**: `EMP_COPY`에는 부서가 없는 사원(`DEPT_ID IS NULL`, 219 조정원·220 한규원)이
있지만, 외래키는 `NULL` 값 자체는 허용하므로(참조 검사 대상이 아님) 이 `ALTER TABLE`은
오류 없이 성공합니다. 외래키가 막는 것은 "참조 대상 테이블에 **존재하지 않는 값**"뿐입니다.

### 4.2 컬럼 추가 — ADD COLUMN

```sql
ALTER TABLE EMP_COPY ADD COLUMN REMARK VARCHAR(50);
```

새로 추가된 `REMARK` 컬럼의 기존 21개 행 값은 모두 `NULL`이 됩니다.

### 4.3 컬럼 변경 — MODIFY COLUMN

```sql
ALTER TABLE EMP_COPY MODIFY COLUMN REMARK VARCHAR(100);
```

컬럼의 데이터 타입이나 길이를 바꿉니다. 이름은 그대로 두고 정의만 바꾼다는 점에서 다음의
`RENAME COLUMN`과 다릅니다.

### 4.4 컬럼 삭제 — DROP COLUMN

```sql
ALTER TABLE EMP_COPY DROP COLUMN REMARK;
```

### 4.5 이름 변경 — RENAME COLUMN / RENAME TABLE

```sql
ALTER TABLE EMP_COPY RENAME COLUMN EMP_NO TO RESIDENT_NO;
ALTER TABLE EMP_COPY RENAME TO STAFF;
ALTER TABLE STAFF RENAME TO EMP_COPY;   -- 원래 이름으로 되돌림
```

---

## 5. DROP TABLE vs TRUNCATE TABLE

테이블(또는 그 안의 데이터)을 통째로 지우는 방법은 두 가지입니다.

```sql
TRUNCATE TABLE EMP_COPY;   -- 데이터만 전부 삭제, 테이블 구조는 남음

CREATE TABLE SCRATCH_TEST (ID INT);
DROP TABLE SCRATCH_TEST;   -- 테이블 구조 + 데이터 모두 삭제
```

| 구분 | 지우는 범위 | 구조(컬럼 정의) | 되돌리기 | 조건절(`WHERE`) |
|---|---|---|---|---|
| `TRUNCATE TABLE` | 데이터 전체 | 유지됨 | 불가(DDL, 자동 커밋) | 사용 불가 |
| `DROP TABLE` | 데이터 + 구조 | 삭제됨 | 불가(DDL, 자동 커밋) | 사용 불가 |
| `DELETE FROM`(DML, 다음 챕터) | 데이터 일부/전체 선택 가능 | 유지됨 | 커밋 전이면 가능 | 사용 가능 |

**설명**: `TRUNCATE TABLE EMP_COPY;`를 실행한 뒤 `SELECT COUNT(*) FROM EMP_COPY;`를
조회하면 `0`이 나오지만, `DESC EMP_COPY;`를 실행하면 컬럼 정의는 그대로 남아 있습니다.
반면 `DROP TABLE SCRATCH_TEST;` 이후에는 `DESC SCRATCH_TEST;` 자체가 "테이블이 없다"는
오류를 냅니다. 데이터 삭제와 테이블 삭제를 명확히 구분해서 사용해야 합니다.

> **주의 — 부모 테이블 먼저 DROP**: `EMP_COPY.DEPT_ID`가 `DEPT_COPY.DEPT_ID`를 참조하는
> 외래키가 걸린 상태(4.1절)에서 `DROP TABLE DEPT_COPY;`처럼 부모 테이블을 먼저 지우려
> 하면 "다른 테이블이 참조 중"이라는 오류가 납니다. 자식 테이블(`EMP_COPY`)의 외래키를
> 먼저 제거하거나, 자식 테이블을 먼저 삭제해야 합니다. 실습 문제 7에서 직접 이 오류를
> 확인하고 해결해봅니다.

---

## 자주 하는 실수

- **FK가 참조하는 컬럼에 PK/UNIQUE가 없는 상태에서 FOREIGN KEY 선언** → `CREATE
  TABLE`/`ALTER TABLE` 자체가 오류로 실패합니다. 참조 대상 컬럼에 먼저 `PRIMARY KEY`나
  `UNIQUE`를 지정해야 합니다.
- **CTAS로 복사하면 제약조건도 함께 복사된다고 착각** → CTAS는 데이터와 컬럼 정의(타입)만
  복사하고, `PRIMARY KEY`/`FOREIGN KEY`/`NOT NULL`/`AUTO_INCREMENT`는 복사하지 않습니다.
  필요하면 `ALTER TABLE`로 직접 추가해야 합니다.
- **FK가 걸린 부모 테이블을 자식보다 먼저 DROP** → 참조 무결성 오류가 납니다. 자식 → 부모
  순서로 삭제하거나, FK 제약을 먼저 제거합니다.
- **`MODIFY COLUMN`으로 길이를 기존 데이터보다 짧게 줄임** → 데이터가 잘리거나(비엄격
  모드) 오류가 발생합니다(엄격 모드, MySQL 8 기본값).
- **`TRUNCATE TABLE`을 `DELETE FROM`처럼 되돌릴 수 있다고 착각** → 둘 다 DDL 성격이 강한
  명령이라 자동 커밋되며 `ROLLBACK`이 통하지 않습니다. 자세한 이유는 `TCL` 챕터에서
  다룹니다.

---

## 핵심 요약

| 명령어 | 용도 | 비고 |
|---|---|---|
| `CREATE TABLE` | 새 테이블 정의(컬럼 + 제약조건) | `PRIMARY KEY`/`FOREIGN KEY`/`NOT NULL`/`UNIQUE`/`DEFAULT`/`CHECK` |
| `CREATE TABLE ... AS SELECT` | 조회 결과로 새 테이블 생성(CTAS) | 제약조건은 복사되지 않음 |
| `ALTER TABLE ... ADD` | 컬럼/제약조건 추가 | `ADD COLUMN`, `ADD CONSTRAINT` |
| `ALTER TABLE ... MODIFY` | 컬럼 정의(타입/길이) 변경 | 이름은 그대로 |
| `ALTER TABLE ... DROP COLUMN` | 컬럼 삭제 | |
| `ALTER TABLE ... RENAME` | 컬럼/테이블 이름 변경 | `RENAME COLUMN`, `RENAME TO` |
| `TRUNCATE TABLE` | 데이터 전체 삭제(구조 유지) | 자동 커밋, `WHERE` 불가 |
| `DROP TABLE` | 테이블 자체(구조+데이터) 삭제 | 자동 커밋, 참조 중이면 실패 |
