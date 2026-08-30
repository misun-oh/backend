# 자사 쇼핑몰 · HTML 프로토타입

사원이 회사 상품(자사 제품·복지몰)을 구매하는 화면 프로토타입입니다.
공통 디자인 시스템(`/css/basic.css` · `/css/component.css`) + 공통 골격(`/css/app-shell.css` · `/js/app-shell.js`).

| 파일 | 화면 | 스프링 매핑(예상) |
|---|---|---|
| `index.html` | 홈 (프로모션 캐러셀 · 카테고리 칩 · 베스트 상품 그리드) | `GET /` , `GET /products` |
| `product-detail.html` | 상품 상세 (이미지 캐러셀 · 가격/할인 · 옵션·수량 · 정보/리뷰/Q&A 탭) | `GET /products/{id}` |
| `cart.html` | 장바구니 (품목 · 수량 조절 · 결제 요약 sticky) | `GET /cart` |
| `checkout.html` | 주문/결제 (배송지 폼 · 결제수단 · 최종 결제) | `GET/POST /orders` |
| 로그인 | `app-shell.js` 공통 모달 | `POST /login` |

**사용 컴포넌트**: 캐러셀, 별점, 탭, 아코디언(Q&A), 드롭다운, 배지, 토스트 + 프로토타입 자체 컴포넌트(카테고리 칩 `.chip`, 수량 조절기 `.stepper`, 가격 `.price`, 요약 `.summary`).

실제 구현에서는 `PRODUCT`, `CART_ITEM`, `ORDER`, `ORDER_ITEM` 테이블과 사원 로그인·복지
포인트를 연결합니다.
