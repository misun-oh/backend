/* =============================================================================
 * app-shell.js  —  프로토타입 공통 화면 골격 (헤더/푸터/로그인)
 * -----------------------------------------------------------------------------
 * 하는 일
 *   1) 페이지 공통 헤더/푸터를 <body data-shell-*> 설정값으로 그려서 주입
 *   2) "로그인" 버튼 → 로그인 모달 → 로그인하면 sessionStorage 에 상태 저장
 *      (앱별로 data-shell-key 네임스페이스로 분리)
 *   3) 로그인 상태면 헤더가 "사용자 칩 + 로그아웃" 으로 바뀌고, 페이지를 옮겨
 *      다녀도 유지됨. 스프링에서는 이 부분이 Thymeleaf fragment + Security 로 대체.
 *
 * 로드 순서 (각 HTML): /js/component.js  →  /js/app-shell.js   (둘 다 defer)
 *
 * 페이지에서 설정하는 방법 — <body> 에 data 속성:
 *   data-shell-brand="📚 사내도서관"          헤더 브랜드(이모지 가능)
 *   data-shell-home="index.html"              브랜드 클릭 시 이동
 *   data-shell-active="home"                  현재 메뉴 key
 *   data-shell-key="library"                  로그인 상태 저장 네임스페이스
 *   data-shell-login="false"                  (선택) 로그인 UI 끄기
 *   data-shell-nav='[{"href":"index.html","key":"home","label":"홈"},
 *                    {"href":"my.html","key":"mine","label":"내 대여"}]'
 *
 * 그리고 본문에 자리만 둡니다:
 *   <header data-app-header></header>  ...  <footer data-app-footer></footer>
 *
 * 전역 API : window.appShell
 *   appShell.auth.get() / .login(username) / .logout()
 *   appShell.config                      (읽어들인 설정)
 *   appShell.renderAuthSlot()            (헤더 우측만 다시 그림)
 * ========================================================================== */
(function () {
  "use strict";

  const body = document.body;
  const cfg = {
    brand:  body.dataset.shellBrand  || "앱",
    home:   body.dataset.shellHome   || "index.html",
    active: body.dataset.shellActive || "",
    key:    body.dataset.shellKey    || body.dataset.shellBrand || "app",
    login:  body.dataset.shellLogin  !== "false",
    nav:    parseNav(body.dataset.shellNav),
  };

  function parseNav(json) {
    if (!json) return [];
    try { return JSON.parse(json); } catch (e) { console.warn("[app-shell] data-shell-nav JSON 오류:", e); return []; }
  }

  /* --- 앱별 가짜 세션 -------------------------------------------------- */
  const STORE = "shell-auth:" + cfg.key;
  const auth = {
    get() {
      try { return JSON.parse(sessionStorage.getItem(STORE) || "null"); }
      catch (e) { return null; }
    },
    login(username) {
      const user = { username: username || "user", name: username || "홍길동" };
      try { sessionStorage.setItem(STORE, JSON.stringify(user)); } catch (e) {}
      return user;
    },
    logout() { try { sessionStorage.removeItem(STORE); } catch (e) {} },
  };

  /* --- 마크업 -------------------------------------------------------- */
  function headerHTML() {
    const links = cfg.nav.map((n) =>
      `<a href="${n.href}"${n.key === cfg.active ? ' aria-current="page"' : ""}>${n.label}</a>`
    ).join("");
    return `
      <div class="site-header__inner container container--wide">
        <a class="site-header__brand" href="${cfg.home}">${cfg.brand}</a>
        <nav class="site-nav hide-mobile" aria-label="주요 메뉴">${links}</nav>
        <span class="spacer"></span>
        <button class="btn btn--ghost btn--sm" type="button" data-theme-toggle aria-label="다크 모드 전환">🌓</button>
        ${cfg.login ? '<span data-auth-slot></span>' : ""}
      </div>`;
  }

  function authSlotHTML(user) {
    if (user) {
      return `
        <span class="user-chip"><span class="avatar avatar--sm">${user.name.slice(0, 1)}</span>${user.name}</span>
        <button class="btn btn--secondary btn--sm" type="button" data-logout>로그아웃</button>`;
    }
    return `<button class="btn btn--primary btn--sm" type="button" data-modal-open="shell-login">로그인</button>`;
  }

  const footerHTML = `
    <div class="container container--wide row row--between">
      <p>${cfg.brand} · 학습용 프로토타입</p>
      <span class="muted" style="font-size: var(--text-sm)">디자인 시스템: /css/basic.css</span>
    </div>`;

  const loginModalHTML = `
    <div class="modal" id="shell-login" data-modal hidden>
      <div class="modal__overlay" data-modal-close></div>
      <div class="modal__panel" role="dialog" aria-modal="true" tabindex="-1" aria-labelledby="shell-login-title" style="width: min(400px, 100%)">
        <button class="modal__close" type="button" data-modal-close aria-label="닫기">×</button>
        <p class="eyebrow">${cfg.brand}</p>
        <h3 class="modal__title" id="shell-login-title">로그인</h3>
        <form class="modal__body stack" style="--gap: var(--space-4)" data-login-form>
          <div class="field">
            <label class="label" for="shell-login-id">아이디</label>
            <input class="input" id="shell-login-id" name="username" value="user01" autocomplete="username" required>
          </div>
          <div class="field">
            <label class="label" for="shell-login-pw">비밀번호</label>
            <input class="input" id="shell-login-pw" name="password" type="password" value="1234" autocomplete="current-password" required>
          </div>
          <label class="check"><input type="checkbox" checked><span>로그인 상태 유지</span></label>
          <button class="btn btn--primary btn--block" type="submit">로그인</button>
          <p class="form-hint text-center">프로토타입입니다 — 아무 값이나 입력해도 로그인됩니다.</p>
        </form>
      </div>
    </div>`;

  /* --- 렌더 -------------------------------------------------------- */
  function renderAuthSlot() {
    const slot = document.querySelector("[data-auth-slot]");
    if (!slot) return;
    slot.innerHTML = authSlotHTML(auth.get());

    const logoutBtn = slot.querySelector("[data-logout]");
    if (logoutBtn) {
      logoutBtn.addEventListener("click", () => {
        auth.logout();
        renderAuthSlot();
        if (window.UI) UI.toast("로그아웃되었습니다.");
      });
    }
    if (window.UI) UI.init(slot); // 새로 그린 로그인 버튼에 모달 열기 연결
  }

  function mount() {
    const headerHost = document.querySelector("[data-app-header]");
    if (headerHost) {
      headerHost.classList.add("site-header");
      headerHost.innerHTML = headerHTML();
    }
    const footerHost = document.querySelector("[data-app-footer]");
    if (footerHost) {
      footerHost.classList.add("site-footer");
      footerHost.innerHTML = footerHTML;
    }
    if (cfg.login && !document.getElementById("shell-login")) {
      document.body.insertAdjacentHTML("beforeend", loginModalHTML);
    }
    if (window.UI) UI.init(document); // 주입된 테마 토글·모달 초기화

    if (cfg.login) {
      renderAuthSlot();
      const form = document.querySelector("[data-login-form]");
      if (form) {
        form.addEventListener("submit", (e) => {
          e.preventDefault();
          const username = form.elements.username.value.trim() || "user01";
          auth.login(username);
          if (window.UI) UI.modal.close("shell-login");
          renderAuthSlot();
          if (window.UI) UI.toast(username + "님, 환영합니다.", { type: "success" });
        });
      }
    }
  }

  if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", mount);
  } else {
    mount();
  }

  window.appShell = { auth, config: cfg, renderAuthSlot };
})();
