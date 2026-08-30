# Day 10. JUnit 테스트 — 실습

## 문제 1. 순수 단위 테스트

`src/test/java/com/example/hr/` 에 `NormalizeTest` 를 만들고, "급여가 null이거나 음수면 0"으로
보정하는 로직을 given-when-then + `assertThat` 으로 검증하세요. 최소 3케이스(null, -1, 정상값).

## 문제 2. `@SpringBootTest` + Service

`EmpServiceTest` 를 만들어 `@Autowired EmpService` 를 주입받고:

- `findAll()` 결과가 21명인지
- `get(200L)` 의 이름이 `"곽상혁"` 인지
- `get(999L)` 이 `NoSuchElementException` 을 던지는지 (`assertThatThrownBy`)

## 문제 3. Mapper 테스트 + 자동 롤백

`EmpMapperTest` 를 `@SpringBootTest @Transactional` 로 만들고:

- `findAll()` 이 21건, `extracting(Emp::getEmpName)` 에 `"곽상혁"`, `"박지민"` 포함
- `findById(999L)` 이 `null`
- (Day 11 `insert` 가 있다면) `insert` 후 `findAll().size()` 가 `before + 1`, `emp.getEmpId()` 가 not null

테스트를 **2번 연속 실행**해서 두 번 다 통과하는지 확인하세요.
**질문**: 왜 두 번째 실행에서도 "21건" 단언이 깨지지 않나요?

## 문제 4. `@BeforeEach` 격리

`CartTest` 를 만들어 `Cart` 클래스(`add(item)`, `totalCount()`)를 테스트하세요.
`@BeforeEach` 로 매번 새 `Cart` 를 만들고, 두 개의 `@Test`(추가 후 개수 1 / 두 번 추가 후 2)가
서로 영향을 주지 않음을 확인하세요.

## 문제 5. 실패 읽기 연습

일부러 `assertThat(list).hasSize(20)` (틀린 기대값)으로 바꿔 실행하고, 콘솔에 나온
실패 메시지에서 다음을 찾아 적으세요.
1. expected 값과 실제(but was) 값
2. 실패한 파일명과 줄 번호

## 문제 6. (개념) 이 "테스트"의 문제

```java
@Test
void test() {
    List<Emp> list = empMapper.findAll();
    System.out.println("size = " + list.size());
    System.out.println(list.get(0).getEmpName());
}
```

이 코드가 "테스트"로서 가진 문제 2가지를 쓰세요. 어떻게 고쳐야 하나요?
