# Day 8-2. Mock 테스트 — 실습 답안

---

## 문제 1. `JobServiceImplTest`

```java
@ExtendWith(MockitoExtension.class)
class JobServiceImplTest {

    @Mock
    JobMapper jobMapper;

    @InjectMocks
    JobServiceImpl jobService;

    @Test
    void 직급_목록_조회는_매퍼에_그대로_위임한다() {
        given(jobMapper.selectAll()).willReturn(List.of(
                new Job("J1", "대표"),
                new Job("J7", "사원")));

        List<Job> jobs = jobService.listAll();

        assertThat(jobs).extracting(Job::getJobName).containsExactly("대표", "사원");
        then(jobMapper).should().selectAll();
    }
}
```

**확인(왜 `@SpringBootTest`가 필요 없나)**: `JobServiceImpl.listAll()`은 매퍼 결과를 그대로 반환할 뿐
검증·계산 로직이 없습니다. 필요한 건 "매퍼를 한 번 불러서 그 값을 그대로 돌려주는지"뿐이고, 이건 가짜
`JobMapper`만으로 충분히 확인됩니다. 실제 DB·SQL이 맞는지는 이 테스트의 관심사가 아닙니다(그건
Day 9의 `JobMapperTest` 같은 통합 테스트의 몫).

---

## 문제 2. `EmpServiceImplTest` — 수정 테스트

```java
@Test
void 없는_사원을_수정하면_NotFoundException이고_update는_호출되지_않는다() {
    given(empMapper.selectById(999L)).willReturn(null);

    assertThatThrownBy(() -> empService.modify(999L, new EmpForm()))
            .isInstanceOf(NotFoundException.class);

    then(empMapper).should(never()).update(any(Emp.class));
}

@Test
void 있는_사원을_수정하면_매퍼_update가_empId와_함께_호출된다() {
    given(empMapper.selectById(205L)).willReturn(Emp.builder().empId(205L).build());

    EmpForm form = new EmpForm();
    form.setEmpName("박지민(수정)");
    form.setEmail("park_jm@company.com");
    form.setDeptId(5L);
    form.setJobCode("J3");
    form.setSalary(3600000);
    form.setHireDate(LocalDate.of(2015, 5, 20));

    empService.modify(205L, form);

    ArgumentCaptor<Emp> captor = ArgumentCaptor.forClass(Emp.class);
    then(empMapper).should().update(captor.capture());
    assertThat(captor.getValue().getEmpId()).isEqualTo(205L);
    assertThat(captor.getValue().getEmpName()).isEqualTo("박지민(수정)");
    assertThat(captor.getValue().getSalary()).isEqualTo(3600000);
}
```

**확인(왜 `never()`까지 확인해야 하나)**: `assertThatThrownBy`는 "예외가 났다"만 증명합니다. 만약
`EmpServiceImpl.modify()`가 실수로 **먼저 `update`를 호출한 뒤에** 예외를 던지도록 잘못 구현돼도
`assertThatThrownBy`는 여전히 통과합니다. "존재하지 않으면 **아예 쓰기 작업이 나가면 안 된다**"는
요구사항은 `then(empMapper).should(never()).update(...)`처럼 **상호작용 자체**를 확인해야 지켜졌는지
알 수 있습니다.

---

## 문제 3. `DeptControllerTest` — 부서 수정

```java
@Test
void 부서_수정_성공하면_목록으로_리다이렉트() throws Exception {
    mvc.perform(post("/depts/5")
                    .sessionAttr(SessionConst.LOGIN_MEMBER, LOGIN_MEMBER)
                    .param("deptName", "해외영업1부(개편)")
                    .param("location", "일본"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/depts"))
            .andExpect(flash().attributeExists("msg"));

    then(deptService).should().modify(eq(5L), any(DeptForm.class));
}
```

`.sessionAttr(SessionConst.LOGIN_MEMBER, LOGIN_MEMBER)`를 빠뜨리면 `LoginCheckInterceptor`가 요청을
가로채 `/login?redirectURL=/depts/5`로 302 리다이렉트되고, `redirectedUrl("/depts")` 단언이
"expected `/depts` but was `/login?...`" 로 실패합니다.

---

## 문제 4. 로그인하지 않은 경우

```java
@Test
void 로그인하지_않으면_로그인페이지로_리다이렉트된다() throws Exception {
    // 세션에 LOGIN_MEMBER를 넣지 않음 -> LoginCheckInterceptor가 컨트롤러 진입 자체를 막는다
    mvc.perform(get("/emps"))
            .andExpect(status().is3xxRedirection())
            .andExpect(header().string("Location", startsWith("/login")));
}
```

`redirectedUrlPattern("/login**")`로 먼저 시도하면 다음처럼 실패합니다.

```
Redirected URL '/login?redirectURL=/emps' does not match the expected URL pattern '/login**'
```

`AntPathMatcher`의 `**`는 **패턴에서 독립된 경로 세그먼트일 때만**(`/login/**`처럼) `/`를 건너뜁니다.
`login**`은 `login` 뒤에 `*`가 두 번 붙은 것과 같아서 여전히 **그 세그먼트 안에서만** 매칭되고,
`/emps`의 `/` 앞에서 멈춰 버립니다. 경로 패턴이 아니라 "이 문자열로 시작하는가"를 확인하고 싶을 땐
`redirectedUrlPattern` 대신 `header().string("Location", startsWith("..."))`처럼 **문자열 매처**를
쓰는 게 맞습니다.

**확인(원인을 어떻게 구분하나)**:
- **(a) 컨트롤러 버그**라면 로그인한 상태로 보낸 다른 테스트들(`사원_목록_조회` 등)도 함께 깨집니다.
- **(b) 인터셉터 설정 문제**라면 `WebConfig.addInterceptors()`의 `excludePathPatterns`에 실수로
  `/emps`가 들어가 있어 애초에 리다이렉트가 안 일어나고 `status().is3xxRedirection()`부터 실패합니다.
- **(c) 테스트 패턴 문법 실수**라면(이번 경우) 다른 테스트는 멀쩡한데 **이 테스트만** 실패하고,
  실패 메시지에 실제 값(`/login?redirectURL=/emps`)이 그대로 찍혀 있어 "패턴이 안 맞을 뿐 리다이렉트
  자체는 됐다"는 걸 바로 알 수 있습니다 — `expected`/`but was`를 먼저 읽는 습관(`1_JUnit테스트.md` 6절)이
  그대로 통합니다.
