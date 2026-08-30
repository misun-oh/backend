# Day 15. REST API — 실습

## 문제 1. 요청/응답 DTO

- `EmpCreateRequest` (record, 검증 애노테이션 포함, `toForm()`)
- `EmpUpdateRequest` (record)
- `EmpResponse` (record, `from(Emp)` 정적 팩터리 — 부서명 포함, 민감정보 제외)

## 문제 2. CRUD API 컨트롤러

`EmpApiController`(`@RestController`, `/api/emps`)를 만들어:

| 메서드 | 응답 |
|---|---|
| `GET /api/emps` | `PageResult<EmpResponse>` (200) |
| `GET /api/emps/{id}` | `EmpResponse` (200), 없으면 404 |
| `POST /api/emps` | 201 + `Location: /api/emps/{id}` + `EmpResponse` |
| `PUT /api/emps/{id}` | `EmpResponse` (200) |
| `DELETE /api/emps/{id}` | 204 |

`curl` 또는 IntelliJ HTTP Client 로 각각 호출해 상태 코드와 본문을 적으세요.

## 문제 3. 표준 에러 응답

`ApiExceptionHandler`(`@RestControllerAdvice`)를 만들어:
- `MethodArgumentNotValidException` → 400 `{code:"VALIDATION_ERROR", message, fieldErrors:{...}}`
- `NoSuchElementException` → 404 `{code:"NOT_FOUND", message}`
- `Exception` → 500 `{code:"INTERNAL_ERROR", message}` + `log.error`

`POST /api/emps` 에 빈 본문 `{}` 을 보내 400 에러 JSON 을 확인하세요.

## 문제 4. MockMvc 테스트

`@WebMvcTest(EmpApiController.class)` + `@MockitoBean EmpService` (Boot 4: `@MockBean` 삭제) 로:
- `GET /api/emps/205` → 200, `$.empName == "박지민"`
- `POST /api/emps` 정상 → 201, `Location` 헤더 확인
- `POST /api/emps` 이름 누락 → 400, `$.code == "VALIDATION_ERROR"`, `$.fieldErrors.empName` 존재
- `GET /api/emps/999` (`empService.get` 이 `NoSuchElementException`) → 404, `$.code == "NOT_FOUND"`

## 문제 5. Jackson 다루기

- `EmpResponse` 의 `hireDate` 가 `"2015-05-20"` 문자열로 나오게 하세요(타임스탬프 숫자 아님).
- `null` 인 `deptName` 은 응답에서 아예 빠지게 하세요.
- (설정 vs 애노테이션) 두 방법 중 하나를 적용하고, 어떤 걸 골랐는지와 이유를 쓰세요.

## 문제 6. (개념) 이 API의 문제

```java
@PostMapping("/api/emps")
public Emp create(Emp emp) {          // (A)
    empService.save(emp);
    return emp;                       // (B)
}
```
`(A)` 와 `(B)` 각각의 문제를 쓰고 고치세요. (힌트: `@RequestBody`, 응답 DTO, 상태 코드)
