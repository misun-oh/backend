# Day 11. MyBatis CRUD & 연관 매핑

| 항목 | 내용 |
|---|---|
| 선수학습 | Day 9(EmpMapper·XML·`#{}`), Day 10(테스트로 검증) |
| 이번 챕터 | `INSERT`/`UPDATE`/`DELETE` → `useGeneratedKeys` → 파라미터 객체·`@Param`·Map → `resultMap` → 사원↔부서 **조인 매핑**(`association`) → 각 기능 JUnit 검증 |
| 권장 진행 | 1일 |
| 결과물 | `EmpMapper` 가 조회+등록+수정+삭제 완비, 목록에 **부서명**이 나옴 |

## 학습목표

- `<insert>`/`<update>`/`<delete>` 를 작성하고, 반환값(영향 행 수)의 의미를 안다.
- `useGeneratedKeys` + `keyProperty` 로 AUTO_INCREMENT 키를 객체에 되받는다.
- 파라미터가 1개(단순/객체)일 때와 2개 이상(`@Param`)일 때 XML에서 어떻게 참조하는지 안다.
- `resultMap` 으로 컬럼↔필드 매핑을 명시하고, `<association>` 으로 조인 결과를 중첩 객체로 담는다.
- 만든 CRUD를 Day 10 방식(`@SpringBootTest @Transactional`)으로 검증한다.

---

## 1. INSERT / UPDATE / DELETE

```xml
<insert id="insert">
  INSERT INTO EMP (EMP_ID, EMP_NAME, EMAIL, DEPT_ID, JOB_CODE, SALARY, HIRE_DATE, ENT_YN)
  VALUES (#{empId}, #{empName}, #{email}, #{deptId}, #{jobCode}, #{salary}, #{hireDate}, 'N')
</insert>

<update id="update">
  UPDATE EMP
  SET EMP_NAME = #{empName}, EMAIL = #{email}, DEPT_ID = #{deptId},
      SALARY = #{salary}, HIRE_DATE = #{hireDate}
  WHERE EMP_ID = #{empId}
</update>

<delete id="deleteById">
  DELETE FROM EMP WHERE EMP_ID = #{empId}
</delete>
```

```java
@Mapper
public interface EmpMapper {
    int insert(Emp emp);            // 반환값 = 영향 받은 행 수
    int update(Emp emp);
    int deleteById(Long empId);
}
```

- 반환값은 **영향 행 수**. `update` 가 `0` 이면 "그 id가 없었다"는 뜻 → 서비스에서 판단.
- `<insert>`/`<update>`/`<delete>` 안의 파라미터도 전부 `#{}`.

---

## 2. `useGeneratedKeys` — 생성된 키 되받기

HR 스키마의 `EMP.EMP_ID` 는 문자열(`VARCHAR(3)`)이라 우리가 직접 채워야 하지만,
실무의 대다수 테이블은 `BIGINT AUTO_INCREMENT` 입니다. 그 경우:

```xml
<insert id="insert" useGeneratedKeys="true" keyProperty="empId">
  INSERT INTO EMP (EMP_NAME, EMAIL, DEPT_ID, SALARY, HIRE_DATE)
  VALUES (#{empName}, #{email}, #{deptId}, #{salary}, #{hireDate})
</insert>
```

```java
Emp emp = Emp.builder().empName("신입")...build();   // empId == null
empMapper.insert(emp);
emp.getEmpId();   // ← DB가 채운 값이 여기로 들어옴 (INSERT 후 자동)
```

- `keyProperty` = 키를 담을 **자바 필드명**.
- 시퀀스 방식(Oracle 등)은 `<selectKey>`.
- 이 과정의 실습은 상황에 따라 두 방식 다 다뤄 봅니다(HR 원본은 직접 채움, 별도 실습 테이블은 auto).

---

## 3. 파라미터 참조 규칙

| 파라미터 | XML에서 |
|---|---|
| 단일 원시/문자열 1개 | `#{아무이름}` (이름 무관, 관례상 의미 있게) |
| 객체 1개 | `#{필드명}` (`#{empName}` → `emp.getEmpName()`) |
| Map 1개 | `#{키}` |
| **2개 이상** | 각 파라미터에 `@Param("x")` 필수 → `#{x}` |

```java
List<Emp> search(@Param("keyword") String keyword, @Param("deptId") Long deptId);
```
```xml
<select id="search" resultType="Emp">
  SELECT ... FROM EMP
  WHERE EMP_NAME LIKE CONCAT('%', #{keyword}, '%')
    AND DEPT_ID = #{deptId}
</select>
```

> 검색 조건이 많아지면 `@Param` 을 나열하기보다 **DTO 하나(`EmpSearchCond`)** 로 묶어 넘깁니다(Day 12).

---

## 4. `resultMap` — 매핑을 명시

`resultType` + `map-underscore-to-camel-case` 로 안 되는 경우(별칭이 곤란, 중첩 객체, 타입 핸들러)엔
`resultMap` 을 만듭니다.

```xml
<resultMap id="empMap" type="Emp">
  <id     property="empId"    column="EMP_ID"/>
  <result property="empName"  column="EMP_NAME"/>
  <result property="email"    column="EMAIL"/>
  <result property="deptId"   column="DEPT_ID"/>
  <result property="salary"   column="SALARY"/>
  <result property="hireDate" column="HIRE_DATE"/>
  <result property="active"   column="ACTIVE"/>
</resultMap>

<select id="findAll" resultMap="empMap">
  SELECT EMP_ID, EMP_NAME, EMAIL, DEPT_ID, SALARY, HIRE_DATE,
         (CASE WHEN ENT_YN='N' THEN 1 ELSE 0 END) AS ACTIVE
  FROM EMP ORDER BY EMP_ID
</select>
```

- `<id>` = 식별자 컬럼(캐싱·중복 판정에 사용). 나머지는 `<result>`.

---

## 5. 연관(조인) 매핑 — `<association>`

**상세 화면**처럼 "사원 + 그 부서 정보 전체(코드·이름·위치)"가 함께 필요할 때는, 조인 결과를
`Emp` 안의 중첩 객체 `Dept dept` 로 담습니다. (목록은 §5 끝의 `deptName` 평평한 필드로 충분)

```java
// domain/Emp.java — 상세용 중첩 객체
public class Emp {
    // ... 기존 필드 + deptName, jobName (목록 조인용 읽기 필드) ...
    private Dept dept;     // 상세에서 <association> 으로 채움
}
```

```xml
<resultMap id="empWithDeptMap" type="Emp">
  <id     property="empId"    column="EMP_ID"/>
  <result property="empName"  column="EMP_NAME"/>
  <result property="deptId"   column="DEPT_ID"/>
  <result property="hireDate" column="HIRE_DATE"/>
  <result property="active"   column="ACTIVE"/>
  <!-- 조인한 DEPT 컬럼들을 emp.dept 로 -->
  <association property="dept" javaType="Dept">
    <id     property="deptId"   column="D_DEPT_ID"/>
    <result property="deptName" column="DEPT_TITLE"/>
    <result property="location" column="LOCATION_ID"/>
  </association>
</resultMap>

<select id="findAllWithDept" resultMap="empWithDeptMap">
  SELECT e.EMP_ID, e.EMP_NAME, e.DEPT_ID, e.HIRE_DATE,
         (CASE WHEN e.ENT_YN='N' THEN 1 ELSE 0 END) AS ACTIVE,
         d.DEPT_ID AS D_DEPT_ID, d.DEPT_TITLE, d.LOCATION_ID
  FROM EMP e
  LEFT JOIN DEPT d ON e.DEPT_ID = d.DEPT_ID
  ORDER BY e.EMP_ID
</select>
```

- 같은 이름 컬럼 충돌(`DEPT_ID`)은 **별칭**(`AS D_DEPT_ID`)으로 구분.
- `1:N`(부서 → 소속 사원 목록)은 `<collection>` (심화).

> 화면 **목록**에는 `<association>` 으로 `Dept` 객체를 통째로 담기보다, 조인 결과를 `Emp` 의
> **평평한 읽기 전용 필드**(`deptName`, `jobName`)에 담는 `resultType="Emp"` 가 더 단순합니다(Day 1).
> `<association>` 은 상세 화면처럼 "사원 + 그 부서 객체"가 통으로 필요할 때 씁니다.

```xml
<select id="findList" resultType="Emp">
  SELECT e.EMP_ID, e.EMP_NAME, e.DEPT_ID, e.JOB_CODE, e.HIRE_DATE,
         d.DEPT_TITLE AS deptName,
         j.JOB_NAME   AS jobName,
         (CASE WHEN e.ENT_YN='N' THEN true ELSE false END) AS active
  FROM EMP e
  LEFT JOIN DEPT d ON e.DEPT_ID = d.DEPT_ID
  LEFT JOIN JOB  j ON e.JOB_CODE = j.JOB_CODE
  ORDER BY e.EMP_ID
</select>
```
(`resultType="Emp"` + `map-underscore-to-camel-case` 로 `EMP_NAME→empName`. 조인해 온 컬럼은
`AS deptName` 처럼 **필드명과 똑같은 별칭**을 주면 그대로 `emp.deptName` 에 채워집니다.)

---

## 6. 테스트로 검증 (Day 10 방식)

```java
@SpringBootTest
@Transactional
class EmpCrudTest {

    @Autowired EmpMapper empMapper;

    @Test
    void 등록_수정_삭제_흐름() {
        // 등록
        Emp e = Emp.builder().empId(900L).empName("실험").email("x@ex.com")
                .deptId(5L).jobCode("J7").salary(2_000_000).hireDate(LocalDate.now()).build();
        assertThat(empMapper.insert(e)).isEqualTo(1);
        assertThat(empMapper.findById(900L).getEmpName()).isEqualTo("실험");

        // 수정
        e = Emp.builder().empId(900L).empName("실험2").email("x@ex.com")
                .deptId(6L).salary(2_100_000).hireDate(LocalDate.now()).build();
        assertThat(empMapper.update(e)).isEqualTo(1);
        assertThat(empMapper.findById(900L).getEmpName()).isEqualTo("실험2");

        // 삭제
        assertThat(empMapper.deleteById(900L)).isEqualTo(1);
        assertThat(empMapper.findById(900L)).isNull();
    }   // @Transactional → 롤백. DB는 21명 그대로

    @Test
    void 목록에_부서명이_채워진다() {
        List<Emp> rows = empMapper.findList();
        assertThat(rows).hasSize(21);
        assertThat(rows).anyMatch(e -> "총무부".equals(e.getDeptName()));   // 곽상혁의 부서
        assertThat(rows).allSatisfy(e -> assertThat(e.getEmpName()).isNotBlank());
    }
}
```

---

## 자주 하는 실수

- **`update`/`delete` 반환값을 무시** → id가 없어 0건이어도 성공한 줄 안다. 서비스에서 `if (n == 0) throw ...`.
- **파라미터 2개인데 `@Param` 안 붙임** → `Parameter 'x' not found` (또는 `param1`, `arg0` 로만 접근 가능).
- **조인 시 컬럼명 충돌** (`DEPT_ID` 가 EMP·DEPT 양쪽) → 한쪽에 `AS 별칭`, resultMap의 `column` 도 별칭으로.
- **조인 컬럼이 `Emp` 필드에 안 담김** → `d.DEPT_TITLE` 을 그냥 SELECT 하면 `emp.deptName` 이 `null`. `AS deptName` 으로 필드명과 일치시키거나 `resultMap` 에 `<result>` 명시.
- **`useGeneratedKeys` 인데 `keyProperty` 오타** → INSERT는 되는데 객체의 id가 계속 null.
- **롤백 안 되는 테스트로 CRUD 검증** → DB에 쓰레기. `@Transactional` 필수.

---

## 핵심 요약

| 요소 | 내용 |
|---|---|
| `<insert>/<update>/<delete>` | 반환값 = 영향 행 수. 0이면 대상 없음 |
| `useGeneratedKeys` + `keyProperty` | AUTO_INCREMENT 키를 객체에 되받기 |
| `@Param("x")` | 파라미터 2개 이상일 때 필수 → `#{x}` |
| `resultMap` | 컬럼↔필드 명시, `<id>` + `<result>` |
| `<association>` | 조인 결과를 중첩 객체(`emp.dept`)로 |
| 목록 조인 | `resultType="Emp"` + `LEFT JOIN` + `AS deptName`/`AS jobName` → `Emp` 읽기 필드 |
| 검증 | `@SpringBootTest @Transactional` 로 등록·수정·삭제 흐름 테스트 |

> 다음(Day 12): 검색어·부서·재직여부에 따라 SQL을 조립하는 **동적 SQL**과 **페이징**.
