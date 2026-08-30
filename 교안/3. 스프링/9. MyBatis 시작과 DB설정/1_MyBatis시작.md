# Day 9. MyBatis 시작 & DB 설정

| 항목 | 내용 |
|---|---|
| 선수학습 | `Java 2.DB연결하기`(JDBC·`EmployeeDao`), SQL 전 과정, Day 5(Repository 인터페이스) |
| 이번 챕터 | 의존성 추가 → `application.yml` DataSource(HikariCP) → MyBatis 설정 → `@Mapper` 인터페이스 + Mapper XML → `#{}` vs `${}` → 메모리 Repository를 Mapper로 교체 |
| 권장 진행 | 1일 |
| DB | SQL 과정의 HR 스키마(`EMP`, `DEPT`, `JOB` …). `교안/2. SQL/0. 환경구성/01_실습데이터.md` 스크립트로 준비 |

## 학습목표

- 스프링 부트에서 DB 접속 정보를 `application.yml` 로 설정하고 커넥션 풀(HikariCP)을 이해한다.
- `mybatis-spring-boot-starter` 로 MyBatis를 붙이고, `@Mapper` 인터페이스 + XML 매핑을 작성한다.
- `Java 2.DB연결하기` 의 `Connection/PreparedStatement/ResultSet` 코드가 무엇으로 대체됐는지 안다.
- `#{}` (파라미터 바인딩)와 `${}` (문자열 치환)의 차이와 위험을 안다.
- Day 5의 `EmpMemoryRepository` 를 MyBatis 구현으로 바꾸되 상위 계층은 그대로 둔다.

---

## 1. 의존성 & 접속 설정

`build.gradle`:
```groovy
implementation 'org.mybatis.spring.boot:mybatis-spring-boot-starter:4.0.1'   // Boot 4.0 대응 라인
runtimeOnly   'com.mysql:mysql-connector-j'
```

`application.yml`:
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/hr?serverTimezone=Asia/Seoul&characterEncoding=UTF-8
    username: hr
    password: hr1234
    driver-class-name: com.mysql.cj.jdbc.Driver
    hikari:
      maximum-pool-size: 10
      connection-timeout: 3000

mybatis:
  mapper-locations: classpath:mapper/*.xml       # XML 위치
  type-aliases-package: com.example.hr.domain    # <resultType="Emp"> 처럼 짧게 쓰기
  configuration:
    map-underscore-to-camel-case: true           # EMP_NAME → empName 자동 매핑

logging:
  level:
    com.example.hr.mapper: DEBUG                  # 실행 SQL·파라미터·결과 건수 로그
```

- `spring-boot-starter-jdbc`(스타터-webmvc에 없음)는 MyBatis 스타터가 끌고 옵니다. **HikariCP**가 기본 커넥션 풀.
- **커넥션 풀**: DB 연결은 만드는 데 비싸므로, 미리 N개 만들어 두고 빌려주고 반납받습니다.
  `Java 2` 에서 매번 `DriverManager.getConnection()` 하던 것을 풀이 대신 관리.

> DB(`hr` 스키마)와 계정은 SQL 과정 `01_실습데이터.md` 로 준비합니다. 계정 생성:
> `CREATE USER 'hr'@'%' IDENTIFIED BY 'hr1234'; GRANT ALL ON hr.* TO 'hr'@'%';`

---

## 2. JDBC 코드 → MyBatis

`Java 2.DB연결하기` 의 `EmployeeDao.findAll()` 은 이랬습니다.

```java
// 예전 (JDBC 직접)
try (Connection con = DBUtil.getConnection();
     PreparedStatement ps = con.prepareStatement("SELECT EMP_ID, EMP_NAME FROM EMP WHERE DEPT_ID = ?")) {
    ps.setLong(1, deptId);
    try (ResultSet rs = ps.executeQuery()) {
        List<Emp> list = new ArrayList<>();
        while (rs.next()) {
            Emp e = new Emp();
            e.setEmpId(rs.getLong("EMP_ID"));
            e.setEmpName(rs.getString("EMP_NAME"));
            list.add(e);
        }
        return list;
    }
}
```

MyBatis에서는 **인터페이스 + XML** 두 파일로 끝납니다.

```java
// mapper/EmpMapper.java
@Mapper
public interface EmpMapper {
    List<Emp> findByDeptId(Long deptId);
}
```

```xml
<!-- src/main/resources/mapper/EmpMapper.xml -->
<mapper namespace="com.example.hr.mapper.EmpMapper">
  <select id="findByDeptId" resultType="Emp">
    SELECT EMP_ID, EMP_NAME, EMAIL, DEPT_ID, SALARY, HIRE_DATE, ENT_YN
    FROM EMP
    WHERE DEPT_ID = #{deptId}
  </select>
</mapper>
```

| JDBC에서 직접 하던 일 | MyBatis에서는 |
|---|---|
| `getConnection()` / `close()` | 자동 (커넥션 풀 + 스프링) |
| `PreparedStatement` + `setLong(1, ...)` | `#{deptId}` |
| `ResultSet` while 루프로 객체 채우기 | `resultType` + `map-underscore-to-camel-case` 자동 매핑 |
| `SQLException` try/catch | 스프링이 런타임 예외(`DataAccessException`)로 변환 |

`@Mapper` 인터페이스는 스프링이 프록시로 구현체를 만들어 빈으로 등록합니다(우리가 클래스를 안 만듦).

---

## 3. `namespace` 와 `id`

- XML의 `namespace` = 매퍼 인터페이스의 **전체 이름**(FQCN).
- `<select id="findByDeptId">` 의 `id` = 인터페이스 **메서드 이름**.
- 이 둘이 정확히 일치해야 연결됩니다. (오타 시 `Invalid bound statement (not found)`)

---

## 4. `#{}` vs `${}`

```xml
<!-- #{} : PreparedStatement 파라미터 바인딩. 값이 ?로 들어가고 드라이버가 안전하게 처리 -->
WHERE EMP_NAME = #{name}          --  ... WHERE EMP_NAME = ?   (name 바인딩)

<!-- ${} : 문자열을 SQL에 그대로 붙임 (치환) -->
ORDER BY ${sortColumn}            --  ... ORDER BY SALARY
```

| | `#{}` | `${}` |
|---|---|---|
| 처리 | `?` 로 바인딩 | SQL 문자열에 그대로 삽입 |
| SQL 인젝션 | 안전 | **위험** (사용자 입력 절대 금지) |
| 쓸 수 있는 곳 | 값(WHERE·SET·VALUES) | 값이 아닌 부분(컬럼명·정렬 방향·테이블명) |

**원칙: 항상 `#{}`.** `${}` 는 "정렬 컬럼명"처럼 값이 아닌 자리에만, 그것도 **화이트리스트로 검증한 후**에만.

```java
// 서비스에서 화이트리스트
private static final Set<String> SORTABLE = Set.of("hireDate", "salary", "empName");
if (!SORTABLE.contains(cond.getSort())) cond.setSort("hireDate");
```

---

## 5. 메모리 Repository → MyBatis 로 교체

Day 5의 계층을 유지하면서 **구현만** 바꿉니다.

방법 A — `EmpRepository` 인터페이스를 그대로 두고 어댑터:
```java
@Repository
@RequiredArgsConstructor
public class EmpRepositoryMybatis implements EmpRepository {
    private final EmpMapper empMapper;

    public List<Emp> findAll()               { return empMapper.findAll(); }
    public Optional<Emp> findById(Long id)    { return Optional.ofNullable(empMapper.findById(id)); }
    public Emp save(Emp emp) {
        if (emp.getEmpId() == null) empMapper.insert(emp); else empMapper.update(emp);
        return emp;
    }
    public void deleteById(Long id)           { empMapper.deleteById(id); }
}
```
`EmpMemoryRepository` 에 있던 `@Repository` 를 떼거나 `@Profile("memory")` 로 남겨 두면,
`EmpService`·`EmpController` 는 한 줄도 안 바뀝니다.

방법 B (이 과정 이후 기본) — `EmpRepository` 인터페이스를 없애고 `EmpService` 가 `EmpMapper` 를
직접 씀. `@Mapper` 인터페이스가 이미 추상화이므로 중복 인터페이스를 안 둔다(Day 5 심화).

```java
@Service
@RequiredArgsConstructor
public class EmpServiceImpl implements EmpService {
    private final EmpMapper empMapper;
    // findAll() → empMapper.findList(cond) ...
}
```

---

## 6. 사원 목록 조회 완성

```java
// mapper/EmpMapper.java
@Mapper
public interface EmpMapper {
    List<Emp> findAll();
    Emp findById(Long empId);
}
```

```xml
<!-- mapper/EmpMapper.xml -->
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
        "https://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.example.hr.mapper.EmpMapper">

  <sql id="empColumns">
    EMP_ID, EMP_NAME, EMAIL, DEPT_ID, JOB_CODE, SALARY, HIRE_DATE,
    (CASE WHEN ENT_YN = 'N' THEN 1 ELSE 0 END) AS ACTIVE
  </sql>

  <select id="findAll" resultType="Emp">
    SELECT <include refid="empColumns"/>
    FROM EMP
    ORDER BY EMP_ID
  </select>

  <select id="findById" resultType="Emp">
    SELECT <include refid="empColumns"/>
    FROM EMP
    WHERE EMP_ID = #{empId}
  </select>
</mapper>
```

- `map-underscore-to-camel-case` 로 `EMP_NAME` → `empName` 자동.
- `HIRE_DATE`(DATE) → `LocalDate` 자동 변환(MyBatis 3.5+ 기본 타입 핸들러).
- `ENT_YN`('Y'/'N') → `boolean active` 는 SQL에서 `CASE` 로 0/1을 만들어 매핑.

이제 `GET /emps` 가 **진짜 MySQL의 21명**을 보여줍니다.

---

## 자주 하는 실수

- **`Invalid bound statement (not found)`** → ① `namespace` 가 인터페이스 FQCN과 불일치 ②
  `<select id>` ≠ 메서드명 ③ `mybatis.mapper-locations` 경로 오타 ④ 빌드 시 XML이 `resources` 밖.
- **`${}` 에 사용자 입력** → SQL 인젝션. 값은 무조건 `#{}`.
- **`@Mapper` 안 붙임** → 빈 등록 안 됨(`No qualifying bean of type EmpMapper`).
  또는 `@MapperScan("com.example.hr.mapper")` 를 설정 클래스에.
- **`map-underscore-to-camel-case` 안 켬** → 컬럼명과 필드명이 안 맞아 값이 `null`. `application.yml` 확인.
- **DB 접속 실패** (`Communications link failure`) → MySQL 안 켜짐/포트/방화벽/계정 권한.
- **타임존 경고/오류** → URL에 `serverTimezone=Asia/Seoul`.
- **한글 깨짐** → URL `characterEncoding=UTF-8`, DB/테이블 `utf8mb4`.

---

## 핵심 요약

| 요소 | 내용 |
|---|---|
| `spring.datasource.*` | DB 접속. HikariCP가 커넥션 풀 |
| `mybatis-spring-boot-starter` | MyBatis 자동 구성 |
| `@Mapper` 인터페이스 | 우리가 구현 안 함. 스프링이 프록시 |
| Mapper XML | `namespace`=인터페이스 FQCN, `id`=메서드명 |
| `#{}` | 파라미터 바인딩(안전). 값 자리엔 항상 이것 |
| `${}` | 문자열 치환(위험). 컬럼명 등 비-값 자리 + 화이트리스트만 |
| `map-underscore-to-camel-case` | `EMP_NAME`→`empName` 자동 |
| 계층 유지 | Repository 구현만 교체, Service·Controller 불변 |

> 다음(Day 10): 방금 만든 Mapper가 진짜 DB에 붙어 동작하는지 **JUnit 테스트**로 검증한다.
