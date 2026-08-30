# 프로토타입 디자인 템플릿

스프링 교안 실습에서 만들 화면들의 **디자인 기준**입니다. Apple(apple.com)의 디자인
언어를 참조했습니다 — 넓은 여백, 얇은 경계선, 큰 제목, 최소한의 색(무채색 + 파란 accent
하나), 알약형 버튼, 반투명 고정 헤더, 라이트/다크.

## 폴더 구성

스프링 `src/main/resources/` 와 **같은 구조**입니다. 그대로 프로젝트에 복사해 쓰면 됩니다.

```
prototypes/
├── static/
│   ├── css/
│   │   ├── basic.css          디자인 토큰(CSS 변수) · 리셋 · 타이포 · 레이아웃 · 버튼 · 폼 · 카드 · 배지 · 표 · 헤더/푸터 · 유틸
│   │   ├── component.css      UI 컴포넌트 스타일 (구조·옵션은 파일 상단 주석)
│   │   ├── app-shell.css      여러 앱이 공유하는 화면 골격 (page-head · toolbar · table-wrap · form-grid · info-table · user-chip)
│   │   ├── hr.css             사원관리 화면 전용 (구 hr/proto.css)
│   │   ├── book.css           도서대여 전용
│   │   ├── shopping-mall.css  쇼핑몰 전용
│   │   └── instagram.css      임스타그램 전용
│   └── js/
│       ├── component.js       컴포넌트 동작 (바닐라 JS, 의존성 0, 전역 UI)
│       └── app-shell.js       공통 헤더/푸터/로그인 모달 주입 + 로그인 상태(sessionStorage)
└── templates/
    ├── index.html             컴포넌트로 만든 예시 랜딩
    ├── components.html        모든 컴포넌트 미리보기
    ├── hr/                    사원/부서 CRUD · 대시보드 (스프링 과정 본 예제)
    ├── book/                  사원이 사내 도서를 대여 (캐러셀·별점·대여 흐름)
    ├── shopping-mall/         사원이 자사 상품을 구매 (상품·장바구니·주문/결제)
    └── instagram/             회원이 일상을 공유하는 사내 SNS (피드·탐색·프로필)
```

- HTML 은 `templates/`, CSS·JS 는 `static/css/` · `static/js/`. 링크는 **스프링식 절대경로**(`/css/basic.css`, `/js/component.js`).
- 각 앱 폴더(`templates/hr/` 등)에는 화면 HTML + `README.md` 가 있습니다. 앱 전용 스타일은 `static/css/<앱>.css`.
- **미리보기**: HTML 이 `templates/` 로 빠져서 그냥 열면 CSS 경로(`/css/...`)가 안 붙습니다.
  스프링 프로젝트에 넣고 실행하거나, `prototypes/` 를 루트로 로컬 서버를 띄운 뒤
  `/templates/hr/index.html` 처럼 여세요(단 `/css/...` 는 `prototypes/static/css/` 가 아니라
  루트 기준이라, 정확한 미리보기는 스프링 실행이 가장 확실).

## 새 화면 만드는 법

```html
<head>
  <link rel="stylesheet" href="/css/basic.css">
  <link rel="stylesheet" href="/css/component.css">
  <link rel="stylesheet" href="/css/app-shell.css">
  <link rel="stylesheet" href="/css/book.css">       <!-- 앱 전용 -->
  <script src="/js/component.js" defer></script>
  <script src="/js/app-shell.js" defer></script>
</head>
<body data-shell-brand="📚 사내도서관" data-shell-key="library" data-shell-home="index.html"
      data-shell-active="catalog"
      data-shell-nav='[{"href":"index.html","key":"catalog","label":"도서"},
                       {"href":"my.html","key":"mine","label":"내 대여"}]'>
  <header data-app-header></header>
  <main class="app-main container container--wide"> ...본문... </main>
  <footer data-app-footer></footer>
</body>
```

- `app-shell.js` 가 `data-shell-*` 설정을 읽어 헤더/푸터/로그인 모달을 그려 넣습니다.
- **로그인**: 헤더 "로그인" 버튼 → 공통 모달 → 로그인하면 `sessionStorage`(앱별 `data-shell-key`
  네임스페이스)에 저장되고, 헤더가 "사용자 칩 + 로그아웃"으로 바뀌며 페이지를 옮겨도 유지됩니다.

## 컴포넌트 (component.js) 전역 API

| 호출 | 설명 |
|---|---|
| `UI.modal.open(id)` / `UI.modal.close(id)` | 모달 열기 / 닫기 |
| `UI.toast(message, { type, duration })` | 토스트. `type`: `default` \| `success` \| `warning` \| `danger` |
| `UI.theme.toggle()` / `UI.theme.set('dark'\|'light')` | 라이트/다크 전환 (localStorage) |
| `UI.carousel(el).next()/.prev()/.goTo(i)` | 캐러셀 수동 제어 |
| `UI.init(root)` | 동적으로 추가한 DOM 초기화 |

자동 초기화 대상: `data-carousel` `data-modal` `data-modal-open` `data-tabs` `data-accordion`
`data-dropdown` `data-rating-input` `data-theme-toggle`.

## 다크 모드

- 자동: OS 설정(`prefers-color-scheme`).
- 수동: `<html data-theme="dark">` 고정, 또는 헤더의 `data-theme-toggle` 버튼 / `UI.theme.toggle()`.

## 스프링(Thymeleaf) 연동 메모

- 이미 스프링 구조이므로 `static/`·`templates/` 를 `src/main/resources/` 아래로 그대로 복사.
  정적 링크(`href="/css/basic.css"`)는 그대로 두거나 `th:href="@{/css/basic.css}"` 로 바꿔도 됩니다.
- `templates/hr/*.html` → 컨트롤러 뷰 이름에 맞게 배치(예: `templates/emp/list.html`).
- `app-shell.js` 가 하는 헤더/푸터/로그인 주입은 **Thymeleaf fragment + Spring Security**
  로 대체되므로 이식하지 않습니다. (컴포넌트 동작이 필요한 `component.js` 만 유지)
- 목록: `.card` / `.table` 행에 `th:each`, 값은 `th:text`.
- 별점: `th:style="'--rate:' + ${x.avgRating}"`.
- 폼 검증: `th:errorclass="field--error"` + `th:errors` 로 `.form-hint` 채우기.
