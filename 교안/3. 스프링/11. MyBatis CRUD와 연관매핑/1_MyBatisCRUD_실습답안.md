# Day 11. MyBatis CRUD & 연관 매핑 — 실습 답안

전체 XML은 `EmpMapper.xml` 참고.

---

## 문제 1. CRUD 구현

```java
@Mapper
public interface EmpMapper {
    List<Emp> findAll();
    Emp findById(Long empId);
    int insert(Emp emp);
    int update(Emp emp);
    int deleteById(Long empId);
    List<Emp> findList();               // 목록: DEPT·JOB 조인해 deptName·jobName 채움
    Emp findByIdWithDept(Long empId);
    int updateSalary(@Param("empId") Long empId, @Param("salary") int salary);
}
```

```xml
<insert id="insert">
  INSERT INTO EMP (EMP_ID, EMP_NAME, EMAIL, PHONE, DEPT_ID, JOB_CODE, SALARY, HIRE_DATE, ENT_YN)
  VALUES (#{empId}, #{empName}, #{email}, #{phone}, #{deptId}, #{jobCode}, #{salary}, #{hireDate}, 'N')
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

---

## 문제 2. CRUD 흐름 테스트

```java
@SpringBootTest
@Transactional
class EmpCrudTest {
    @Autowired EmpMapper empMapper;

    @Test
    void 등록_수정_삭제() {
        Emp e = Emp.builder().empId(900L).empName("실험").email("x@ex.com")
                .deptId(5L).jobCode("J7").salary(2_000_000).hireDate(LocalDate.now()).build();

        assertThat(empMapper.insert(e)).isEqualTo(1);
        assertThat(empMapper.findById(900L).getEmpName()).isEqualTo("실험");

        Emp mod = Emp.builder().empId(900L).empName("실험2").email("x@ex.com")
                .deptId(6L).salary(2_100_000).hireDate(LocalDate.now()).build();
        assertThat(empMapper.update(mod)).isEqualTo(1);
        assertThat(empMapper.findById(900L).getEmpName()).isEqualTo("실험2");

        assertThat(empMapper.deleteById(900L)).isEqualTo(1);
        assertThat(empMapper.findById(900L)).isNull();
    }
}
```

3번 연속 통과: `@Transactional` 이 매번 롤백 → `EMP_ID = 900` 이 매 실행 시작 시 항상 존재하지 않음.

---

## 문제 3. 목록 + 부서명

```xml
<select id="findList" resultType="Emp">
  SELECT e.EMP_ID, e.EMP_NAME, e.DEPT_ID, e.JOB_CODE, e.HIRE_DATE,
         d.DEPT_TITLE AS deptName,
         j.JOB_NAME   AS jobName,
         (CASE WHEN e.ENT_YN = 'N' THEN true ELSE false END) AS active
  FROM EMP e
  LEFT JOIN DEPT d ON e.DEPT_ID = d.DEPT_ID
  LEFT JOIN JOB  j ON e.JOB_CODE = j.JOB_CODE
  ORDER BY e.EMP_ID
</select>
```

```java
@Test
void 목록에_부서명() {
    List<Emp> rows = empMapper.findList();
    assertThat(rows).hasSize(21);
    assertThat(rows).filteredOn(e -> e.getEmpId().equals(200L))
                    .first().extracting(Emp::getDeptName).isEqualTo("총무부");
    assertThat(rows).allSatisfy(e -> assertThat(e.getEmpName()).isNotBlank());
}
```

`EmpServiceImpl.findAll()` →
```java
public List<Emp> findAll() { return empMapper.findList(); }
```
화면(`/emps`)의 `${e.deptName}` 이 채워진다. (등록·수정 경로의 `Emp` 에선 `deptName` 이 `null`)

---

## 문제 4. `<association>`

```xml
<resultMap id="empWithDeptMap" type="Emp">
  <id     property="empId"    column="EMP_ID"/>
  <result property="empName"  column="EMP_NAME"/>
  <result property="email"    column="EMAIL"/>
  <result property="deptId"   column="DEPT_ID"/>
  <result property="salary"   column="SALARY"/>
  <result property="hireDate" column="HIRE_DATE"/>
  <result property="active"   column="ACTIVE"/>
  <association property="dept" javaType="com.example.hr.domain.Dept">
    <id     property="deptId"   column="D_DEPT_ID"/>
    <result property="deptName" column="DEPT_TITLE"/>
    <result property="location" column="LOCATION_ID"/>
  </association>
</resultMap>

<select id="findByIdWithDept" resultMap="empWithDeptMap">
  SELECT e.EMP_ID, e.EMP_NAME, e.EMAIL, e.DEPT_ID, e.SALARY, e.HIRE_DATE,
         (CASE WHEN e.ENT_YN='N' THEN 1 ELSE 0 END) AS ACTIVE,
         d.DEPT_ID AS D_DEPT_ID, d.DEPT_TITLE, d.LOCATION_ID
  FROM EMP e
  LEFT JOIN DEPT d ON e.DEPT_ID = d.DEPT_ID
  WHERE e.EMP_ID = #{empId}
</select>
```

```java
@Test
void 상세에_부서객체() {
    Emp e = empMapper.findByIdWithDept(205L);
    assertThat(e.getDept().getDeptName()).isEqualTo("해외영업1부");
}
```

`DEPT_ID` 컬럼이 EMP·DEPT 양쪽에 있어 `d.DEPT_ID AS D_DEPT_ID` 로 구분한 점이 핵심.

---

## 문제 5. `@Param`

```java
int updateSalary(@Param("empId") Long empId, @Param("salary") int salary);
```
```xml
<update id="updateSalary">
  UPDATE EMP SET SALARY = #{salary} WHERE EMP_ID = #{empId}
</update>
```

`@Param("salary")` 를 빼면:
```
org.apache.ibatis.binding.BindingException:
Parameter 'salary' not found. Available parameters are [arg1, arg0, param1, param2]
```
파라미터가 2개 이상이면 이름 정보가 사라져 `arg0/arg1` 또는 `param1/param2` 로만 접근 가능하다.
→ 2개 이상은 무조건 `@Param`.

---

## 문제 6. `update` 가 0

**언제 0인가**

1. `WHERE EMP_ID = #{empId}` 에 해당하는 행이 **없다**(이미 삭제됐거나 잘못된 id).
2. (낙관적 잠금 등 조건이 더 붙은 경우) 조건이 안 맞아 매칭되는 행이 없다.
   — 단, "값이 기존과 똑같아 실제로 바뀐 게 없는 경우"는 MySQL 기본 설정에서 **매칭 행 수**를 반환하므로
   보통 1이다(`useAffectedRows` 설정에 따라 다름).

**서비스 처리**
```java
public void modify(Long id, EmpForm form) {
    int n = empMapper.update(toEmp(id, form));
    if (n == 0) throw new NoSuchElementException("수정 대상 사원 없음: " + id);
}
```
`update`/`delete` 의 반환값을 **반드시 확인**해, 0이면 "대상 없음"으로 예외 → Day 14에서 404로 변환.
