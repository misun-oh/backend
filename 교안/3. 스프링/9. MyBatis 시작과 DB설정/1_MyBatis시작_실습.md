# Day 9. MyBatis 시작 & DB 설정 — 실습

준비: MySQL에 `hr` 스키마 + HR 실습 데이터(`교안/2. SQL/0. 환경구성/01_실습데이터.md`),
계정 `hr / hr1234`, 권한 부여.

## 문제 1. DB 연결

`build.gradle` 에 `mybatis-spring-boot-starter:4.0.1`(부트 BOM이 관리 안 하므로 버전 명시), `mysql-connector-j` 추가.
`application.yml` 에 datasource·mybatis 설정(교안 1절).
앱을 실행해 **커넥션 오류 없이 기동**되는지 확인하세요.

**확인할 것**: 기동 로그에서 `HikariPool-1 - Start completed.` 를 찾으세요.

## 문제 2. EmpMapper — 목록/상세

`com.example.hr.mapper.EmpMapper` 인터페이스와 `src/main/resources/mapper/EmpMapper.xml` 을
만들어 다음을 구현하세요.

- `List<Emp> findAll()` — 전체 사원, `EMP_ID` 순
- `Emp findById(Long empId)`

`Emp` 도메인 필드: `empId, empName, email, deptId, jobCode, salary, hireDate, active`.
`ENT_YN='N'` → `active=true` 로 매핑(SQL `CASE` 또는 심화의 TypeHandler).

임시 컨트롤러 `GET /raw/emps`, `GET /raw/emps/{id}` 에서 `empMapper` 를 직접 호출해
결과가 나오는지 확인(21명, 곽상혁 등).

## 문제 3. 계층 연결 — 메모리 → MyBatis 교체

Day 5의 `EmpMemoryRepository` 를 비활성화(`@Repository` 제거 또는 `@Profile("memory")`)하고,
`EmpMapper` 를 쓰는 새 구현으로 교체하세요.

- 방법 A: `EmpRepository` 인터페이스 유지 + `EmpRepositoryMybatis implements EmpRepository`
- 방법 B: `EmpRepository` 제거 + `EmpServiceImpl` 이 `EmpMapper` 직접 사용

둘 중 하나를 택해 `GET /emps`(화면, Day 8) 가 **DB의 21명**을 보여주게 하세요.

**확인할 것**: `EmpController` 또는 `EmpService` 를 몇 줄 고쳤나요? 방법 A라면 몇 줄? 방법 B라면?

## 문제 4. `#{}` vs `${}` 실험

`EmpMapper` 에 `List<Emp> findByNameLike(String keyword)` 를 두 버전으로 만들어 비교하세요.

- 안전: `WHERE EMP_NAME LIKE CONCAT('%', #{keyword}, '%')`
- 위험(학습용): `WHERE EMP_NAME LIKE '%${keyword}%'`

`keyword` 로 `김' OR '1'='1` 을 넣어 각각 호출하고 결과 건수를 비교하세요.
**주의**: 위험 버전은 실습 후 반드시 삭제.

## 문제 5. (진단) 이 오류의 원인

앱 기동 또는 호출 시 다음이 나올 때 각각 가능한 원인을 2개씩 적으세요.

1. `org.apache.ibatis.binding.BindingException: Invalid bound statement (not found): com.example.hr.mapper.EmpMapper.findAll`
2. `Field 'empName' ... is null` (조회는 됐는데 필드가 전부 null)
3. `java.sql.SQLException: Access denied for user 'hr'@'localhost'`
