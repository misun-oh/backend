# Day 17. 열거형(enum)

| 항목 | 내용 |
|---|---|
| 선수 학습 | 조건문과 switch (Day 3), 클래스와 생성자 (Day 9), 인터페이스 (Day 13) |
| 이번 챕터 | "정해진 값들 중 하나"를 안전하게 표현하는 `enum` |

## 학습목표
- `static final int` 상수 방식이 가진 문제(타입 안정성 없음)를 설명할 수 있다
- `enum`으로 상수 집합을 정의하고 `switch` 문에서 활용할 수 있다
- `enum`에 생성자·필드·메서드를 추가해 상수마다 다른 데이터를 가지게 할 수 있다
- `values()`, `valueOf()`, `name()`, `ordinal()`을 사용할 수 있다
- `enum`도 인터페이스를 구현할 수 있다는 것을 안다

---

## 1. 문제 상황: `int` 상수로 상태값을 관리하면?

학생의 재적 상태(재학/휴학/졸업)를 아래처럼 `int` 상수로 관리한다고 해봅시다.

```java
public class Student {
    public static final int STATUS_ENROLLED = 0;
    public static final int STATUS_LEAVE = 1;
    public static final int STATUS_GRADUATED = 2;

    private String name;
    private int status;
    // 생성자, getter/setter 생략
}
```

```java
public void printStatus(int status) {
    if (status == Student.STATUS_ENROLLED) {
        System.out.println("재학중");
    } else if (status == Student.STATUS_LEAVE) {
        System.out.println("휴학중");
    } else if (status == Student.STATUS_GRADUATED) {
        System.out.println("졸업");
    }
}
```

**문제**: `printStatus()`의 파라미터 타입은 그냥 `int`입니다. `printStatus(99)`처럼 **말도 안 되는 값을 넘겨도 컴파일 에러가 나지 않습니다.** `status`가 학생 상태를 뜻한다는 것도 변수명으로만 알 수 있을 뿐, 코드(타입)로는 전혀 보장되지 않습니다. 이런 "매직 넘버" 문제를 해결하는 것이 `enum`입니다.

---

## 2. enum 정의와 기본 사용

```java
public enum StudentStatus {
    ENROLLED, LEAVE, GRADUATED
}
```

```java
public class Student {
    private String name;
    private StudentStatus status; // int가 아니라 StudentStatus 타입!

    public Student(String name, StudentStatus status) {
        this.name = name;
        this.status = status;
    }

    public StudentStatus getStatus() {
        return status;
    }
}
```

**설명**: `enum StudentStatus`는 `ENROLLED`, `LEAVE`, `GRADUATED` **딱 3개의 값만 가질 수 있는 새로운 타입**을 만듭니다. `Student`의 `status` 필드를 `int`가 아니라 `StudentStatus`로 선언하면, `new Student("홍길동", 99)`처럼 잘못된 값을 넣는 것 자체가 **컴파일 에러**가 됩니다. Day1에서 배운 `int`, `String`과 똑같이 하나의 "타입"으로 취급된다는 점이 핵심입니다.

```java
Student s1 = new Student("홍길동", StudentStatus.ENROLLED); // 항상 타입.상수 형태로 접근
```

---

## 3. switch 문에서 enum 사용하기 (Day3 복습)

```java
public void printStatus(StudentStatus status) {
    switch (status) {
        case ENROLLED:
            System.out.println("재학중");
            break;
        case LEAVE:
            System.out.println("휴학중");
            break;
        case GRADUATED:
            System.out.println("졸업");
            break;
    }
}
```

**설명**: Day3에서 배운 `switch`문에 `enum` 타입 변수를 그대로 넣을 수 있습니다. `case` 뒤에는 `StudentStatus.ENROLLED`가 아니라 **상수 이름만**(`ENROLLED`) 씁니다. `if-else`로 `==` 비교를 반복하던 1절 코드보다 훨씬 읽기 좋습니다.

---

## 4. enum에 필드와 생성자 추가하기

`enum`은 단순히 이름만 나열하는 것을 넘어, **상수마다 서로 다른 데이터**를 가질 수 있습니다. 성적 등급마다 "합격 최소 점수"가 다르다고 해봅시다.

```java
public enum Grade {
    A(90), B(80), C(70), D(60), F(0); // 상수 이름(생성자에 넘길 값)

    private final int minScore;

    // enum의 생성자는 항상 private (직접 new로 호출 불가, Day9 생성자 복습)
    Grade(int minScore) {
        this.minScore = minScore;
    }

    public int getMinScore() {
        return minScore;
    }

    public boolean isPassing() {
        return minScore >= 60;
    }
}
```

```java
public class GradeExample {
    public static void main(String[] args) {
        Grade g = Grade.B;
        System.out.println(g + " 등급의 최소 점수: " + g.getMinScore()); // B 등급의 최소 점수: 80
        System.out.println(g.isPassing()); // true

        System.out.println(Grade.F.isPassing()); // false
    }
}
```

**설명**: `A(90)`처럼 상수 이름 뒤에 괄호로 값을 적으면, 그 값이 `enum`의 **생성자**로 전달됩니다. 일반 클래스의 생성자(Day9)와 똑같이 필드에 저장하고 getter로 꺼내 쓸 수 있습니다. 다만 `enum`의 생성자는 **항상 `private`**이라 `new Grade(90)`처럼 직접 만들 수 없고, 오직 코드 맨 위에 나열된 상수(`A`, `B`, `C`, `D`, `F`)만 존재합니다 — 즉 `Grade` 타입의 객체는 딱 5개뿐이며, 그 이상 만들어지지 않습니다.

---

## 5. `values()` / `valueOf()` / `name()` / `ordinal()`

```java
public class GradeUtilExample {
    public static void main(String[] args) {
        // values() : 모든 상수를 선언 순서대로 배열로 반환
        for (Grade g : Grade.values()) {
            System.out.println(g.name() + " : " + g.getMinScore());
        }

        // valueOf(문자열) : 문자열을 enum 상수로 변환
        Grade g = Grade.valueOf("A");
        System.out.println(g == Grade.A); // true

        // ordinal() : 선언된 순서(0부터). 상수 순서를 바꾸면 값도 바뀌므로 로직에 쓰지 않는 게 좋음
        System.out.println(Grade.A.ordinal()); // 0
        System.out.println(Grade.F.ordinal()); // 4

        // 존재하지 않는 이름이면 예외 발생
        try {
            Grade.valueOf("S");
        } catch (IllegalArgumentException e) {
            System.out.println("존재하지 않는 등급: " + e.getMessage());
        }
    }
}
```

**설명**: `values()`는 `enum`의 모든 상수를 배열로 주기 때문에, "전체 목록을 화면에 뿌려야 할 때"(예: 검색 조건 드롭다운) 자주 씁니다. `valueOf("A")`는 사용자 입력(문자열)을 `enum`으로 바꿀 때 쓰는데, **목록에 없는 문자열을 넘기면 `IllegalArgumentException`이 발생**합니다 — 반대로 말하면, 허용되지 않은 값은 애초에 `Grade` 타입이 될 수 없다는 뜻이라 안전합니다. `ordinal()`은 선언 순서일 뿐 의미 있는 값이 아니므로, 등급을 비교하고 싶다면 `ordinal()`이 아니라 4절처럼 직접 만든 `minScore` 같은 필드를 써야 합니다.

---

## 6. enum도 인터페이스를 구현할 수 있다 (Day13 복습)

```java
public interface Describable {
    String describe();
}

public enum Grade implements Describable {
    A(90), B(80), C(70), D(60), F(0);

    private final int minScore;

    Grade(int minScore) {
        this.minScore = minScore;
    }

    public int getMinScore() { return minScore; }

    @Override
    public String describe() {
        return name() + "등급 (최소 " + minScore + "점)";
    }
}
```

**설명**: `enum`도 클래스처럼 `implements`로 인터페이스를 구현할 수 있습니다(Day13에서 배운 `implements` 규칙 그대로). 다만 `enum`은 이미 자바 내부적으로 `Enum` 클래스를 상속하고 있는 상태라 **다른 클래스를 `extends`할 수는 없습니다** — 인터페이스 구현만 가능합니다.

---

## 7. 실전에서는 이렇게 쓴다 — "허용된 값의 화이트리스트"

`enum`이 가장 힘을 발휘하는 상황은 **"사용자 입력을 코드가 아는 값으로만 제한해야 할 때"** 입니다. 예를 들어 게시글 목록 화면에서 정렬 기준을 받을 때, 정렬 컬럼명을 문자열로 그대로 받으면 SQL 인젝션 위험이 있습니다. 이때 `enum`으로 허용 가능한 정렬 기준만 미리 정의해두면, 5절에서 배운 `valueOf()`를 스프링이 자동으로 호출해서 **목록에 없는 값은 아예 요청 단계에서 걸러집니다.**

```java
public enum EmpSort {
    EMP_ID("emp_id", "asc"),
    HIRE_DATE("hire_date", "desc"),
    SALARY("salary", "desc");

    private final String column;
    private final String direction;

    EmpSort(String column, String direction) {
        this.column = column;
        this.direction = direction;
    }

    public String getColumn() { return column; }
    public String getDirection() { return direction; }
}
```

`?sort=SALARY` 로 요청이 오면 스프링이 `EmpSort.valueOf("SALARY")`를 대신 호출해서 파라미터에 바인딩해줍니다. `?sort=DROP_TABLE` 처럼 목록에 없는 값을 보내면 `IllegalArgumentException`이 발생해 요청 자체가 거부됩니다. 이 패턴은 스프링 커리큘럼 **Day11(동적 SQL과 페이징)** 에서 `ORDER BY ${...}` 절의 정렬 컬럼을 안전하게 제한할 때 그대로 사용합니다.

---

## 자주 하는 실수

1. **상수 나열 뒤에 세미콜론을 빠뜨림 (필드/메서드가 있을 때)**
   ```java
   public enum Grade {
       A(90), B(80) // 세미콜론 없음 -> 컴파일 에러!
       private final int minScore;
       ...
   }
   public enum Grade {
       A(90), B(80); // 필드/메서드가 있으면 상수 나열 끝에 반드시 세미콜론
       private final int minScore;
       ...
   }
   ```

2. **`new`로 enum 객체를 직접 생성하려 시도**
   ```java
   Grade g = new Grade(90); // 컴파일 에러! enum 생성자는 private, 직접 생성 불가
   Grade g = Grade.A;       // 올바른 사용법 — 이미 만들어진 상수를 참조만 함
   ```

3. **`valueOf()`에 없는 문자열을 그대로 전달**
   ```java
   Grade g = Grade.valueOf("a"); // 런타임 예외! 대소문자까지 정확히 일치해야 함(A로 써야 함)
   ```

4. **`ordinal()`을 비즈니스 로직(순위·점수 비교)에 사용**
   ```java
   if (Grade.A.ordinal() < Grade.B.ordinal()) { ... } // 동작은 하지만, 상수 순서를 바꾸면 로직이 깨짐
   if (Grade.A.getMinScore() > Grade.B.getMinScore()) { ... } // 의미가 명확한 필드로 비교
   ```

5. **enum 상수 이름을 소문자로 작성**
   ```java
   public enum StudentStatus { enrolled, leave, graduated } // 동작은 하지만 관례 위반
   public enum StudentStatus { ENROLLED, LEAVE, GRADUATED } // enum 상수는 대문자 스네이크 케이스 관례
   ```

---

## 핵심 요약

| 항목 | 핵심 내용 |
|---|---|
| enum | "정해진 값들 중 하나"를 표현하는 특수한 타입. `static final int` 상수의 타입 안정성 문제를 해결 |
| 정의 | `public enum 이름 { 상수1, 상수2, ... }` |
| switch | enum 타입 변수를 그대로 switch 대상으로 사용, `case`에는 상수 이름만 |
| 필드·생성자 | 상수 뒤 괄호로 값 전달 → private 생성자가 필드에 저장 → 상수마다 다른 데이터 보유 가능 |
| values() | 모든 상수를 선언 순서 배열로 반환 (드롭다운·전체 순회용) |
| valueOf(문자열) | 문자열 → enum 변환, 없는 값이면 `IllegalArgumentException` |
| ordinal() | 선언 순서(0부터). 순서가 바뀌면 값도 바뀌므로 로직에 사용 금지 |
| 인터페이스 구현 | enum도 `implements` 가능 (단, 다른 클래스 `extends`는 불가) |
| 실전 활용 | 정렬 기준·상태값처럼 "허용된 값의 집합"을 강제 → 화이트리스트로 SQL 인젝션 방지 (스프링 Day11 `EmpSort` 참고) |

다음 단계로는 예외 처리(Exception Handling)를 학습하는 것을 추천합니다 — `valueOf()`가 던지는 `IllegalArgumentException`을 안전하게 다루는 방법도 그때 함께 다룹니다.
