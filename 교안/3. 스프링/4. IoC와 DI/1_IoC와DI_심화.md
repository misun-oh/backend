# Day 4. IoC와 DI — 심화 (경험자용)

## 1. `BeanFactory` vs `ApplicationContext`, 빈 등록 절차

- `BeanFactory` : 빈 조회의 최소 인터페이스(지연 생성).
- `ApplicationContext` : `BeanFactory` + 이벤트 발행, 메시지 소스, 환경(`Environment`), 리소스 로딩.
  부트가 만드는 건 이것(웹이면 `AnnotationConfigServletWebServerApplicationContext`).

기동 시 대략:
1. `BeanDefinition` 수집 (스캔 + `@Bean` + 자동 구성)
2. `BeanFactoryPostProcessor` 실행 — 정의 자체를 조작 (`ConfigurationClassPostProcessor`,
   `PropertySourcesPlaceholderConfigurer` 등)
3. `BeanPostProcessor` 등록 — 인스턴스 생성 전후 개입 (`AutowiredAnnotationBeanPostProcessor`,
   AOP 프록시 생성기 등)
4. 싱글턴 인스턴스화 → 의존성 주입 → `@PostConstruct` → `InitializingBean` → initMethod
5. `ApplicationRunner`/`CommandLineRunner`

## 2. 순환 참조

A가 B를, B가 A를 생성자 주입하면 부트 2.6+ 는 **기동 실패**시킵니다(`spring.main.allow-circular-references`
로 억지로 켤 수 있으나 냄새). 해결:

- 설계를 재검토 — 대개 한 클래스가 너무 많은 책임을 가짐. 공통 로직을 3번째 빈으로 추출.
- 정말 필요하면 한쪽을 `ObjectProvider<B>` 로 지연 조회하거나 `@Lazy`.
- 이벤트(`ApplicationEventPublisher`)로 결합을 끊기.

## 3. `@Qualifier` 를 애노테이션으로 감싸기

문자열 `@Qualifier("kakao")` 는 오타에 약합니다. 커스텀 한정자:

```java
@Qualifier @Retention(RUNTIME) @Target({FIELD, PARAMETER, TYPE})
public @interface Kakao {}

@Kakao @Component class KakaoPay implements PaymentGateway {}
// 주입
public OrderService(@Kakao PaymentGateway gw) {}
```

## 4. `@ConfigurationProperties` — 타입 안전한 설정 바인딩

`@Value` 를 여러 개 쓰는 대신:

```java
@ConfigurationProperties(prefix = "hr.file")
@Component            // 또는 @EnableConfigurationProperties(FileProps.class)
public record FileProps(String baseDir, long maxSize, List<String> allowedExt) {}
```
```yaml
hr:
  file:
    base-dir: /var/hr/upload
    max-size: 10485760
    allowed-ext: [png, jpg, pdf]
```

`spring-boot-configuration-processor` 를 추가하면 IDE 자동완성 + 메타데이터가 생깁니다.

## 5. `@Conditional` 로 조건부 빈

```java
@Bean
@ConditionalOnProperty(name = "hr.notify.enabled", havingValue = "true")
NotifyClient notifyClient() { ... }

@Bean
@ConditionalOnMissingBean
NotifyClient noopNotifyClient() { return new NoopNotifyClient(); }
```

자동 구성이 쓰는 것과 같은 메커니즘(Day 2 심화).

## 6. 프로토타입 빈을 싱글턴에서 매번 새로 받기

싱글턴에 프로토타입을 그냥 주입하면 **한 번만** 주입됩니다. 매 호출마다 새로 원하면:

```java
@Service
public class ReportService {
    private final ObjectProvider<ReportBuilder> builderProvider;
    public String make() {
        ReportBuilder b = builderProvider.getObject();   // 호출 때마다 새 인스턴스
        ...
    }
}
```

(또는 `@Lookup` 메서드 주입, `Provider<T>`.)

## 7. 테스트에서의 DI

- 순수 단위 테스트: 스프링 없이 `new EmpService(new FakeEmpMapper())` — 생성자 주입이라 가능.
- `@SpringBootTest` : 전체 컨텍스트. 느리지만 통합.
- 슬라이스: `@WebMvcTest`(컨트롤러 계층만), `@MybatisTest`(매퍼만) — 필요한 빈만 로딩.
- `@MockitoBean` : 컨텍스트의 특정 빈을 목으로 교체(Day 10, 15). (3.x의 `@MockBean` 은 Boot 4에서 삭제)

## 8. 빈 이름 규칙 / 충돌

- `@Component` 의 기본 빈 이름 = 클래스명 첫 글자 소문자(`empService`).
- `@Bean` 의 빈 이름 = 메서드명.
- 같은 이름 빈이 두 번 정의되면 나중 것이 덮거나(설정에 따라) 기동 실패.
  `spring.main.allow-bean-definition-overriding=false`(부트 기본)면 실패로 빨리 잡힌다.
