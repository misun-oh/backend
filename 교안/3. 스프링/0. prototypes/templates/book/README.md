# 사내 도서대여 · HTML 프로토타입

사원이 사내 도서관에서 책을 빌리는 화면 프로토타입입니다.
공통 디자인 시스템(`/css/basic.css` · `/css/component.css`)과 공통 골격(`/js/app-shell.js` ·
`/css/app-shell.css`)을 사용합니다.

| 파일 | 화면 | 스프링 매핑(예상) |
|---|---|---|
| `index.html` | 도서 홈 (신간 캐러셀 · 검색/분류/가능여부 필터 · 도서 카드 그리드 · 페이징) | `GET /books` |
| `book-detail.html` | 도서 상세 (표지 · 별점 요약 · 소개/리뷰 탭 · 별점 입력 · 대여 확인 모달) | `GET /books/{id}` |
| `my-rentals.html` | 내 대여 (대여중/반납완료 탭 · D-day·연체 배지 · 연장 버튼) | `GET /my/rentals` |
| 로그인 | `app-shell.js` 가 주입하는 공통 모달 | `POST /login` |

**사용 컴포넌트**: 캐러셀, 별점(표시/입력), 토글 스위치, 탭, 드롭다운, 모달, 툴팁, 토스트, 배지, 페이지네이션.

프로토타입 데이터는 임의이며, 실제 구현에서는 `BOOK`, `RENTAL`, `REVIEW` 테이블과
사원(`EMP`) 로그인을 연결합니다.
