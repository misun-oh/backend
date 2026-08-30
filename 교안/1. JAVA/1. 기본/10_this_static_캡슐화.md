# Day 10. 클래스 - this, static, 캡슐화

> 참고 교재: 이것이 자바다 CHAPTER 6(클래스) 일부 범위
> 이전 챕터: Day 9에서 클래스, 객체, 생성자를 배웠습니다.

## 학습목표
- this 키워드의 역할을 이해하고 사용할 수 있다
- 접근제한자(private/public)와 캡슐화의 개념을 설명할 수 있다
- getter/setter 메서드를 작성해 필드에 안전하게 접근하도록 만들 수 있다
- static 멤버와 인스턴스 멤버의 차이를 구분할 수 있다
- final 필드로 상수를 선언할 수 있다

---

## 1. this 키워드

`this`는 "현재 만들어지고 있는(또는 실행 중인) 객체 자기 자신"을 가리키는 키워드입니다. 주로 매개변수 이름과 필드 이름이 같을 때, 이 둘을 구분하기 위해 사용합니다.

```java
public class 학생 {
    String 이름;
    int 나이;

    public 학생(String 이름, int 나이) {
        this.이름 = 이름; // this.이름 = 필드, 오른쪽 이름 = 매개변수
        this.나이 = 나이;
    }
}
```

**설명**: 생성자의 매개변수 이름을 필드와 똑같이 `이름`, `나이`로 지었기 때문에, 그냥 `이름 = 이름;`이라고 쓰면 자바는 매개변수를 자기 자신에게 대입하는 것으로 처리해버려서 필드에는 아무 값도 들어가지 않습니다. `this.이름`이라고 명시해야 "이 객체의 필드"라는 뜻이 되어 매개변수 값이 필드에 정확히 저장됩니다.

**만약 매개변수 이름이 다르다면 this가 필수는 아닙니다.**
```java
public 학생(String studentName, int studentAge) {
    이름 = studentName; // 이름이 겹치지 않으므로 this 없이도 필드에 정확히 대입됨
    나이 = studentAge;
}
```
하지만 매개변수 이름을 필드와 동일하게 짓고 `this`로 명확히 구분하는 방식이 실무에서 더 널리 쓰입니다.

---

## 2. 접근제한자와 캡슐화

**캡슐화(encapsulation)** 는 객체의 필드를 외부에서 함부로 바꾸지 못하도록 숨기고, 정해진 방법(메서드)을 통해서만 접근하도록 만드는 것입니다.

| 접근제한자 | 설명 |
|---|---|
| `private` | 같은 클래스 내부에서만 접근 가능 (외부에서 직접 접근 불가) |
| `public` | 어디서든 접근 가능 |

```java
public class 학생 {
    private String 이름; // private: 클래스 외부에서 직접 접근 불가
    private int 나이;

    public 학생(String 이름, int 나이) {
        this.이름 = 이름;
        this.나이 = 나이;
    }
}
```

```java
public class 캡슐화예제 {
    public static void main(String[] args) {
        학생 학생1 = new 학생("홍길동", 20);

        // 학생1.이름 = "김철수"; // 컴파일 에러! private 필드는 클래스 밖에서 직접 접근 불가
    }
}
```

**왜 필드를 숨길까?**: 필드를 `public`으로 열어두면 누구나 `학생1.나이 = -5;`처럼 말도 안 되는 값을 넣을 수 있습니다. `private`로 막아두고 검증 로직이 있는 메서드로만 값을 바꾸게 하면, 잘못된 값이 들어오는 것을 막을 수 있습니다.

---

## 3. Getter와 Setter 메서드

`private` 필드의 값을 클래스 외부에서 읽거나(get) 수정(set)할 수 있도록 만든 `public` 메서드입니다. (Day9에서 이미 get 메서드를 일부 사용했습니다)

| 메서드 종류 | 이름 규칙 | 역할 |
|---|---|---|
| Getter | `get필드명()` | private 필드의 값을 반환 |
| Setter | `set필드명(값)` | private 필드에 값을 대입 (검증 로직 추가 가능) |

```java
public class 학생 {
    private String 이름;
    private int 나이;

    public 학생(String 이름, int 나이) {
        this.이름 = 이름;
        this.나이 = 나이;
    }

    public String get이름() {
        return 이름;
    }

    public void set이름(String 이름) {
        this.이름 = 이름;
    }

    public int get나이() {
        return 나이;
    }

    public void set나이(int 나이) {
        if (나이 < 0) {
            System.out.println("나이는 0보다 작을 수 없습니다.");
            return; // 잘못된 값이면 저장하지 않고 메서드 종료
        }
        this.나이 = 나이;
    }
}
```

```java
public class GetSet예제 {
    public static void main(String[] args) {
        학생 학생1 = new 학생("홍길동", 20);

        System.out.println(학생1.get이름()); // 홍길동
        학생1.set이름("김철수");
        System.out.println(학생1.get이름()); // 김철수

        학생1.set나이(-5); // "나이는 0보다 작을 수 없습니다." 출력, 나이는 바뀌지 않음
        System.out.println(학생1.get나이()); // 20 (그대로 유지)
    }
}
```

**설명**: `set나이()`에 검증 로직을 넣어두면, 필드를 직접 `public`으로 열어두는 것보다 훨씬 안전하게 데이터를 관리할 수 있습니다. 이것이 캡슐화의 실질적인 이점입니다.

---

## 4. static 멤버와 인스턴스 멤버

지금까지 만든 필드는 객체마다 각자 따로 값을 가지는 **인스턴스 멤버**였습니다. `static`을 붙이면 객체마다 따로 있는 것이 아니라, **클래스 전체가 공유하는 단 하나의 값**이 됩니다.

```java
public class 학생 {
    private String 이름;
    static int 전체학생수 = 0; // static: 모든 학생 객체가 공유하는 값

    public 학생(String 이름) {
        this.이름 = 이름;
        전체학생수++; // 학생 객체가 하나 생성될 때마다 공유 값 증가
    }
}
```

```java
public class Static예제 {
    public static void main(String[] args) {
        학생 학생1 = new 학생("홍길동");
        학생 학생2 = new 학생("김철수");
        학생 학생3 = new 학생("이영희");

        System.out.println("전체 학생 수: " + 학생.전체학생수); // 3
    }
}
```

**설명**: `이름`은 각 학생 객체마다 다른 값을 가지지만(인스턴스 멤버), `전체학생수`는 모든 `학생` 객체가 공동으로 사용하는 하나의 값입니다(static 멤버). 그래서 `학생1.이름`처럼 객체를 통해 접근하는 인스턴스 필드와 달리, static 필드는 `학생.전체학생수`처럼 **클래스 이름으로** 접근하는 것이 일반적입니다.

| 구분 | 인스턴스 멤버 | static 멤버 |
|---|---|---|
| 소속 | 객체(인스턴스)마다 별도로 존재 | 클래스 전체에 하나만 존재 |
| 접근 방법 | `객체.필드` | `클래스.필드` (객체를 통해서도 접근은 가능하지만 권장하지 않음) |
| 생성 시점 | 객체가 `new`될 때마다 생성 | 프로그램 시작 시 클래스가 로딩될 때 한 번만 생성 |

---

## 5. final 필드와 상수

`final`이 붙은 필드는 **한 번 값이 정해지면 다시 바꿀 수 없습니다**. 프로그램 전체에서 값이 변하지 않는 상수를 선언할 때 사용합니다.

```java
public class 원 {
    static final double PI = 3.14159; // static + final: 클래스 전체가 공유하는 변하지 않는 값(상수)

    private double 반지름;

    public 원(double 반지름) {
        this.반지름 = 반지름;
    }

    public double 넓이구하기() {
        return 반지름 * 반지름 * PI;
    }
}
```

**설명**: 상수는 관례적으로 이름을 대문자와 밑줄로 작성합니다(`PI`, `MAX_SCORE` 등). `static final`을 함께 사용하면 "클래스 전체가 공유하면서 절대 바뀌지 않는 값"이 되어, 원주율처럼 프로그램 어디서도 동일해야 하는 값을 표현하기에 적합합니다.

```java
public class Final예제 {
    public static void main(String[] args) {
        System.out.println(원.PI); // 3.14159

        // 원.PI = 3.14; // 컴파일 에러! final 필드는 값을 재대입할 수 없음
    }
}
```

---

## 자주 하는 실수

1. **this를 빠뜨려 매개변수가 필드에 대입되지 않음**
   ```java
   public 학생(String 이름) {
       이름 = 이름; // this 없이 쓰면 매개변수를 자기 자신에게 대입 (아무 효과 없음)
   }
   ```

2. **private 필드를 클래스 밖에서 직접 접근하려다 컴파일 에러**
   ```java
   학생1.이름 = "김철수"; // private 필드는 직접 접근 불가, set이름()을 사용해야 함
   ```

3. **static 필드를 객체를 통해 접근(문법적으로는 가능하지만 권장하지 않음)**
   ```java
   학생1.전체학생수 // 동작은 하지만, static은 클래스이름으로 접근하는 것이 관례: 학생.전체학생수
   ```

4. **final 필드에 값을 재대입하려다 컴파일 에러**
   ```java
   원.PI = 3.14; // 컴파일 에러: final 필드는 한 번 정해지면 변경 불가
   ```

---

## 핵심 요약

| 항목 | 핵심 내용 |
|---|---|
| this | 현재 객체 자신을 가리킴, 매개변수와 필드 이름이 같을 때 구분용으로 사용 |
| private | 클래스 내부에서만 접근 가능, 캡슐화의 핵심 |
| public | 어디서든 접근 가능 |
| getter/setter | private 필드에 안전하게 접근/수정하는 public 메서드, `get필드명()`/`set필드명(값)` |
| static | 객체마다 따로 있지 않고 클래스 전체가 공유하는 멤버, `클래스.필드`로 접근 |
| final | 한 번 값이 정해지면 변경 불가, 상수 선언 시 `static final`과 함께 사용 |
