# Day 15. REST API — 실습 답안

---

## 문제 1. DTO

```java
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

public record EmpUpdateRequest(
        @NotBlank String empName, @NotBlank @Email String email,
        @NotNull Long deptId, @NotNull @PositiveOrZero Integer salary,
        @NotNull @PastOrPresent LocalDate hireDate) {
    public EmpForm toForm() { /* 동일 */ }
}

@JsonInclude(JsonInclude.Include.NON_NULL)
public record EmpResponse(Long empId, String empName, String email,
                          String deptName,
                          @JsonFormat(pattern = "yyyy-MM-dd") LocalDate hireDate,
                          boolean active) {
    public static EmpResponse from(Emp e) {
        return new EmpResponse(e.getEmpId(), e.getEmpName(), e.getEmail(),
                e.getDeptName(),                    // 조회 SQL이 DEPT 조인해 채운 읽기 필드
                e.getHireDate(), e.isActive());
    }
}
```

---

## 문제 2. CRUD API 컨트롤러

```java
@RestController
@RequestMapping("/api/emps")
@RequiredArgsConstructor
public class EmpApiController {

    private final EmpService empService;

    @GetMapping
    public PageResult<EmpResponse> list(EmpSearchCond cond) {
        PageResult<Emp> page = empService.search(cond);
        return new PageResult<>(page.content().stream().map(EmpResponse::from).toList(),
                page.totalElements(), page.page(), page.size());
    }

    @GetMapping("/{id}")
    public EmpResponse detail(@PathVariable Long id) { return EmpResponse.from(empService.getWithDept(id)); }

    @PostMapping
    public ResponseEntity<EmpResponse> create(@Valid @RequestBody EmpCreateRequest req) {
        Long id = empService.register(req.toForm());
        return ResponseEntity.created(URI.create("/api/emps/" + id))
                .body(EmpResponse.from(empService.getWithDept(id)));
    }

    @PutMapping("/{id}")
    public EmpResponse update(@PathVariable Long id, @Valid @RequestBody EmpUpdateRequest req) {
        empService.modify(id, req.toForm());
        return EmpResponse.from(empService.getWithDept(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        empService.remove(id);
        return ResponseEntity.noContent().build();
    }
}
```

호출 결과(예):
```
GET  /api/emps/205        → 200  {"empId":205,"empName":"박지민",...}
GET  /api/emps/999        → 404  {"code":"NOT_FOUND","message":"사원 없음: 999"}
POST /api/emps            → 201  Location: /api/emps/222   body: {...}
PUT  /api/emps/222        → 200  {...}
DELETE /api/emps/222      → 204  (본문 없음)
```

---

## 문제 3. 표준 에러 응답

```java
public record ApiError(String code, String message, Map<String, String> fieldErrors) {
    public static ApiError of(String c, String m) { return new ApiError(c, m, null); }
}

@Slf4j
@RestControllerAdvice(basePackages = "com.example.hr.controller")
public class ApiExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError validation(MethodArgumentNotValidException e) {
        Map<String, String> fe = new LinkedHashMap<>();
        e.getBindingResult().getFieldErrors().forEach(x -> fe.putIfAbsent(x.getField(), x.getDefaultMessage()));
        return new ApiError("VALIDATION_ERROR", "입력값을 확인하세요", fe);
    }

    @ExceptionHandler(NoSuchElementException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError notFound(NoSuchElementException e) { return ApiError.of("NOT_FOUND", e.getMessage()); }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError etc(Exception e) { log.error("API 예외", e); return ApiError.of("INTERNAL_ERROR", "서버 오류"); }
}
```

`POST /api/emps` 본문 `{}` →
```json
400
{"code":"VALIDATION_ERROR","message":"입력값을 확인하세요",
 "fieldErrors":{"empName":"...","email":"...","deptId":"...","salary":"...","hireDate":"..."}}
```

---

## 문제 4. MockMvc 테스트

```java
@WebMvcTest(EmpApiController.class)
class EmpApiControllerTest {
    @Autowired MockMvc mvc;
    @MockitoBean EmpService empService;      // Boot 4: @MockBean → @MockitoBean

    @Test void 단건_200() throws Exception {
        given(empService.getWithDept(205L)).willReturn(
            Emp.builder().empId(205L).empName("박지민").email("p@x.com")
               .hireDate(LocalDate.of(2015,5,20)).active(true).build());
        mvc.perform(get("/api/emps/205"))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.empName").value("박지민"));
    }

    @Test void 등록_201() throws Exception {
        given(empService.register(any())).willReturn(222L);
        given(empService.getWithDept(222L)).willReturn(Emp.builder().empId(222L).empName("신입").build());
        mvc.perform(post("/api/emps").contentType(MediaType.APPLICATION_JSON).content("""
            {"empName":"신입","email":"a@b.com","deptId":5,"salary":2500000,"hireDate":"2026-09-01"}"""))
           .andExpect(status().isCreated())
           .andExpect(header().string("Location", "/api/emps/222"));
    }

    @Test void 이름누락_400() throws Exception {
        mvc.perform(post("/api/emps").contentType(MediaType.APPLICATION_JSON).content("""
            {"email":"a@b.com","deptId":5,"salary":100,"hireDate":"2026-01-01"}"""))
           .andExpect(status().isBadRequest())
           .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
           .andExpect(jsonPath("$.fieldErrors.empName").exists());
    }

    @Test void 없는사원_404() throws Exception {
        given(empService.getWithDept(999L)).willThrow(new NoSuchElementException("사원 없음: 999"));
        mvc.perform(get("/api/emps/999"))
           .andExpect(status().isNotFound())
           .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }
}
```
> `@WebMvcTest` 는 `@RestControllerAdvice` 도 로딩하므로 에러 JSON 검증이 가능. `ApiExceptionHandler`
> 가 다른 패키지면 `@Import(ApiExceptionHandler.class)` 추가.

---

## 문제 5. Jackson

**방법 A — 전역 설정(`application.yml`)**
```yaml
spring:
  jackson:
    default-property-inclusion: non_null   # null 필드 생략
    # 날짜 → ISO-8601 문자열: Jackson 3(Boot 4) 기본이라 별도 설정 불필요
```

**방법 B — DTO 애노테이션**
```java
@JsonInclude(JsonInclude.Include.NON_NULL)
public record EmpResponse(..., @JsonFormat(pattern = "yyyy-MM-dd") LocalDate hireDate, ...) {}
```

**선택**: 프로젝트 전체 응답 규약(날짜 ISO, null 생략)은 **전역 설정(A)** 으로 통일하고,
특정 필드만 다른 형식이 필요할 때 애노테이션(B)로 국소 재정의. → 일관성 + 예외 처리 모두 가능.

---

## 문제 6. 이 API의 문제

- **(A) `public Emp create(Emp emp)`**
  - `@RequestBody` 가 없어 JSON 본문이 바인딩되지 않는다 → `emp` 필드가 전부 null(또는 폼 파라미터로 해석).
  - 도메인 `Emp` 를 요청으로 직접 받으면 클라이언트가 `empId`, `active` 등 서버 결정 필드를 임의로
    보낼 수 있다(Mass Assignment). → `@Valid @RequestBody EmpCreateRequest`.

- **(B) `return emp;` + 메서드 반환이 `Emp`**
  - 도메인 객체를 그대로 응답 → 내부 필드·`dept` 순환참조·(있다면) 민감정보 노출. → `EmpResponse.from(...)`.
  - 생성인데 상태 코드가 200. → `ResponseEntity.created(uri).body(...)` 로 201 + `Location`.

고친 형태는 문제 2의 `create()` 와 동일.
