# Day 7. Spring MVC 기초 — 실습

Day 5 의 `Emp` 계층(메모리 구현)을 그대로 사용합니다. 이번엔 **화면용 `@Controller`** 를 만듭니다.
Thymeleaf 문법은 Day 8에서 배우므로, 지금은 아주 단순한 HTML만 씁니다.

## 문제 1. 화면 컨트롤러 뼈대

`com.example.hr.controller.EmpViewController` 를 `@Controller` 로 만들고 `@RequestMapping("/emps")`.

- `GET /emps` → `Model` 에 `emps`(목록) 담고 `"emp/list"` 반환
- `GET /emps/{id}` → `emp` 담고 `"emp/detail"` 반환
- `GET /emps/new` → `"emp/form"` 반환

`src/main/resources/templates/emp/` 에 `list.html`, `detail.html`, `form.html` 을 아주 간단히 만드세요
(예: `list.html` 에 `<h1>사원 목록</h1>` 만 있어도 됨). 세 주소가 각각 뜨는지 확인.

## 문제 2. 파라미터 바인딩 3종

`EmpViewController` 에 다음을 추가하세요.

- `GET /emps` 에 `@RequestParam(required=false) String keyword`, `@RequestParam(defaultValue="1") int page`
  를 받아, 지금은 `System.out` 이나 `log` 로만 값을 찍기.
- `GET /emps/{id}` 의 `id` 는 `@PathVariable Long id`.
- (문제 3에서 `@ModelAttribute` 사용)

`GET /emps` 와 `GET /emps?keyword=김&page=3` 을 각각 호출하고 로그에 찍힌 값을 적으세요.

## 문제 3. 폼 등록 + PRG

- `GET /emps/new` → `form.html` 에 실제 `<form method="post" action="/emps">` 를 만들고
  `empName`, `email`, `deptId`, `salary`, `hireDate` 입력 필드.
- `POST /emps` → `@ModelAttribute EmpForm form` 으로 받아 `empService.register(form)`,
  `RedirectAttributes` 로 `message` 플래시 속성 담고 `redirect:/emps/{생성된id}`.

**확인할 것**: 등록 후 상세 화면으로 이동하는지, 그 화면을 **새로고침해도 중복 등록이 안 되는지**.

## 문제 4. 삭제 (화면 방식)

`POST /emps/{id}/delete` → `empService.remove(id)` 후 `redirect:/emps`.
`detail.html` 에 `<form method="post" th:action="...">` (지금은 그냥 `action="/emps/그id/delete"`) 삭제 버튼.

## 문제 5. (개념) 왜 안 될까

아래 두 코드가 각각 왜 원하는 대로 동작하지 않는지 설명하세요.

```java
// (A)
@Controller
public class A {
    @GetMapping("/a")
    public String a() { return "안녕하세요"; }   // 브라우저에 500 또는 이상한 결과
}

// (B)
@Controller
@RequestMapping("/emps")
public class B {
    @GetMapping("/{id}")
    public String detail(@PathVariable Long empId, Model m) { ... }   // 400? 500?
}
```
