# Day 4. IoC와 DI — 실습 답안

---

## 문제 1. 첫 빈 만들고 주입받기

```java
// service/GreetingService.java
@Service
@RequiredArgsConstructor
public class GreetingService {
    public String greet(String name) {
        return "환영합니다, " + name + "님";
    }
}

// controller/HelloController.java
@RestController
@RequiredArgsConstructor
public class HelloController {

    private final GreetingService greetingService;   // final → 생성자 주입 대상

    @GetMapping("/welcome")
    public String welcome(@RequestParam String name) {
        return greetingService.greet(name);
    }
}
```

`@RequiredArgsConstructor` 가 `final` 필드를 받는 생성자를 만들어 주고, 생성자가 하나이므로
`@Autowired` 없이 주입됩니다.

---

## 문제 2. `@Bean` 으로 라이브러리 객체 등록

```java
// config/AppConfig.java
@Configuration
public class AppConfig {
    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }
}

// service/GreetingService.java
@Service
@RequiredArgsConstructor
public class GreetingService {
    private final Clock clock;

    public String greet(String name) {
        LocalTime now = LocalTime.now(clock);
        return "환영합니다, %s님 (%02d:%02d)".formatted(name, now.getHour(), now.getMinute());
    }
}
```

`Clock` 은 우리가 만든 클래스가 아니라 `@Component` 를 못 붙이므로 `@Bean` 으로 등록합니다.
테스트에서는 `new GreetingService(Clock.fixed(instant, zone))` 로 시각을 고정할 수 있습니다.

---

## 문제 3. 같은 타입 빈 2개 + 선택

```java
public interface Notifier { String send(String msg); }

@Component
class EmailNotifier implements Notifier {
    public String send(String msg) { return "[email] " + msg; }
}

@Primary
@Component
class SmsNotifier implements Notifier {
    public String send(String msg) { return "[sms] " + msg; }
}

@Service
@RequiredArgsConstructor
public class AlertService {

    @Qualifier("emailNotifier")               // 이메일을 명시적으로 지정
    private final Notifier emailNotifier;
    // 참고: @RequiredArgsConstructor + 필드 @Qualifier 조합이 안 먹으면
    //       생성자 파라미터에 직접 @Qualifier 를 붙인다.

    private final List<Notifier> allNotifiers; // 타입이 같은 빈 전부

    public String alert(String msg)      { return emailNotifier.send(msg); }
    public List<String> alertAll(String msg) {
        return allNotifiers.stream().map(n -> n.send(msg)).toList();
    }
}
```

생성자 파라미터에 붙이는 형태(권장):
```java
public AlertService(@Qualifier("emailNotifier") Notifier emailNotifier,
                    List<Notifier> allNotifiers) { ... }
```

- 단일 `Notifier` 주입 시엔 `@Primary` 인 `SmsNotifier` 가 기본으로 들어간다.
- `@Qualifier("emailNotifier")` 로 이메일을 콕 집는다.
- `List<Notifier>` 는 두 빈을 모두 담는다.

---

## 문제 4. 스코프와 상태 버그

```java
@Service
public class CounterService {
    private int count;
    public int increment() { return ++count; }
}
```

**답**

- `CounterService` 는 **싱글턴**(스프링 기본). 인스턴스가 하나이므로 `count` 필드도 하나뿐.
- 여러 사용자가 동시에 `/count` 를 호출하면 같은 `count` 를 함께 증가시켜, 값이 사용자 수만큼 섞이고
  `++count`(읽기-증가-쓰기)는 원자적이지 않아 경쟁 조건까지 생긴다.
- "요청마다 1"이 목적이면 **필드에 상태를 두지 않는다.** 예를 들어 요청 파라미터나 지역변수로 계산하거나,
  진짜 누적이 필요하면 `AtomicInteger` + 그 의미(전역 카운트)를 인정하거나, `@Scope("request")` 로
  요청 스코프 빈으로 만든다.

---

## 문제 5. 생명주기 콜백

```java
@Slf4j
@Component
public class StartupChecker {
    @PostConstruct
    public void init() { log.info("의존성 주입 완료"); }

    @PreDestroy
    public void close() { log.info("종료 정리"); }
}
```

콘솔 순서: 기동 로그들 → `의존성 주입 완료` → (앱 사용) → 종료 시 `종료 정리` → JVM 종료.
`@PostConstruct` 는 생성자 실행 + 의존성 주입이 **끝난 뒤** 호출됩니다(생성자에서 주입된 값을 못 쓰는 경우 여기서).

---

## 문제 6. 이 코드의 문제

`new EmpService()` 로 만든 객체는 **스프링 빈이 아니므로**:

1. `EmpService` 안의 `@Autowired`/생성자 주입이 채워지지 않는다 → 내부 `empMapper` 등이 `null` → NPE.
2. `@Transactional`, `@Cacheable`, AOP 어드바이스가 **전혀 동작하지 않는다**(프록시가 안 씌워짐).
3. 컨테이너가 관리하지 않으므로 스코프·생명주기 콜백(`@PostConstruct` 등)도 무시된다.

→ `OrderController` 가 `EmpService` 를 **생성자 주입**받아 써야 한다.
