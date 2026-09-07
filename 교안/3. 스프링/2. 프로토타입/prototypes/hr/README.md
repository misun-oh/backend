# 사원관리 시스템 · HTML 프로토타입

스프링 과정에서 **실제로 구현할 화면**을 미리 그려본 정적 프로토타입입니다.
공통 디자인 시스템(`/css/basic.css` · `/css/component.css`) + 레이아웃(`/css/app-shell.css`)을 쓰고,
이 폴더에는 화면별 마크업만 있습니다. 이 앱 전용 스타일은 `../../static/css/hr.css`.

> 공통 헤더/푸터는 **`_menu.html`(메뉴 폼)** 의 마크업을 각 페이지 `<body>` 에 **그대로 붙여넣어** 씁니다.
> (예전엔 `app-shell.js` 가 JS로 주입했지만 제거했습니다.) Thymeleaf 전환 시 `_menu.html` → fragment.

## 화면 목록

| 파일 | 화면 | 나중에 만들 스프링 매핑(예상) |
|---|---|---|
| `index.html` | 사원 목록 (검색·부서필터·재직토글·정렬·페이징) | `GET /emps` |
| `emp-detail.html` | 사원 상세 (기본정보/급여/조직 탭, 삭제 확인 모달) | `GET /emps/{id}` |
| `emp-form.html` | 사원 등록·수정 폼 (검증 에러 표시 포함) | `GET/POST /emps/new`, `/emps/{id}/edit` |
| `depts.html` | 부서 목록 (부서 추가 모달) | `GET /depts` |
| `dashboard.html` | 대시보드 (통계 카드, 부서별 인원, 최근 입사자) | `GET /` |
| `login.html` | 로그인 (가운데 카드, 아이디/비밀번호/로그인 유지/실패 배너) | `GET/POST /login` (Day 12, 심화는 부록 3) |
| `_menu.html` | 공통 메뉴(메뉴 폼) — 헤더/푸터 마크업. 각 페이지에 인라인 | Thymeleaf `fragments/menu` |

> **에러 페이지**는 앱 공용이라 `templates/error.html` + `templates/error/{403,404,500}.html` 에 있습니다(Spring Boot 가 상태코드로 자동 선택).
> **검색 폼**은 `<form method="get" action="/emps">` 로, 파라미터명은 `keyword` · `deptId` · `workingOnly` · `sort` · `page` 로 통일했습니다.
> **처리 결과 메시지**는 `index.html` 상단의 `.alert`(component.css 15절) 자리에 PRG 리다이렉트로 넘겨 렌더링합니다. 0건이면 `.empty-state`(16절).

## 공통 레이아웃 (메뉴 폼)

각 페이지 `<body>` 맨 위에 `_menu.html` 의 `<header class="site-header">…</header>` 를,
맨 아래에 `<footer class="site-footer">…</footer>` 를 **그대로 붙여넣습니다**.
현재 페이지에 해당하는 `<nav>` 링크에만 `aria-current="page"` 를 남깁니다.

- 다크 모드 토글(🌓)·모달·드롭다운 등은 `/js/component.js` 가 자동 초기화하므로 그대로 동작합니다.
- 로그인은 프로토타입에선 `/login` 링크만. 실제 인증(모달/세션)은 **Day 12**에서, 토큰(JWT)은 **부록 2**에서.
- Thymeleaf 전환 시 `_menu.html` → `templates/fragments/menu.html` 의 `th:fragment` 로 바꿔
  `<header th:replace="~{fragments/menu :: siteHeader('emp')}"></header>` 처럼 재사용합니다.

자세한 사용법은 `../../README.md` 참고.

## 실행 / 미리보기

HTML 이 `templates/` 로 빠져서 파일을 그냥 열면 `/css/...` 링크가 안 붙습니다.
정확한 미리보기는 **스프링 프로젝트에 `static/`·`templates/` 를 복사해 실행**하는 게 확실합니다.
(빠르게 보려면 `prototypes/` 를 루트로 `npx serve` 후 `/templates/hr/index.html`.)

## 스프링(Thymeleaf) 이식 메모

- 공통 파일(`basic.css` · `component.css` · `component.js` · `app-shell.css`)은
  `src/main/resources/static/{css,js}` 로, 링크는 `th:href="@{/css/basic.css}"`.
- 목록의 `<tbody>` 행에 `th:each="e : ${emps}"`, 값은 `th:text`.
- 상태 배지: `th:classappend="${e.active} ? 'badge--success' : 'badge--danger'"`.
- 폼 검증: `th:errorclass="field--error"` + `th:errors` 로 `.form-hint` 채우기.
- `_menu.html` 은 `templates/fragments/menu.html` 의 `th:fragment` 로 옮기고, 각 페이지의
  인라인 `<header>`/`<footer>` 를 `th:replace` 한 줄로 교체합니다.
