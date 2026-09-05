# Day 10. Thymeleaf

| 항목 | 내용 |
|---|---|
| 선수학습 | Day 6(`@Controller`·뷰 이름), Day 9(MyBatis CRUD — `EmpService`로 실제 사원 조회 가능) |
| 이번 챕터 | 컨트롤러 → 뷰로 데이터 전달(`Model`/`ModelAndView`) → 화면 이동(`redirect`/`forward`) → Thymeleaf 기본 문법(`th:text`·`th:each`·`th:if`·`th:href`) → 폼 바인딩(`th:object`·`th:field`) → 레이아웃 fragment → 정적 리소스 → **MyBatis CRUD 결과에 화면 씌우기** |
| 권장 진행 | 1일 |
| 결과물 | `2. 프로토타입/prototypes/hr/` 프로토타입과 똑같이 생긴 사원 목록·상세·폼 화면이, Day 9에서 만든 **진짜 MyBatis 데이터**로 렌더링 |

## 학습목표

- 컨트롤러가 `Model`(또는 `ModelAndView`)에 데이터를 담아 뷰로 넘기는 원리를 설명할 수 있다.
- `redirect:`와 `forward:`의 차이(새 요청 여부·주소창·데이터 유지)를 설명하고 언제 무엇을 쓰는지 안다.
- Thymeleaf가 "동작하는 정적 HTML(natural template)"이라는 개념을 설명할 수 있다.
- `th:text`, `th:each`, `th:if`/`th:unless`, `th:href`/`th:src`, `th:class`/`th:classappend` 를 쓸 수 있다.
- `th:object` + `th:field` 로 폼과 DTO를 양방향 바인딩할 수 있다.
- `th:fragment` / `th:replace` 로 공통 레이아웃(헤더·푸터)을 재사용할 수 있다.
- 정적 리소스(`static/`)를 `@{...}` 로 링크할 수 있다.
- 프로토타입 HTML을 Day 9의 MyBatis CRUD 결과로 채우는 화면으로 바꿀 수 있다.

---

## 1. Thymeleaf란

`spring-boot-starter-thymeleaf` 를 넣으면 `src/main/resources/templates/*.html` 이 뷰가 됩니다.

- **natural template**: `th:*` 속성은 브라우저가 무시하므로, 템플릿 파일을 그냥 열어도 정적 HTML로 보입니다.
  → 디자이너·프론트와 협업이 쉽고, 프로토타입 HTML을 거의 그대로 쓸 수 있습니다.
- 서버에서 렌더링될 때 `th:*` 가 실제 값으로 치환됩니다.
- 네임스페이스 선언: `<html xmlns:th="http://www.thymeleaf.org">`

---

## 2. 컨트롤러 → 뷰로 데이터 전달 — `Model`과 `ModelAndView`

Day 6에서 컨트롤러가 문자열(뷰 이름)을 반환하면 그 이름으로 `templates/*.html` 을 찾는다는 것까지
배웠습니다. 그런데 화면에는 보통 뷰 이름만이 아니라 **데이터**도 같이 필요합니다 — 사원 목록
화면이면 진짜 사원 리스트가 있어야 하듯이. 그 데이터를 담아 뷰로 넘기는 봉투가 `Model`입니다.

```java
@Controller
public class EmpViewController {

    private final EmpService empService;   // Day 9에서 만든 서비스

    public EmpViewController(EmpService empService) {
        this.empService = empService;
    }

    @GetMapping("/emps")
    public String list(Model model) {
        model.addAttribute("emps", empService.findAll());   // 진짜 DB 데이터
        model.addAttribute("title", "사원 목록");
        return "hr/index";
    }
}
```

- `model.addAttribute("이름", 값)` 로 담은 값을 템플릿에서 `${이름}` 으로 꺼내 씁니다(바로 2절 아래
  "값 출력"에서 이어집니다).
- `Model` 은 인터페이스이고, 스프링이 매개변수로 **자동으로 만들어 넘겨줍니다** — 매개변수 목록에
  `Model model` 이라고 선언만 하면 됩니다. 직접 `new` 할 필요가 없습니다.

### `ModelAndView` — 뷰 이름과 데이터를 한 객체로

`Model` + 문자열 반환의 대안으로, 뷰 이름과 데이터를 **한 객체**에 합친 `ModelAndView` 도 있습니다.

```java
@GetMapping("/emps")
public ModelAndView list() {
    ModelAndView mav = new ModelAndView("hr/index");     // 뷰 이름을 생성자에
    mav.addObject("emps", empService.findAll());          // Model 대신 addObject
    mav.addObject("title", "사원 목록");
    return mav;
}
```

| | `Model` + `String` 반환 | `ModelAndView` |
|---|---|---|
| 반환 타입 | `String`(뷰 이름) | `ModelAndView`(뷰 이름 + 데이터를 한 객체에) |
| 데이터 담기 | `model.addAttribute(...)` | `mav.addObject(...)` |
| 이 교안에서 | **기본으로 사용** — 대부분의 스프링 코드가 이 스타일 | 뷰 이름을 조건에 따라 동적으로 정해야 할 때 가끔 사용 |

두 방식은 결과가 완전히 같습니다 — 어떤 화면을 배우든 `Model` + `String` 반환이 나오면 "아, 데이터는
`Model`에, 뷰 이름은 반환값에" 라고 읽으면 됩니다.

> **자주 하는 실수**: `Model`에 값을 안 담고 템플릿에서 `${emps}` 처럼 참조하면, 화면에 그 값이
> 빈 채로 나오거나 오류가 납니다. "화면에 값이 안 보인다"는 대부분 컨트롤러에서 `addAttribute` 를
> 빠뜨렸거나, 템플릿의 `${이름}`과 컨트롤러의 `addAttribute("이름", ...)` 의 **이름 철자가 다른** 경우입니다.

---

## 3. 화면 이동 두 가지 방법 — `redirect`와 `forward`

지금까지는 컨트롤러가 뷰 이름을 반환하면 **그 요청 안에서** 화면이 그려졌습니다. 그런데 "저장 후
목록으로 이동", "권한 없으면 다른 화면으로" 처럼, 지금 요청과 **다른 주소**로 옮겨야 할 때가
있습니다. 스프링은 반환하는 뷰 이름 앞에 접두어를 붙여 이걸 구분합니다.

```java
return "redirect:/emps";   // 브라우저에게 "/emps로 다시 요청해" 라고 지시
return "forward:/emps";    // 서버 안에서 곧바로 "/emps" 처리로 넘김(브라우저는 모름)
```

| | `redirect:` | `forward:` |
|---|---|---|
| 동작 | 302 응답 + `Location` 헤더 → **브라우저가 새 요청**을 보냄 | 서버 안에서 같은 요청·응답 객체로 다른 핸들러에 **그대로 넘김** |
| 주소창 | 최종 URL로 바뀜 | **안 바뀜**(처음 요청한 주소 그대로) |
| 요청 횟수 | 2번(원래 요청 + 새 요청) | 1번 |
| `Model` 데이터 | **사라짐**(새 요청이라 이전 `Model`은 없음) | **그대로 유지**(같은 요청이므로) |
| 새로고침(F5) | 안전 — 마지막 GET이 다시 실행됨 | **위험** — POST 처리 후 forward하면 새로고침 시 그 POST가 재실행될 수 있음 |
| 주로 쓰는 곳 | 폼 저장 후 목록·상세로 이동(PRG 패턴) | 같은 요청 안에서 다른 컨트롤러·에러 페이지로 내부 전달(흔치 않음) |

```java
@PostMapping
public String create(@ModelAttribute EmpForm form, RedirectAttributes ra) {
    Long id = empService.register(form);
    ra.addFlashAttribute("message", "등록되었습니다");   // 리다이렉트 후 1회만 살아있는 값
    return "redirect:/emps/" + id;
}
```

- `redirect`는 데이터가 사라지므로, 딱 한 번만 살아 있는 값(방금 "등록되었습니다" 같은 메시지)을
  넘기고 싶으면 `RedirectAttributes.addFlashAttribute(...)` 를 씁니다.
- 실무에서는 **폼 저장 후엔 거의 항상 `redirect:`** 를 씁니다(PRG 패턴 — Post 저장 → Redirect →
  Get으로 결과 화면). `forward:` 는 브라우저가 원래 주소를 기억한 채로 다른 처리 결과를 보여주는
  것이라 새로고침 시 중복 처리 위험이 있고, 주소창도 실제 처리된 곳과 달라 헷갈립니다.

> `forward:` 를 직접 쓰는 일은 드뭅니다 — 에러 페이지 처리처럼 프레임워크가 내부적으로 forward를
> 쓰는 경우는 있지만, 우리가 짜는 컨트롤러는 대부분 `redirect:` 나 그냥 뷰 이름 반환이면 충분합니다.

---

## 4. 값 출력 — `th:text`, `th:utext`, `[[...]]`

```html
<h1 th:text="${title}">기본 텍스트(브라우저 미리보기용)</h1>
<span th:text="${emp.empName}">홍길동</span>
<span th:text="|${emp.empName} (${emp.empId})|">리터럴 조합</span>   <!-- | | : 문자열 리터럴 치환 -->
<p>[[${emp.email}]]</p>                                            <!-- 인라인 표현식 -->
```

- `th:text` : **HTML 이스케이프** (안전, 기본). `<b>` 를 `&lt;b&gt;` 로 출력.
- `th:utext` : 이스케이프 안 함(신뢰된 HTML만. XSS 주의).
- 표현식: `${...}`(변수), `@{...}`(URL), `#{...}`(메시지), `*{...}`(선택된 객체의 필드 — `th:object` 와 함께).

### 유틸리티 객체

```html
<span th:text="${#temporals.format(emp.hireDate, 'yyyy-MM-dd')}">2015-05-20</span>
<span th:text="${#numbers.formatInteger(emp.salary, 3, 'COMMA')}">3,500,000</span>
<span th:text="${#strings.isEmpty(emp.email)} ? '없음' : ${emp.email}">이메일</span>
```

---

## 5. 반복 — `th:each`

프로토타입(`2. 프로토타입/prototypes/hr/index.html`)의 표를 그대로 가져와 `<tbody>` 행에
`th:each` 만 얹습니다.

```html
<table class="table">
  <thead>
    <tr><th>사번</th><th>이름</th><th>부서</th><th>입사일</th><th>상태</th></tr>
  </thead>
  <tbody>
    <tr th:each="e : ${emps}">
      <td th:text="${e.empId}">200</td>
      <td><a th:href="@{/emps/{id}(id=${e.empId})}" th:text="${e.empName}">곽상혁</a></td>
      <td th:text="${e.deptName}">총무부</td>
      <td th:text="${#temporals.format(e.hireDate, 'yyyy-MM-dd')}">2013-03-02</td>
      <td>
        <span class="badge badge--success badge--dot" th:if="${e.active}">재직</span>
        <span class="badge badge--danger badge--dot"  th:unless="${e.active}">퇴사</span>
      </td>
    </tr>
  </tbody>
</table>
```

- `th:each="e : ${emps}"` — `emps` 의 각 원소를 `e` 로.
- 상태 객체: `th:each="e, stat : ${emps}"` → `stat.index`(0부터), `stat.count`(1부터), `stat.first`, `stat.last`, `stat.odd`.
- 목록이 비었을 때: `<tr th:if="${#lists.isEmpty(emps)}"><td colspan="5">데이터가 없습니다</td></tr>`

---

## 6. 조건 & 클래스

```html
<div th:if="${emp.active}">재직 중</div>
<div th:unless="${emp.active}">퇴사</div>

<!-- switch -->
<div th:switch="${emp.jobCode}">
  <span th:case="'J1'">대표</span>
  <span th:case="'J3'">부장</span>
  <span th:case="*">기타</span>
</div>

<!-- 클래스 조건부 추가 -->
<span class="badge"
      th:classappend="${emp.active} ? 'badge--success' : 'badge--danger'">상태</span>

<!-- 별점: 프로토타입의 --rate 스타일 변수만 바꾸면 됨 -->
<span class="rating" th:style="'--rate:' + ${emp.avgRating}"></span>
```

`th:if` 가 false면 그 요소는 **DOM에서 아예 빠집니다**(display:none 이 아님).

---

## 7. 링크 — `@{...}`

```html
<a th:href="@{/emps}">목록</a>
<a th:href="@{/emps/{id}(id=${emp.empId})}">상세</a>              <!-- /emps/205 -->
<a th:href="@{/emps(keyword=${cond.keyword}, page=${page})}">검색</a> <!-- /emps?keyword=김&page=2 -->
<link rel="stylesheet" th:href="@{/css/basic.css}">
<script th:src="@{/js/component.js}" defer></script>
```

`@{...}` 는 **컨텍스트 경로**(배포 시 `/hr` 같은)를 자동으로 앞에 붙여 줍니다. 그래서 링크는 항상 `@{...}`.

---

## 8. 폼 바인딩 — `th:object`, `th:field`

컨트롤러가 `model.addAttribute("form", new EmpForm())` 로 빈 폼 객체를 넘겨줍니다(2절).

```html
<form th:action="@{/emps}" th:object="${form}" method="post">

  <div class="field">
    <label class="label" for="empName">이름</label>
    <input class="input" type="text" th:field="*{empName}">
  </div>

  <div class="field">
    <label class="label" for="deptId">부서</label>
    <select class="select" th:field="*{deptId}">
      <option value="">선택</option>
      <option th:each="d : ${depts}" th:value="${d.deptId}" th:text="${d.deptName}">부서</option>
    </select>
  </div>

  <input type="date" th:field="*{hireDate}">
  <button type="submit" class="btn btn--primary">저장</button>
</form>
```

`th:field="*{empName}"` 하나가 `id="empName"`, `name="empName"`, `value="${form.empName}"` 를
**한꺼번에** 만들어 줍니다. 수정 화면에서는 `form` 에 기존 값이 들어 있으므로 자동으로 채워집니다.

- 체크박스·라디오·셀렉트도 `th:field` 가 선택 상태(`checked`, `selected`)를 알아서 처리.
- 검증 에러 표시(`#fields.hasErrors`, `th:errors`, `th:errorclass`)는 **Day 14**에서.

---

## 9. 레이아웃 재사용 — fragment

프로토타입에서 각 페이지에 인라인해 두었던 **메뉴 폼**(`2. 프로토타입/prototypes/hr/_menu.html` 의
헤더/푸터)을 fragment로 뽑아, 이제 진짜로 "한 곳에 정의 → 여러 페이지에서 재사용"합니다.

`templates/fragments/layout.html`:
```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head th:fragment="head(title)">
  <meta charset="UTF-8">
  <title th:text="${title}">HR</title>
  <link rel="stylesheet" th:href="@{/css/basic.css}">
  <link rel="stylesheet" th:href="@{/css/component.css}">
  <script th:src="@{/js/component.js}" defer></script>
</head>
<body>
  <header th:fragment="header" class="site-header">
    <div class="site-header__inner container container--wide">
      <a class="site-header__brand" th:href="@{/emps}">👔 사원관리</a>
      <nav class="site-nav">
        <a th:href="@{/emps}">사원</a>
        <a th:href="@{/depts}">부서</a>
      </nav>
    </div>
  </header>

  <footer th:fragment="footer" class="site-footer">
    <div class="container container--wide"><p>© 2026 HR</p></div>
  </footer>
</body>
</html>
```

각 화면에서 가져다 씁니다.
```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head th:replace="~{fragments/layout :: head('사원 목록')}"></head>
<body>
  <div th:replace="~{fragments/layout :: header}"></div>

  <main class="app-main container container--wide">
    <h1>사원 목록</h1>
    <!-- ... th:each 표 ... -->
  </main>

  <div th:replace="~{fragments/layout :: footer}"></div>
</body>
</html>
```

- `th:replace="~{템플릿 :: fragment이름}"` — 그 요소를 fragment로 **교체**.
- `th:insert` — 자식으로 **삽입**. 대개 `th:replace` 를 씀.
- 파라미터 fragment: `th:fragment="head(title)"` ↔ `:: head('사원 목록')`.
- 더 정교한 레이아웃은 `thymeleaf-layout-dialect`(`layout:decorate`)를 추가로 쓴다(심화).

---

## 10. 정적 리소스 배치

프로토타입의 디자인 시스템 파일을 프로젝트로 옮깁니다.

```
src/main/resources/static/
├── css/  basic.css  component.css  app-shell.css
└── js/   component.js
```

`http://localhost:8080/css/basic.css` 로 바로 서빙됩니다. 템플릿에서는 `th:href="@{/css/basic.css}"`.
(프로토타입에는 `app-shell.js` 가 없습니다 — 공통 헤더/푸터는 위 9절처럼 fragment 로 만듭니다.)

---

## 자주 하는 실수

- **`xmlns:th` 선언 누락** → `th:*` 가 그냥 속성으로 남고 아무 일도 안 일어남.
- **`Model`에 담지 않은 값을 템플릿에서 참조** → 2절 참고. 이름 철자가 컨트롤러·템플릿에서 같은지 확인.
- **폼 저장 후 `forward:`로 결과 화면을 그림** → 새로고침 시 저장이 중복 실행될 위험. 저장은
  `redirect:`(PRG 패턴, 3절).
- **`th:href="/emps/${id}"`** (문자열 안에 표현식) → `@{/emps/{id}(id=${id})}` 를 써야 URL 인코딩·컨텍스트 경로 처리가 됨.
- **`th:text` 와 태그 안 텍스트를 둘 다 기대** → `th:text` 가 있으면 태그 내용은 **완전히 덮어씀**(미리보기용 더미일 뿐).
- **`th:if` 로 숨겼는데 CSS로 다시 보임** → `th:if` false면 DOM에서 제거됨. 숨김이 아니라 제거.
- **`th:field` 없이 `name` 만** → 수정 화면에서 기존 값이 안 채워짐. `th:field` 로 통일.
- **템플릿 캐시** → 운영 기본 캐시 on. 개발은 DevTools가 꺼 주지만, 안 되면 `spring.thymeleaf.cache=false`.
- **fragment 경로 오타** → `~{fragments/layout :: header}` 의 앞부분은 `templates/` 기준 경로.

---

## 핵심 요약

| 문법 | 용도 |
|---|---|
| `Model.addAttribute` / `ModelAndView.addObject` | 컨트롤러 → 뷰로 데이터 전달 (2절) |
| `redirect:` / `forward:` | 새 요청으로 이동(주소창 바뀜) / 서버 내부 전달(주소창 그대로). 폼 저장 후엔 `redirect:` (3절) |
| `th:text` / `th:utext` | 이스케이프 출력 / 원시 HTML |
| `th:each="e : ${list}"` | 반복 (`stat` 상태 객체) |
| `th:if` / `th:unless` / `th:switch` | 조건 (false면 DOM 제거) |
| `th:href="@{/emps/{id}(id=${id})}"` | 링크 (컨텍스트 경로 자동) |
| `th:object` + `th:field="*{name}"` | 폼 ↔ DTO 바인딩 (id·name·value 한 번에) |
| `th:fragment` / `th:replace="~{tpl :: frag}"` | 레이아웃 조각 재사용 |
| `static/` + `@{/css/...}` | 정적 리소스 |

> 다음(Day 11): 지금 만든 사원 목록 화면에 검색·페이징을 붙인다 — **동적 SQL과 페이징**.
