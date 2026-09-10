# Day 8-2. Mock 테스트 — 실습

`2_Mock테스트.md`에서 배운 `@Mock`/`@InjectMocks`(Service)와 `@WebMvcTest`/`@MockitoBean`(Controller)을
직접 써 봅니다. 정답은 `2_Mock테스트_실습답안.md`에 있고, 실제 코드는 `hr/src/test/java/com/example/hr/`에
그대로 있습니다.

## 문제 1. `JobServiceImplTest` 작성하기

`JobServiceImpl`은 `JobMapper` 하나에만 의존하는 가장 단순한 서비스입니다.

- `JobMapper`를 `@Mock`으로, `JobServiceImpl`을 `@InjectMocks`로 준비하세요.
- `jobMapper.selectAll()`이 `Job("J1","대표")`, `Job("J7","사원")` 두 건을 반환하도록 `given`을 설정하세요.
- `jobService.listAll()`을 호출한 결과가 그 두 건인지 `assertThat(...).extracting(Job::getJobName)`으로 확인하세요.
- `then(jobMapper).should().selectAll()`로 매퍼가 실제로 호출됐는지도 확인하세요.

**확인할 것**: 이 테스트에 `@SpringBootTest`가 필요 없는 이유를 한 줄로 설명해 보세요.

---

## 문제 2. `EmpServiceImplTest`에 "수정" 테스트 추가하기

`EmpServiceImpl.modify(Long, EmpForm)`은 먼저 `get(empId)`로 **존재를 확인**하고, 있으면
`empMapper.update(...)`를 호출합니다. 아래 두 가지를 각각 테스트로 작성하세요.

1. `empMapper.selectById(999L)`가 `null`을 반환하도록 하고, `empService.modify(999L, new EmpForm())`을
   호출하면 `NotFoundException`이 나는지 확인하세요. **그리고** 이때 `empMapper.update(...)`가
   **호출되지 않았는지**(`never()`)도 함께 확인하세요.
2. `empMapper.selectById(205L)`가 기존 사원을 반환하도록 하고, 이름·급여가 바뀐 `EmpForm`으로
   `empService.modify(205L, form)`을 호출한 뒤 `ArgumentCaptor<Emp>`로 `empMapper.update(...)`에
   **넘어간 인자**를 붙잡아, `empId`가 205로 세팅됐는지·이름이 바뀐 값인지 확인하세요.

**확인할 것**: 왜 "존재 확인 실패 시 update가 호출되지 않는다"까지 검증해야 하는지, `assertThatThrownBy`만으로는
부족한 이유를 한 줄로 설명해 보세요.

---

## 문제 3. `DeptControllerTest`에 "부서 수정" 테스트 추가하기

지금까지의 `DeptControllerTest`에는 부서 **추가**·**삭제** 테스트만 있고 **수정**(`POST /depts/{id}`)
테스트가 없습니다. 아래 시나리오를 `@WebMvcTest(DeptController.class)`로 작성하세요.

- `POST /depts/5`에 `deptName=해외영업1부(개편)`, `location=일본` 파라미터를 보냅니다.
- 응답이 `/depts`로 리다이렉트되고, `flash` 속성에 `msg`가 있는지 확인하세요.
- `then(deptService).should().modify(eq(5L), any(DeptForm.class))`로 서비스가 올바른 부서 번호로
  호출됐는지 확인하세요.

**주의**: `DeptController`는 `LoginCheckInterceptor`(`WebConfig`)의 보호를 받습니다 — 요청을 보낼 때
빠뜨리기 쉬운 게 있습니다. `2_Mock테스트.md` 5.3절을 다시 보세요.

---

## 문제 4. 로그인하지 않으면 어떻게 되는지 검증하기

`EmpControllerTest`의 다른 테스트들은 전부 로그인한 세션을 흉내 냈습니다(`.sessionAttr(...)`).
그렇다면 **세션 없이** 요청을 보내면 어떻게 될까요? R6("로그인한 담당자만")가 실제로 지켜지는지
아래를 검증하는 테스트를 작성하세요.

- `.sessionAttr(...)` **없이** `GET /emps`를 보냅니다.
- 응답이 3xx(리다이렉트)이고, 리다이렉트 주소가 `/login`으로 시작하는지 확인하세요.
  (힌트: 실제 리다이렉트 주소는 `/login?redirectURL=/emps`처럼 **쿼리스트링 안에 `/`가 또 섞여** 있습니다.
  `redirectedUrlPattern("/login**")`로 되겠지 싶겠지만, AntPathMatcher는 `**`가 **독립된 세그먼트**로
  올 때만 `/`를 건너뜁니다 — `login**`처럼 다른 글자에 붙어 있으면 `*`와 똑같이 그 세그먼트 안에서만
  매칭됩니다. 그래서 `/emps`의 `/`를 못 건너가 실패합니다. `header().string("Location", startsWith("/login"))`
  처럼 **문자열 접두어 매칭**을 쓰는 게 더 명확합니다.)

**확인할 것**: 이 테스트가 실패한다면, 그 원인이 (a) 컨트롤러 로직 버그인지 (b) 인터셉터 설정 문제인지
(c) 테스트의 패턴 문법 실수인지 — 세 가지를 어떻게 구분할지 한 줄로 적어 보세요.
