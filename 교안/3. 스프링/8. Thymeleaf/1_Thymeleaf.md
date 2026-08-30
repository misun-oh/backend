# Day 8. Thymeleaf

| 항목 | 내용 |
|---|---|
| 선수학습 | Day 7(`@Controller`·`Model`·뷰 이름), HTML/CSS |
| 이번 챕터 | Thymeleaf 기본 문법(`th:text`·`th:each`·`th:if`·`th:href`) → 폼 바인딩(`th:object`·`th:field`) → 레이아웃 fragment → 정적 리소스 → **프로토타입을 서버 렌더링으로** |
| 권장 진행 | 1일 |
| 결과물 | `prototypes/templates/hr/` 프로토타입과 똑같이 생긴 사원 목록·상세·폼 화면이 DB(아직 메모리) 데이터로 렌더링 |

## 학습목표

- Thymeleaf가 "동작하는 정적 HTML(natural template)"이라는 개념을 설명할 수 있다.
- `th:text`, `th:each`, `th:if`/`th:unless`, `th:href`/`th:src`, `th:class`/`th:classappend` 를 쓸 수 있다.
- `th:object` + `th:field` 로 폼과 DTO를 양방향 바인딩할 수 있다.
- `th:fragment` / `th:replace` 로 공통 레이아웃(헤더·푸터)을 재사용할 수 있다.
- 정적 리소스(`static/`)를 `@{...}` 로 링크할 수 있다.
- 프로토타입 HTML을 실제 데이터로 채우는 화면으로 바꿀 수 있다.

---

## 1. Thymeleaf란

`spring-boot-starter-thymeleaf` 를 넣으면 `src/main/resources/templates/*.html` 이 뷰가 됩니다.

- **natural template**: `th:*` 속성은 브라우저가 무시하므로, 템플릿 파일을 그냥 열어도 정적 HTML로 보입니다.
  → 디자이너·프론트와 협업이 쉽고, 프로토타입 HTML을 거의 그대로 쓸 수 있습니다.
- 서버에서 렌더링될 때 `th:*` 가 실제 값으로 치환됩니다.
- 네임스페이스 선언: `<html xmlns:th="http://www.thymeleaf.org">`

---

## 2. 값 출력 — `th:text`, `th:utext`, `[[...]]`

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

## 3. 반복 — `th:each`

프로토타입(`prototypes/templates/hr/index.html`)의 표를 그대로 가져와 `<tbody>` 행에 `th:each` 만 얹습니다.

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

## 4. 조건 & 클래스

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

## 5. 링크 — `@{...}`

```html
<a th:href="@{/emps}">목록</a>
<a th:href="@{/emps/{id}(id=${emp.empId})}">상세</a>              <!-- /emps/205 -->
<a th:href="@{/emps(keyword=${cond.keyword}, page=${page})}">검색</a> <!-- /emps?keyword=김&page=2 -->
<link rel="stylesheet" th:href="@{/css/basic.css}">
<script th:src="@{/js/component.js}" defer></script>
```

`@{...}` 는 **컨텍스트 경로**(배포 시 `/hr` 같은)를 자동으로 앞에 붙여 줍니다. 그래서 링크는 항상 `@{...}`.

---

## 6. 폼 바인딩 — `th:object`, `th:field`

컨트롤러가 `model.addAttribute("form", new EmpForm())` 로 빈 폼 객체를 넘겨줍니다(Day 7).

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

## 7. 레이아웃 재사용 — fragment

`prototypes/templates/`의 공통 헤더/푸터를 fragment로 뽑습니다.

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

## 8. 정적 리소스 배치

프로토타입의 디자인 시스템 파일을 프로젝트로 옮깁니다.

```
src/main/resources/static/
├── css/  basic.css  component.css  app-shell.css
└── js/   component.js
```

`http://localhost:8080/css/basic.css` 로 바로 서빙됩니다. 템플릿에서는 `th:href="@{/css/basic.css}"`.
(`app-shell.js` 가 하던 헤더 주입은 Thymeleaf fragment 로 대체됐으므로 옮기지 않습니다.)

---

## 자주 하는 실수

- **`xmlns:th` 선언 누락** → `th:*` 가 그냥 속성으로 남고 아무 일도 안 일어남.
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
| `th:text` / `th:utext` | 이스케이프 출력 / 원시 HTML |
| `th:each="e : ${list}"` | 반복 (`stat` 상태 객체) |
| `th:if` / `th:unless` / `th:switch` | 조건 (false면 DOM 제거) |
| `th:href="@{/emps/{id}(id=${id})}"` | 링크 (컨텍스트 경로 자동) |
| `th:object` + `th:field="*{name}"` | 폼 ↔ DTO 바인딩 (id·name·value 한 번에) |
| `th:fragment` / `th:replace="~{tpl :: frag}"` | 레이아웃 조각 재사용 |
| `static/` + `@{/css/...}` | 정적 리소스 |

> 다음(Day 9): 지금까지 메모리였던 데이터를 **진짜 MySQL**에 붙인다 — MyBatis 시작 & DB 설정.
