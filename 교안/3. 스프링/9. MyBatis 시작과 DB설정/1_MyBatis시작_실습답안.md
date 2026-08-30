# Day 9. MyBatis 시작 & DB 설정 — 실습 답안

---

## 문제 1. DB 연결

`build.gradle`:
```groovy
implementation 'org.mybatis.spring.boot:mybatis-spring-boot-starter:4.0.1'
runtimeOnly   'com.mysql:mysql-connector-j'
```
`application.yml` : 교안 1절 그대로.

기동 로그:
```
HikariPool-1 - Starting...
HikariPool-1 - Added connection com.mysql.cj.jdbc.ConnectionImpl@...
HikariPool-1 - Start completed.
```

---

## 문제 2. EmpMapper — 목록/상세

```java
// mapper/EmpMapper.java
@Mapper
public interface EmpMapper {
    List<Emp> findAll();
    Emp findById(Long empId);
}
```

```xml
<!-- src/main/resources/mapper/EmpMapper.xml -->
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
        "https://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.example.hr.mapper.EmpMapper">

  <sql id="cols">
    EMP_ID, EMP_NAME, EMAIL, DEPT_ID, JOB_CODE, SALARY, HIRE_DATE,
    (CASE WHEN ENT_YN = 'N' THEN 1 ELSE 0 END) AS ACTIVE
  </sql>

  <select id="findAll" resultType="Emp">
    SELECT <include refid="cols"/> FROM EMP ORDER BY EMP_ID
  </select>

  <select id="findById" resultType="Emp">
    SELECT <include refid="cols"/> FROM EMP WHERE EMP_ID = #{empId}
  </select>
</mapper>
```

임시 컨트롤러:
```java
@RestController
@RequiredArgsConstructor
class RawEmpController {
    private final EmpMapper empMapper;
    @GetMapping("/raw/emps")        List<Emp> all()               { return empMapper.findAll(); }
    @GetMapping("/raw/emps/{id}")   Emp one(@PathVariable Long id) { return empMapper.findById(id); }
}
```
`GET /raw/emps` → 21건, `GET /raw/emps/200` → 곽상혁.

---

## 문제 3. 계층 연결

**방법 A** (인터페이스 유지):

```java
// EmpMemoryRepository : @Repository 제거하거나
@Repository
@Profile("memory")
public class EmpMemoryRepository implements EmpRepository { ... }

// 새 구현
@Repository
@RequiredArgsConstructor
public class EmpRepositoryMybatis implements EmpRepository {
    private final EmpMapper empMapper;
    public List<Emp> findAll()            { return empMapper.findAll(); }
    public Optional<Emp> findById(Long id){ return Optional.ofNullable(empMapper.findById(id)); }
    public Emp save(Emp emp)              { /* insert/update — Day 11 */ return emp; }
    public void deleteById(Long id)       { /* Day 11 */ }
}
```
→ `EmpService`, `EmpController` **0줄 수정**. 스프링이 `EmpRepository` 자리에 새 구현을 주입.

**방법 B** (인터페이스 제거):
```java
@Service
@RequiredArgsConstructor
public class EmpServiceImpl implements EmpService {
    private final EmpMapper empMapper;

    public List<Emp> findAll() {
        return empMapper.findAll();          // 부서명 조인은 Day 11
    }
    public Emp get(Long id) {
        Emp e = empMapper.findById(id);
        if (e == null) throw new NoSuchElementException("사원 없음: " + id);
        return e;
    }
    // register/modify/remove 는 Day 11
}
```
→ `EmpServiceImpl` 내부만 수정(생성자 필드 + 메서드 본문). `EmpController` 는 그대로.

**확인**: A는 상위 계층 0줄, B는 `EmpServiceImpl` 만. 어느 쪽이든 `EmpController` 는 안 바뀐다 —
인터페이스에 의존했기 때문(Day 4~5).

---

## 문제 4. `#{}` vs `${}`

```xml
<select id="findByNameLikeSafe" resultType="Emp">
  SELECT <include refid="cols"/> FROM EMP
  WHERE EMP_NAME LIKE CONCAT('%', #{keyword}, '%')
</select>

<!-- 학습용, 실습 후 삭제 -->
<select id="findByNameLikeUnsafe" resultType="Emp">
  SELECT <include refid="cols"/> FROM EMP
  WHERE EMP_NAME LIKE '%${keyword}%'
</select>
```

`keyword = 김' OR '1'='1` 입력 시:

- **safe(`#{}`)**: 실제 실행 SQL은 `... LIKE CONCAT('%', ?, '%')`, `?` 에 문자열 `김' OR '1'='1`
  이 통째로 바인딩 → 이름에 그 문자열이 들어간 사원(없음) → **0건**.
- **unsafe(`${}`)**: SQL이 `... LIKE '%김' OR '1'='1%'` 로 조립됨 → `OR '1'='1'` 이 참 →
  **전체 21건** (인젝션 성공).

→ 값 자리엔 무조건 `#{}`. `${}` 는 SQL 조각(정렬 컬럼 등)에만, 화이트리스트 후.

---

## 문제 5. 오류 진단

1. **`Invalid bound statement (not found)`**
   - `namespace` 가 `com.example.hr.mapper.EmpMapper` 와 정확히 일치하지 않음(오타/패키지 다름).
   - `<select id="findAll">` 이 없거나 메서드명과 다름.
   - (추가) `mybatis.mapper-locations: classpath:mapper/*.xml` 경로가 실제 XML 위치와 다름 /
     XML이 `src/main/resources` 밖에 있어 빌드 산출물에 안 들어감.

2. **필드가 전부 null**
   - `mybatis.configuration.map-underscore-to-camel-case: true` 가 빠짐(`EMP_NAME` ↔ `empName` 미매핑).
   - `SELECT *` 로 가져왔는데 도메인 필드명과 컬럼명이 안 맞음 / 별칭(`AS empName`) 미지정.

3. **`Access denied for user 'hr'@'localhost'`**
   - 비밀번호 불일치(`application.yml` vs 실제 계정).
   - 계정이 `'hr'@'%'` 로만 생성돼 `'hr'@'localhost'` 접속이 별도 인증됨 / `GRANT` 누락.
