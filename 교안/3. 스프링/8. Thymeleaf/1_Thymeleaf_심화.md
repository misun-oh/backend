# Day 8. Thymeleaf — 심화 (경험자용)

## 1. 표현식 종류 정리

| 문법 | 의미 | 예 |
|---|---|---|
| `${...}` | 변수 표현식 (Model, SpEL) | `${emp.empName}`, `${#authentication.name}` |
| `*{...}` | 선택 변수 — `th:object` 대상의 필드 | `*{empName}` = `${form.empName}` |
| `@{...}` | URL (컨텍스트 경로, 파라미터, path var) | `@{/emps/{id}(id=${id})}` |
| `#{...}` | 메시지 (i18n) | `#{emp.title}` → `messages.properties` |
| `~{...}` | fragment 표현식 | `~{fragments/layout :: header}` |
| `\|...\|` | 리터럴 치환 | `\|Hello ${name}\|` |

SpEL 이므로 `${emps.?[active]}` (필터), `${emps.![empName]}` (프로젝션),
`${T(java.time.LocalDate).now()}` 등도 되지만, 뷰에 로직을 많이 넣지 말 것 — 컨트롤러/서비스에서 준비.

## 2. `th:with`, `th:attr`, `th:block`

```html
<div th:with="fullName=${emp.lastName + emp.firstName}, isVip=${emp.salary > 5000000}">
  <span th:text="${fullName}"></span>
  <span th:if="${isVip}">VIP</span>
</div>

<td th:attr="data-id=${emp.empId}, aria-label=${emp.empName}"></td>

<!-- 렌더링에 남지 않는 논리 블록 -->
<th:block th:each="e : ${emps}">
  <tr>...</tr>
  <tr class="detail">...</tr>
</th:block>
```

## 3. 레이아웃: fragment vs layout-dialect

- **fragment (`th:replace`)**: 헤더/푸터/사이드바를 조각으로. 페이지가 조각들을 조립.
- **layout-dialect** (`nz.net.ultraq.thymeleaf:thymeleaf-layout-dialect`): 공통 뼈대(`layout.html`)를
  두고 각 페이지가 `layout:decorate="~{layout}"` 로 상속, `<div layout:fragment="content">` 만 채움.
  페이지가 늘어날수록 이쪽이 편함.

```html
<!-- layout.html -->
<html layout:decorate... 아니고, 이게 base -->
<body>
  <div th:replace="~{fragments/nav :: nav}"></div>
  <main layout:fragment="content"></main>
</body>
<!-- list.html -->
<html layout:decorate="~{layout}">
<body>
  <main layout:fragment="content"> ... 이 페이지 내용만 ... </main>
</body>
```

## 4. 국제화(i18n)

- `src/main/resources/messages.properties`, `messages_en.properties` …
- `#{emp.list.title}` → 로케일에 맞는 문구.
- 파라미터: `#{welcome.msg(${user.name})}` ↔ `welcome.msg=반갑습니다, {0}님`.
- 로케일 결정: `Accept-Language` 헤더 → `AcceptHeaderLocaleResolver`. 쿠키/세션 기반으로 바꾸려면
  `LocaleResolver` + `LocaleChangeInterceptor` 빈 등록.

## 5. XSS 안전

- 기본 `th:text` 는 이스케이프 → 안전. 사용자 입력을 `th:utext` 로 출력하지 말 것.
- URL 파라미터도 `@{...}` 를 쓰면 인코딩됨.
- 인라인 자바스크립트에 값 넣기: `<script th:inline="javascript">const name = /*[[${emp.empName}]]*/ '';</script>`
  — `th:inline="javascript"` 가 JS 문자열로 안전하게 이스케이프.

## 6. 조각 캐싱 / 성능

- `spring.thymeleaf.cache=true`(운영 기본) — 파싱된 템플릿 재사용.
- 큰 목록은 서버에서 페이지 크기를 제한(Day 12). 뷰에서 수천 행 `th:each` 는 렌더링 비용.
- 정적으로 변하지 않는 조각은 CDN/정적 파일로.

## 7. 프래그먼트에 스타일/스크립트 모으기

`layout :: head(title, extraCss)` 처럼 파라미터로 페이지별 추가 리소스를 받게 설계하면
페이지마다 `<head>` 를 복붙하지 않는다:
```html
<head th:fragment="head(title, extra)">
  ...
  <th:block th:if="${extra != null}" th:replace="${extra}"></th:block>
</head>
```

## 8. 테스트

- `@WebMvcTest` + `MockMvc` : `andExpect(content().string(containsString("사원 목록")))`,
  `xpath("//tr")` 개수 검증.
- `spring.test.mockmvc` 는 실제 렌더링을 수행하므로 Thymeleaf 문법 오류도 잡힌다.
- 스냅샷/HTML 파서(jsoup)로 특정 요소 존재를 검증하면 견고.
