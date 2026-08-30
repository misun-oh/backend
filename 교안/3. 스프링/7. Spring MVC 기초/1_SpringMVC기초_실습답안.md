# Day 7. Spring MVC 기초 — 실습 답안

---

## 문제 1 & 2. 화면 컨트롤러 + 파라미터 바인딩

```java
// controller/EmpViewController.java
@Slf4j
@Controller
@RequestMapping("/emps")
@RequiredArgsConstructor
public class EmpViewController {

    private final EmpService empService;

    @GetMapping
    public String list(@RequestParam(required = false) String keyword,
                       @RequestParam(defaultValue = "1") int page,
                       Model model) {
        log.info("목록 요청: keyword={}, page={}", keyword, page);
        model.addAttribute("emps", empService.findAll());
        return "emp/list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("emp", empService.get(id));
        return "emp/detail";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("form", new EmpForm());
        return "emp/form";
    }
}
```

`templates/emp/list.html` (아주 단순):
```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<body>
  <h1>사원 목록</h1>
  <a href="/emps/new">+ 등록</a>
  <ul>
    <li th:each="e : ${emps}">
      <a th:href="@{/emps/{id}(id=${e.empId})}" th:text="${e.empName}">이름</a>
    </li>
  </ul>
</body>
</html>
```

**확인**

- `GET /emps` → 로그 `목록 요청: keyword=null, page=1`
- `GET /emps?keyword=김&page=3` → 로그 `목록 요청: keyword=김, page=3`

---

## 문제 3. 폼 등록 + PRG

`templates/emp/form.html`:
```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<body>
  <h1>사원 등록</h1>
  <form method="post" action="/emps">
    이름 <input name="empName"><br>
    이메일 <input name="email"><br>
    부서ID <input name="deptId" type="number"><br>
    급여 <input name="salary" type="number"><br>
    입사일 <input name="hireDate" type="date"><br>
    <button type="submit">저장</button>
  </form>
</body>
</html>
```

```java
@PostMapping
public String create(@ModelAttribute EmpForm form, RedirectAttributes ra) {
    Long id = empService.register(form);
    ra.addFlashAttribute("message", "등록되었습니다");
    return "redirect:/emps/" + id;
}
```

**확인**

- 저장 → `302 Location: /emps/202` → 상세 화면 GET.
- 상세 화면에서 **새로고침해도 재등록 안 됨** (마지막 요청이 GET `/emps/202` 이므로).
  POST 응답을 그대로 렌더링했다면 새로고침 시 "폼 재전송" 경고 + 중복 등록됐을 것.
- `detail.html` 에서 `<p th:if="${message}" th:text="${message}"></p>` 로 플래시 메시지 표시(1회성).

---

## 문제 4. 삭제 (화면 방식)

```java
@PostMapping("/{id}/delete")
public String delete(@PathVariable Long id, RedirectAttributes ra) {
    empService.remove(id);
    ra.addFlashAttribute("message", "삭제되었습니다");
    return "redirect:/emps";
}
```
```html
<!-- detail.html -->
<form th:action="@{/emps/{id}/delete(id=${emp.empId})}" method="post">
  <button type="submit">삭제</button>
</form>
```

HTML `<form>` 은 GET/POST만 지원하므로 삭제를 `POST .../delete` 로 표현.
(REST `DELETE` 메서드는 Day 15 API에서.)

---

## 문제 5. 왜 안 될까

**(A)** `@Controller` 의 메서드가 반환한 `"안녕하세요"` 는 **뷰 이름**으로 해석된다.
`templates/안녕하세요.html` 을 찾다가 없어서 오류(뷰를 못 찾음). 문자열을 그대로 응답하려면
메서드에 `@ResponseBody` 를 붙이거나 클래스를 `@RestController` 로 한다.

**(B)** `@GetMapping("/{id}")` 의 경로 변수 이름은 `id` 인데, 파라미터는 `@PathVariable Long empId`.
이름이 안 맞아 바인딩에 실패한다(스프링 6 이후엔 컴파일 `-parameters` 여부에 따라
`MissingPathVariableException` → 500). 해결: `@PathVariable("id") Long empId` 또는 파라미터명을 `id` 로.
