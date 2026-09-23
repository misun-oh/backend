# Day 11-1. 동적 SQL과 검색 — 실습 답안

---

## 문제 1. 검색 조건 DTO

```java
@Data
public class EmpSearchCond {
    private String keyword;
    private String deptId;
    private boolean workingOnly;
}
```

---

## 문제 2. 동적 검색 SQL

```xml
<sql id="searchWhere">
  <where>
    <if test="keyword != null and keyword != ''">
      AND (e.emp_name LIKE CONCAT('%', #{keyword}, '%')
           OR e.email LIKE CONCAT('%', #{keyword}, '%')
           OR e.emp_no LIKE CONCAT('%', #{keyword}, '%'))
    </if>
    <if test="deptId != null and deptId != ''"> AND e.dept_id = #{deptId} </if>
    <if test="workingOnly"> AND e.ent_yn = 'N' </if>
  </where>
</sql>

<select id="selectByCond" resultType="EmpDto">
  SELECT e.emp_id, e.emp_name, e.email, e.emp_no,
         e.dept_id, d.dept_title AS deptName,
         e.hire_date, e.ent_yn, e.phone, e.salary, e.bonus
  FROM emp e
  LEFT JOIN dept d ON e.dept_id = d.dept_id
  <include refid="searchWhere"/>
  ORDER BY e.emp_id
</select>
```

테스트:
```java
@SpringBootTest
class EmpSearchTest {
    @Autowired EmpMapper mapper;

    private EmpSearchCond cond() { return new EmpSearchCond(); }

    @Test void 조건없음_21()      { assertThat(mapper.selectByCond(cond())).hasSize(21); }
    @Test void 재직만_20()        { var c = cond(); c.setWorkingOnly(true); assertThat(mapper.selectByCond(c)).hasSize(20); }
    @Test void D5_5명()          { var c = cond(); c.setDeptId("D5");      assertThat(mapper.selectByCond(c)).hasSize(5); }
    @Test void D5_재직_5명()      { var c = cond(); c.setDeptId("D5"); c.setWorkingOnly(true); assertThat(mapper.selectByCond(c)).hasSize(5); }
    @Test void 키워드_김()        {
        var c = cond(); c.setKeyword("김");
        assertThat(mapper.selectByCond(c)).extracting("empName").allMatch(n -> ((String) n).contains("김"));
    }
}
```
> 아직 `LIMIT`이 없으므로 `selectByCond` 결과의 크기(`.size()`)가 곧 조건에 맞는 전체 건수입니다.
> `2_페이징.md` 에서 `LIMIT`을 붙이고 나면 더 이상 이 방법으로 전체 건수를 알 수 없어서, 별도의
> `countByCond` 가 필요해집니다.

---

## 문제 3. `<foreach>` IN

```xml
<select id="findByIds" resultType="EmpDto">
  SELECT emp_id, emp_name FROM emp
  WHERE emp_id IN
  <foreach collection="ids" item="id" open="(" separator="," close=")">#{id}</foreach>
</select>
```

```java
@Test void 세개중_존재하는_두건() {
    assertThat(mapper.findByIds(List.of(200, 205, 999)))
        .extracting("empId").containsExactlyInAnyOrder(200, 205);
}
```

`ids = []` 이면 SQL이 `... WHERE emp_id IN ()` → **SQL 문법 오류**.
서비스에서 방어:
```java
public List<EmpDto> getByIds(List<Integer> ids) {
    if (ids == null || ids.isEmpty()) return List.of();
    return mapper.findByIds(ids);
}
```

---

## 문제 4. 화면 검색폼

```html
<form class="toolbar" th:object="${cond}" method="get" action="/emps">
  <input class="input" type="search" th:field="*{keyword}" placeholder="이름 · 사번 · 이메일">
  <select class="select" th:field="*{deptId}">
    <option value="">전체 부서</option>
    <option th:each="d : ${depts}" th:value="${d.deptId}" th:text="${d.deptTitle}"></option>
  </select>
  <label class="switch">
    <input type="checkbox" th:field="*{workingOnly}">
    <span class="switch__label">재직자만</span>
  </label>
  <button class="btn btn--secondary">검색</button>
</form>
```

`method="get"` 이므로 검색 후 URL이 `/emps?keyword=김&deptId=D5&workingOnly=true` 형태가 되고,
새로고침·북마크해도 검색 조건이 그대로 유지됩니다.
