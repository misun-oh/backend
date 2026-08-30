# Day 6. AOP — 실습

`build.gradle` 에 `spring-boot-starter-aop` 를 추가한 뒤 진행합니다.

## 문제 1. 서비스 실행 시간 로깅 애스펙트

교안 5절의 `LoggingAspect` 를 `com.example.hr.common` 에 만드세요. 그리고
`GET /api/emps`, `POST /api/emps` 를 호출한 뒤 콘솔 로그에서 `▶`, `◀` 줄을 찾아 붙여넣으세요.
(로그 레벨: `com.example.hr: DEBUG`)

## 문제 2. 컨트롤러도 포함

포인트컷을 확장해 `controller` 패키지의 메서드도 시간 측정 대상에 포함하세요.
`service()` 와 `controller()` 두 `@Pointcut` 을 만들고 `@Around("service() || controller()")` 로 묶으세요.

**확인할 것**: `GET /api/emps` 한 번에 `▶/◀` 가 몇 쌍 찍히는가? 순서(컨트롤러가 먼저인지 서비스가 먼저인지)는?

## 문제 3. 커스텀 애노테이션 `@Timed`

- `com.example.hr.common.Timed` 애노테이션(`@Retention(RUNTIME)`, `@Target(METHOD)`)을 만들고,
  `int warnMs() default 100;` 속성을 추가.
- `@annotation(timed)` 포인트컷으로 `@Timed` 붙은 메서드만 시간 측정.
- 실행 시간이 `warnMs` 를 넘으면 `log.warn`, 아니면 `log.debug`.
- `EmpServiceImpl.findAll()` 에 `@Timed(warnMs = 1)` 을 붙여 확인.

## 문제 4. 내부 호출 한계 재현

`EmpServiceImpl` 에 다음을 추가하고 실행해 보세요.

```java
@Timed
public void outer() { inner(); }

@Timed
public void inner() { }
```

`outer()` 를 호출하는 임시 엔드포인트(`GET /aop/test`)를 만들어 호출하세요.

**질문**: `@Timed` 로그가 `outer`, `inner` 중 어느 것에 대해 찍히는가? 왜인가? 두 문장으로 설명.

## 문제 5. (개념) 이 애스펙트의 버그

```java
@Around("execution(* com.example.hr..*(..))")
public Object bad(ProceedingJoinPoint pjp) throws Throwable {
    log.info("call {}", pjp.getSignature());
    // proceed() 호출을 깜빡함
    return null;
}
```

이 애스펙트를 적용하면 애플리케이션에 무슨 일이 생기는가? 두 가지 문제를 쓰세요.
(하나는 포인트컷, 하나는 `proceed()`)
