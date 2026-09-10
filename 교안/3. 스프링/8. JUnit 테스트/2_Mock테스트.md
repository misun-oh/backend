# Day 8-2. Mock 테스트 — 가짜 의존성으로 빠르게 검증하기

| 항목 | 내용 |
|---|---|
| 선수학습 | `1_JUnit테스트.md`(`@Test`, AssertJ, `@SpringBootTest`), 계층형 아키텍처(Controller-Service-Mapper) |
| 이번 챕터 | 왜 Mock인가 → Mockito로 Service 단위 테스트 → 예외 검증 → `@WebMvcTest`로 Controller 슬라이스 테스트 |
| 권장 진행 | 반나절 |
| 도구 | Mockito(`spring-boot-starter-webmvc-test`에 포함), MockMvc |
| 실제 코드 | `hr/src/test/java/com/example/hr/service/*`, `hr/src/test/java/com/example/hr/controller/*` |

## 학습목표

- `@SpringBootTest`(실제 DB 연결)와 **Mock 테스트**(가짜 의존성)의 차이와 각각의 쓰임을 설명할 수 있다.
- `@Mock`/`@InjectMocks`/`@ExtendWith(MockitoExtension.class)`로 **Service를 DB 없이** 검증할 수 있다.
- `given().willReturn()`/`willThrow()`/`then().should()`로 **상호작용**(호출 여부·인자·횟수)을 검증할 수 있다.
- `@WebMvcTest` + `@MockitoBean`으로 **Controller만** 띄워 요청·응답·리다이렉트·검증 실패를 확인할 수 있다.
- `@WebMvcTest`가 `WebMvcConfigurer`(인터셉터 등록)까지 함께 로딩한다는 **함정**을 알고 대응할 수 있다.

---

## 1. 왜 Mock인가

`1_JUnit테스트.md`의 `@SpringBootTest`는 **진짜 스프링 컨테이너 + 진짜 MySQL**에 붙습니다. 정확하지만:

- 컨테이너 기동 + DB 커넥션 때문에 테스트 하나에도 몇 초가 걸립니다. 서비스 로직 30개를 검증하려면 느려서 못 씁니다.
- **DB가 없으면 아예 실행이 안 됩니다.** 예를 들어 이 저장소를 내려받아 MySQL을 아직 설치하지 않은 사람도,
  Service의 "없는 사원을 조회하면 예외가 난다" 같은 **순수 로직**은 검증할 수 있어야 합니다.

해결책은 **Mapper를 가짜(mock)로 바꾸는 것**입니다. `EmpService`가 실제로 필요한 건 "`EmpMapper`라는
**약속(인터페이스)**"이지 "진짜 MySQL"이 아닙니다(설계 문서 5.3 "구현이 아니라 약속에 의존"과 같은 이유).
Mockito로 `EmpMapper`의 가짜 구현을 만들어 끼워 넣으면, DB 없이도 `EmpServiceImpl`의 분기·예외·계산 로직만
빠르게 검증할 수 있습니다.

| | `@SpringBootTest` (통합) | Mock 테스트 (단위) |
|---|---|---|
| 속도 | 느림(컨테이너+DB) | 빠름(밀리초) |
| 필요한 것 | 실행 중인 MySQL | 없음 |
| 검증 대상 | "진짜로 조립됐을 때" 전체 흐름 | 클래스 하나의 로직·분기 |
| 이 프로젝트 예 | (선택) `EmpMapperTest` | `EmpServiceImplTest`, `EmpControllerTest` |

---

## 2. Mockito로 Service 단위 테스트

`EmpServiceImpl`은 생성자로 `EmpMapper` 하나만 받습니다(`@RequiredArgsConstructor`). 이 자리에
**진짜 매퍼 대신 가짜 매퍼**를 넣습니다.

```java
@ExtendWith(MockitoExtension.class)
class EmpServiceImplTest {

    @Mock
    EmpMapper empMapper;

    @InjectMocks
    EmpServiceImpl empService;

    @Test
    void 없는_사원을_조회하면_NotFoundException() {
        given(empMapper.selectById(999L)).willReturn(null);

        assertThatThrownBy(() -> empService.get(999L))
                .isInstanceOf(NotFoundException.class);

        then(empMapper).should().selectById(999L);
    }
}
```

- `@ExtendWith(MockitoExtension.class)` : 이 테스트에서 `@Mock`/`@InjectMocks`를 쓰겠다고 JUnit에 알림.
- `@Mock EmpMapper empMapper` : `EmpMapper` **인터페이스의 가짜 구현체**. 아무것도 설정 안 하면 모든
  메서드가 `null`(또는 0, false)을 반환.
- `@InjectMocks EmpServiceImpl empService` : `EmpServiceImpl`을 만들면서, 생성자에 필요한 `@Mock`들을
  자동으로 채워 넣음. **스프링 컨테이너가 전혀 뜨지 않는다** — 그래서 빠르다.
- `given(mock.method(인자)).willReturn(값)` : "이 메서드가 이 인자로 불리면 이 값을 반환해라"는 **준비**.
- `then(mock).should().method(인자)` : "이 메서드가 이 인자로 **실제로 호출됐는지**" 검증(상호작용 검증).
  단순히 반환값만 보는 `assertThat`과 달리, "매퍼를 제대로 불렀는가"까지 확인합니다.

### 2.1 생성된 키 검증 — `ArgumentCaptor`

`EmpMapper.insert(Emp)`는 MyBatis의 `useGeneratedKeys="true"`로, **넘겨준 객체에 생성된 PK를 채워서
돌려줍니다**(9절 참고). Mock은 진짜 DB가 아니라 이 동작을 대신할 수 없으므로, `willAnswer`로 흉내 냅니다.

```java
@Test
void 등록하면_매퍼에_insert하고_생성된_id를_반환한다() {
    willAnswer(invocation -> {
        Emp emp = invocation.getArgument(0);
        emp.setEmpId(221L);      // MyBatis가 실제로 하는 일을 대신 흉내
        return null;
    }).given(empMapper).insert(any(Emp.class));

    EmpForm form = new EmpForm();
    form.setEmpName("신입사원");
    form.setEmail("new@company.com");
    form.setDeptId(5L);
    form.setJobCode("J7");
    form.setSalary(2500000);
    form.setHireDate(LocalDate.of(2026, 9, 1));

    Long empId = empService.register(form);

    assertThat(empId).isEqualTo(221L);

    ArgumentCaptor<Emp> captor = ArgumentCaptor.forClass(Emp.class);
    then(empMapper).should().insert(captor.capture());
    assertThat(captor.getValue().getEmpName()).isEqualTo("신입사원");
}
```

`ArgumentCaptor`는 "매퍼가 호출될 때 **실제로 넘어간 인자**"를 붙잡아 둡니다. `EmpServiceImpl.register()`가
`EmpForm`을 `Emp`로 올바르게 변환해서 넘겼는지(예: `deptId`가 제대로 옮겨졌는지)를 확인할 때 씁니다.

---

## 3. 예외 정책 검증 — `DeptServiceImplTest`

부서 삭제 정책(설계 실습답안 문제6, 정책①)처럼 **"조건에 따라 예외를 던지는" 로직**은 Mock으로
검증하기 아주 좋은 대상입니다. 실제 DB로 이런 걸 확인하려면 매번 "소속 사원이 있는 부서"를 미리
만들어 둬야 하지만, Mock은 그냥 원하는 값을 바로 돌려주면 됩니다.

```java
@ExtendWith(MockitoExtension.class)
class DeptServiceImplTest {

    @Mock DeptMapper deptMapper;
    @Mock EmpMapper empMapper;      // DeptServiceImpl은 매퍼 2개에 의존 -> @Mock도 2개
    @InjectMocks DeptServiceImpl deptService;

    @Test
    void 소속_사원이_있으면_삭제가_막힌다() {
        given(deptMapper.selectById(5L)).willReturn(Dept.builder().deptId(5L).deptName("해외영업1부").build());
        given(empMapper.countByDeptId(5L)).willReturn(5);

        assertThatThrownBy(() -> deptService.remove(5L))
                .isInstanceOf(DeptInUseException.class)
                .hasMessageContaining("5");

        then(deptMapper).should(never()).deleteById(anyLong());   // 삭제 SQL이 나가지 않았는지까지 확인
    }

    @Test
    void 소속_사원이_없으면_정상적으로_삭제된다() {
        given(deptMapper.selectById(3L)).willReturn(Dept.builder().deptId(3L).deptName("마케팅부").build());
        given(empMapper.countByDeptId(3L)).willReturn(0);

        deptService.remove(3L);

        then(deptMapper).should().deleteById(3L);
    }
}
```

`@InjectMocks` 대상이 의존성을 여러 개 갖고 있으면(`DeptServiceImpl`이 `DeptMapper`와 `EmpMapper`
둘 다 필요), **필요한 만큼 `@Mock`을 선언**하면 Mockito가 타입을 보고 알아서 채워 넣습니다.

`then(deptMapper).should(never()).deleteById(...)`처럼 **"호출되지 않았음"** 을 검증하는 것도
중요합니다 — 정책이 삭제를 막았다면, 진짜로 `DELETE` SQL을 만드는 메서드가 불려선 안 되기 때문입니다.

---

## 4. 의존성이 없으면 Mockito도 필요 없다 — `LoginServiceImplTest`

`LoginServiceImpl`은 (지금은) 어떤 매퍼도 쓰지 않는 순수 로직입니다. 이럴 땐 Mockito조차 필요 없이,
`1_JUnit테스트.md` 3절의 "순수 단위 테스트"처럼 그냥 `new` 해서 씁니다.

```java
class LoginServiceImplTest {

    LoginService loginService = new LoginServiceImpl();

    @Test
    void 아이디_비밀번호가_맞으면_로그인_정보를_반환한다() {
        LoginMember member = loginService.login("admin", "1234");

        assertThat(member).isNotNull();
        assertThat(member.getUsername()).isEqualTo("admin");
    }

    @Test
    void 비밀번호가_틀리면_null을_반환한다() {
        assertThat(loginService.login("admin", "wrong")).isNull();
    }
}
```

**판단 기준**: 의존성이 없으면 순수 단위 테스트, 의존성이 있으면 Mockito. `@ExtendWith(MockitoExtension.class)`를
습관적으로 모든 테스트에 붙이지 않습니다.

---

## 5. Controller 슬라이스 테스트 — `@WebMvcTest`

Service까지는 Mockito만으로 충분하지만, **"요청을 어떤 URL로 보내면 어떤 뷰·리다이렉트가 나오는가"**는
Controller가 관여해야 확인됩니다. 그렇다고 `@SpringBootTest`로 전체를 띄우면 다시 느려집니다 →
**슬라이스 테스트** `@WebMvcTest`를 씁니다.

```java
@WebMvcTest(EmpController.class)
class EmpControllerTest {

    @Autowired MockMvc mvc;

    @MockitoBean EmpService empService;   // Controller가 의존하는 서비스 3개를
    @MockitoBean DeptService deptService; // 모두 가짜 빈으로 교체
    @MockitoBean JobService jobService;

    @Test
    void 사원_목록_조회() throws Exception {
        Emp emp = Emp.builder().empId(205L).empName("박지민").deptName("해외영업1부")
                .jobName("부장").hireDate(LocalDate.of(2015, 5, 20)).active(true).build();
        given(empService.search(any(EmpSearchCond.class)))
                .willReturn(new PageResult<>(List.of(emp), 1, 1, 10));
        given(deptService.listAll()).willReturn(List.of(Dept.builder().deptId(5L).deptName("해외영업1부").build()));
        given(empService.countAll()).willReturn(21L);
        given(empService.countActive()).willReturn(20L);

        mvc.perform(get("/emps"))
                .andExpect(status().isOk())
                .andExpect(view().name("hr/index"))
                .andExpect(model().attributeExists("page", "depts", "cond"))
                .andExpect(content().string(containsString("박지민")));   // 실제 렌더링된 HTML까지 확인
    }
}
```

- `@WebMvcTest(EmpController.class)` : **이 컨트롤러 하나만** 빈으로 등록. `@Service`/`@Repository`는
  자동으로 빈 등록에서 빠집니다 → `EmpService` 등을 평소처럼 `@Autowired` 하면 "그런 빈 없음" 오류가 납니다.
- `@MockitoBean` : Mockito의 `@Mock`과 달리 **스프링 컨텍스트 안의 그 빈 자체를 가짜로 교체**합니다.
  Spring Boot 4에서 예전 `@MockBean`을 대체합니다(3.x 코드를 보면 `@MockBean` → 그대로 `@MockitoBean`으로
  바꿔 읽으면 됩니다).
- 뷰가 `hr/index` 처럼 **Thymeleaf 템플릿**이면, `@WebMvcTest`는 실제 `ThymeleafViewResolver`로
  **진짜로 렌더링**까지 합니다(뷰만 흉내 내는 게 아님). 그래서 `content().string(...)`으로 화면에 찍힌
  텍스트까지 검증할 수 있고 — 반대로 템플릿의 `th:text`/`th:each` 식이 잘못됐다면 **이 테스트가 잡아냅니다.**

### 5.1 예외 → 상태코드 검증 — `GlobalExceptionHandler`도 함께 뜬다

```java
@Test
void 없는_사원_상세조회는_404() throws Exception {
    given(empService.get(999L)).willThrow(new NotFoundException("사원을 찾을 수 없습니다."));

    mvc.perform(get("/emps/999"))
            .andExpect(status().isNotFound())
            .andExpect(view().name("error/404"));
}
```

`@WebMvcTest`는 `@Controller`뿐 아니라 **`@ControllerAdvice`도 자동으로 함께 로딩**합니다. 그래서
`GlobalExceptionHandler`가 `NotFoundException`을 404 + `error/404` 뷰로 바꾸는 것까지 별도 설정 없이
검증됩니다.

### 5.2 검증 실패 → 폼 재표시

```java
@Test
void 사원_등록_필수값_누락이면_폼을_다시_보여준다() throws Exception {
    given(deptService.listAll()).willReturn(List.of());
    given(jobService.listAll()).willReturn(List.of());
    given(empService.listActiveForDropdown()).willReturn(List.of());

    mvc.perform(post("/emps").param("empName", ""))
            .andExpect(status().isOk())
            .andExpect(view().name("hr/emp-form"));
}
```

`@NotBlank`/`@NotNull` 같은 Bean Validation도 `@WebMvcTest`에서 정상 동작합니다(`spring-boot-starter-validation`이
클래스패스에 있으면 자동 구성). `empName`만 보내고 나머지 필수값을 비웠으니 `BindingResult`에 오류가 쌓이고,
`EmpController.save()`가 리다이렉트 대신 **폼을 다시 그리는 분기**를 타는지 확인합니다.

### 5.3 함정 — `@WebMvcTest`도 `WebMvcConfigurer`는 로딩한다

`@WebMvcTest`는 "컨트롤러만 뜬다"고 알려져 있지만, 정확히는 **웹 계층과 관련된 것들**을 포함합니다.
여기엔 `@Controller`/`@ControllerAdvice`뿐 아니라 **`Filter`, `HandlerInterceptor`, `WebMvcConfigurer`
구현체도 포함**됩니다. 이 프로젝트의 `WebConfig`는 `WebMvcConfigurer`를 구현해서 `LoginCheckInterceptor`를
등록하므로, `@WebMvcTest(EmpController.class)`를 실행해도 **로그인 여부를 검사하는 인터셉터가 그대로 동작**합니다.

```
java.lang.AssertionError: Status expected:<200> but was:<302>
```

세션에 로그인 정보가 없으니 컨트롤러에 도달하기도 전에 `/login`으로 302 리다이렉트되어 테스트가 실패합니다
— **실제로 이 문서를 쓰면서 겪은 오류**입니다. 해결책은 요청을 보낼 때 로그인한 세션을 흉내 내는 것입니다.

```java
private static final LoginMember LOGIN_MEMBER = new LoginMember("admin", "인사담당자");

mvc.perform(get("/emps").sessionAttr(SessionConst.LOGIN_MEMBER, LOGIN_MEMBER))
        .andExpect(status().isOk());
```

`MockHttpServletRequestBuilder.sessionAttr(키, 값)`으로 세션에 `LoginCheckInterceptor`가 찾는 속성을
미리 넣어 두면, 인터셉터를 통과해 컨트롤러까지 정상 도달합니다. (인터셉터 자체를 우회하는 게 아니라
**"로그인한 사용자처럼" 요청을 보내는 것** — 그래서 R6 "로그인한 담당자만" 요구사항이 실제로 지켜지고
있다는 것도 간접적으로 검증하는 셈입니다.)

---

## 6. `Model`을 매개변수로 받는 메서드 테스트하기

`EmpController`의 `addFormSupport(Model model, Long excludeEmpId)`처럼, **`Model`을 직접 받아서
`addAttribute`로 채우는 메서드**는 어떻게 테스트할까요? `Model`은 스프링이 뷰에 값을 넘기는 통로일
뿐인 **단순한 키-값 저장소**라서, 방법이 두 가지입니다.

### 6.1 진짜 `Model` 구현체를 넣고 최종 상태를 확인 (상태 기반)

```java
EmpController controller = new EmpController(empService, deptService, jobService);
Model model = new ConcurrentModel();     // 스프링이 제공하는 가벼운 실제 구현체

controller.addFormSupport(model, 205L);  // 205번(자기 자신)은 관리자 후보에서 빠져야 한다

assertThat(model.getAttribute("depts")).isSameAs(depts);
assertThat((List<?>) model.getAttribute("managers"))
        .extracting("empId")
        .containsExactly(200L);
```

- `MockMvc` 없이 컨트롤러를 **그냥 `new`** 합니다 — 생성자에 이미 `@MockitoBean`으로 만들어 둔
  가짜 서비스들을 그대로 넘기면 됩니다.
- `org.springframework.ui.ConcurrentModel`은 스프링이 제공하는 **진짜** `Model` 구현체(내부는
  `Map` 하나)입니다. 목이 아니라 실제 객체이므로, `addAttribute`로 넣은 값을 `getAttribute`로
  그대로 꺼내 **최종 결과**를 검증합니다.
- 이 메서드가 `private`이면 테스트에서 호출할 수 없습니다. 테스트하려는 로직이 있다면
  `private` 대신 **패키지 전용(package-private)** 으로 낮춰서 같은 패키지의 테스트가 직접
  부를 수 있게 합니다(`EmpController.addFormSupport`가 이렇게 되어 있습니다).

### 6.2 `Model`을 목으로 만들고 호출 여부를 확인 (상호작용 기반)

```java
Model mockModel = mock(Model.class);   // @Mock 애노테이션 없이 Mockito.mock()으로 바로 생성

controller.addFormSupport(mockModel, null);

then(mockModel).should().addAttribute(eq("depts"), any());
then(mockModel).should().addAttribute(eq("jobs"), any());
then(mockModel).should().addAttribute(eq("managers"), any());
```

**`Model`도 목으로 만들 수 있습니다** — `@Mock`이 아니라 `Mockito.mock(Model.class)`을 바로 써도
되고(이 클래스처럼 `@ExtendWith(MockitoExtension.class)` 없이 이미 `@WebMvcTest`를 쓰는 중이어도
문제없습니다), `then(mock).should().addAttribute(...)`로 "어떤 키로 값을 넣었는지"를 검증합니다.

**어느 쪽을 쓸까?** `Model`은 `EmpMapper`처럼 "여러 SQL 중 무엇이 불렸는지"가 중요한 대상이 아니라,
그냥 값을 담는 상자입니다. 그래서 보통 **6.1(진짜 `Model` + 상태 검증)이 더 자연스럽고 읽기 쉽습니다**
— "무엇을 넣었는가"가 아니라 "결과가 무엇인가"를 확인하니까요. 6.2(목 + 상호작용 검증)는 "정확히
이 메서드가 호출됐는지" 자체가 중요할 때(예: 특정 조건에서 `addAttribute`가 **호출되지 않아야** 함을
`then(mock).should(never())...`로 확인할 때) 씁니다. 실제로 이 프로젝트의 컨트롤러 테스트들
(5절)도 `MockMvc`를 통해 간접적으로 `Model`의 최종 상태(`model().attributeExists(...)`)를
확인하는 6.1 방식을 따릅니다.

---

## 7. Mapper는 Mock이 아니라 통합 테스트로

지금까지는 **Service·Controller**를 검증하면서 `EmpMapper`/`DeptMapper`를 전부 가짜로 두었습니다.
그렇다면 **매퍼 자신**(SQL이 실제로 맞는지)은 누가 검증할까요? **Mock으로는 검증할 수 없습니다** —
Mock은 "이 메서드가 불리면 내가 준비한 값을 돌려줘"일 뿐, **진짜 SQL 문법이 맞는지, 조인이 맞는지,
집계가 맞는지는 전혀 확인하지 못합니다.** 이건 반드시 **진짜 MySQL**에 붙여야 합니다 —
`1_JUnit테스트.md` 5절의 `@SpringBootTest` + `@Transactional` 패턴 그대로입니다.

```java
@SpringBootTest
@Transactional              // 각 테스트 후 자동 롤백 -> data.sql로 채운 21명이 그대로 유지
@Tag("integration")         // MySQL이 필요한 테스트라는 표시
class EmpMapperTest {

    @Autowired EmpMapper empMapper;

    @Test
    void selectById_있으면_부서명_직급명까지_조인해서_채운다() {
        Emp emp = empMapper.selectById(205L);

        assertThat(emp.getDeptName()).isEqualTo("해외영업1부");   // JOIN DEPT 가 맞는지
        assertThat(emp.getJobName()).isEqualTo("부장");          // JOIN JOB 이 맞는지
    }
}
```

- `@Tag("integration")`을 붙이고, `build.gradle`에서 기본 `test` 태스크는 이 태그를 **제외**,
  별도 `integrationTest` 태스크만 **포함**하도록 나눕니다(`1_JUnit테스트_심화.md` 9절 "느린 통합
  테스트는 태그로 분리 실행"과 같은 이유).

  ```groovy
  tasks.named('test') {
      useJUnitPlatform { excludeTags 'integration' }
  }
  tasks.register('integrationTest', Test) {
      testClassesDirs = sourceSets.test.output.classesDirs
      classpath = sourceSets.test.runtimeClasspath
      useJUnitPlatform { includeTags 'integration' }
  }
  ```

- `./gradlew test`(평소·CI) : DB 없이 Mock 테스트만 — 항상 빠르고 항상 초록.
- `./gradlew integrationTest`(MySQL이 떠 있을 때) : `EmpMapperTest`/`DeptMapperTest`까지 — SQL 자체를 검증.

**정리하면 세 계층이 서로 다른 방식으로 검증됩니다.**

| 계층 | 검증 방법 | 확인하는 것 |
|---|---|---|
| Mapper | `@SpringBootTest` + 진짜 DB (`integrationTest`) | SQL·조인·집계가 실제로 맞는가 |
| Service | Mockito로 Mapper를 가짜로 | 분기·예외·변환 로직이 맞는가 |
| Controller | `@WebMvcTest` + `@MockitoBean`으로 Service를 가짜로 | 라우팅·뷰·검증 실패 분기가 맞는가 |

한 기능(예: "사원 상세 조회")을 셋 다 테스트하는 게 중복 같아 보이지만, **서로 다른 것을 확인**하고
있어서 중복이 아닙니다 — Mapper 테스트가 SQL 오타를 잡고, Service 테스트가 "없으면 예외" 같은
로직을 잡고, Controller 테스트가 "404로 응답하는가"를 잡습니다.

---

## 자주 하는 실수

- **모든 테스트에 `@SpringBootTest`** → 순수 로직/컨트롤러는 Mock으로 충분히 빠르게 검증됩니다. DB까지
  진짜로 붙어야 의미 있는 것(Day 9의 Mapper 테스트)만 통합 테스트로 남깁니다.
- **`@Mock`과 `@MockitoBean`을 혼동** → `@Mock`(순수 Mockito, 스프링 무관)은 `EmpServiceImplTest`처럼
  컨테이너 없는 테스트에, `@MockitoBean`(스프링)은 `@WebMvcTest`/`@SpringBootTest`처럼 **컨텍스트 안의 빈을
  통째로 바꿔치기**할 때. Spring Boot 4에서 `@MockBean`/`@SpyBean`은 삭제되었습니다 — 예전 자료를 참고할 때
  `@MockitoBean`으로 바꿔 읽으세요.
- **스텁만 해두고 검증은 안 함(`given`만 있고 `then().should()`/`assertThat` 없음)** → "설정"과 "검증"은
  다릅니다. 매퍼가 특정 인자로 호출됐는지까지 확인하려면 `then(mock).should()`가 필요합니다.
- **쓰지 않는 스텁(`given(...)`)을 남겨 둠** → Mockito의 엄격 스텁(strict stubs) 모드에서
  `UnnecessaryStubbingException`으로 테스트가 실패합니다. 그 테스트 케이스에서 실제로 호출되는 경로에
  필요한 스텁만 남깁니다.
- **`@WebMvcTest`에서 302가 나오는데 원인을 못 찾음** → 5.3절처럼 `WebMvcConfigurer`(인터셉터)가 함께
  로딩됐을 가능성을 먼저 의심합니다. `.sessionAttr(...)`로 로그인 세션을 흉내 내거나, 정말 인터셉터
  없이 컨트롤러만 보고 싶다면 `@WebMvcTest(controllers = ..., excludeFilters = ...)`로 명시적으로 제외합니다.
- **Thymeleaf 렌더링 실패를 컨트롤러 버그로 착각** → `@WebMvcTest`는 뷰를 진짜로 렌더링하므로, 모델에
  템플릿이 참조하는 속성(`cond`, `depts`, `jobs`, `managers` 등)을 빠짐없이 채워 줘야 합니다. 500 오류가
  나면 스택트레이스에서 Thymeleaf 예외(`TemplateProcessingException`)인지 스프링 바인딩 오류인지부터 구분합니다.

---

## 핵심 요약

| 요소 | 언제 | 이 프로젝트 예 |
|---|---|---|
| `@Mock` + `@InjectMocks` | Service를 매퍼 없이 검증 | `EmpServiceImplTest`, `DeptServiceImplTest`, `DashboardServiceImplTest` |
| 의존성 없는 순수 `new` | 매퍼·빈 의존성이 아예 없을 때 | `LoginServiceImplTest` |
| `given/willReturn/willThrow` | 가짜 메서드의 반환값·예외를 준비 | 소속 사원 있음 → `DeptInUseException` |
| `then(mock).should()` / `ArgumentCaptor` | 호출 여부·인자까지 검증 | `insert`에 넘어간 `Emp` 확인 |
| `@WebMvcTest` + `@MockitoBean` | Controller의 라우팅·뷰·리다이렉트·검증 실패 분기 | `EmpControllerTest`, `DeptControllerTest` |
| `WebMvcConfigurer`도 함께 로딩됨 | 인터셉터가 걸려 있으면 세션을 흉내 내야 함 | `.sessionAttr(SessionConst.LOGIN_MEMBER, ...)` |

> 다음: Mapper까지 진짜로 검증하려면 `1_JUnit테스트.md` 5절의 `@SpringBootTest` + `@Transactional` 롤백
> 패턴을 그대로 쓰면 됩니다 — 이 문서의 Mock 테스트와 상호 보완적입니다(Mock = 빠른 로직 검증,
> Mapper 통합 테스트 = SQL이 실제로 맞는지 검증).
