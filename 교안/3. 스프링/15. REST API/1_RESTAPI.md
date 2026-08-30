# Day 15. REST API

| 항목 | 내용 |
|---|---|
| 선수학습 | Day 7(MVC), Day 12(페이징), Day 14(검증·예외), HTTP 메서드·상태코드 |
| 이번 챕터 | `@RestController` → `@RequestBody`/`@ResponseBody` → `ResponseEntity` 와 상태 코드 → 요청/응답 DTO → Jackson(JSON) → `@RestControllerAdvice` 공통 에러 응답 → MockMvc 테스트 |
| 권장 진행 | 1일 |
| 결과물 | `/api/emps` CRUD API + 표준 에러 응답 + API 테스트 |

## 학습목표

- 화면(`@Controller`)과 API(`@RestController`)의 응답 방식 차이를 안다.
- `@RequestBody` 로 JSON 본문을 DTO로 받고, 객체 반환이 JSON이 되는 과정을 안다.
- `ResponseEntity` 로 상태 코드·헤더를 제어하고, REST 관례에 맞는 코드를 고른다.
- 요청 DTO와 응답 DTO를 분리하는 이유를 안다.
- `@RestControllerAdvice` 로 검증 실패·업무 예외를 **일관된 JSON 에러**로 변환한다.
- `MockMvc` 로 API를 테스트한다.

---

## 1. 화면 vs API

| | 화면 (`@Controller`) | API (`@RestController`) |
|---|---|---|
| 요청 데이터 | 폼(`@ModelAttribute`) | JSON 본문(`@RequestBody`) |
| 응답 | 뷰 이름 → HTML | 객체 → JSON |
| 오류 표시 | 폼으로 되돌아가 `th:errors` | JSON 에러 본문 + 상태 코드 |
| 리다이렉트 | PRG | 없음(클라이언트가 판단) |

이 과정은 URL로 구분합니다: `/emps` = 화면, **`/api/emps` = API**.

---

## 2. 기본 CRUD API

```java
@RestController
@RequestMapping("/api/emps")
@RequiredArgsConstructor
public class EmpApiController {

    private final EmpService empService;

    // 목록 (페이징) : GET /api/emps?keyword=김&page=1&size=10
    @GetMapping
    public PageResult<EmpResponse> list(EmpSearchCond cond) {
        PageResult<Emp> page = empService.search(cond);      // 서비스는 도메인 Emp
        return new PageResult<>(                             // API 경계에서 응답 DTO로
                page.content().stream().map(EmpResponse::from).toList(),
                page.totalElements(), page.page(), page.size());
    }

    // 단건 : GET /api/emps/205
    @GetMapping("/{id}")
    public EmpResponse detail(@PathVariable Long id) {
        return EmpResponse.from(empService.get(id));
    }

    // 등록 : POST /api/emps  (본문 JSON)
    @PostMapping
    public ResponseEntity<EmpResponse> create(@Valid @RequestBody EmpCreateRequest req) {
        Long id = empService.register(req.toForm());
        EmpResponse body = EmpResponse.from(empService.get(id));
        return ResponseEntity
                .created(URI.create("/api/emps/" + id))   // 201 + Location 헤더
                .body(body);
    }

    // 수정 : PUT /api/emps/205
    @PutMapping("/{id}")
    public EmpResponse update(@PathVariable Long id, @Valid @RequestBody EmpUpdateRequest req) {
        empService.modify(id, req.toForm());
        return EmpResponse.from(empService.get(id));
    }

    // 삭제 : DELETE /api/emps/205
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        empService.remove(id);
        return ResponseEntity.noContent().build();          // 204
    }
}
```

- `@RequestBody` : HTTP 본문 JSON → 객체 (Jackson 이 자동 구성 — Spring Boot 4는 **Jackson 3**).
- 반환 객체 → JSON (`@RestController` = `@Controller` + `@ResponseBody`).
- 화면과 달리 API는 `DELETE`, `PUT` 메서드를 실제로 씁니다(브라우저 form 제약 없음).

---

## 3. 상태 코드

| 상황 | 코드 | 방법 |
|---|---|---|
| 조회 성공 | 200 OK | 그냥 객체 반환 |
| 생성 성공 | **201 Created** + `Location` | `ResponseEntity.created(uri).body(...)` |
| 수정 성공(본문 있음) | 200 | 객체 반환 |
| 성공했으나 응답 본문 없음 | **204 No Content** | `ResponseEntity.noContent().build()` |
| 잘못된 입력 | **400 Bad Request** | 검증 실패 자동 / `@ResponseStatus` |
| 인증 필요 | 401 | Day 17~19 |
| 권한 없음 | 403 | Day 17 |
| 리소스 없음 | **404 Not Found** | 예외 → advice |
| 업무 규칙 위반(충돌) | **409 Conflict** | 커스텀 예외 → advice |
| 서버 오류 | 500 | advice catch-all |

`@ResponseStatus` 로 간단히, 세밀한 제어는 `ResponseEntity`.

---

## 4. 요청 DTO / 응답 DTO 분리

화면용 `EmpForm` 을 API에 그대로 쓰지 않습니다.

```java
// 요청: 클라이언트가 보낼 수 있는 것만
public record EmpCreateRequest(
        @NotBlank String empName,
        @NotBlank @Email String email,
        @NotNull Long deptId,
        @NotNull @PositiveOrZero Integer salary,
        @NotNull @PastOrPresent LocalDate hireDate) {

    public EmpForm toForm() {
        EmpForm f = new EmpForm();
        f.setEmpName(empName); f.setEmail(email); f.setDeptId(deptId);
        f.setSalary(salary); f.setHireDate(hireDate);
        return f;
    }
}

// 응답: 클라이언트에게 보여줄 것만 (내부 필드·민감정보 제외)
public record EmpResponse(Long empId, String empName, String email,
                          String deptName, LocalDate hireDate, boolean active) {
    public static EmpResponse from(Emp e) {
        return new EmpResponse(e.getEmpId(), e.getEmpName(), e.getEmail(),
                e.getDeptName(),                    // 조회 SQL이 DEPT 조인해 채운 읽기 필드
                e.getHireDate(), e.isActive());
    }
}
```

이유:
- API 계약을 명시적으로 고정(도메인이 바뀌어도 응답은 안 바뀜).
- 등록 시 `empId`·`active` 같은 서버 결정 필드를 클라이언트가 못 보냄.
- 응답에서 비밀번호·주민번호 등 민감 필드를 자연히 제외.

---

## 5. Jackson(JSON) 자주 쓰는 것

Spring Boot 4의 기본 JSON 라이브러리는 **Jackson 3** 입니다. 아래 설정·애노테이션은 3에서도 그대로입니다.

```yaml
spring:
  jackson:
    default-property-inclusion: non_null   # null 필드는 응답에서 생략
    time-zone: Asia/Seoul
```

- Jackson 3은 **날짜를 기본으로 ISO-8601 문자열**로 씁니다(3.x에서 필요하던
  `serialization.write-dates-as-timestamps: false` 가 이제 기본값).
- Jackson 3에서 프로그램적으로 매퍼를 다룰 때는 클래스 패키지가 `com.fasterxml.jackson` →
  **`tools.jackson`** 으로 바뀝니다(`tools.jackson.databind.ObjectMapper`).
  하지만 **아래 애노테이션은 패키지가 그대로**(`com.fasterxml.jackson.annotation.*`)라
  DTO에 붙이는 코드는 바꿀 게 없습니다.

| 애노테이션 (`com.fasterxml.jackson.annotation.*`) | 용도 |
|---|---|
| `@JsonProperty("emp_name")` | JSON 키 이름 지정 |
| `@JsonIgnore` | 직렬화 제외 |
| `@JsonFormat(pattern = "yyyy-MM-dd")` | 날짜 형식 |
| `@JsonInclude(NON_NULL)` | null 생략 (클래스/필드) |
| `@JsonCreator` / `record` | 역직렬화 생성자 |

`LocalDate`/`LocalDateTime`(java.time) 은 Jackson 3에서 **코어에 내장**되어 별도 모듈 없이 자동 처리됩니다.

---

## 6. 표준 에러 응답 — `@RestControllerAdvice`

```java
public record ApiError(String code, String message, Map<String, String> fieldErrors) {
    public static ApiError of(String code, String message) { return new ApiError(code, message, null); }
}

@Slf4j
@RestControllerAdvice(basePackages = "com.example.hr.controller")   // API 컨트롤러 대상
public class ApiExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError validation(MethodArgumentNotValidException e) {
        Map<String, String> fe = new LinkedHashMap<>();
        e.getBindingResult().getFieldErrors()
         .forEach(x -> fe.putIfAbsent(x.getField(), x.getDefaultMessage()));
        return new ApiError("VALIDATION_ERROR", "입력값을 확인하세요", fe);
    }

    @ExceptionHandler(NoSuchElementException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError notFound(NoSuchElementException e) {
        return ApiError.of("NOT_FOUND", e.getMessage());
    }

    @ExceptionHandler(BusinessException.class)     // 커스텀 런타임 예외
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError business(BusinessException e) {
        return ApiError.of(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError etc(Exception e) {
        log.error("API 예외", e);
        return ApiError.of("INTERNAL_ERROR", "서버 오류");
    }
}
```

응답 예:
```json
POST /api/emps  → 400
{ "code": "VALIDATION_ERROR", "message": "입력값을 확인하세요",
  "fieldErrors": { "empName": "이름은 필수입니다", "email": "이메일 형식이 아닙니다" } }
```

> 화면용 `@ControllerAdvice`(Day 14)와 API용 `@RestControllerAdvice` 를 `basePackages` 로 나눠
> 둘이 안 겹치게 합니다.

---

## 7. MockMvc 로 API 테스트

```java
@WebMvcTest(EmpApiController.class)
class EmpApiControllerTest {

    @Autowired MockMvc mvc;                  // @WebMvcTest 가 자동 구성 (Boot 4에서도 그대로)
    @MockitoBean EmpService empService;      // Boot 4: @MockBean → @MockitoBean

    @Test
    void 단건조회_200_그리고_JSON() throws Exception {
        given(empService.get(205L)).willReturn(
                Emp.builder().empId(205L).empName("박지민").email("p@x.com")
                   .hireDate(LocalDate.of(2015,5,20)).active(true).build());

        mvc.perform(get("/api/emps/205"))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.empId").value(205))
           .andExpect(jsonPath("$.empName").value("박지민"));
    }

    @Test
    void 등록_201_Location헤더() throws Exception {
        given(empService.register(any())).willReturn(222L);
        given(empService.get(222L)).willReturn(Emp.builder().empId(222L).empName("신입").build());

        String body = """
            {"empName":"신입","email":"a@b.com","deptId":5,"salary":2500000,"hireDate":"2026-09-01"}
            """;

        mvc.perform(post("/api/emps").contentType(APPLICATION_JSON).content(body))
           .andExpect(status().isCreated())
           .andExpect(header().string("Location", "/api/emps/222"));
    }

    @Test
    void 이름_없으면_400_에러JSON() throws Exception {
        mvc.perform(post("/api/emps").contentType(APPLICATION_JSON)
                .content("""{"email":"a@b.com","deptId":5,"salary":100,"hireDate":"2026-01-01"}"""))
           .andExpect(status().isBadRequest())
           .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
           .andExpect(jsonPath("$.fieldErrors.empName").exists());
    }
}
```

- `@WebMvcTest` : 컨트롤러 계층만. 서비스는 `@MockitoBean` (Boot 4: `@MockBean` 삭제). MockMvc는 자동 구성.
- `jsonPath("$.필드")` 로 응답 JSON 검증.
- `@SpringBootTest` + MockMvc 를 쓸 땐 Boot 4에서 **`@AutoConfigureMockMvc` 를 명시**해야 합니다
  (3.x는 `@SpringBootTest` 만으로 됐지만 4.0에서 자동설정이 빠졌습니다). `@WebMvcTest` 는 여전히 자동.

---

## 자주 하는 실수

- **`@RequestBody` 없이 JSON 받기** → 폼 파라미터로 해석돼 전부 null. JSON은 `@RequestBody`.
- **`@RequestBody` DTO에 기본 생성자/세터 없음(일반 클래스)** → 역직렬화 실패. `record` 또는 Lombok.
- **생성인데 200 반환** → 201 + `Location` 이 REST 관례.
- **응답에 도메인 객체 그대로** → 내부 필드·순환참조·민감정보 노출. 응답 DTO로.
- **에러도 200 + `{success:false}`** → 상태 코드로 표현하는 게 표준. 4xx/5xx + 에러 본문.
- **`@ControllerAdvice` 하나로 화면·API 다 처리** → API 예외에 HTML이 나가거나 그 반대. 분리.
- **날짜 형식을 커스텀하고 싶다** → 필드에 `@JsonFormat(pattern="yyyy-MM-dd")`. (Jackson 3 기본은 ISO-8601)

---

## 핵심 요약

| 요소 | 내용 |
|---|---|
| `@RestController` + `@RequestBody` | JSON 본문 ↔ 객체 (Jackson) |
| `ResponseEntity` | 상태 코드·헤더 제어. `created()`, `noContent()` |
| 상태 코드 | 200/201/204/400/404/409/500 관례 |
| 요청/응답 DTO 분리 | API 계약 고정, 민감정보 차단 |
| `@RestControllerAdvice` | 검증·업무 예외 → 일관된 `ApiError` JSON |
| MockMvc | `@WebMvcTest` + `jsonPath` 로 API 검증 |

> 다음(Day 16): 사진·문서 같은 **파일 업로드와 다운로드**.
