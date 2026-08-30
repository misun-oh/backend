# Day 15. REST API — 심화 (경험자용)

## 1. 리소스 설계 & URL

- 명사, 복수형: `/emps`, `/emps/{id}`, `/emps/{id}/attachments`.
- 동사는 HTTP 메서드로. 예외적 액션은 하위 리소스/`POST /emps/{id}:resign` (실용 절충).
- 필터·정렬·페이지는 쿼리스트링: `/emps?deptId=D5&sort=salary,desc&page=0&size=20`.
- 부분 수정은 `PATCH`(변경 필드만) vs `PUT`(전체 교체). PATCH는 "null = 안 바꿈" 규칙을 문서화.

## 2. 페이징 응답 규격

```json
{ "content": [...], "page": 0, "size": 20, "totalElements": 137, "totalPages": 7,
  "first": true, "last": false, "sort": "salary,desc" }
```
프론트가 재사용하도록 공통 `PageResponse<T>` 로 고정. 커서 페이징이면 `nextCursor` 필드.

## 3. 에러 응답 표준 — RFC 7807 ProblemDetail

Boot 3 내장:
```yaml
spring.mvc.problemdetails.enabled: true
```
```java
@ExceptionHandler(BusinessException.class)
ProblemDetail handle(BusinessException e) {
    ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, e.getMessage());
    pd.setType(URI.create("https://api.example.com/problems/" + e.getCode()));
    pd.setTitle("업무 규칙 위반");
    pd.setProperty("code", e.getCode());
    return pd;
}
```
`Content-Type: application/problem+json`. 외부 공개 API면 표준을 따르는 편이 클라이언트 친화적.

## 4. HATEOAS / 버저닝

- HATEOAS(`spring-boot-starter-hateoas`): 응답에 `_links`. 순수 REST지만 실무 채택률은 낮음.
- 버저닝: URL(`/api/v1/...`) 이 가장 단순·명확. 헤더(`Accept: application/vnd.hr.v2+json`)는 캐시·라우팅 복잡.
  Breaking change 시에만 올리고, 필드 추가는 무버전.

## 5. 직렬화 성능·안전

- `@JsonView` 로 목록/상세 응답 필드 차등.
- 순환참조(`Emp.dept.emps...`)는 응답 DTO로 끊는 게 정석. `@JsonManagedReference`/`@JsonBackReference` 는 임시방편.
- 큰 목록은 스트리밍(`ResponseBodyEmitter`, `StreamingResponseBody`) 또는 페이징 강제.
- 입력 크기 제한: `spring.mvc.async.request-timeout`, 본문 크기(리버스 프록시), `@Size` 검증.
- Mass Assignment: 요청 DTO에 허용 필드만 두면 자연히 방어(도메인 통짜 바인딩 금지).

## 6. 문서화

- **springdoc-openapi**(`springdoc-openapi-starter-webmvc-ui`): `/swagger-ui.html`, `/v3/api-docs`.
  컨트롤러·DTO에서 스키마 자동 생성. `@Operation`, `@Schema` 로 보강.
- 또는 REST Docs(테스트가 통과해야 문서 생성 → 문서-코드 불일치 방지, 작성 비용 큼).

## 7. CORS / 콘텐츠 협상

- SPA(다른 오리진)에서 호출하면 CORS 설정: `@CrossOrigin` 또는 `WebMvcConfigurer.addCorsMappings`
  또는 Security의 `cors()`. 자격 증명(`allowCredentials`) 시 `*` 불가.
- `produces = "application/json"`, `consumes` 명시로 협상 명확화.

## 8. 통합 테스트

> **Boot 4 변경**: `@SpringBootTest` + `TestRestTemplate` 에는 **`@AutoConfigureTestRestTemplate`** 를
> 명시하고, 테스트 의존성 `spring-boot-resttestclient`(+ 런타임 `spring-boot-restclient`)를 추가해야 합니다.
> (3.x는 자동이었음)

```java
@SpringBootTest(webEnvironment = RANDOM_PORT)
@AutoConfigureTestRestTemplate
class EmpApiIT {
    @Autowired TestRestTemplate rest;

    @Test void 등록_조회_삭제() {
        var req = new EmpCreateRequest("신입","a@b.com",5L,2500000, LocalDate.now());
        var created = rest.postForEntity("/api/emps", req, EmpResponse.class);
        assertThat(created.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Long id = created.getBody().empId();

        rest.delete("/api/emps/" + id);
        var after = rest.getForEntity("/api/emps/" + id, String.class);
        assertThat(after.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
```
`WebTestClient`(리액티브 클라이언트지만 MVC도 테스트 가능)도 표현력이 좋다.
