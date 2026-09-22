# Day 11-1. 동적 SQL과 검색

| 항목 | 내용 |
|---|---|
| 선수학습 | Day 9(EmpMapper·resultType), Day 10(Thymeleaf 화면), Day 1(조건문) |
| 이번 챕터 | `<if>`·`<where>`·`<choose>`·`<foreach>`·`<trim>`·`<bind>` → 검색 조건 조합 → 검색 화면 완성 (페이징은 `2_페이징.md`에서 이어서) |
| 권장 진행 | 0.5일 |
| 결과물 | `/emps?keyword=김&deptId=D5&workingOnly=true` 로 검색되는 사원 목록 (페이징 없이 전체 결과) |

## 학습목표

- `<if>`, `<where>`, `<choose>`, `<foreach>`, `<trim>`, `<bind>` 로 조건에 따라 SQL을 조립할 수 있다.
- 검색 조건을 담는 DTO(`cond`) 하나를 파라미터로 넘겨 동적 WHERE를 만든다.
- 검색폼을 컨트롤러의 `cond`와 연결해 화면에서 검색이 동작하게 만든다.

---

## 1. `<if>` 와 `<where>`

> 아래 1~4절 예제는 모두 검색조건을 담을 `EmpSearchCond`(줄여서 `cond`) 객체 하나를 매퍼
> 파라미터로 받는다고 가정합니다. `cond`가 정확히 어떤 필드를 가지는지는 5절에서 정의합니다 —
> 지금은 "`cond.keyword`, `cond.deptId`, `cond.workingOnly` 같은 필드가 있는 객체구나" 정도로
> 읽고 넘어가면 됩니다.

```xml
<select id="selectByCond" resultType="EmpDto">
  SELECT e.emp_id, e.emp_name, e.email, e.emp_no,
         e.dept_id, d.dept_title AS deptName,
         e.hire_date, e.ent_yn, e.phone, e.salary, e.bonus
  FROM emp e
  LEFT JOIN dept d ON e.dept_id = d.dept_id
  <where>
    <if test="keyword != null and keyword != ''">
      AND (e.emp_name LIKE CONCAT('%', #{keyword}, '%')
           OR e.email LIKE CONCAT('%', #{keyword}, '%'))
    </if>
    <if test="deptId != null and deptId != ''">
      AND e.dept_id = #{deptId}
    </if>
    <if test="workingOnly">
      AND e.ent_yn = 'N'
    </if>
  </where>
  ORDER BY e.emp_id
</select>
```

- `<where>` : 안에 조건이 하나라도 있으면 `WHERE` 를 붙이고, **맨 앞의 `AND`/`OR` 를 자동 제거**한다.
  조건이 하나도 없으면 `WHERE` 자체를 안 붙인다.
- `test` 는 OGNL 식. 문자열은 `keyword != null and keyword != ''` 로 빈 값도 거른다.
- 파라미터가 검색조건 DTO(`cond`) 객체 하나면, `test="keyword != null"` 처럼 **필드명으로 바로** 참조.

```java
// EmpMapper.java
List<EmpDto> selectByCond(EmpSearchCond cond);
```

---

## 2. `<choose>` / `<when>` / `<otherwise>`

여러 조건 중 **하나만** 적용(자바 `switch`).

```xml
<choose>
  <when test="status == 'ACTIVE'">  AND e.ent_yn = 'N' </when>
  <when test="status == 'LEFT'">    AND e.ent_yn = 'Y' </when>
  <otherwise>                       <!-- 전체 --> </otherwise>
</choose>
```

---

## 3. `<foreach>` — IN 절

```java
List<EmpDto> findByIds(@Param("ids") List<Integer> ids);
```
```xml
<select id="findByIds" resultType="EmpDto">
  SELECT emp_id, emp_name FROM emp
  WHERE emp_id IN
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

### `<trim>` — `<where>`가 사실 이걸로 만들어진 것

1절에서 쓴 `<where>` 는 사실 **`<trim>` 을 미리 설정해 둔 특수한 형태**입니다. `<trim>` 은
자식 내용의 **앞/뒤에 뭘 붙일지**, **앞/뒤의 뭘 지울지**를 직접 정하는 범용 도구입니다.

| 속성 | 뜻 |
|---|---|
| `prefix` | 자식 내용 앞에 붙일 문자열 |
| `prefixOverrides` | 자식 내용 맨 앞에 이 문자열이 있으면 지움(`\|` 로 여러 개 나열) |
| `suffix` / `suffixOverrides` | 뒤쪽 기준으로 동일 |

```xml
<!-- 1절의 <where> 를 <trim> 으로 풀어 쓰면 -->
<trim prefix="WHERE" prefixOverrides="AND |OR ">
  <if test="keyword != null and keyword != ''">
    AND (e.emp_name LIKE ... OR e.email LIKE ...)
  </if>
  <if test="deptId != null and deptId != ''">
    AND e.dept_id = #{deptId}
  </if>
</trim>
```

`<where>`/`<set>`(UPDATE용, `SET`을 붙이고 뒤 콤마 제거 — **Day 9 심화**)으로 대부분 충분하지만,
그 둘의 프리셋으로 안 되는 모양(예: 맨 **앞**의 콤마를 지워야 하는 경우)이 필요할 때만 `<trim>`을
직접 씁니다.

### `<bind>` — 검색어를 DB 함수 없이 안전하게 조립

1절 예제는 `LIKE` 검색에 `CONCAT('%', #{keyword}, '%')` 를 썼습니다. `CONCAT`은 **DB 함수**라
DB마다 이름·문법이 다릅니다(SQL Server는 `+`). `<bind>`는 자바 쪽(OGNL)에서 문자열을 **미리
조립**해서 넘기는, DB에 의존하지 않는 방법입니다.

```xml
<select id="selectByCond" resultType="EmpDto">
  <bind name="kw" value="'%' + keyword + '%'"/>
  SELECT e.emp_id, e.emp_name, ...
  FROM emp e
  <where>
    <if test="keyword != null and keyword != ''">
      AND (e.emp_name LIKE #{kw} OR e.email LIKE #{kw})
    </if>
  </where>
</select>
```

- `value` 는 **OGNL 표현식** — 파라미터 객체의 필드(`keyword`)를 자바 문자열처럼 `+` 로 이어 붙입니다.
- `#{kw}` 로 바인딩되므로 여전히 **파라미터 바인딩**(SQL 인젝션 안전)이고, DB 함수가 없어
  **어떤 DB로 바꿔도 그대로** 동작합니다.

---

## 5. 검색 조건 DTO(`cond`) 만들기

화면에서 넘어온 검색어·필터를 담을 그릇이 필요합니다. 나중에(`2_페이징.md`) 페이지 요청 정보(`page`,
`size`, 정렬)도 이 DTO에 함께 담을 예정이지만, 지금은 **검색 조건만** 담습니다.

```java
// dto/EmpSearchCond.java
@Data
public class EmpSearchCond {
    private String keyword;
    private String deptId;
    private boolean workingOnly;
}
```

`<sql>` 로 WHERE 조각을 따로 빼두면, 나중에 "현재 페이지 목록"과 "전체 건수" **두 SQL이 똑같은
조건을 공유**해야 할 때(2_페이징.md) 그대로 재사용할 수 있습니다.

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

### Service

```java
@Override
public void selectByCond(EmpSearchCond cond, Model model) {
    List<EmpDto> list = mapper.selectByCond(cond);
    model.addAttribute("list", list);
}
```

---

## 6. 화면 연결

```java
// EmpController.java
@GetMapping({"/", "/emps"})
public String list(@ModelAttribute EmpSearchCond cond, Model model) {
    service.selectByCond(cond, model);

    model.addAttribute("cond", cond);
    model.addAttribute("depts", deptService.selectAll());
    return "/index";
}
```

- `@ModelAttribute EmpSearchCond cond` : 쿼리스트링(`keyword`, `deptId`, `workingOnly`)이 필드명
  기준으로 자동 바인딩된다.
- 검색폼에 다시 값을 채워 넣어야 하니 `cond` 자체도 모델에 실어 화면으로 돌려준다.

```html
<!-- 검색폼: templates/index.html -->
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

> `method="get"` 이라 조건이 URL 쿼리스트링에 남는다 → 새로고침·북마크·뒤로가기에 그대로 재사용된다.
> (다음 문서에서 페이지 링크에도 이 조건을 그대로 이어붙인다.)

---

## 자주 하는 실수

- **`<where>` 없이 `WHERE 1=1 AND ...`** → 동작은 하지만 `<where>` 를 쓰는 게 깔끔. `<if>` 안의 조건은 `AND` 로 시작.
- **`test="keyword != ''"` 만** → `keyword` 가 `null` 이면 OGNL 오류. `keyword != null and keyword != ''`.
- **`<foreach>` 에 빈 컬렉션** → `IN ()` SQL 오류. 서비스에서 빈 경우 조기 반환.
- **체크박스 미체크 시 파라미터 자체가 안 옴** → `boolean workingOnly` 필드는 값이 없으면 기본값 `false`로 바인딩되므로 문제 없음. `Boolean`(래퍼) 타입으로 바꾸지 않는다.

---

## 핵심 요약

| 태그/클래스 | 용도 |
|---|---|
| `<if test="...">` | 조건부 SQL 조각 |
| `<where>` | 조건 있을 때만 WHERE, 앞의 AND/OR 제거 — `<trim>`의 프리셋 |
| `<trim>` | `<where>`/`<set>`의 일반형. `prefix`/`prefixOverrides`/`suffix`/`suffixOverrides` 직접 지정 |
| `<choose>/<when>/<otherwise>` | 여러 중 하나 |
| `<foreach>` | IN 절, 다중 값 (빈 컬렉션 주의) |
| `<bind>` | OGNL 표현식을 변수로 미리 계산 — `CONCAT` 같은 DB 함수 없이 `LIKE` 패턴 조립 |
| `EmpSearchCond`(`cond`) | 검색어·필터를 담는 DTO. 매퍼 파라미터로 그대로 전달 |
| `<sql id="searchWhere">` | 여러 SQL이 공유하는 WHERE 조각 (다음 문서의 COUNT 쿼리와 공유 예정) |

> 다음(`2_페이징.md`): 지금 만든 `cond`에 `page`/`size`/정렬을 추가하고, `LIMIT`+`COUNT`로 페이징을 붙입니다.
