# Day 14. Object 클래스

| 항목 | 내용 |
|---|---|
| 선수 학습 | 인터페이스 (Day 13) |
| 이번 챕터 | 모든 자바 클래스가 상속받는 최상위 클래스 `Object` |

## 학습목표
- 모든 자바 클래스가 `Object`를 상속받고 있다는 것을 설명할 수 있다
- `toString()`을 오버라이딩해서 객체를 읽기 좋은 문자열로 출력할 수 있다
- `equals()`를 오버라이딩해서 객체의 내용 비교 기준을 직접 정의할 수 있다
- `equals()`를 재정의할 때 `hashCode()`도 함께 재정의해야 하는 이유를 설명할 수 있다

---

## 1. 모든 클래스의 조상: Object

Day9에서 클래스를 만들 때 `extends`를 따로 쓰지 않았습니다.

```java
public class Student {
    // extends를 쓰지 않았지만...
}
```

사실 이 코드는 자바 컴파일러가 보이지 않게 다음과 같이 처리합니다.

```java
public class Student extends Object { // 모든 클래스는 명시하지 않아도 Object를 상속받음
}
```

**설명**: 자바의 모든 클래스는 직접 명시하지 않아도 자동으로 `Object` 클래스를 상속받습니다. `Object`는 자바 클래스 계층의 **최상위 부모**입니다. Day11~12에서 배운 `Person → Student/Professor`처럼, 사실은 그 위에 `Object → Person → Student/Professor`라는 한 단계가 더 있었던 것입니다. 이 덕분에 모든 객체는 `Object`가 제공하는 몇 가지 기본 메서드를 공짜로 물려받습니다.

---

## 2. Object가 제공하는 기본 메서드

| 메서드 | 설명 |
|---|---|
| `toString()` | 객체를 문자열로 표현할 때 사용 (기본은 클래스명@해시코드 형태) |
| `equals(Object)` | 다른 객체와 같은지 비교 (기본은 참조값(주소) 비교, Day4 `==`과 동일) |
| `hashCode()` | 객체를 정수 하나로 표현한 값, 주로 `equals()`와 짝지어 사용 |
| `getClass()` | 객체의 클래스 정보를 반환 (Day14 리플렉션에서 이미 사용) |

```java
public class Student {
    private String name;
    private String studentId;

    public Student(String name, String studentId) {
        this.name = name;
        this.studentId = studentId;
    }
}
```

```java
public class ObjectDefaultBehaviorExample {
    public static void main(String[] args) {
        Student student1 = new Student("홍길동", "S001");

        System.out.println(student1); // 예: 학생@1b6d3586 (클래스명@해시코드, 읽기 어려움)
        System.out.println(student1.toString()); // 위와 완전히 같은 결과 (println이 내부적으로 toString()을 호출함)
    }
}
```

**설명**: `System.out.println(객체)`처럼 객체를 그대로 출력하면, 자바는 내부적으로 그 객체의 `toString()`을 호출합니다. `Student` 클래스는 `toString()`을 직접 작성한 적이 없으므로, `Object`로부터 물려받은 기본 `toString()`이 실행되어 "클래스명@해시코드" 형태의, 사람이 읽기엔 별 의미 없는 문자열이 출력됩니다.

---

## 3. toString() 오버라이딩하기

`Object`의 `toString()`을 우리 클래스에 맞게 오버라이딩하면, 객체를 출력할 때 원하는 형태로 보여줄 수 있습니다.

```java
public class Student {
    private String name;
    private String studentId;

    public Student(String name, String studentId) {
        this.name = name;
        this.studentId = studentId;
    }

    @Override
    public String toString() {
        return "학생[이름=" + name + ", 학번=" + studentId + "]";
    }
}
```

```java
public class ToStringExample {
    public static void main(String[] args) {
        Student student1 = new Student("홍길동", "S001");
        System.out.println(student1); // 학생[이름=홍길동, 학번=S001]
    }
}
```

**설명**: `toString()`도 Day11에서 배운 오버라이딩과 완전히 같은 방식입니다 — 부모(`Object`)가 제공한 메서드를, 자식(`Student`)이 원하는 대로 재정의한 것뿐입니다. 이제 `println(student1)`을 호출하면 우리가 정의한 읽기 좋은 문자열이 출력됩니다.

---

## 4. equals() 오버라이딩하기

Day4에서 참조형의 `==`은 "같은 주소를 가리키는지"만 비교한다고 배웠습니다. `Object`의 기본 `equals()`도 마찬가지로 내부적으로 `==`과 똑같이 동작합니다.

```java
public class EqualsDefaultExample {
    public static void main(String[] args) {
        Student student1 = new Student("홍길동", "S001");
        Student student2 = new Student("홍길동", "S001"); // 이름, 학번이 완전히 같은 다른 객체

        System.out.println(student1.equals(student2)); // false! 기본 equals()는 내용이 아니라 주소를 비교
    }
}
```

**설명**: `student1`과 `student2`는 이름과 학번이 완전히 같지만, `new`로 각각 따로 만들어진 **서로 다른 객체**입니다. `Object`의 기본 `equals()`는 "완전히 같은 객체(같은 주소)인지"만 확인하므로 `false`가 나옵니다. "이름과 학번이 같으면 같은 학생으로 취급하고 싶다"면, `equals()`를 직접 오버라이딩해야 합니다.

```java
public class Student {
    private String name;
    private String studentId;

    public Student(String name, String studentId) {
        this.name = name;
        this.studentId = studentId;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;            // 완전히 같은 객체면 바로 true
        if (!(obj instanceof Student)) return false; // 학생 타입이 아니면 비교 불가, false

        Student other = (Student) obj; // Object 타입으로 넘어온 것을 다시 학생 타입으로 다운캐스팅 (Day12 복습)
        return this.studentId.equals(other.studentId); // 학번이 같으면 같은 학생으로 판단
    }
}
```

```java
public class EqualsOverrideExample {
    public static void main(String[] args) {
        Student student1 = new Student("홍길동", "S001");
        Student student2 = new Student("홍길동", "S001");

        System.out.println(student1.equals(student2)); // true! 학번이 같으므로 같은 학생으로 판단
    }
}
```

**설명**: `equals()`의 매개변수 타입이 `Object`인 이유는, `Object`에 정의된 원래 메서드의 형태를 그대로 따라야(오버라이딩 규칙, Day11 복습) 하기 때문입니다. `instanceof`는 "이 객체가 특정 타입인지" 확인하는 연산자로, 여기서는 넘어온 `obj`가 진짜 `Student` 타입이 맞는지 먼저 확인합니다. 타입이 맞다면 `(Student) obj`로 다운캐스팅해서 `studentId` 필드를 비교합니다. 이번 예제는 "학번이 같으면 같은 학생"이라는 규칙을 직접 정한 것이며, 어떤 필드를 기준으로 같음을 판단할지는 클래스를 설계하는 사람이 정하기 나름입니다.

---

## 5. equals()를 재정의하면 hashCode()도 함께 재정의해야 하는 이유

자바에는 "`equals()`로 같다고 판단되는 두 객체는, `hashCode()`도 반드시 같은 값을 반환해야 한다"는 규칙이 있습니다. 이 규칙을 지키지 않으면, `HashSet`이나 `HashMap`처럼 해시코드를 활용하는 자료구조(다음 단계에서 배울 컬렉션들)에서 예상치 못한 오류가 생길 수 있습니다.

```java
public class Student {
    private String name;
    private String studentId;

    // ... 생성자, equals() 생략 ...

    @Override
    public int hashCode() {
        return studentId.hashCode(); // equals()에서 비교에 사용한 필드(학번)를 기준으로 hashCode도 생성
    }
}
```

**설명**: `equals()`가 `studentId`을 기준으로 같음을 판단했다면, `hashCode()`도 똑같이 `studentId`을 기준으로 값을 만들어야 합니다. 그래야 "같다고 판단되는 두 객체는 hashCode도 같다"는 규칙이 지켜집니다. 지금 단계에서는 "`equals()`를 재정의하면 `hashCode()`도 같이 재정의하는 것이 규칙"이라는 점만 기억해도 충분합니다. 실무에서는 IDE(이클립스, 인텔리제이)의 자동 생성 기능으로 이 둘을 한 번에 만드는 경우가 많습니다.

---

## 자주 하는 실수

1. **toString()의 매개변수/반환형을 잘못 작성해 오버라이딩 실패**
   ```java
   public void toString() { ... } // 컴파일 에러! 반환형이 String이어야 하는데 void로 씀
   public String toString() { ... } // 올바른 표현
   ```

2. **equals()의 매개변수 타입을 Object가 아닌 걸로 작성 (오버로딩이 되어버림)**
   ```java
   public boolean equals(Student other) { ... } // 이건 오버라이딩이 아니라 새로운 메서드(오버로딩)가 됨
   public boolean equals(Object obj) { ... } // 올바른 표현: 반드시 Object 타입으로 받아야 진짜 오버라이딩
   ```

3. **equals()만 재정의하고 hashCode()는 그대로 둠**
   ```java
   // equals()는 학번 기준으로 재정의했지만 hashCode()는 그대로 두면
   // "equals가 true면 hashCode도 같아야 한다"는 규칙이 깨져 나중에 컬렉션에서 예상 밖의 동작이 생길 수 있음
   ```

4. **instanceof 확인 없이 바로 다운캐스팅해서 ClassCastException 위험**
   ```java
   public boolean equals(Object obj) {
       Student other = (Student) obj; // obj가 학생이 아닌 다른 타입이면 여기서 예외 발생
       return this.studentId.equals(other.studentId);
   }
   ```

---

## 핵심 요약

| 항목 | 핵심 내용 |
|---|---|
| Object | 모든 자바 클래스가 자동으로 상속받는 최상위 클래스 |
| toString() | 객체를 문자열로 표현, 기본은 "클래스명@해시코드", 오버라이딩해서 읽기 좋게 변경 |
| equals() | 기본은 참조(주소) 비교(Day4 `==`과 동일), 오버라이딩해서 내용 비교 기준을 직접 정의 |
| equals() 오버라이딩 규칙 | 매개변수는 반드시 `Object` 타입, `instanceof`로 타입 확인 후 다운캐스팅 |
| hashCode() | equals()를 재정의하면 반드시 함께 재정의, "같으면 hashCode도 같아야 한다"는 규칙 |
| getClass() | 객체의 클래스 정보 반환 (Day14 리플렉션에서 이미 사용) |

다음 챕터(Day15)에서는 제네릭을 배우면서, `Object`가 왜 "모든 타입을 담을 수 있는 만능 타입"으로 쓰였는지, 그리고 그 한계를 제네릭이 어떻게 해결하는지 연결해서 이해합니다.

---

<sub>이 내용은 Day15(제네릭)에서 "왜 Object로 아무 타입이나 담을 수 있는지"를 이해하는 데 필요하고, Day16(컬렉션 프레임워크)의 `contains()`에서 쓰일 `equals()` 재정의의 기초가 됩니다.</sub>
