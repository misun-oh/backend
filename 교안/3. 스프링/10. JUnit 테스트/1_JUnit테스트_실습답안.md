# Day 10. JUnit 테스트 — 실습 답안

---

## 문제 1. 순수 단위 테스트

```java
class NormalizeTest {

    static int normalizeSalary(Integer s) {
        return (s == null || s < 0) ? 0 : s;
    }

    @Test void null이면_0() {
        assertThat(normalizeSalary(null)).isZero();
    }
    @Test void 음수면_0() {
        assertThat(normalizeSalary(-1)).isZero();
    }
    @Test void 정상값은_그대로() {
        assertThat(normalizeSalary(2_500_000)).isEqualTo(2_500_000);
    }
}
```

---

## 문제 2. `@SpringBootTest` + Service

```java
@SpringBootTest
class EmpServiceTest {

    @Autowired EmpService empService;

    @Test void 전체_21명() {
        assertThat(empService.findAll()).hasSize(21);
    }

    @Test void 사번200은_곽상혁() {
        assertThat(empService.get(200L).getEmpName()).isEqualTo("곽상혁");
    }

    @Test void 없는_사번은_예외() {
        assertThatThrownBy(() -> empService.get(999L))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("사원 없음");
    }
}
```

---

## 문제 3. Mapper 테스트 + 자동 롤백

```java
@SpringBootTest
@Transactional
class EmpMapperTest {

    @Autowired EmpMapper empMapper;

    @Test void 전체_21건_이름포함() {
        List<Emp> list = empMapper.findAll();
        assertThat(list).hasSize(21);
        assertThat(list).extracting(Emp::getEmpName).contains("곽상혁", "박지민");
    }

    @Test void 없는_사번은_null() {
        assertThat(empMapper.findById(999L)).isNull();
    }

    @Test void insert하면_한명_늘고_키가_채워진다() {
        int before = empMapper.findAll().size();
        Emp emp = Emp.builder()
                .empName("테스트").email("t@ex.com").deptId(5L)
                .salary(2_500_000).hireDate(LocalDate.now()).active(true).build();
        empMapper.insert(emp);
        assertThat(empMapper.findAll()).hasSize(before + 1);
        assertThat(emp.getEmpId()).isNotNull();
    }
}
```

**질문 답**: `@Transactional` 이 각 테스트를 트랜잭션으로 감싸고 **끝나면 무조건 롤백**하기 때문.
`insert` 로 늘어난 행은 커밋되지 않고 사라져, 다음 실행 때도 `EMP` 는 21건 그대로다.
(그래서 실제 DB로 테스트하면서도 데이터가 오염되지 않는다.)

---

## 문제 4. `@BeforeEach` 격리

```java
class CartTest {
    Cart cart;

    @BeforeEach void 새_장바구니() { cart = new Cart(); }

    @Test void 하나_담으면_1() {
        cart.add("book");
        assertThat(cart.totalCount()).isEqualTo(1);
    }

    @Test void 두번_담으면_2() {
        cart.add("book");
        cart.add("pen");
        assertThat(cart.totalCount()).isEqualTo(2);
    }
}
```

`@BeforeEach` 덕분에 각 `@Test` 는 빈 `cart` 로 시작 → 두 테스트가 서로 간섭하지 않는다.
만약 `cart` 를 필드 초기화(`= new Cart()`)로 한 번만 만들면, 실행 순서에 따라 두 번째 테스트가
누적된 값을 보고 깨질 수 있다.

---

## 문제 5. 실패 읽기

```
org.opentest4j.AssertionFailedError:
Expected size: 20 but was: 21 in:
<[Emp(empId=200,...), ...]>
	at com.example.hr.EmpMapperTest.전체_21건_이름포함(EmpMapperTest.java:15)
```

1. expected(내가 쓴 기대값) = **20**, 실제(but was) = **21**
2. 파일 `EmpMapperTest.java`, 줄 **15** (그 줄의 `assertThat(list).hasSize(20)`)

→ 기대값이 틀렸으니 `hasSize(21)` 로 되돌린다.

---

## 문제 6. 이 "테스트"의 문제

1. **단언(assertion)이 없다.** `System.out.println` 은 사람이 눈으로 봐야 하고, `size` 가 5든 21이든
   테스트는 항상 **초록(성공)**으로 표시된다 → 회귀를 못 잡는다.
2. **격리·롤백이 없다.** `@Transactional` 이 없어 만약 insert 계열이면 DB가 오염되고,
   이름이 서술적이지 않아(`test`) 실패해도 무엇이 깨졌는지 모른다.

고침:
```java
@SpringBootTest
@Transactional
class EmpMapperReadTest {
    @Autowired EmpMapper empMapper;

    @Test
    void findAll은_21명을_반환하고_첫_사원은_곽상혁이다() {
        List<Emp> list = empMapper.findAll();
        assertThat(list).hasSize(21);
        assertThat(list.get(0).getEmpName()).isEqualTo("곽상혁");
    }
}
```
