# SQLSTATE · MySQL 오류번호 참고표 (프로시저/트리거 HANDLER 용)

프로시저·트리거의 `DECLARE ... HANDLER FOR <조건>` 과 `SIGNAL` / `RESIGNAL` 에서 쓰는
오류 식별자 정리. ([1_PROCEDURE_TRIGGER.md](1_PROCEDURE_TRIGGER.md) 5절과 함께 본다.)

---

## 1. SQLSTATE 란

- **5글자 문자열** (표준. 예: `'23000'`, `'02000'`, `'HY000'`).
- 앞 **2글자 = 클래스(class)**, 큰 분류. 뒤 3글자 = 세부.
- 하나의 SQLSTATE 에 여러 개의 **MySQL 오류번호**(errno, 예 `1062`)가 매핑됨 → errno 가 더 구체적.

### HANDLER 가 특별 취급하는 3개 클래스

| 클래스 | 뜻 | `FOR` 에 쓰는 이름 |
|---|---|---|
| `00` | 성공 | (해당 없음) |
| `01` | 경고 | `SQLWARNING` |
| `02` | 데이터 없음(NOT FOUND) | `NOT FOUND` |
| 그 외 전부 | 오류 | `SQLEXCEPTION` |

즉 `SQLEXCEPTION` = "`00`·`01`·`02` 로 시작하지 않는 모든 SQLSTATE".

---

## 2. 자주 만나는 SQLSTATE / errno

| SQLSTATE | errno | 의미 | 언제 발생 |
|---|---|---|---|
| `00000` | – | 성공 | 정상 |
| `01000` | – | 일반 경고 | 암묵적 형변환·잘림 경고 등 (`SQLWARNING`) |
| `02000` | 1329 | 데이터 없음 | 커서가 끝을 지나 `FETCH` / `SELECT ... INTO` 결과 0행 (`NOT FOUND`) |
| `21000` | 1242 | 서브쿼리가 2행 이상 반환 | `x = (SELECT ...)` 또는 `SELECT ... INTO` 가 여러 행 |
| `22001` | 1406 | 문자열이 컬럼 길이보다 김 | `INSERT`/`UPDATE`, strict 모드 |
| `22003` | 1264 | 숫자 범위 초과 | 컬럼 타입 범위를 벗어난 값 |
| `22007` | 1292 | 잘못된 날짜/시간 값 | `'2026-13-40'` 같은 값 |
| `22012` | 1365 | 0으로 나눔 | `x / 0` (모드에 따라 오류) |
| `23000` | 1062 | **중복 키** | PK·UNIQUE 위반 |
| `23000` | 1048 | NOT NULL 컬럼에 NULL | 필수값 누락 |
| `23000` | 1451 | FK 위반 – 부모 행 삭제/수정 불가 | 참조하는 자식 행이 있음 |
| `23000` | 1452 | FK 위반 – 없는 부모 값 참조 | 자식에 부모에 없는 값 삽입 |
| `23000` | 3819 | CHECK 제약 위반 | (MySQL 8.0.16+) |
| `40001` | 1213 | **데드락** | 트랜잭션 교착 → 보통 잡아서 **재시도** |
| `HY000` | 1205 | 락 대기 타임아웃 | `innodb_lock_wait_timeout` 초과 |
| `42S02` | 1146 | 테이블 없음 | 오타·미생성 |
| `42S22` | 1054 | 컬럼 없음 | |
| `42000` | 1064 | SQL 문법 오류 | |
| `45000` | 1644 | **사용자 정의 예외** | `SIGNAL SQLSTATE '45000'` 로 직접 던진 것 |

> 전체 목록: MySQL 매뉴얼 *Server Error Message Reference*. `SHOW WARNINGS;` / `SHOW ERRORS;`
> 로 방금 발생한 것의 `Code`(errno)·`SQLSTATE`·`Message` 를 볼 수 있다.

---

## 3. HANDLER 에서 지정하는 3가지 방법

```sql
-- (1) 이름 (특별 클래스 3개)
DECLARE CONTINUE HANDLER FOR NOT FOUND      SET v_done = 1;
DECLARE EXIT     HANDLER FOR SQLEXCEPTION   BEGIN ROLLBACK; RESIGNAL; END;

-- (2) SQLSTATE 문자열 (클래스 단위로 넓게)
DECLARE EXIT HANDLER FOR SQLSTATE '23000'   -- 제약조건 위반 전부(중복/NULL/FK/CHECK)
    SELECT '제약조건 위반' AS msg;

-- (3) MySQL 오류번호 (가장 좁고 정확)
DECLARE CONTINUE HANDLER FOR 1062           -- 중복 키만
    BEGIN END;

-- 여러 개 나열도 가능
DECLARE CONTINUE HANDLER FOR 1451, 1452
    SELECT 'FK 위반' AS msg;

-- 이름을 붙여서 가독성 up
DECLARE dup_key CONDITION FOR SQLSTATE '23000';
DECLARE CONTINUE HANDLER FOR dup_key BEGIN END;
```

**좁게 잡을수록 안전**: `SQLEXCEPTION` 전체를 `CONTINUE` 로 삼키면 진짜 버그도 조용히 묻힌다.

---

## 4. 오류를 직접 던지기 - SIGNAL / RESIGNAL

```sql
-- 업무 규칙 위반을 명시적 오류로 (사용자 정의 예외 = SQLSTATE '45000')
IF p_amt <= 0 THEN
    SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = '이체 금액은 0보다 커야 합니다', MYSQL_ERRNO = 1644;
END IF;
```

```sql
-- 잡은 오류를 그대로 다시 올리기 (핸들러 안에서만)
DECLARE EXIT HANDLER FOR SQLEXCEPTION
BEGIN
    ROLLBACK;
    RESIGNAL;                    -- 원래 SQLSTATE·메시지 유지한 채 호출부로 전달
END;
```

| 문 | 위치 | 용도 |
|---|---|---|
| `SIGNAL SQLSTATE '값' SET ...` | 아무 데나 | 새 오류를 **발생**시킴 (검증 실패 등) |
| `RESIGNAL [SET ...]` | **핸들러 안** | 지금 처리 중인 오류를 **다시 발생**시킴 (정리 후 재전파) |

`GET DIAGNOSTICS` 로 핸들러 안에서 오류 상세를 꺼낼 수도 있다:

```sql
DECLARE CONTINUE HANDLER FOR SQLEXCEPTION
BEGIN
    GET DIAGNOSTICS CONDITION 1
        @errno = MYSQL_ERRNO, @sqlstate = RETURNED_SQLSTATE, @text = MESSAGE_TEXT;
    INSERT INTO ERROR_LOG (ERRNO, SQLSTATE, MESSAGE, LOGGED_AT)
        VALUES (@errno, @sqlstate, @text, NOW());
END;
```
