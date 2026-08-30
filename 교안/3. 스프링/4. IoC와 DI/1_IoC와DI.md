# Day 4. IoC와 DI

| 항목 | 내용 |
|---|---|
| 선수학습 | `Java 3.에노테이션`의 **수동 DI 컨테이너**, Day 2(`@ComponentScan`) |
| 이번 챕터 | 제어의 역전(IoC) → 스프링 컨테이너 → 빈 등록 3가지(`@Component`·`@Bean`·`@Configuration`) → 컴포넌트 스캔 → **생성자 주입** → 빈 스코프·생명주기 |
| 권장 진행 | 1일 |

## 학습목표

- "제어의 역전(IoC)"이 무엇을 뒤집은 것인지, `new` 를 직접 하던 코드와 비교해 설명할 수 있다.
- `Java 3.에노테이션`에서 만든 컨테이너와 스프링 컨테이너를 대응시킬 수 있다.
- `@Component`(스캔) / `@Bean`(설정 메서드) / `@Configuration` 으로 빈을 등록할 수 있다.
- **생성자 주입**을 기본으로 쓰고, 필드 주입을 왜 피하는지 안다.
- 같은 타입 빈이 여러 개일 때 `@Qualifier` / `@Primary` 로 고를 수 있다.
- 빈 스코프(singleton/prototype)와 생성·소멸 콜백을 안다.

---

## 1. 제어의 역전(IoC) — 무엇이 뒤집혔나

`Java 3.에노테이션` 부록에서 이렇게 했습니다.

```java
학과 컴공 = new 학과("컴퓨터공학과");
교수 김교수 = new 교수("김교수");
학생 학생1 = new 학생("홍길동", 컴공, 김교수);   // 내가 직접 만들어 넣어줌
```

여기서 **"객체를 언제·어떻게 만들고 연결할지"의 제어권은 내 코드(main)** 에 있습니다.

**IoC**(Inversion of Control)는 이 제어권을 **컨테이너**에 넘기는 것입니다.

```java
@Service
public class EmpService {
    private final EmpMapper empMapper;
    public EmpService(EmpMapper empMapper) {   // "나는 EmpMapper 가 필요하다"고 선언만
        this.empMapper = empMapper;
    }
}
```

`EmpService` 는 `EmpMapper` 를 `new` 하지 않습니다. 컨테이너가 `EmpMapper` 구현체를 만들어
생성자로 **넣어줍니다(주입, Dependency Injection)**. 객체 생성·연결·소멸의 **제어권이 컨테이너로 역전**됐습니다.

| | 부록의 수동 컨테이너 | 스프링 |
|---|---|---|
| "이건 컨테이너가 관리해" 표시 | 커스텀 `@주입` | `@Component`/`@Service`/`@Repository`/`@Controller` |
| 클래스 뒤져서 대상 찾기 | 리플렉션으로 패키지 스캔 | `@ComponentScan` |
| 만든 객체 보관 | `Map<Class, Object>` | `ApplicationContext`(빈 저장소) |
| 필요한 것 꽂아주기 | 리플렉션으로 필드 set | 생성자 파라미터로 주입 |

스프링이 관리하는 객체를 **빈(bean)** 이라고 부릅니다.

---

## 2. 빈 등록 방법 3가지

### (1) `@Component` 계열 + 컴포넌트 스캔 — 우리가 만든 클래스

```java
@Service                 // = @Component + "서비스 계층" 의미 표시
public class EmpService { ... }

@Repository               // = @Component + "DB 접근" + 예외 변환
public class EmpRepositoryImpl { ... }

@Controller / @RestController   // = @Component + 웹 요청 처리
```

`@ComponentScan`(= `@SpringBootApplication` 에 포함) 이 `com.example.hr` 하위를 훑어
이 애노테이션이 붙은 클래스를 자동으로 빈으로 등록합니다. **우리가 직접 만드는 클래스는 대부분 이 방식.**

`@Component`, `@Service`, `@Repository`, `@Controller` 는 기능상 거의 같지만 **의미(계층)** 를 드러내려고
구분해서 씁니다. Day 1 클래스 다이어그램의 계층이 그대로 애노테이션이 됩니다.

### (2) `@Bean` 메서드 — 라이브러리 객체 등 내가 직접 만들어야 하는 것

```java
@Configuration
public class AppConfig {

    @Bean
    public ObjectMapper objectMapper() {          // 외부 라이브러리 클래스는 @Component 를 못 붙임
        return JsonMapper.builder()
                .addModule(new JavaTimeModule())
                .build();
    }

    @Bean
    public RestClient restClient() {
        return RestClient.builder().baseUrl("https://api.example.com").build();
    }
}
```

메서드 이름이 빈 이름이 되고, 반환 객체가 빈으로 등록됩니다.

### (3) `@Configuration` — `@Bean` 들을 담는 설정 클래스

`@Configuration` 안의 `@Bean` 메서드끼리 서로 호출해도 **매번 new 되지 않고 같은 싱글턴**이 반환됩니다
(스프링이 프록시로 보장). `@Configuration` 대신 일반 클래스에 `@Bean` 을 쓰면 이 보장이 없습니다.

---

## 3. 의존성 주입 — 생성자 주입을 쓴다

세 가지 방식이 있지만 **생성자 주입이 표준**입니다.

```java
@Service
public class EmpService {

    private final EmpMapper empMapper;
    private final DeptMapper deptMapper;

    // 생성자가 하나면 @Autowired 생략 가능
    public EmpService(EmpMapper empMapper, DeptMapper deptMapper) {
        this.empMapper = empMapper;
        this.deptMapper = deptMapper;
    }
}
```

Lombok `@RequiredArgsConstructor` 를 쓰면 `final` 필드로 생성자를 자동 생성:

```java
@Service
@RequiredArgsConstructor
public class EmpService {
    private final EmpMapper empMapper;
    private final DeptMapper deptMapper;
}
```

### 왜 필드 주입(`@Autowired` 필드)을 피하나

```java
@Autowired private EmpMapper empMapper;   // 피한다
```

| 생성자 주입 | 필드 주입 |
|---|---|
| `final` 가능 → 불변, 주입 누락을 컴파일에서 인지 | `final` 불가, `null` 상태가 생길 수 있음 |
| 의존성이 생성자 시그니처에 다 드러남 | 클래스를 다 읽어야 의존성을 앎 |
| 테스트에서 `new EmpService(mockMapper)` 로 스프링 없이 생성 가능 | 리플렉션/스프링 없이는 주입 불가 |
| 순환 참조를 기동 시 즉시 발견 | 순환 참조가 늦게 터짐 |

---

## 4. 같은 타입 빈이 여러 개일 때

```java
public interface PaymentGateway { ... }

@Component("kakao")  class KakaoPay implements PaymentGateway { ... }
@Component("toss")   class TossPay  implements PaymentGateway { ... }
```

이 상태로 `PaymentGateway` 를 주입하면 "어느 걸 넣어야 하지?" 오류(`NoUniqueBeanDefinitionException`).

- **`@Primary`** : 기본으로 쓸 하나를 지정.
  ```java
  @Primary @Component class TossPay implements PaymentGateway { ... }
  ```
- **`@Qualifier`** : 주입 지점에서 이름을 명시.
  ```java
  public OrderService(@Qualifier("kakao") PaymentGateway gateway) { ... }
  ```
- **여러 개 다 필요하면** `List<PaymentGateway>` 또는 `Map<String, PaymentGateway>` 로 전부 주입.

---

## 5. 빈 스코프와 생명주기

### 스코프

- **singleton** (기본): 컨테이너에 **딱 하나**. 대부분 이걸로 충분(상태 없는 서비스).
- **prototype**: 주입/조회할 때마다 새 인스턴스. 상태를 갖는 특수한 경우.
- 웹 스코프: `request`, `session` (요청/세션 단위).

```java
@Component
@Scope("prototype")
public class ReportBuilder { ... }
```

> 싱글턴 빈에 요청마다 달라지는 값을 필드로 저장하면 **동시성 버그**가 납니다. 서비스 빈은 상태를
> 갖지 않게 만들고, 요청별 데이터는 메서드 파라미터·지역변수로 다룹니다.

### 생성·소멸 콜백

```java
@Component
public class CacheWarmer {

    @PostConstruct           // 의존성 주입 완료 후 1회
    public void init() { log.info("캐시 예열"); }

    @PreDestroy              // 컨테이너 종료 직전
    public void close() { log.info("자원 반납"); }
}
```

`@Bean(initMethod = "...", destroyMethod = "...")` 로도 지정 가능.

---

## 자주 하는 실수

- **필드 주입 남용** → 생성자 주입 + `final` 을 기본으로.
- **`new EmpService()` 를 코드에서 직접 함** → 그 객체는 빈이 아니라서 그 안의 `@Autowired`·`@Transactional` 이 동작 안 함. 스프링이 주는 빈을 주입받아 쓴다.
- **`@Component` 를 붙였는데 스캔이 안 됨** → 클래스가 `com.example.hr` 바깥에 있음(Day 2 참고).
- **`@Configuration` 없이 `@Bean` 사용** → `@Bean` 메서드 간 호출이 싱글턴을 보장 안 함. 설정은 `@Configuration` 안에.
- **싱글턴 서비스에 가변 상태 필드** → 동시 요청이 서로의 값을 덮어씀.
- **같은 타입 빈 2개 + `@Qualifier`/`@Primary` 없음** → 기동 실패. 하나를 `@Primary` 로.

---

## 핵심 요약

| 개념 | 스프링에서 |
|---|---|
| IoC | 객체 생성·연결·소멸의 제어를 컨테이너가 가짐 |
| DI | 필요한 협력 객체를 컨테이너가 넣어줌(생성자로) |
| 빈 등록 | `@Component` 계열 + 스캔 / `@Bean` 메서드 / `@Configuration` |
| 주입 방식 | **생성자 주입**(+ `@RequiredArgsConstructor`). 필드 주입은 지양 |
| 선택 | `@Primary`(기본 하나) / `@Qualifier`(이름 지정) / `List`·`Map`(전부) |
| 스코프 | 기본 singleton. 상태는 갖지 않기 |
| 생명주기 | `@PostConstruct` / `@PreDestroy` |

> 다음(Day 5): 이 빈들을 Controller–Service–Repository 로 **어떻게 나누고 연결**하는가 — 계층형 아키텍처.
