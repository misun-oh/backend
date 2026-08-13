# 부록 3. 수동 DI 컨테이너 만들기 - 실습 답안

---

## 기본

### 문제 1. @주입 애노테이션 정의하기

```java
// 주입.java
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface 주입 {
}
```

**설명**: 부록1에서 배운 것과 동일한 방식으로 애노테이션을 정의했습니다. `RUNTIME`으로 지정해야 나중에 리플렉션에서 이 표시를 읽을 수 있습니다.

**틀리기 쉬운 포인트**: `@Retention`을 빠뜨리면 기본값(`CLASS`)이 적용되어, 실행 중에는 이 애노테이션 정보가 사라져 있습니다. 반드시 명시적으로 `RUNTIME`을 지정해야 합니다.

---

### 문제 2. @주입이 붙은 필드 찾기

```java
// 학과.java
public class 학과 {
    private String 학과명;

    public 학과(String 학과명) {
        this.학과명 = 학과명;
    }

    public String get학과명() {
        return 학과명;
    }
}
```

```java
// 교수.java
public class 교수 {
    private String 교수명;

    @주입
    private 학과 소속학과;

    public 교수(String 교수명) {
        this.교수명 = 교수명;
    }

    public String get교수명() {
        return 교수명;
    }

    public 학과 get소속학과() {
        return 소속학과;
    }
}
```

```java
// Practice2.java
import java.lang.reflect.Field;

public class Practice2 {
    public static void main(String[] args) {
        Field[] fields = 교수.class.getDeclaredFields();

        for (Field field : fields) {
            if (field.isAnnotationPresent(주입.class)) {
                System.out.println("주입이 필요한 필드: " + field.getName());
            }
        }
    }
}
```

**설명**: `교수` 클래스는 `교수명`과 `소속학과` 두 필드를 가지지만, `@주입`이 붙은 것은 `소속학과`뿐이므로 그것만 출력됩니다. `교수` 클래스의 생성자에서 `소속학과`를 초기화하지 않은 것에 주목하세요 — 이 필드는 나중에 컨테이너가 채워줄 것이기 때문입니다.

**틀리기 쉬운 포인트**: `교수명`처럼 컨테이너가 자동으로 채워줄 필요가 없는 값(생성자로 직접 넘겨받는 값)에는 `@주입`을 붙이지 않습니다. `@주입`은 "이 필드는 컨테이너가 대신 찾아서 넣어줘야 한다"는 의미이므로, 이미 생성자에서 값을 받는 필드에는 필요 없습니다.

---

## 응용

### 문제 3. 컨테이너의 등록() 메서드만 구현하기

```java
// 컨테이너.java
import java.util.HashMap;
import java.util.Map;

public class 컨테이너 {
    private Map<Class<?>, Object> 저장소 = new HashMap<>();

    public void 등록(Object obj) {
        저장소.put(obj.getClass(), obj);
    }

    public Map<Class<?>, Object> get저장소() {
        return 저장소;
    }
}
```

```java
// Practice3.java
public class Practice3 {
    public static void main(String[] args) {
        컨테이너 컨테이너 = new 컨테이너();
        학과 컴공 = new 학과("컴퓨터공학과");

        컨테이너.등록(컴공);

        학과 조회결과 = (학과) 컨테이너.get저장소().get(학과.class); // Day12 다운캐스팅 복습
        System.out.println("등록된 학과명: " + 조회결과.get학과명());
    }
}
```

**설명**: `등록()`은 넘어온 객체의 실제 클래스(`obj.getClass()`)를 key로, 객체 자신을 value로 저장소(`Map`)에 저장합니다. `저장소.get(학과.class)`로 다시 꺼내면 `Object` 타입으로 반환되므로, `학과`의 메서드를 쓰려면 `(학과)`로 다운캐스팅해야 합니다(Day12 복습).

**틀리기 쉬운 포인트**: `Map.get(...)`의 반환 타입은 항상 `Object`이므로, 원래 타입의 메서드(`get학과명()`)를 바로 호출할 수 없습니다. 반드시 원래 타입으로 캐스팅한 뒤 사용해야 합니다.

---

### 문제 4. 컨테이너의 주입() 메서드까지 완성

```java
// 컨테이너.java
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class 컨테이너 {
    private Map<Class<?>, Object> 저장소 = new HashMap<>();

    public void 등록(Object obj) {
        저장소.put(obj.getClass(), obj);
    }

    public void 주입(Object target) throws Exception {
        Class<?> clazz = target.getClass();

        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(주입.class)) {
                Class<?> 필요한타입 = field.getType();
                Object 준비된객체 = 저장소.get(필요한타입);

                if (준비된객체 != null) {
                    field.setAccessible(true);
                    field.set(target, 준비된객체);
                }
            }
        }
    }
}
```

```java
// Practice4.java
public class Practice4 {
    public static void main(String[] args) throws Exception {
        컨테이너 컨테이너 = new 컨테이너();
        학과 컴공 = new 학과("컴퓨터공학과");

        컨테이너.등록(컴공);

        교수 교수1 = new 교수("김교수");
        컨테이너.주입(교수1); // 교수1의 @주입 필드(소속학과)를 컨테이너가 채워줌

        System.out.println("교수의 소속학과: " + 교수1.get소속학과().get학과명());
    }
}
```

**설명**: `주입()`은 `교수1`의 필드를 리플렉션으로 순회하다가 `소속학과` 필드에서 `@주입`을 발견합니다. 이 필드의 타입(`field.getType()`)이 `학과`이므로, 저장소에서 `학과.class`로 등록된 객체(`컴공`)를 찾아 `field.set(...)`으로 채워 넣습니다.

**틀리기 쉬운 포인트**: `컨테이너.등록(컴공)`을 `컨테이너.주입(교수1)`보다 먼저 호출해야 합니다. 등록되지 않은 타입은 저장소에 없으므로, 주입 시점에 `준비된객체`가 `null`이 되어 아무것도 채워지지 않습니다.

---

## 도전

### 문제 5. 여러 필드를 동시에 주입받는 학생 만들기

```java
// 학생.java
public class 학생 {
    private String 이름;

    @주입
    private 학과 소속학과;

    @주입
    private 교수 지도교수;

    public 학생(String 이름) {
        this.이름 = 이름;
    }

    public String get이름() {
        return 이름;
    }

    public 학과 get소속학과() {
        return 소속학과;
    }

    public 교수 get지도교수() {
        return 지도교수;
    }
}
```

```java
// Practice5.java
public class Practice5 {
    public static void main(String[] args) throws Exception {
        컨테이너 컨테이너 = new 컨테이너();

        학과 컴공 = new 학과("컴퓨터공학과");
        교수 김교수 = new 교수("김교수");

        컨테이너.등록(컴공);
        컨테이너.등록(김교수);

        학생 학생1 = new 학생("홍길동");
        컨테이너.주입(학생1); // 한 번의 호출로 소속학과, 지도교수 둘 다 채워짐

        System.out.println("학생의 소속학과: " + 학생1.get소속학과().get학과명());
        System.out.println("학생의 지도교수: " + 학생1.get지도교수().get교수명());
    }
}
```

**설명**: `주입()` 메서드 안의 `for` 반복문이 `학생1`의 모든 필드를 순회하기 때문에, `@주입`이 여러 개 붙어있어도 한 번의 `컨테이너.주입(학생1)` 호출로 전부 처리됩니다. `소속학과`는 저장소에서 `학과.class`를, `지도교수`는 `교수.class`를 찾아 각각 알맞게 채워집니다.

**틀리기 쉬운 포인트**: 두 필드를 각각 따로 주입해야 한다고 생각하기 쉬운데, `주입()` 메서드 자체가 "모든 `@주입` 필드"를 순회하도록 설계되어 있으므로 필드가 몇 개든 한 번의 호출로 충분합니다.

---

### 문제 6. 종합 - 등록되지 않은 타입은 주입되지 않음을 확인

```java
public class Practice6 {
    public static void main(String[] args) throws Exception {
        컨테이너 컨테이너 = new 컨테이너();

        학과 컴공 = new 학과("컴퓨터공학과");
        컨테이너.등록(컴공); // 교수는 등록하지 않음

        학생 학생1 = new 학생("홍길동");
        컨테이너.주입(학생1);

        System.out.println("학생의 소속학과: " + 학생1.get소속학과().get학과명());
        System.out.println("학생의 지도교수: " + 학생1.get지도교수()); // null 출력
    }
}
```

**설명**: `주입()` 메서드에서 `저장소.get(필요한타입)`이 `null`을 반환하면(`교수` 타입이 등록되지 않았으므로), `if (준비된객체 != null)` 조건에 걸려 `field.set(...)`이 아예 실행되지 않습니다. 그래서 `지도교수` 필드는 처음 상태 그대로(`null`)로 남습니다.

**틀리기 쉬운 포인트**: 컨테이너가 "필요한 걸 알아서 다 채워줄 것"이라고 무조건 믿기 쉬운데, 실제로는 **등록된 것만** 채울 수 있습니다. 이는 실제 스프링에서도 마찬가지로, 필요한 빈(Bean)이 등록되어 있지 않으면 `@Autowired` 필드가 채워지지 않고 오히려 `NoSuchBeanDefinitionException` 같은 명확한 에러를 던져줍니다(우리가 만든 미니 버전은 에러 없이 조용히 null로 남긴다는 점이 실제 스프링과의 차이입니다).
