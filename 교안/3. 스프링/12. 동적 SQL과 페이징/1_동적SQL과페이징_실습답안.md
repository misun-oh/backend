# Day 12. 동적 SQL과 페이징 — 실습 답안

---

## 문제 1. DTO / enum

```java
@Getter @Setter
public class EmpSearchCond {
    private String keyword;
    private String deptId;
    private boolean activeOnly;
    private EmpSort sort = EmpSort.EMP_ID;
    private int page = 1;
    private int size = 10;
    public int getOffset() { return (Math.max(page, 1) - 1) * size; }
}

public record PageResult<T>(List<T> content, long totalElements, int page, int size) {
    public int totalPages()  { return size == 0 ? 0 : (int) Math.ceil((double) totalElements / size); }
    public boolean hasPrev() { return page > 1; }
    public boolean hasNext() { return page < totalPages(); }
}

public enum EmpSort {
    EMP_ID("EMP_ID", "ASC"),
    HIRE_DATE("HIRE_DATE", "DESC"),
    SALARY("SALARY", "DESC"),
    NAME("EMP_NAME", "ASC");
    public final String column, direction;
    EmpSort(String c, String d) { this.column = c; this.direction = d; }
}
```

---

## 문제 2. 동적 검색 SQL

```xml
<sql id="searchWhere">
  <where>
    <if test="keyword != null and keyword != ''">
      AND (e.EMP_NAME LIKE CONCAT('%', #{keyword}, '%')
           OR e.EMAIL LIKE CONCAT('%', #{keyword}, '%'))
    </if>
    <if test="deptId != null and deptId != ''"> AND e.DEPT_ID = #{deptId} </if>
    <if test="activeOnly"> AND e.ENT_YN = 'N' </if>
  </where>
</sql>

<select id="findPage" resultType="Emp">
  SELECT e.EMP_ID, e.EMP_NAME, e.DEPT_ID, e.JOB_CODE, e.HIRE_DATE,
         d.DEPT_TITLE AS deptName,
         j.JOB_NAME AS jobName,
         (CASE WHEN e.ENT_YN='N' THEN true ELSE false END) AS active
  FROM EMP e
  LEFT JOIN DEPT d ON e.DEPT_ID = d.DEPT_ID
  LEFT JOIN JOB  j ON e.JOB_CODE = j.JOB_CODE
  <include refid="searchWhere"/>
  ORDER BY e.${sort.column} ${sort.direction}
  LIMIT #{size} OFFSET #{offset}
</select>

<select id="countPage" resultType="long">
  SELECT COUNT(*) FROM EMP e
  <include refid="searchWhere"/>
</select>
```

테스트:
```java
@SpringBootTest @Transactional
class EmpSearchTest {
    @Autowired EmpMapper mapper;

    private EmpSearchCond cond() { return new EmpSearchCond(); }

    @Test void 조건없음_21()      { assertThat(mapper.countPage(cond())).isEqualTo(21); }
    @Test void 재직만_20()        { var c = cond(); c.setActiveOnly(true);  assertThat(mapper.countPage(c)).isEqualTo(20); }
    @Test void D5_5명()          { var c = cond(); c.setDeptId("D5");       assertThat(mapper.countPage(c)).isEqualTo(5); }
    @Test void D5_재직_5명()      { var c = cond(); c.setDeptId("D5"); c.setActiveOnly(true); assertThat(mapper.countPage(c)).isEqualTo(5); }
    @Test void 키워드_김()        {
        var c = cond(); c.setKeyword("김");
        assertThat(mapper.findPage(c)).extracting("empName").allMatch(n -> ((String)n).contains("김"));
    }
}
```
> 주의: `findPage` 를 부를 땐 `size`/`offset` 이 필요하므로 `cond` 기본값(size 10, page 1)을 그대로 사용.
> `countPage` 는 `LIMIT` 이 없으니 조건만 반영.

---

## 문제 3. 페이징

```java
@Override
public PageResult<Emp> search(EmpSearchCond cond) {
    List<Emp> content = empMapper.findPage(cond);
    long total = empMapper.countPage(cond);
    return new PageResult<>(content, total, cond.getPage(), cond.getSize());
}
```

```java
@Test void 첫페이지_10건_총3페이지() {
    var c = new EmpSearchCond();
    var p = empService.search(c);
    assertThat(p.content()).hasSize(10);
    assertThat(p.totalElements()).isEqualTo(21);
    assertThat(p.totalPages()).isEqualTo(3);
    assertThat(p.hasNext()).isTrue();
}
@Test void 마지막페이지_1건() {
    var c = new EmpSearchCond(); c.setPage(3);
    var p = empService.search(c);
    assertThat(p.content()).hasSize(1);
    assertThat(p.hasNext()).isFalse();
}
```

---

## 문제 4. 정렬

```java
@Test void 급여내림차순_첫행은_곽상혁() {
    var c = new EmpSearchCond();
    c.setSort(EmpSort.SALARY);
    var p = empService.search(c);
    assertThat(p.content().get(0).empName()).isEqualTo("곽상혁");   // 8,000,000
}
```

`?sort=DROP TABLE` 로 호출하면: 스프링이 `String "DROP TABLE"` → `EmpSort` 변환을 시도하다
`IllegalArgumentException: No enum constant EmpSort.DROP TABLE` → **400 Bad Request**.
enum 상수 밖의 값은 아예 컨트롤러에 들어오지 못하므로, `${sort.column}` 에는 항상 안전한 컬럼명만.

---

## 문제 5. `<foreach>` IN

```xml
<select id="findByIds" resultType="Emp">
  SELECT <include refid="cols"/> FROM EMP
  WHERE EMP_ID IN
  <foreach collection="ids" item="id" open="(" separator="," close=")">#{id}</foreach>
</select>
```

```java
@Test void 세개중_존재하는_두건() {
    assertThat(mapper.findByIds(List.of(200L, 205L, 999L)))
        .extracting(Emp::getEmpId).containsExactlyInAnyOrder(200L, 205L);
}
```

`ids = []` 이면 SQL이 `... WHERE EMP_ID IN ()` → **SQL 문법 오류**.
서비스에서 방어:
```java
public List<Emp> getByIds(List<Long> ids) {
    if (ids == null || ids.isEmpty()) return List.of();
    return empMapper.findByIds(ids);
}
```

---

## 문제 6. 화면 (핵심)

```html
<form th:object="${cond}" method="get" action="/emps" class="toolbar">
  <input class="input" type="search" th:field="*{keyword}" placeholder="이름·이메일">
  <select class="select" th:field="*{deptId}">
    <option value="">전체 부서</option>
    <option th:each="d : ${depts}" th:value="${d.deptId}" th:text="${d.deptName}"></option>
  </select>
  <label class="switch"><input type="checkbox" th:field="*{activeOnly}">
    <span class="switch__track"><span class="switch__thumb"></span></span>
    <span class="switch__label">재직자만</span></label>
  <button class="btn btn--secondary">검색</button>
</form>

<nav class="pagination" th:if="${page.totalPages() > 1}">
  <a class="pagination__item"
     th:each="p : ${#numbers.sequence(1, page.totalPages())}"
     th:href="@{/emps(keyword=${cond.keyword}, deptId=${cond.deptId}, activeOnly=${cond.activeOnly}, sort=${cond.sort}, page=${p})}"
     th:text="${p}"
     th:classappend="${p == page.page} ? ' is-current' : ''"></a>
</nav>
```

페이지 링크에 `keyword/deptId/activeOnly/sort` 를 다 실었으므로, `김` 을 검색한 상태로 2페이지를
눌러도 `/emps?keyword=김&...&page=2` 가 되어 검색이 유지된다.
