# 자사 쇼핑몰 · HTML 프로토타입

사원이 회사 상품(자사 제품·복지몰)을 구매하는 화면 프로토타입입니다.
공통 디자인 시스템(`/css/basic.css` · `/css/component.css` · `/css/app-shell.css`).
공통 헤더/푸터는 **`_menu.html`(메뉴 폼)** 의 마크업을 각 페이지에 인라인합니다.

| 파일 | 화면 | 스프링 매핑(예상) |
|---|---|---|
| `index.html` | 홈 (프로모션 캐러셀 · 카테고리 칩 · 베스트 상품 그리드) | `GET /` , `GET /products` |
| `product-detail.html` | 상품 상세 (이미지 캐러셀 · 가격/할인 · 옵션·수량 · 정보/리뷰/Q&A 탭) | `GET /products/{id}` |
| `cart.html` | 장바구니 (품목 · 수량 조절 · 결제 요약 sticky) | `GET /cart` |
| `checkout.html` | 주문/결제 (배송지 폼 · 결제수단 · 최종 결제) | `GET/POST /orders` |
| `_menu.html` | 공통 메뉴 폼 (헤더/푸터) | Thymeleaf `fragments/menu` |
| 로그인 | 헤더의 `/login` 링크 | `POST /login` (Spring Security) |

**사용 컴포넌트**: 캐러셀, 별점, 탭, 아코디언(Q&A), 드롭다운, 배지, 토스트 + 프로토타입 자체 컴포넌트(카테고리 칩 `.chip`, 수량 조절기 `.stepper`, 가격 `.price`, 요약 `.summary`).

실제 구현에서는 `PRODUCT`, `CART_ITEM`, `ORDER`, `ORDER_ITEM` 테이블과 사원 로그인·복지
포인트를 연결합니다.
