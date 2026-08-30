# Day 8. Thymeleaf — 실습 답안

전체 완성 파일은 `예제코드.md` 참고. 여기서는 핵심과 개념 답만.

---

## 문제 1. 공통 레이아웃 fragment

`templates/fragments/layout.html` — 교안 7절과 동일. 요점:

- `<head th:fragment="head(title)">` : 파라미터 fragment. 호출부 `~{fragments/layout :: head('사원 목록')}`.
- `<header th:fragment="header">`, `<footer th:fragment="footer">`.
- 정적 리소스는 반드시 `th:href="@{/css/basic.css}"`.

---

## 문제 2. 사원 목록 화면 (핵심)

```html
<tbody>
  <tr th:each="e : ${emps}">
    <td th:text="${e.empId}">200</td>
    <td><a th:href="@{/emps/{id}(id=${e.empId})}" th:text="${e.empName}">이름</a></td>
    <td th:text="${e.deptName}">부서</td>
    <td th:text="${#temporals.format(e.hireDate, 'yyyy-MM-dd')}">2015-05-20</td>
    <td>
      <span class="badge badge--dot"
            th:classappend="${e.active} ? 'badge--success' : 'badge--danger'"
            th:text="${e.active} ? '재직' : '퇴사'">상태</span>
    </td>
  </tr>
  <tr th:if="${#lists.isEmpty(emps)}">
    <td colspan="5">등록된 사원이 없습니다</td>
  </tr>
</tbody>
```

---

## 문제 3. 사원 상세 (핵심)

```html
<table class="info-table">
  <tr><th>사번</th><td th:text="${emp.empId}"></td></tr>
  <tr><th>이름</th><td th:text="${emp.empName}"></td></tr>
  <tr><th>이메일</th><td th:text="${emp.email}"></td></tr>
  <tr><th>입사일</th><td th:text="${#temporals.format(emp.hireDate,'yyyy-MM-dd')}"></td></tr>
  <tr><th>상태</th><td th:text="${emp.active} ? '재직' : '퇴사'"></td></tr>
</table>

<a th:href="@{/emps}">목록</a>
<a th:href="@{/emps/{id}/edit(id=${emp.empId})}">수정</a>
<form th:action="@{/emps/{id}/delete(id=${emp.empId})}" method="post">
  <button type="submit">삭제</button>
</form>
```

---

## 문제 4 & 5. 등록/수정 폼 재사용

```html
<form th:object="${form}" method="post"
      th:action="${form.empId != null}
                  ? @{/emps/{id}(id=${form.empId})}
                  : @{/emps}">

  <input type="hidden" th:field="*{empId}">

  <div class="field">
    <label class="label" for="empName">이름</label>
    <input class="input" type="text" th:field="*{empName}">
  </div>

  <div class="field">
    <label class="label" for="deptId">부서</label>
    <select class="select" th:field="*{deptId}">
      <option value="">선택</option>
      <option th:each="d : ${depts}" th:value="${d.deptId}" th:text="${d.deptName}"></option>
    </select>
  </div>

  <input type="date" th:field="*{hireDate}">
  <button type="submit" class="btn btn--primary">저장</button>
</form>
```

컨트롤러:
```java
@GetMapping("/{id}/edit")
public String editForm(@PathVariable Long id, Model model) {
    Emp e = empService.get(id);
    EmpForm form = new EmpForm();
    form.setEmpId(e.getEmpId());     // EmpForm 에 empId 필드 추가 필요
    form.setEmpName(e.getEmpName());
    form.setEmail(e.getEmail());
    form.setDeptId(e.getDeptId());
    form.setSalary(e.getSalary());
    form.setHireDate(e.getHireDate());
    model.addAttribute("form", form);
    model.addAttribute("depts", deptService.findAll());
    return "emp/form";
}

@PostMapping("/{id}")
public String update(@PathVariable Long id, @ModelAttribute EmpForm form, RedirectAttributes ra) {
    empService.modify(id, form);
    ra.addFlashAttribute("message", "수정되었습니다");
    return "redirect:/emps/" + id;
}
```

**확인**: `form.html` **한 파일**로 등록·수정 모두 처리. 수정 화면은 `th:field` 가 `form` 의 기존 값을
자동으로 `value` 에 채운다. `th:action` 은 `empId` 유무로 분기.

---

## 문제 6. 두 링크의 차이

배포 컨텍스트 경로가 `/hr` 일 때:

- **A** `'/emps/' + ${e.empId}` → `/emps/205` (컨텍스트 경로 `/hr` 가 안 붙음 → **깨진 링크**)
- **B** `@{/emps/{id}(id=${e.empId})}` → `/hr/emps/205` (컨텍스트 경로 자동 반영, path variable 인코딩)

→ 항상 **B (`@{...}`)** 를 쓴다. 정적 파일 링크(`@{/css/...}`)도 마찬가지.
