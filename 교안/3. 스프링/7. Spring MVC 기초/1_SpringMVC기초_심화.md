# Day 7. Spring MVC 기초 — 심화 (경험자용)

## 1. 요청 처리 파이프라인 상세

`DispatcherServlet.doDispatch()` 대략:
1. `HandlerMapping` 조회 → `HandlerExecutionChain`(핸들러 + 인터셉터들)
2. 매칭되는 `HandlerAdapter` 선택 (`RequestMappingHandlerAdapter`)
3. `HandlerInterceptor.preHandle()` 들
4. **인자 해석**: `HandlerMethodArgumentResolver` 들 (`@RequestParam`, `@PathVariable`, `@RequestBody`,
   `Model`, `Pageable` …)
5. 컨트롤러 메서드 호출
6. **반환값 처리**: `HandlerMethodReturnValueHandler` 들 (String→뷰, `@ResponseBody`→메시지 컨버터,
   `ResponseEntity`, `ModelAndView` …)
7. `postHandle()` → 뷰 렌더링 → `afterCompletion()`

확장 지점: `WebMvcConfigurer` (인터셉터·리졸버·컨버터·CORS·정적리소스 핸들러 등록),
`@ControllerAdvice`(`@InitBinder`, `@ModelAttribute`, `@ExceptionHandler` 전역화).

## 2. `HandlerInterceptor` vs 서블릿 `Filter` vs AOP

| | Filter | Interceptor | 컨트롤러 AOP |
|---|---|---|---|
| 위치 | 서블릿 컨테이너 (DispatcherServlet 바깥) | DispatcherServlet 안, 핸들러 전후 | 빈 메서드 호출 전후 |
| 접근 정보 | `ServletRequest/Response` (핸들러 모름) | 어떤 핸들러가 처리할지 앎(`HandlerMethod`) | 메서드 시그니처·인자 |
| 대표 용도 | 인코딩, CORS, 보안(Spring Security), 로깅 원본 | 로그인 체크, 공통 모델, 처리시간 | 서비스 로깅·트랜잭션 |

## 3. 타입 변환 & 포매팅

- 문자열 → `Long`/`LocalDate`/enum : `ConversionService` + `Formatter`.
- 커스텀: `Converter<String, EmpStatus>` 를 빈으로 등록하거나 `WebMvcConfigurer.addFormatters`.
- `@InitBinder` 로 컨트롤러 국소 설정: `binder.registerCustomEditor(...)`,
  `binder.setDisallowedFields("id")`(대량 바인딩 취약점 방지).
- 날짜 전역 포맷: `spring.mvc.format.date=yyyy-MM-dd`, `spring.mvc.format.date-time=...`.

## 4. `@ModelAttribute` 의 두 얼굴

- **파라미터**로: 요청 데이터를 객체에 바인딩.
- **메서드**로 (`@ModelAttribute("depts") List<Dept> depts()`): 그 컨트롤러의 모든 뷰에 공통 모델 주입.
  드롭다운 목록처럼 매 화면에 필요한 데이터에 유용. `@ControllerAdvice` 에 두면 전역.

바인딩 시 검증 실패를 잡으려면 바로 뒤에 `BindingResult` 파라미터(Day 14):
```java
public String create(@Valid @ModelAttribute EmpForm form, BindingResult br) { ... }
```
`BindingResult` 는 반드시 `@ModelAttribute` **바로 다음** 위치에.

## 5. 콘텐츠 협상 (Content Negotiation)

같은 URL이 Accept 헤더에 따라 HTML/JSON을 다르게 줄 수 있음. 부트 기본은 헤더 기반.
`spring.mvc.contentnegotiation.favor-parameter=true` + `?format=json` 등. 대개는 URL을 분리하는 게 명확
(`/emps` = 화면, `/api/emps` = JSON — 이 과정 방식).

## 6. 정적 리소스 & 경로 매핑

- `src/main/resources/static/**` → `/**` 로 서빙 (`static/css/basic.css` → `/css/basic.css`).
- `spring.web.resources.static-locations`, `spring.mvc.static-path-pattern` 으로 조정.
- Thymeleaf 에서는 `th:href="@{/css/basic.css}"` — 컨텍스트 경로를 자동으로 붙여 준다.
- 프로토타입(`prototypes/static/`)의 `css/basic.css` 등을 여기로 옮기면 Day 8에서 바로 쓸 수 있다.

## 7. 예외의 기본 처리

컨트롤러에서 예외가 나면: `HandlerExceptionResolver` 체인 →
`ExceptionHandlerExceptionResolver`(`@ExceptionHandler`) → `ResponseStatusExceptionResolver`
(`@ResponseStatus`, `ResponseStatusException`) → `DefaultHandlerExceptionResolver`(스프링 표준 예외 →
상태코드) → 못 잡으면 `/error`(`BasicErrorController`, Whitelabel).
전역 커스터마이징은 Day 14 `@ControllerAdvice`.

## 8. 테스트: `@WebMvcTest` + `MockMvc`

```java
@WebMvcTest(EmpViewController.class)
class EmpViewControllerTest {
    @Autowired MockMvc mvc;
    @MockitoBean EmpService empService;     // Boot 4: @MockBean → @MockitoBean

    @Test void 목록화면() throws Exception {
        given(empService.findAll()).willReturn(List.of(/* ... */));
        mvc.perform(get("/emps"))
           .andExpect(status().isOk())
           .andExpect(view().name("emp/list"))
           .andExpect(model().attributeExists("emps"));
    }
}
```
컨트롤러 계층만 로딩(서비스는 목). 자세한 사용은 Day 10·15.
