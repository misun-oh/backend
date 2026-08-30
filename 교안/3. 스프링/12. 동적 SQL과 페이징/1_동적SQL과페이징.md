# Day 12. 동적 SQL과 페이징

| 항목 | 내용 |
|---|---|
| 선수학습 | Day 11(EmpMapper·resultType), SQL `4. SUBQUERY`(LIMIT), Day 1(`EmpSearchCond`) |
| 이번 챕터 | `<if>`·`<where>`·`<choose>`·`<foreach>`·`<trim>` → 검색 조건 조합 → `LIMIT` + `COUNT` 페이징 → 정렬 화이트리스트 → 목록 화면 완성 |
| 권장 진행 | 1일 |
| 결과물 | `/emps?keyword=김&deptId=D5&activeOnly=true&page=2` 로 검색·페이징 되는 사원 목록 |

## 학습목표

- `<if>`, `<where>`, `<choose>`, `<foreach>`, `<trim>`, `<bind>` 로 조건에 따라 SQL을 조립할 수 있다.
- 검색 조건 DTO(`EmpSearchCond`) 하나를 파라미터로 넘겨 동적 WHERE를 만든다.
- `LIMIT ? OFFSET ?` 로 현재 페이지를, 별도 `COUNT(*)` 로 전체 건수를 구해 `PageResult` 로 반환한다.
- 정렬 컬럼을 `${}` 로 넣되 **화이트리스트(enum)** 로만 허용한다.
- 목록 화면에 검색폼·페이지네이션을 붙인다.

---

## 1. `<if>` 와 `<where>`

```xml
<select id="findList" resultType="Emp">
  SELECT e.EMP_ID, e.EMP_NAME, e.DEPT_ID, e.HIRE_DATE,
         d.DEPT_TITLE AS deptName,
         (CASE WHEN e.ENT_YN='N' THEN true ELSE false END) AS active
  FROM EMP e
  LEFT JOIN DEPT d ON e.DEPT_ID = d.DEPT_ID
  <where>
    <if test="keyword != null and keyword != ''">
      AND (e.EMP_NAME LIKE CONCAT('%', #{keyword}, '%')
           OR e.EMAIL LIKE CONCAT('%', #{keyword}, '%'))
    </if>
    <if test="deptId != null and deptId != ''">
      AND e.DEPT_ID = #{deptId}
    </if>
    <if test="activeOnly">
      AND e.ENT_YN = 'N'
    </if>
  </where>
  ORDER BY e.EMP_ID
</select>
```

- `<where>` : 안에 조건이 하나라도 있으면 `WHERE` 를 붙이고, **맨 앞의 `AND`/`OR` 를 자동 제거**한다.
  조건이 하나도 없으면 `WHERE` 자체를 안 붙인다.
- `test` 는 OGNL 식. 문자열은 `keyword != null and keyword != ''` 로 빈 값도 거른다.
- 파라미터가 `EmpSearchCond` 객체 하나면, `test="keyword != null"` 처럼 **필드명으로 바로** 참조.

```java
List<Emp> findList(EmpSearchCond cond);
long countList(EmpSearchCond cond);
```

---

## 2. `<choose>` / `<when>` / `<otherwise>`

여러 조건 중 **하나만** 적용(자바 `switch`).

```xml
<choose>
  <when test="status == 'ACTIVE'">  AND e.ENT_YN = 'N' </when>
  <when test="status == 'LEFT'">    AND e.ENT_YN = 'Y' </when>
  <otherwise>                       <!-- 전체 --> </otherwise>
</choose>
```

---

## 3. `<foreach>` — IN 절

```java
List<Emp> findByIds(@Param("ids") List<Long> ids);
```
```xml
<select id="findByIds" resultType="Emp">
  SELECT <include refid="cols"/> FROM EMP
  WHERE EMP_ID IN
  <foreach collection="ids" item="id" open="(" separator="," close=")">
    #{id}
  </foreach>
</select>
```

- `ids` 가 빈 리스트면 `IN ()` → SQL 오류. 서비스에서 **빈 리스트를 먼저 거른다**
  (`if (ids.isEmpty()) return List.of();`).
- `collection` 값: `List` 는 `list` 또는 `@Param` 이름, 배열은 `array`, `Map` 은 키.

---

## 4. `<trim>`, `<set>`, `<bind>`

- `<trim prefix="WHERE" prefixOverrides="AND |OR ">` : `<where>` 의 일반형. `<set>` 은 UPDATE용(Day 11 심화).
- `<bind name="pattern" value="'%' + keyword + '%'"/>` : 변수 미리 계산.
  ```xml
  <bind name="kw" value="'%' + keyword + '%'"/>
  ... e.EMP_NAME LIKE #{kw}
  ```

---

## 5. 페이징 — LIMIT + COUNT

목록 화면에는 **두 개의 SQL**이 필요합니다.

1. 현재 페이지 데이터: `... LIMIT #{size} OFFSET #{offset}`
2. 페이지 버튼용 전체 건수: `SELECT COUNT(*) ...` (같은 WHERE, ORDER BY·LIMIT 없이)

### DTO

```java
// dto/EmpSearchCond.java
@Getter @Setter
public class EmpSearchCond {
    private String keyword;
    private String deptId;
    private boolean activeOnly;
    private EmpSort sort = EmpSort.EMP_ID;
    private int page = 1;               // 1부터
    private int size = 10;

    public int getOffset() { return (Math.max(page, 1) - 1) * size; }
}

// dto/PageResult.java
public record PageResult<T>(List<T> content, long totalElements, int page, int size) {
    public int totalPages() { return (int) Math.ceil((double) totalElements / size); }
    public boolean hasPrev() { return page > 1; }
    public boolean hasNext() { return page < totalPages(); }
}
```

### XML

```xml
<sql id="searchWhere">
  <where>
    <if test="keyword != null and keyword != ''">
      AND (e.EMP_NAME LIKE CONCAT('%', #{keyword}, '%') OR e.EMAIL LIKE CONCAT('%', #{keyword}, '%'))
    </if>
    <if test="deptId != null and deptId != ''"> AND e.DEPT_ID = #{deptId} </if>
    <if test="activeOnly"> AND e.ENT_YN = 'N' </if>
  </where>
</sql>

<select id="findPage" resultType="Emp">
  SELECT e.EMP_ID, e.EMP_NAME, e.DEPT_ID, e.HIRE_DATE,
         d.DEPT_TITLE AS deptName,
         (CASE WHEN e.ENT_YN='N' THEN true ELSE false END) AS active
  FROM EMP e LEFT JOIN DEPT d ON e.DEPT_ID = d.DEPT_ID
  <include refid="searchWhere"/>
  ORDER BY e.${sort.column} ${sort.direction}
  LIMIT #{size} OFFSET #{offset}
</select>

<select id="countPage" resultType="long">
  SELECT COUNT(*)
  FROM EMP e LEFT JOIN DEPT d ON e.DEPT_ID = d.DEPT_ID
  <include refid="searchWhere"/>
</select>
```

### Service

```java
@Override
public PageResult<Emp> search(EmpSearchCond cond) {
    List<Emp> content = empMapper.findPage(cond);
    long total = empMapper.countPage(cond);
    return new PageResult<>(content, total, cond.getPage(), cond.getSize());
}
```

> `COUNT(*)` 는 조인을 안 해도 되면 빼서 더 빠르게(여기선 DEPT 조인이 결과 수에 영향 없으므로
> `FROM EMP e` 만으로도 됨). 대량이면 `SQL_CALC_FOUND_ROWS`(deprecated)·커버링 인덱스·키셋 페이징(심화).

---

## 6. 정렬 — `${}` 는 enum으로만

`ORDER BY ${...}` 는 값이 아니라 SQL 조각이라 `#{}` 를 못 씁니다. 그래서 **임의 문자열이 못 들어오게**
enum으로 제한합니다.

```java
public enum EmpSort {
    EMP_ID("e.EMP_ID", "ASC"),
    HIRE_DATE("e.HIRE_DATE", "DESC"),
    SALARY("e.SALARY", "DESC"),
    NAME("e.EMP_NAME", "ASC");

    public final String column;
    public final String direction;
    EmpSort(String c, String d) { this.column = c; this.direction = d; }
}
```

컨트롤러가 `?sort=SALARY` 를 받으면 `EmpSort.valueOf("SALARY")` — 목록에 없는 값이면 스프링이
400 또는 기본값. XML에서는 `${sort.column} ${sort.direction}` → `e.SALARY DESC`. 사용자가 `sort`
문자열을 조작해도 enum 상수 밖은 못 만듭니다.

---

## 7. 화면 연결

```java
@GetMapping("/emps")
public String list(@ModelAttribute EmpSearchCond cond, Model model) {
    PageResult<Emp> page = empService.search(cond);
    model.addAttribute("page", page);
    model.addAttribute("cond", cond);
    model.addAttribute("depts", deptService.findAll());
    return "emp/list";
}
```

```html
<!-- 검색폼 -->
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

<!-- 페이지네이션 -->
<nav class="pagination" th:if="${page.totalPages() > 1}">
  <a class="pagination__item" th:if="${page.hasPrev()}"
     th:href="@{/emps(keyword=${cond.keyword}, deptId=${cond.deptId}, activeOnly=${cond.activeOnly}, page=${page.page - 1})}">‹</a>
  <a class="pagination__item"
     th:each="p : ${#numbers.sequence(1, page.totalPages())}"
     th:href="@{/emps(keyword=${cond.keyword}, deptId=${cond.deptId}, activeOnly=${cond.activeOnly}, page=${p})}"
     th:text="${p}" th:classappend="${p == page.page} ? 'is-current' : ''"></a>
  <a class="pagination__item" th:if="${page.hasNext()}"
     th:href="@{/emps(..., page=${page.page + 1})}">›</a>
</nav>
```

> 페이지 링크에 **현재 검색 조건을 계속 붙여야** 2페이지로 넘어가도 검색이 유지됩니다.
> 조건이 많으면 `th:href` 가 지저분해지므로 fragment 나 `@{...(${cond})}` (객체 펼치기)로 정리.

---

## 자주 하는 실수

- **`<where>` 없이 `WHERE 1=1 AND ...`** → 동작은 하지만 `<where>` 를 쓰는 게 깔끔. `<if>` 안의 조건은 `AND` 로 시작.
- **`test="keyword != ''"` 만** → `keyword` 가 `null` 이면 OGNL 오류. `keyword != null and keyword != ''`.
- **`<foreach>` 에 빈 컬렉션** → `IN ()` SQL 오류. 서비스에서 빈 경우 조기 반환.
- **정렬을 `#{sort}` 로** → `ORDER BY '?'` 가 되어 정렬 안 됨(문자열 상수로 정렬). `${}` + enum.
- **`${}` 에 사용자 문자열 그대로** → 인젝션. enum·화이트리스트 필수.
- **COUNT SQL의 WHERE 를 데이터 SQL과 다르게** → 페이지 수가 안 맞음. `<sql>` 로 공유.
- **페이지 링크에 검색 조건 안 붙임** → 2페이지에서 검색이 풀림.

---

## 핵심 요약

| 태그 | 용도 |
|---|---|
| `<if test="...">` | 조건부 SQL 조각 |
| `<where>` | 조건 있을 때만 WHERE, 앞의 AND/OR 제거 |
| `<choose>/<when>/<otherwise>` | 여러 중 하나 |
| `<foreach>` | IN 절, 다중 값 (빈 컬렉션 주의) |
| `<bind>` | 표현식 미리 계산 |
| 페이징 | `LIMIT #{size} OFFSET #{offset}` + 별도 `COUNT(*)` |
| 정렬 | `ORDER BY ${enum.column} ${enum.direction}` (화이트리스트) |
| `PageResult` | content + totalElements → totalPages/hasNext |

> 다음(Day 13): 등록·수정처럼 **여러 SQL을 하나로 묶는** 트랜잭션.
