# 임스타그램 (사내 SNS) · HTML 프로토타입

회원이 일상을 사진으로 공유하는 인스타그램 형태의 사내 SNS 프로토타입입니다.
공통 디자인 시스템(`/css/basic.css` · `/css/component.css` · `/css/app-shell.css`).
공통 헤더/푸터는 **`_menu.html`(메뉴 폼)** 의 마크업을 각 페이지에 인라인합니다.

| 파일 | 화면 | 스프링 매핑(예상) |
|---|---|---|
| `index.html` | 피드 (스토리 바 · 게시물 카드: 사진 캐러셀·좋아요·댓글·저장·댓글 입력) | `GET /feed` |
| `explore.html` | 탐색 (검색 · 3열 정사각 사진 그리드) | `GET /explore` |
| `profile.html` | 프로필 (아바타·통계·소개 · 게시물/저장됨 탭 그리드 · 프로필 편집 모달) | `GET /users/{name}` |
| `_menu.html` | 공통 메뉴 폼 (헤더/푸터) | Thymeleaf `fragments/menu` |
| 로그인 | 헤더의 `/login` 링크 | `POST /login` (Spring Security) |

**사용 컴포넌트**: 캐러셀(사진 여러 장), 탭, 모달(프로필 편집), 토글 스위치(비공개 계정), 아바타, 토스트 + 프로토타입 자체 스타일(스토리 바 `.stories`, 게시물 `.post`, 사진 그리드 `.photo-grid`).

**인터랙션(프로토타입)**: 하트 아이콘 클릭 → 좋아요 토글 + 카운트 증감, 저장 아이콘 → 토스트,
댓글 입력 후 게시 → 토스트.

실제 구현에서는 `MEMBER`, `POST`, `PHOTO`, `LIKE`, `COMMENT`, `FOLLOW` 테이블과 파일
업로드(사진)를 연결합니다. — 스프링 과정 Day 16 "파일 업로드"의 확장 예시로 좋습니다.
