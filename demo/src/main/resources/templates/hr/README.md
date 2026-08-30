# 사원관리 시스템 · HTML 프로토타입

스프링 과정에서 **실제로 구현할 화면**을 미리 그려본 정적 프로토타입입니다.
공통 디자인 시스템(`/css/basic.css` · `/css/component.css`) + 공통 골격(`/css/app-shell.css` · `/js/app-shell.js`)을 쓰고,
이 폴더에는 화면별 마크업만 있습니다. 이 앱 전용 스타일은 `../../static/css/hr.css`.

## 화면 목록

| 파일 | 화면 | 나중에 만들 스프링 매핑(예상) |
|---|---|---|
| `index.html` | 사원 목록 (검색·부서필터·재직토글·정렬·페이징) | `GET /emps` |
| `emp-detail.html` | 사원 상세 (기본정보/급여/조직 탭, 삭제 확인 모달) | `GET /emps/{id}` |
| `emp-form.html` | 사원 등록·수정 폼 (검증 에러 표시 포함) | `GET/POST /emps/new`, `/emps/{id}/edit` |
| `depts.html` | 부서 목록 (부서 추가 모달) | `GET /depts` |
| `dashboard.html` | 대시보드 (통계 카드, 부서별 인원, 최근 입사자) | `GET /` 또는 `GET /dashboard` |
| 로그인 | **모달**로 처리 (`/js/app-shell.js` 가 모든 페이지에 주입) | `GET/POST /login` (Spring Security) |

## 공통 레이아웃 / 로그인

각 페이지는 `<body>` 에 `data-shell-*` 설정을 두고, 본문에는
`<header data-app-header></header>` · `<footer data-app-footer></footer>` 자리만 둡니다.
`/js/app-shell.js` 가 설정을 읽어 헤더/푸터/로그인 모달을 주입합니다.

- 헤더의 **로그인** 버튼 → 공통 로그인 모달 → 아무 값이나 입력 후 **로그인**하면
  `sessionStorage`(키 `hr`)에 저장 → 헤더가 "사용자 칩 + 로그아웃"으로 바뀌고
  **다른 페이지로 이동해도 유지**됨.
- 스프링에서는 이 부분이 **Thymeleaf fragment + Spring Security**(세션/JWT)로 대체됩니다.

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
- `app-shell.js` 의 헤더/모달 주입은 fragment 로 대체되므로 이식하지 않습니다.
