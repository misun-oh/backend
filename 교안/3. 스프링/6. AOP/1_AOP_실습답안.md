# Day 6. AOP — 실습 답안

---

## 문제 1. 서비스 실행 시간 로깅 애스펙트

교안 5절 `LoggingAspect` 그대로. `GET /api/emps` 호출 시:

```
▶ EmpServiceImpl.findAll(..) args=[]
◀ EmpServiceImpl.findAll(..) 2ms → [Emp(empId=201, empName=신입, ...)]
```

`POST /api/emps` 호출 시:
```
▶ EmpServiceImpl.register(..) args=[com.example.hr.dto.EmpForm@...]
◀ EmpServiceImpl.register(..) 1ms → 202
```

`EmpServiceImpl` 코드는 손대지 않았는데 로그가 붙는다 — 관심사 분리.

---

## 문제 2. 컨트롤러도 포함

```java
@Pointcut("execution(* com.example.hr.service..*(..))")
public void serviceLayer() {}

@Pointcut("execution(* com.example.hr.controller..*(..))")
public void controllerLayer() {}

@Around("serviceLayer() || controllerLayer()")
public Object logAndTime(ProceedingJoinPoint pjp) throws Throwable { ... }
```

**확인**: `GET /api/emps` 한 번에 `▶/◀` 가 **2쌍**.
순서는 바깥→안쪽으로:
```
▶ EmpController.list(..)
  ▶ EmpServiceImpl.findAll(..)
  ◀ EmpServiceImpl.findAll(..) 2ms
◀ EmpController.list(..) 4ms
```
컨트롤러가 서비스를 호출하므로 컨트롤러의 `@Around` 가 먼저 진입하고 나중에 빠져나온다.

---

## 문제 3. 커스텀 애노테이션 `@Timed`

```java
// common/Timed.java
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Timed {
    int warnMs() default 100;
}

// common/TimedAspect.java
@Slf4j
@Aspect
@Component
public class TimedAspect {

    @Around("@annotation(timed)")
    public Object measure(ProceedingJoinPoint pjp, Timed timed) throws Throwable {
        long start = System.nanoTime();
        try {
            return pjp.proceed();
        } finally {
            long ms = (System.nanoTime() - start) / 1_000_000;
            String sig = pjp.getSignature().toShortString();
            if (ms >= timed.warnMs()) log.warn("[느림] {} {}ms (기준 {}ms)", sig, ms, timed.warnMs());
            else log.debug("[시간] {} {}ms", sig, ms);
        }
    }
}
```

```java
@Timed(warnMs = 1)
@Override
public List<Emp> findAll() { ... }
```

`GET /api/emps` → `findAll` 이 1ms 이상 걸리면 `[느림] EmpServiceImpl.findAll(..) 2ms (기준 1ms)`.

---

## 문제 4. 내부 호출 한계

```java
@Timed public void outer() { inner(); }
@Timed public void inner() { }
```

`GET /aop/test` → `outer()` 호출.

**답**: `@Timed` 로그는 **`outer` 에 대해서만** 찍힌다.
`outer()` 는 컨트롤러가 프록시(`EmpService` 프록시)를 통해 호출하므로 `@Timed` 어드바이스가 걸린다.
그런데 `outer()` 안에서 `inner()` 를 부르는 것은 `this.inner()` — 프록시를 거치지 않고 진짜 객체에서
바로 호출되므로 `inner()` 의 `@Timed` 는 무시된다. (스프링 AOP는 프록시 경유 호출에만 적용)

해결: `inner()` 를 별도 빈(`EmpInternalService` 등)으로 분리해 주입받아 호출.

---

## 문제 5. 이 애스펙트의 버그

1. **포인트컷이 너무 넓다** — `execution(* com.example.hr..*(..))` 는 컨트롤러·서비스뿐 아니라
   도메인 객체의 getter, 설정 클래스, 애스펙트 자기 자신까지 전부 걸린다. 프록시가 대량 생성돼
   기동이 느려지고 로그가 폭증하며, 애스펙트가 자기 자신을 다시 감싸 무한 재귀 위험도 있다.
2. **`proceed()` 를 호출하지 않는다** — 진짜 대상 메서드가 **아예 실행되지 않고** 항상 `null` 이 반환된다.
   `GET /api/emps` 는 빈 응답, `register` 는 아무 것도 저장하지 않고 `null` → NPE 연쇄.

고침: 포인트컷을 `com.example.hr.service..*` 등으로 좁히고, `@Around` 는 반드시
`Object result = pjp.proceed(); ... return result;`.
