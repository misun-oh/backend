/* =============================================================================
 * component.js  —  도서대여시스템 디자인 템플릿 (컴포넌트 동작)
 * -----------------------------------------------------------------------------
 * 순수 바닐라 JS. 외부 라이브러리 의존성 없음. <script src="/js/component.js" defer>
 *
 * 자동 초기화 : DOMContentLoaded 시 data-* 속성이 붙은 요소를 찾아 켭니다.
 *   data-carousel        캐러셀
 *   data-modal / data-modal-open="id"   모달
 *   data-accordion       아코디언(한 번에 하나만 열기)
 *   data-tabs            탭
 *   data-dropdown        드롭다운
 *   data-rating-input    입력용 별점
 *   data-theme-toggle    라이트/다크 전환 버튼
 *
 * 수동 제어 : 전역 객체 UI
 *   UI.modal.open(id) / UI.modal.close(id)
 *   UI.toast(message, { type, duration })
 *   UI.theme.set('dark' | 'light') / UI.theme.toggle()
 *   UI.carousel(element).next() / .prev() / .goTo(index)
 *
 * 동적으로 추가한 DOM 은 UI.init(rootElement) 로 다시 초기화할 수 있습니다.
 * ========================================================================== */
(function (global) {
  "use strict";

  /* --- 작은 도우미들 ----------------------------------------------------- */
  const $  = (sel, ctx = document) => ctx.querySelector(sel);
  const $$ = (sel, ctx = document) => Array.from(ctx.querySelectorAll(sel));
  const onceInit = (el, key) => {
    // 같은 요소를 두 번 초기화하지 않도록 표시
    if (el.dataset[key] === "ready") return false;
    el.dataset[key] = "ready";
    return true;
  };
  const prefersReducedMotion = () =>
    global.matchMedia && global.matchMedia("(prefers-reduced-motion: reduce)").matches;

  /* =========================================================================
   * 캐러셀
   * ====================================================================== */
  function initCarousel(root) {
    if (!onceInit(root, "carouselState")) return null;

    const track  = $(".carousel__track", root);
    const slides = $$(".carousel__slide", root);
    const prevBtn = $(".carousel__arrow--prev", root);
    const nextBtn = $(".carousel__arrow--next", root);
    const dotsBox = $(".carousel__dots", root);
    if (!track || slides.length === 0) return null;

    const perView = parseInt(root.dataset.perView || "1", 10);
    const loop    = root.hasAttribute("data-loop");
    const autoplayMs = parseInt(root.dataset.autoplay || "0", 10);
    const maxIndex = Math.max(0, slides.length - perView);

    root.style.setProperty("--per-view", perView);
    let index = 0;
    let timer = null;

    function render() {
      track.style.transform = `translateX(-${(index * 100) / perView}%)`;
      if (dotsBox) {
        $$(".carousel__dot", dotsBox).forEach((d, i) =>
          d.setAttribute("aria-current", String(i === index))
        );
      }
      if (!loop) {
        if (prevBtn) prevBtn.disabled = index === 0;
        if (nextBtn) nextBtn.disabled = index === maxIndex;
      }
    }
    function goTo(i) {
      if (loop) index = (i + (maxIndex + 1)) % (maxIndex + 1);
      else index = Math.min(Math.max(i, 0), maxIndex);
      render();
    }
    const next = () => goTo(index + 1);
    const prev = () => goTo(index - 1);

    // 점(dot) 생성
    if (dotsBox) {
      dotsBox.innerHTML = "";
      for (let i = 0; i <= maxIndex; i++) {
        const dot = document.createElement("button");
        dot.className = "carousel__dot";
        dot.type = "button";
        dot.setAttribute("aria-label", `${i + 1}번째 슬라이드`);
        dot.addEventListener("click", () => { goTo(i); restart(); });
        dotsBox.appendChild(dot);
      }
    }

    if (nextBtn) nextBtn.addEventListener("click", () => { next(); restart(); });
    if (prevBtn) prevBtn.addEventListener("click", () => { prev(); restart(); });

    // 키보드
    root.tabIndex = 0;
    root.addEventListener("keydown", (e) => {
      if (e.key === "ArrowRight") { next(); restart(); }
      if (e.key === "ArrowLeft")  { prev(); restart(); }
    });

    // 터치 스와이프
    let startX = 0, dragging = false;
    root.addEventListener("touchstart", (e) => { startX = e.touches[0].clientX; dragging = true; stop(); }, { passive: true });
    root.addEventListener("touchend", (e) => {
      if (!dragging) return;
      dragging = false;
      const dx = e.changedTouches[0].clientX - startX;
      if (Math.abs(dx) > 40) (dx < 0 ? next : prev)();
      restart();
    });

    // 자동재생
    function start() {
      if (autoplayMs > 0 && !prefersReducedMotion()) timer = setInterval(next, autoplayMs);
    }
    function stop() { if (timer) { clearInterval(timer); timer = null; } }
    function restart() { stop(); start(); }
    root.addEventListener("mouseenter", stop);
    root.addEventListener("mouseleave", start);

    render();
    start();

    const api = { next, prev, goTo, start, stop };
    root._carousel = api;
    return api;
  }

  /* =========================================================================
   * 모달
   * ====================================================================== */
  const FOCUSABLE = 'a[href],button:not([disabled]),input:not([disabled]),select:not([disabled]),textarea:not([disabled]),[tabindex]:not([tabindex="-1"])';
  let lastFocused = null;

  function openModal(id) {
    const modal = document.getElementById(id);
    if (!modal || !modal.hasAttribute("data-modal")) return;
    lastFocused = document.activeElement;
    modal.hidden = false;
    // 다음 프레임에 클래스 추가 → CSS 트랜지션 발동
    requestAnimationFrame(() => modal.classList.add("is-open"));
    document.body.classList.add("modal-open");

    const panel = $(".modal__panel", modal);
    const focusables = panel ? $$(FOCUSABLE, panel) : [];
    (focusables[0] || panel || modal).focus({ preventScroll: true });

    modal._onKeydown = (e) => {
      if (e.key === "Escape") closeModal(id);
      if (e.key === "Tab" && focusables.length) {
        const first = focusables[0];
        const last = focusables[focusables.length - 1];
        if (e.shiftKey && document.activeElement === first) { e.preventDefault(); last.focus(); }
        else if (!e.shiftKey && document.activeElement === last) { e.preventDefault(); first.focus(); }
      }
    };
    document.addEventListener("keydown", modal._onKeydown);
  }

  function closeModal(id) {
    const modal = document.getElementById(id);
    if (!modal) return;
    modal.classList.remove("is-open");
    document.body.classList.remove("modal-open");
    if (modal._onKeydown) document.removeEventListener("keydown", modal._onKeydown);

    const done = () => { modal.hidden = true; modal.removeEventListener("transitionend", done); };
    if (prefersReducedMotion()) done();
    else {
      modal.addEventListener("transitionend", done);
      setTimeout(done, 400); // 안전장치
    }
    if (lastFocused) lastFocused.focus({ preventScroll: true });
  }

  function initModals(root) {
    $$("[data-modal-open]", root).forEach((btn) => {
      if (!onceInit(btn, "modalTrigger")) return;
      btn.addEventListener("click", () => openModal(btn.getAttribute("data-modal-open")));
    });
    $$("[data-modal]", root).forEach((modal) => {
      if (!onceInit(modal, "modalReady")) return;
      $$("[data-modal-close]", modal).forEach((el) =>
        el.addEventListener("click", () => closeModal(modal.id))
      );
    });
  }

  /* =========================================================================
   * 아코디언 (한 번에 하나만 열기)
   * ====================================================================== */
  function initAccordion(root) {
    if (!onceInit(root, "accordionReady")) return;
    const items = $$(".accordion__item", root);
    items.forEach((item) => {
      item.addEventListener("toggle", () => {
        if (item.open) {
          items.forEach((other) => { if (other !== item) other.open = false; });
        }
      });
    });
  }

  /* =========================================================================
   * 탭
   * ====================================================================== */
  function initTabs(root) {
    if (!onceInit(root, "tabsReady")) return;
    const tabs = $$('[role="tab"]', root);
    const panels = $$('[role="tabpanel"]', root);

    function activate(tab) {
      tabs.forEach((t) => t.setAttribute("aria-selected", String(t === tab)));
      const targetId = tab.getAttribute("aria-controls");
      panels.forEach((p) => { p.hidden = p.id !== targetId; });
    }
    tabs.forEach((tab, i) => {
      tab.addEventListener("click", () => activate(tab));
      tab.addEventListener("keydown", (e) => {
        if (e.key !== "ArrowRight" && e.key !== "ArrowLeft") return;
        e.preventDefault();
        const dir = e.key === "ArrowRight" ? 1 : -1;
        const nextTab = tabs[(i + dir + tabs.length) % tabs.length];
        nextTab.focus();
        activate(nextTab);
      });
    });
  }

  /* =========================================================================
   * 드롭다운
   * ====================================================================== */
  function initDropdown(root) {
    if (!onceInit(root, "dropdownReady")) return;
    const toggle = $("[data-dropdown-toggle]", root);
    if (!toggle) return;

    const close = () => {
      root.classList.remove("is-open");
      toggle.setAttribute("aria-expanded", "false");
    };
    const open = () => {
      root.classList.add("is-open");
      toggle.setAttribute("aria-expanded", "true");
    };
    toggle.addEventListener("click", (e) => {
      e.stopPropagation();
      root.classList.contains("is-open") ? close() : open();
    });
    document.addEventListener("click", (e) => { if (!root.contains(e.target)) close(); });
    root.addEventListener("keydown", (e) => { if (e.key === "Escape") { close(); toggle.focus(); } });
    // 항목 클릭 시 선택 표시 + 라벨 갱신 + 닫기
    const labelEl = toggle.querySelector("[data-dropdown-label]");
    $$(".dropdown__item", root).forEach((item) => {
      item.addEventListener("click", () => {
        $$(".dropdown__item", root).forEach((i) => i.removeAttribute("aria-current"));
        item.setAttribute("aria-current", "true");
        if (labelEl && root.dataset.dropdownSyncLabel !== "false") {
          labelEl.textContent = item.textContent.trim();
        }
        root.dispatchEvent(new CustomEvent("dropdown:select", {
          bubbles: true,
          detail: { value: item.dataset.value ?? item.textContent.trim() },
        }));
        close();
      });
    });
  }

  /* =========================================================================
   * 입력용 별점 : data-rating-input
   *   - 안에 라디오가 있으면 그대로 사용
   *   - 없으면 data-name / data-value(초기값) 로 별 5개 + hidden input 생성
   * ====================================================================== */
  function initRatingInput(root) {
    if (!onceInit(root, "ratingReady")) return;

    let radios = $$('input[type="radio"]', root);
    if (radios.length === 0) {
      const name = root.dataset.name || "rating";
      const initial = parseInt(root.dataset.value || "0", 10);
      root.innerHTML = "";
      // row-reverse 레이아웃이므로 5 → 1 순서로 넣는다
      for (let v = 5; v >= 1; v--) {
        const id = `${name}-${v}-${Math.random().toString(36).slice(2, 7)}`;
        const input = document.createElement("input");
        input.type = "radio"; input.name = name; input.value = String(v); input.id = id;
        if (v === initial) input.checked = true;
        const label = document.createElement("label");
        label.setAttribute("for", id);
        label.setAttribute("aria-label", `${v}점`);
        label.textContent = "★";
        root.append(input, label);
      }
      radios = $$('input[type="radio"]', root);
    }

    root.addEventListener("change", () => {
      const checked = radios.find((r) => r.checked);
      root.dispatchEvent(new CustomEvent("rating:change", {
        bubbles: true,
        detail: { value: checked ? Number(checked.value) : 0 },
      }));
    });
  }

  /* =========================================================================
   * 토스트
   * ====================================================================== */
  function getToastStack() {
    let stack = $(".toast-stack");
    if (!stack) {
      stack = document.createElement("div");
      stack.className = "toast-stack";
      document.body.appendChild(stack);
    }
    return stack;
  }
  function toast(message, opts = {}) {
    const { type = "default", duration = 3000 } = opts;
    const stack = getToastStack();

    const el = document.createElement("div");
    el.className = "toast" + (type !== "default" ? ` toast--${type}` : "");
    el.setAttribute("role", type === "danger" ? "alert" : "status");

    const text = document.createElement("span");
    text.textContent = message;
    const close = document.createElement("button");
    close.className = "toast__close";
    close.type = "button";
    close.setAttribute("aria-label", "닫기");
    close.textContent = "×";
    el.append(text, close);
    stack.appendChild(el);

    requestAnimationFrame(() => el.classList.add("is-visible"));

    let timer;
    const remove = () => {
      el.classList.add("is-leaving");
      el.classList.remove("is-visible");
      el.addEventListener("transitionend", () => el.remove(), { once: true });
      setTimeout(() => el.remove(), 400);
      clearTimeout(timer);
    };
    close.addEventListener("click", remove);
    if (duration > 0) timer = setTimeout(remove, duration);
    return { dismiss: remove };
  }

  /* =========================================================================
   * 테마 (라이트 / 다크)
   *   저장: localStorage['ui-theme']  (없으면 OS 설정을 따름)
   * ====================================================================== */
  const theme = {
    get() {
      try { return localStorage.getItem("ui-theme"); } catch (e) { return null; }
    },
    set(value) {
      document.documentElement.setAttribute("data-theme", value);
      try { localStorage.setItem("ui-theme", value); } catch (e) { /* 무시 */ }
      $$("[data-theme-toggle]").forEach((b) => b.setAttribute("aria-pressed", String(value === "dark")));
    },
    toggle() {
      const current =
        document.documentElement.getAttribute("data-theme") ||
        (global.matchMedia("(prefers-color-scheme: dark)").matches ? "dark" : "light");
      this.set(current === "dark" ? "light" : "dark");
    },
    _restore() {
      const saved = this.get();
      if (saved) document.documentElement.setAttribute("data-theme", saved);
    },
  };
  function initThemeToggles(root) {
    const isDark =
      (document.documentElement.getAttribute("data-theme") ||
        (global.matchMedia("(prefers-color-scheme: dark)").matches ? "dark" : "light")) === "dark";
    $$("[data-theme-toggle]", root).forEach((btn) => {
      btn.setAttribute("aria-pressed", String(isDark)); // 현재 테마와 동기화
      if (!onceInit(btn, "themeToggleReady")) return;
      btn.addEventListener("click", () => theme.toggle());
    });
  }

  /* =========================================================================
   * 통합 초기화
   * ====================================================================== */
  function init(root = document) {
    $$("[data-carousel]", root).forEach(initCarousel);
    initModals(root);
    $$("[data-accordion]", root).forEach(initAccordion);
    $$("[data-tabs]", root).forEach(initTabs);
    $$("[data-dropdown]", root).forEach(initDropdown);
    $$("[data-rating-input]", root).forEach(initRatingInput);
    initThemeToggles(root);
  }

  theme._restore(); // FOUC 방지: 최대한 빨리 저장된 테마 적용

  const UI = {
    init,
    modal: { open: openModal, close: closeModal },
    toast,
    theme,
    carousel: (el) => el._carousel || initCarousel(el),
  };
  global.UI = UI;

  if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", () => init());
  } else {
    init();
  }
})(window);
