# Day 10. JUnit 테스트 — 심화 (경험자용)

## 1. 테스트 피라미드 / 슬라이스

- **단위(많이)**: 스프링 없이 `new`. 빠르다.
- **슬라이스(적당히)**: 필요한 계층만 로딩.
  - `@WebMvcTest(EmpController.class)` : MVC 계층. 서비스는 `@MockitoBean` (MockMvc는 여전히 자동설정).
  - `@MybatisTest` : 매퍼 + DataSource. 기본은 **내장 DB로 교체** → 실 DB로 하려면
    `@AutoConfigureTestDatabase(replace = NONE)`.
  - `@DataJdbcTest`, `@JsonTest`, `@RestClientTest` …
- **통합(적게)**: `@SpringBootTest` 전체. 느리다. E2E 흐름.

## 2. 컨텍스트 캐싱

`@SpringBootTest` 는 설정이 같으면 컨텍스트를 재사용한다. `@MockitoBean`, `@TestPropertySource`,
`@ActiveProfiles` 등이 다르면 **새 컨텍스트**를 만들어 캐시가 늘어나고 느려진다 → 조합을 최소화,
공통 베이스 테스트 클래스로 통일.

## 3. Mockito

```java
@ExtendWith(MockitoExtension.class)
class EmpServiceUnitTest {
    @Mock EmpMapper empMapper;
    @InjectMocks EmpServiceImpl empService;

    @Test void 없는_사원을_get하면_예외() {
        given(empMapper.findById(999L)).willReturn(null);
        assertThatThrownBy(() -> empService.get(999L))
            .isInstanceOf(NoSuchElementException.class);
        then(empMapper).should().findById(999L);
    }
}
```
- `@Mock` 가짜, `@InjectMocks` 대상에 주입. 스프링 컨텍스트 없이 초고속.
- `given(...).willReturn(...)`, `willThrow(...)`, `verify`/`then().should()`.
- 상호작용(횟수·순서·인자 `ArgumentCaptor`) 검증에 강함.

## 4. `@MockitoBean` vs `@Mock`

- `@Mock` (Mockito): 스프링 무관. 단위 테스트.
- `@MockitoBean` (스프링): **컨텍스트의 그 빈을 목으로 교체**. `@WebMvcTest`/`@SpringBootTest` 에서 서비스
  대체용. 컨텍스트 캐시 키에 영향(느려질 수 있음).
  import `org.springframework.test.context.bean.override.mockito.MockitoBean` (스파이는 `@MockitoSpyBean`).
- **Spring Boot 4 에서 `@MockBean`/`@SpyBean` 은 삭제됐습니다.** 3.x 자료의 `@MockBean` 은 전부
  `@MockitoBean` 으로 바꿔 씁니다(3.4부터 있던 애노테이션이라 동작은 같습니다).

## 5. 테스트 데이터 관리

- `@Sql("/sql/emp-fixture.sql")` : 메서드 전에 SQL 실행. `@Sql(executionPhase = AFTER_TEST_METHOD)` 로 정리.
- Testcontainers: 진짜 MySQL 컨테이너를 테스트마다/클래스마다 띄움. CI에서 "내 PC에선 되는데" 방지.
  `@Testcontainers` + `@Container MySQLContainer` + `@DynamicPropertySource` 로 URL 주입.
- `@Transactional` 롤백은 편하지만 **커밋 시점 로직**(트리거, `AFTER_COMMIT` 리스너, 자동증가 갭)은 못 본다.
  그럴 땐 `@Rollback(false)` + 수동 정리 또는 Testcontainers.

## 6. `@ParameterizedTest`

```java
@ParameterizedTest
@CsvSource({ "0, 0", "-1, 0", "2500000, 2500000" })
void 급여_보정(Integer input, int expected) {
    assertThat(normalize(input)).isEqualTo(expected);
}
```
`@ValueSource`, `@EnumSource`, `@MethodSource` 로 다양한 입력을 한 테스트로.

## 7. 시간·랜덤 고정

- `Clock` 을 빈으로(Day 4) → 테스트에서 `Clock.fixed(...)`.
- `@MockitoBean Clock` 또는 생성자에 직접 주입.
- 랜덤은 시드 고정 가능한 `Random(seed)` 를 주입.

## 8. MockMvc / WebTestClient (Day 15 연계)

```java
@WebMvcTest(EmpController.class)
class EmpControllerTest {
    @Autowired MockMvc mvc;
    @MockitoBean EmpService empService;      // Boot 4: @MockBean → @MockitoBean

    @Test void 목록_JSON() throws Exception {
        given(empService.findAll()).willReturn(List.of(
            Emp.builder().empId(200L).empName("곽상혁").deptName("총무부").hireDate(LocalDate.now()).active(true).build()));
        mvc.perform(get("/api/emps"))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$[0].empName").value("곽상혁"));
    }
}
```

## 9. 커버리지 & CI

- JaCoCo 플러그인(`jacocoTestReport`). 커버리지는 **참고 지표**이지 목표가 아님(의미 없는 테스트로 올릴 수 있음).
- 빌드에 `test` 를 물려 PR마다 자동 실행. 느린 통합 테스트는 태그(`@Tag("slow")`)로 분리 실행.
