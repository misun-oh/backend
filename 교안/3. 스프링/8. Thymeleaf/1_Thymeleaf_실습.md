# Day 8. Thymeleaf — 실습

준비: `prototypes/static/` 의 `css/basic.css`, `css/component.css`, `js/component.js` 를
`src/main/resources/static/css/`, `.../js/` 로 복사하세요.

## 문제 1. 공통 레이아웃 fragment

`templates/fragments/layout.html` 에 `head(title)`, `header`, `footer` fragment 를 만드세요
(교안 7절 참고, 헤더 브랜드는 "👔 사원관리", 링크는 `/emps`, `/depts`).

## 문제 2. 사원 목록 화면

`templates/emp/list.html` 을 `prototypes/templates/hr/index.html` 의 표 마크업을 바탕으로 만드세요.

- `head`, `header`, `footer` fragment 사용
- `${emps}` 를 `th:each` 로 표에 렌더링 (사번·이름·부서명·입사일·상태 badge)
- 이름은 `@{/emps/{id}(id=${e.empId})}` 링크
- 입사일은 `${#temporals.format(e.hireDate, 'yyyy-MM-dd')}`
- 상태: 재직이면 `badge--success`, 아니면 `badge--danger` (`th:classappend` 또는 `th:if`/`th:unless`)
- 목록이 비어 있으면 "등록된 사원이 없습니다" 한 줄

`EmpViewController` 가 메모리 저장소로부터 `emps` 를 넘겨주도록 Day 7 코드를 그대로 사용.
브라우저에서 `/emps` 가 프로토타입과 비슷하게 보이면 성공.

## 문제 3. 사원 상세 화면

`templates/emp/detail.html` — `${emp}` 의 필드를 `info-table` 로 표시. 목록으로 가는 링크,
수정(`/emps/{id}/edit`) 링크, 삭제 `<form method="post">` 버튼.

## 문제 4. 등록/수정 폼 (`th:object` / `th:field`)

`templates/emp/form.html`:

- `th:object="${form}"`, 각 입력에 `th:field="*{empName}"` 등
- 부서 `<select>` 는 `${depts}` 를 `th:each` 로 `<option>` 렌더링, `th:field="*{deptId}"`
- 폼 `th:action="@{/emps}"` (등록) — 수정은 문제 5

`EmpViewController.newForm()` 에서 `model.addAttribute("form", new EmpForm())` 와
`model.addAttribute("depts", deptService.findAll())` 를 넘기세요.

## 문제 5. 수정 화면 재사용

- `GET /emps/{id}/edit` → 기존 사원을 `EmpForm` 으로 채워 `form` 에 담고 `emp/form` 반환.
- 폼 `th:action` 을 등록이면 `/emps`, 수정이면 `/emps/{id}` 가 되도록 분기
  (`th:action="${form.empId != null} ? @{/emps/{id}(id=${form.empId})} : @{/emps}"` 또는 hidden 필드 + 컨트롤러 분리).
- `POST /emps/{id}` → `empService.modify(id, form)` 후 `redirect:/emps/{id}`.

**확인할 것**: 같은 `form.html` 하나로 등록과 수정이 모두 되는가? 수정 화면에 기존 값이 채워지는가?

## 문제 6. (개념) 다음 두 줄의 차이

```html
<a th:href="'/emps/' + ${e.empId}">A</a>
<a th:href="@{/emps/{id}(id=${e.empId})}">B</a>
```

배포 시 컨텍스트 경로가 `/hr` 라면 각각 어떤 URL이 되는가? 어느 쪽을 써야 하나?
