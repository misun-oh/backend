# Day 4. IoC와 DI — 실습

## 문제 1. 첫 빈 만들고 주입받기

- `com.example.hr.service` 에 `GreetingService` 를 `@Service` 로 만들고,
  `String greet(String name)` → `"환영합니다, name님"` 을 반환.
- `HelloController` 가 `GreetingService` 를 **생성자 주입**받아 `GET /welcome?name=박지민` 에서 사용.
- Lombok `@RequiredArgsConstructor` 를 써서 생성자를 자동 생성하세요.

## 문제 2. `@Bean` 으로 라이브러리 객체 등록

`com.example.hr.config.AppConfig` 를 `@Configuration` 으로 만들고,
`java.time.Clock` 을 `@Bean` 으로 등록하세요(`Clock.systemDefaultZone()`).
`GreetingService` 가 이 `Clock` 을 주입받아, 인사말에 현재 시각(시:분)을 붙이세요.

**왜 `Clock` 을 빈으로?** 테스트에서 `Clock.fixed(...)` 로 바꿔 끼우면 시간에 의존하는 로직을
검증할 수 있습니다(Day 10 예고).

## 문제 3. 같은 타입 빈 2개 + 선택

```java
public interface Notifier { String send(String msg); }
```

- `EmailNotifier`, `SmsNotifier` 두 구현을 `@Component` 로 등록.
- `SmsNotifier` 를 기본(`@Primary`)으로.
- `AlertService` 가 **이메일 알림**을 명시적으로 쓰도록 `@Qualifier` 로 주입.
- `GET /notify?type=all` 요청 시 두 알림을 모두 호출하도록, `List<Notifier>` 를 주입받는 메서드도 만들기.

## 문제 4. 스코프와 상태 버그

`@Service` 인 `CounterService` 에 `private int count;` 필드와 `int increment()` 메서드를 두고,
`GET /count` 에서 호출하게 하세요. 브라우저에서 여러 번 새로고침하면 값이 계속 증가합니다.

**질문**: `CounterService` 는 어떤 스코프인가? 여러 사용자가 동시에 호출하면 왜 문제가 되는가?
이 카운터를 "요청마다 1"로 만들려면 어떻게 해야 하나?

## 문제 5. 생명주기 콜백

`@Component` 인 `StartupChecker` 에 `@PostConstruct` 로 `"의존성 주입 완료"`,
`@PreDestroy` 로 `"종료 정리"` 를 로그로 남기세요. 앱을 켜고 끄며 콘솔에서 순서를 확인하세요.

## 문제 6. (개념) 이 코드의 문제

```java
@RestController
public class OrderController {
    public String order() {
        EmpService s = new EmpService();   // ← 문제
        return s.someMethod();
    }
}
```

`new EmpService()` 로 만든 객체를 쓰면 무엇이 동작하지 않는가? 두 가지 이상 쓰세요.
(힌트: 그 객체는 빈이 아니다)
