# Day 6. AOP — 심화 (경험자용)

## 1. JDK 동적 프록시 vs CGLIB

| | JDK 동적 프록시 | CGLIB |
|---|---|---|
| 방식 | 인터페이스 구현 프록시 생성 | 대상 클래스를 상속한 서브클래스 생성 |
| 조건 | 대상이 인터페이스를 구현해야 | 인터페이스 없어도 됨. `final` 클래스/메서드 불가 |
| 부트 기본 | `spring.aop.proxy-target-class=true` → **CGLIB 우선** | |

CGLIB이라서 생기는 것: 대상 클래스에 **기본 생성자**가 없어도 되지만(Objenesis), `final` 이면 프록시 불가.
`@Configuration` 클래스도 CGLIB 프록시(그래서 `@Bean` 간 호출이 싱글턴 보장).

## 2. 어드바이스 실행 순서

한 조인포인트에 애스펙트가 여러 개면 `@Order`(작을수록 바깥). `@Around` 는
`proceed()` **전 = 진입 순서**, `proceed()` **후 = 역순**. 트랜잭션 어드바이스의 기본 order 는
`Ordered.LOWEST_PRECEDENCE`(가장 안쪽) — 그래서 커스텀 애스펙트가 보통 트랜잭션 바깥에서 돈다.
트랜잭션 커밋 이후 시점을 잡고 싶으면 애스펙트 대신 `TransactionSynchronization` /
`@TransactionalEventListener(phase = AFTER_COMMIT)`.

## 3. 포인트컷 지시자 총정리

| 지시자 | 매칭 대상 |
|---|---|
| `execution(...)` | 메서드 실행 (가장 많이 씀) |
| `within(패키지..*)` | 특정 타입 내부의 조인포인트 |
| `@within(A)` | 클래스에 `@A` 가 붙은 타입의 메서드 |
| `@annotation(A)` | 메서드에 `@A` 가 붙은 것 |
| `args(..)` / `@args(A)` | 인자 타입 / 인자 타입에 애노테이션 |
| `this(T)` / `target(T)` | 프록시 / 실제 대상이 T 타입 |
| `bean(empService)` | 빈 이름 패턴 (스프링 전용) |

`@annotation(timed)` 처럼 파라미터로 바인딩하면 어드바이스에서 애노테이션 값을 읽을 수 있다:
```java
@Around("@annotation(timed)")
public Object around(ProceedingJoinPoint pjp, Timed timed) { int warnMs = timed.warnMs(); ... }
```

## 4. `@Around` 로 재시도 애스펙트

```java
@Retention(RUNTIME) @Target(METHOD)
public @interface Retryable { int max() default 3; Class<? extends Throwable> on() default Exception.class; }

@Aspect @Component @Slf4j
public class RetryAspect {
    @Around("@annotation(r)")
    public Object retry(ProceedingJoinPoint pjp, Retryable r) throws Throwable {
        int attempt = 0;
        while (true) {
            try { return pjp.proceed(); }
            catch (Throwable e) {
                if (!r.on().isInstance(e) || ++attempt >= r.max()) throw e;
                log.warn("{} 재시도 {}/{} : {}", pjp.getSignature().toShortString(), attempt, r.max(), e.toString());
                Thread.sleep(200L * attempt);
            }
        }
    }
}
```
운영에서는 이런 건 Resilience4j `@Retry`/`@CircuitBreaker` 를 쓰는 편(백오프·지터·메트릭 포함).

## 5. 프록시 없이 진짜 AOP — AspectJ 위빙

스프링 AOP의 한계(내부 호출·private·필드 접근)를 넘어야 하면 **컴파일/로드타임 위빙(AspectJ)**:

- `spring-aspects` + AspectJ 컴파일러(ajc) 또는 `-javaagent:aspectjweaver.jar`.
- `@EnableLoadTimeWeaving`, `aop.xml`.
- 바이트코드를 실제로 수정하므로 내부 호출도 걸린다. 단, 빌드·기동 복잡도 상승, 디버깅 난이도 ↑.
- 대부분의 웹 애플리케이션은 스프링 AOP로 충분. 위빙은 도메인 객체(빈이 아닌)에 어드바이스가 꼭
  필요한 특수 상황에서만.

## 6. 성능·주의

- `@Around` + `ProceedingJoinPoint` 는 리플렉션 호출 비용이 있음. 초당 수만 콜의 hot path 라면 포인트컷을
  좁히거나 마이크로미터 `@Timed`(actuator) 사용.
- 포인트컷을 `execution(* com.example..*(..))` 처럼 넓게 잡으면 프록시가 대량 생성되어 기동이 느려짐.
- 애스펙트 안에서 다시 대상 서비스 호출 시 **무한 재귀** 조심(포인트컷에서 애스펙트 패키지 제외).
- 반환값을 바꾸는 어드바이스는 팀에 명확히 문서화 — 디버깅 시 "코드엔 없는 동작"이 됨.

## 7. 테스트

```java
@SpringBootTest
class LoggingAspectTest {
    @Autowired EmpService empService;   // 프록시가 주입됨
    @Test void 실행시간_로그가_찍힌다() {
        // OutputCaptureExtension 또는 ListAppender 로 로그 캡처해서 검증
    }
}
```
애스펙트 단위 테스트는 `AspectJProxyFactory` 로 대상만 감싸서 검증할 수도 있다.
