# Day 10. JUnit 테스트

| 항목 | 내용 |
|---|---|
| 선수학습 | Day 9(EmpMapper, DB 연결), Java 기본(예외) |
| 이번 챕터 | JUnit이 무엇인가 → **왜 테스트를 자동화하나** → `@Test`·단언·`@BeforeEach`·given-when-then → `@SpringBootTest` → **Mapper를 실제 DB에 붙여 검증** → `@Transactional` 테스트 자동 롤백 |
| 권장 진행 | 1일 |
| 도구 | JUnit 6(Jupiter), AssertJ — `spring-boot-starter-test` 에 포함 (Spring Boot 4 기준) |

## 학습목표

- JUnit이 무엇이고, 손으로 확인하는 대신 **테스트 코드로 자동 검증**하는 이유를 4가지로 말할 수 있다.
- `@Test`, `Assertions`(AssertJ `assertThat`), `@BeforeEach`, given-when-then 구조를 쓸 수 있다.
- `@SpringBootTest` 로 스프링 컨테이너를 띄워 빈을 주입받아 테스트할 수 있다.
- Day 9의 `EmpMapper` 가 **진짜 DB에 붙어** SELECT/INSERT 되는지 테스트로 확인할 수 있다.
- `@Transactional` 을 테스트에 붙이면 **끝나고 자동 롤백**되어 DB가 더럽혀지지 않음을 안다.
- 단언 실패 메시지(expected/actual)와 스택트레이스를 읽을 수 있다.

---

## 1. JUnit이란

**JUnit** 은 자바의 표준 테스트 프레임워크입니다(Spring Boot 4 기준 **JUnit 6**, 모듈 이름 Jupiter).
"어떤 코드가 의도대로 동작하는지 **검증하는 코드**"를 짜고, IDE·빌드 도구가 그걸 자동으로 실행해
성공/실패를 알려줍니다.

> JUnit 5 → 6 은 **패키지(`org.junit.jupiter.api.*`)와 기본 애노테이션(`@Test` 등)이 같습니다.**
> 최소 자바가 17로 올라간 정도라, 이 교안 코드는 그대로 동작합니다. 버전은
> `spring-boot-starter-test` 가 관리하므로 `build.gradle` 에 **직접 적지 않습니다.**

- 테스트 코드는 `src/test/java` 아래에, 보통 `대상클래스명 + Test` 로.
- `./gradlew test` 또는 IDE의 ▶ 로 실행. 실패한 테스트만 빨갛게 표시.

---

## 2. 왜 테스트를 자동화하나

지금까지는 코드를 고치면 앱을 띄우고 브라우저로 눌러 확인했습니다. 규모가 커지면 이게 안 됩니다.

| 이유 | 설명 |
|---|---|
| **반복 확인 비용** | 기능이 30개면 하나 고칠 때마다 30개를 다 눌러 볼 수 없다. 테스트는 몇 초에 다 돈다 |
| **회귀 방지** | "A를 고쳤더니 B가 깨졌다"를 배포 전에 잡는다 |
| **리팩터링 안전망** | 구조를 바꿔도 테스트가 초록이면 동작이 유지됐다는 근거 |
| **문서** | `등록후_목록에_한명_늘어난다()` 같은 테스트는 "이 기능은 이렇게 동작한다"는 살아있는 명세 |

특히 이 과정에서는 **Mapper를 만들면 곧바로 테스트로 DB에 붙여 본다** — 화면을 만들기 전에
데이터 계층부터 확실히 검증합니다.

---

## 3. 첫 테스트 — 순수 단위 테스트

스프링 없이도 되는 로직부터.

```java
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class EmpFormTest {

    @Test
    void 급여가_null이면_0으로_등록된다() {
        // given (준비)
        EmpForm form = new EmpForm();
        form.setEmpName("신입");
        form.setSalary(null);

        // when (실행)
        int salary = (form.getSalary() == null) ? 0 : form.getSalary();

        // then (검증)
        assertThat(salary).isZero();
    }
}
```

- `@Test` : 이 메서드가 하나의 테스트.
- **given–when–then** : 준비 / 실행 / 검증 3단으로 나누면 읽기 쉽다.
- 테스트 이름은 한글로 "무엇을 하면 어떻게 된다" 로 쓰면 실패 시 바로 이해된다.

### 단언(assertion) — AssertJ

```java
assertThat(list).hasSize(21);
assertThat(emp.getEmpName()).isEqualTo("곽상혁");
assertThat(emp.getEmail()).isNotNull().contains("@");
assertThat(emp.isActive()).isTrue();
assertThat(names).contains("곽상혁", "박지민").doesNotContain("없는사람");
assertThatThrownBy(() -> empService.get(999L))
        .isInstanceOf(NoSuchElementException.class)
        .hasMessageContaining("사원 없음");
```

`assertThat(actual).isEqualTo(expected)` — **실제값을 먼저**, 기대값을 뒤에. 실패하면
`expected: "곽상혁" but was: "권진우"` 처럼 둘 다 보여 준다.

### `@BeforeEach` / `@AfterEach`

```java
class CalcTest {
    Calculator calc;

    @BeforeEach void setUp() { calc = new Calculator(); }   // 각 @Test 전마다 새로

    @Test void 더하기() { assertThat(calc.add(2, 3)).isEqualTo(5); }
    @Test void 빼기()   { assertThat(calc.sub(5, 2)).isEqualTo(3); }
}
```
각 `@Test` 는 **서로 독립**이어야 한다. `@BeforeEach` 로 매번 새 상태를 만든다.

---

## 4. `@SpringBootTest` — 컨테이너를 띄우는 테스트

빈(서비스·매퍼)을 주입받아 테스트하려면 스프링 컨테이너가 필요합니다.

```java
@SpringBootTest
class EmpServiceTest {

    @Autowired EmpService empService;

    @Test
    void 전체_조회하면_21명() {
        List<Emp> rows = empService.findAll();
        assertThat(rows).hasSize(21);
    }
}
```

- `@SpringBootTest` : `HrApplication` 을 기준으로 전체 컨텍스트를 로딩(실제 DB 연결 포함).
- 느리지만 "실제 조립된 상태"를 검증. 매 테스트가 아니라 클래스당 한 번 컨텍스트를 캐시해서 재사용.

---

## 5. Mapper를 실제 DB에 붙여 검증

Day 9에서 만든 `EmpMapper` 가 진짜로 동작하는지.

```java
@SpringBootTest
@Transactional                    // ★ 각 테스트 끝나면 자동 롤백
class EmpMapperTest {

    @Autowired EmpMapper empMapper;

    @Test
    void findAll_로_21명을_가져온다() {
        List<Emp> list = empMapper.findAll();
        assertThat(list).hasSize(21);
        assertThat(list).extracting(Emp::getEmpName).contains("곽상혁", "박지민");
    }

    @Test
    void findById_없는_사번이면_null() {
        assertThat(empMapper.findById(999L)).isNull();
    }

    @Test
    void insert_하면_한_명_늘어난다() {
        int before = empMapper.findAll().size();

        Emp emp = Emp.builder()
                .empName("테스트사원").email("t@ex.com").deptId(5L)
                .salary(2500000).hireDate(LocalDate.now()).active(true)
                .build();
        empMapper.insert(emp);            // Day 11에서 만들 insert

        assertThat(empMapper.findAll()).hasSize(before + 1);
        assertThat(emp.getEmpId()).isNotNull();   // 생성 키가 채워졌는지
    }
    // 이 테스트가 끝나면 @Transactional 이 롤백 → DB의 EMP 는 다시 21명
}
```

### `@Transactional` 이 테스트에서 하는 일

- 일반 코드에서 `@Transactional` = "정상 반환 시 커밋, 예외 시 롤백"(Day 13).
- **테스트 클래스/메서드에 붙이면** 스프링 테스트가 각 테스트를 트랜잭션으로 감싸고,
  **테스트가 끝나면 무조건 롤백**합니다. → `insert` 테스트를 100번 돌려도 DB의 `EMP` 는 그대로 21명.
- 덕분에 실제 DB로 테스트하면서도 데이터를 더럽히지 않습니다.

> 주의: 롤백되므로 `insert` 후 **다른 커넥션**(예: DBeaver)에서는 그 데이터가 안 보입니다. 정상입니다.

---

## 6. 실패 메시지 읽기

```
org.opentest4j.AssertionFailedError:
expected: 21
 but was: 20
	at com.example.hr.EmpMapperTest.findAll_로_21명을_가져온다(EmpMapperTest.java:18)
```

- `expected` / `but was` : 내가 기대한 값 / 실제 나온 값.
- `at ...Test.java:18` : 실패한 단언의 위치. 여기부터 본다.
- 그 아래 `Caused by:` 가 있으면 진짜 원인(예: `BindingException`, `SQLSyntaxError`).

---

## 자주 하는 실수

- **테스트끼리 순서·데이터를 공유** → 하나 실패하면 줄줄이 실패. `@BeforeEach` 로 격리, `@Transactional` 로 롤백.
- **`@Transactional` 없이 `insert` 테스트** → DB에 쓰레기 데이터가 쌓임. 다음 실행부터 개수 단언이 깨짐.
- **`assertThat(expected).isEqualTo(actual)` 순서 뒤바꿈** → 메시지의 expected/actual이 헷갈리게 나옴.
- **`System.out.println` 으로 확인하고 단언 안 함** → 그건 테스트가 아니라 로그. 반드시 `assertThat`.
- **한 테스트에서 여러 개를 검증하고 이름은 `test1`** → 실패해도 뭐가 문젠지 모른다. 한 테스트 = 한 관심사, 이름은 서술형.
- **`@SpringBootTest` 를 모든 테스트에** → 느리다. 순수 로직은 스프링 없이, 슬라이스(`@MybatisTest`,`@WebMvcTest`)를 활용(심화).

---

## 핵심 요약

| 요소 | 내용 |
|---|---|
| JUnit 6 (Jupiter) | 자바 표준 테스트 프레임워크. `src/test/java`, `XxxTest`. 버전은 `starter-test` 가 관리 |
| `@Test` | 하나의 테스트. given-when-then 3단 |
| AssertJ `assertThat` | `.isEqualTo`, `.hasSize`, `.contains`, `assertThatThrownBy` … |
| `@BeforeEach` | 각 테스트 전 초기화(격리) |
| `@SpringBootTest` | 전체 컨텍스트 + 빈 주입 (실제 DB) |
| `@Transactional`(테스트) | 각 테스트 후 **자동 롤백** → DB 안 더럽힘 |
| 실패 메시지 | `expected`/`but was` + `at ...:줄번호` 부터 읽기 |

> 다음(Day 11): 이제 안심하고 `insert`/`update`/`delete` 와 조인 매핑을 만든다 — 테스트로 검증하며.
