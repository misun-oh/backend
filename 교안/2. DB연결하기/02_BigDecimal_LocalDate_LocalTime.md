#5. BigDecimal, LocalDate, LocalTime

| 항목 | 내용 |
|---|---|
| 선수 학습 | 클래스와 객체 (Day 9), 참조 타입 (Day 4) |
| 이번 챕터 | 정확한 소수 계산(BigDecimal), 날짜·시간 다루기(LocalDate, LocalTime) |

이 챕터는 자바 표준 라이브러리(java.math, java.time 패키지)가 제공하는 클래스를 "가져다 쓰는" 방법을 다룹니다. 지금까지 우리가 직접 클래스를 설계했던 것과 달리, 이번에는 이미 잘 만들어진 클래스의 사용법을 익힙니다.

## 학습목표
- `double`로 돈 계산을 하면 안 되는 이유를 설명하고, `BigDecimal`로 정확한 소수 계산을 할 수 있다
- `LocalDate`로 날짜를 생성하고, 날짜 간 연산·비교를 할 수 있다
- `LocalTime`으로 시각을 생성하고, 시간 간 연산·비교를 할 수 있다

---

## 1. 문제 상황: double로 돈 계산하면 생기는 오차

Day1에서 실수형 `double`을 배웠습니다. 그런데 `double`로 돈 같은 소수 계산을 하면 예상치 못한 오차가 생깁니다.

```java
public class DoubleProblemExample {
    public static void main(String[] args) {
        double a = 0.1;
        double b = 0.2;
        System.out.println(a + b); // 0.30000000000000004 (0.3이 아님!)
    }
}
```

**설명**: 컴퓨터는 내부적으로 실수를 2진수로 저장합니다. `0.1`이나 `0.2`는 10진수로는 딱 떨어지는 소수지만, 2진수로 바꾸면 `0.000110011001100...`처럼 **끝없이 반복되는 무한소수**가 됩니다. `double`은 정해진 자리 수(비트 수)만큼만 저장할 수 있어서, 이 무한히 반복되는 부분을 어느 지점에서 잘라낼 수밖에 없고, 그 잘려나간 부분이 바로 오차가 됩니다. 마치 `1/3`을 10진수로 쓰면 `0.333...`이 끝없이 반복되어 정확히 적을 수 없는 것과 같은 원리입니다. 일반적인 계산에서는 무시할 만한 오차지만, **등록금·계좌 잔액 같은 금액 계산**에서는 이 오차가 누적되면 실제 금액과 달라지는 심각한 문제가 됩니다. 이때 사용하는 것이 `BigDecimal`입니다.

### 더 알아보기: 소수를 2진수로 바꾸는 방법

궁금하신 분들을 위해, 소수부(소수점 아래)를 2진수로 바꾸는 절차를 소개합니다. **소수부에 2를 계속 곱하면서, 매번 나오는 정수부(0 또는 1)를 순서대로 적어 나가는** 방식입니다. 소수부가 0이 되면 끝나고(유한소수), 이전과 같은 소수부가 다시 나오면 그 구간이 영원히 반복됩니다(무한소수).

**예시 1: 0.625 → 딱 떨어지는 경우**

| 단계 | 계산 | 정수부(비트) | 남은 소수부 |
|---|---|---|---|
| 1 | 0.625 × 2 = 1.25 | 1 | 0.25 |
| 2 | 0.25 × 2 = 0.5 | 0 | 0.5 |
| 3 | 0.5 × 2 = 1.0 | 1 | 0.0 (소수부가 0이 되어 종료) |

결과: `0.625(10진수) = 0.101(2진수)` — 검산하면 `1×(1/2) + 0×(1/4) + 1×(1/8) = 0.625`로 정확히 일치합니다.

**예시 2: 0.1 → 무한소수가 되는 경우**

| 단계 | 계산 | 정수부(비트) | 남은 소수부 |
|---|---|---|---|
| 1 | 0.1 × 2 = 0.2 | 0 | 0.2 |
| 2 | 0.2 × 2 = 0.4 | 0 | 0.4 |
| 3 | 0.4 × 2 = 0.8 | 0 | 0.8 |
| 4 | 0.8 × 2 = 1.6 | 1 | 0.6 |
| 5 | 0.6 × 2 = 1.2 | 1 | 0.2 (1단계와 같은 소수부가 다시 나타남 → 반복 확정) |

결과: `0.1(10진수) = 0.0(0011)(2진수)` — 괄호 안 "0011"이 무한 반복됩니다.

**왜 어떤 건 끝나고 어떤 건 안 끝나는가**: `0.625 = 5/8 = 5/2³`는 분모가 2의 거듭제곱뿐이라 몇 번 곱하다 보면 정확히 나누어떨어집니다. 반면 `0.1 = 1/10 = 1/(2×5)`는 분모에 5가 섞여 있어서, 아무리 2를 곱해도 소수부가 0이 되는 순간이 오지 않습니다. **10진수는 분모가 2 또는 5로만 이루어지면 유한소수가 되지만, 2진수는 분모가 오직 2의 거듭제곱일 때만 유한소수가 됩니다.** 이 차이 때문에 10진수로는 깔끔한 `0.1`이, 2진수로는 무한소수가 되는 것입니다.

---

## 2. BigDecimal 생성하기

`BigDecimal`은 소수를 오차 없이 정확하게 표현하는 클래스입니다. `java.math` 패키지에 있습니다.

| 생성 방법 | 예시 | 주의 |
|---|---|---|
| 문자열로 생성 (권장) | `new BigDecimal("0.1")` | 정확한 값 그대로 생성됨 |
| 정수로 생성 | `new BigDecimal(100)` | 정수는 오차 문제가 없어 안전 |
| double로 생성 (비권장) | `new BigDecimal(0.1)` | **이미 오차가 생긴 double 값을 그대로 가져와서 위험** |

```java
import java.math.BigDecimal;

public class BigDecimalCreateExample {
    public static void main(String[] args) {
        BigDecimal correct = new BigDecimal("0.1"); // 올바른 방법: 문자열로 생성
        BigDecimal wrong = new BigDecimal(0.1);      // 잘못된 방법: double로 생성

        System.out.println(correct); // 0.1
        System.out.println(wrong);   // 0.1000000000000000055511151231257827021181583404541015625 (이미 오차가 반영됨)
    }
}
```

**설명**: `new BigDecimal(0.1)`은 겉보기엔 편해 보이지만, `0.1`이라는 `double` 리터럴 자체가 이미 미세한 오차를 가지고 있어서 그 오차까지 그대로 옮겨옵니다. **`BigDecimal`을 만들 때는 항상 문자열 생성자를 사용하는 것이 안전한 습관**입니다.

---

## 3. BigDecimal 연산

`BigDecimal`은 `+`, `-`, `*`, `/` 같은 연산자를 직접 쓸 수 없습니다. 대신 메서드를 호출합니다.

| 메서드 | 설명 | 예시 |
|---|---|---|
| `add(값)` | 더하기 | `a.add(b)` |
| `subtract(값)` | 빼기 | `a.subtract(b)` |
| `multiply(값)` | 곱하기 | `a.multiply(b)` |
| `divide(값)` | 나누기 | `a.divide(b)` |
| `compareTo(값)` | 크기 비교 (음수/0/양수 반환, 부록4(람다식)의 Comparator와 같은 관례) | `a.compareTo(b)` |

```java
import java.math.BigDecimal;

public class BigDecimalCalcExample {
    public static void main(String[] args) {
        BigDecimal tuition = new BigDecimal("3500000");   // 등록금
        BigDecimal discount = new BigDecimal("0.1");        // 할인율 10%

        BigDecimal discountAmount = tuition.multiply(discount);       // 할인 금액
        BigDecimal finalAmount = tuition.subtract(discountAmount);    // 최종 등록금

        System.out.println("할인 금액: " + discountAmount);
        System.out.println("최종 등록금: " + finalAmount);
    }
}
```

**출력 결과**
```
할인 금액: 350000.0
최종 등록금: 3150000.0
```

**설명**: `BigDecimal`은 Day1에서 배운 `int`, `double`과 달리 `+`, `*` 연산자를 직접 쓸 수 없고, `add()`, `multiply()` 같은 메서드로 계산해야 합니다. 계산 결과도 새로운 `BigDecimal` 객체로 반환되므로, `finalAmount`처럼 변수에 다시 담아 사용합니다.

---

## 4. BigDecimal 비교 시 주의점: equals() vs compareTo()

Day14(Object 클래스)에서 배운 `equals()`를 `BigDecimal`에 그대로 쓰면 예상과 다른 결과가 나올 수 있습니다.

```java
BigDecimal a = new BigDecimal("1.0");
BigDecimal b = new BigDecimal("1.00");

System.out.println(a.equals(b));     // false! 값은 같지만 소수점 자릿수(scale)가 달라서 다르다고 판단
System.out.println(a.compareTo(b) == 0); // true! 값 자체만 비교하면 같음
```

**설명**: `BigDecimal`의 `equals()`는 값뿐만 아니라 **소수점 자릿수(scale)까지 완전히 같아야** `true`를 반환합니다. `1.0`과 `1.00`은 수학적으로 같은 값이지만 자릿수 표현이 다르므로 `equals()`는 `false`입니다. **금액이 같은지 비교할 때는 `equals()`가 아니라 `compareTo() == 0`을 사용해야 합니다.**

---

## 5. LocalDate로 날짜 다루기

`LocalDate`는 시간 없이 **날짜만** 표현하는 클래스입니다. `java.time` 패키지에 있습니다.

| 메서드 | 설명 | 예시 |
|---|---|---|
| `LocalDate.now()` | 오늘 날짜 생성 | `LocalDate.now()` |
| `LocalDate.of(년, 월, 일)` | 특정 날짜 생성 | `LocalDate.of(2026, 3, 2)` |
| `plusDays(n)` / `minusDays(n)` | n일 더하기/빼기 (년·월 단위도 동일하게 plusMonths, plusYears 등) | `date.plusDays(7)` |
| `isBefore(날짜)` / `isAfter(날짜)` | 두 날짜의 순서 비교 | `a.isBefore(b)` |
| `until(날짜, ChronoUnit.DAYS)` | 두 날짜 사이의 일수 계산 | `start.until(end, ChronoUnit.DAYS)` |

```java
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class LocalDateExample {
    public static void main(String[] args) {
        LocalDate enrollmentDate = LocalDate.of(2023, 3, 2); // 입학일
        LocalDate today = LocalDate.now();                    // 오늘 날짜

        System.out.println("입학일: " + enrollmentDate);
        System.out.println("오늘: " + today);

        LocalDate graduationDate = enrollmentDate.plusYears(4); // 4년 뒤 졸업 예정일
        System.out.println("졸업 예정일: " + graduationDate);

        long daysSinceEnrollment = enrollmentDate.until(today, ChronoUnit.DAYS); // 입학 후 지난 일수
        System.out.println("입학 후 " + daysSinceEnrollment + "일 경과");

        System.out.println("아직 졸업 전인가? " + today.isBefore(graduationDate));
    }
}
```

**설명**: `LocalDate.of(2023, 3, 2)`처럼 생성자 대신 `of()`라는 **정적 메서드(static method, Day10 복습)** 로 객체를 만듭니다. `plusYears(4)`는 원본 날짜를 바꾸지 않고 **4년 뒤의 새로운 `LocalDate` 객체를 반환**합니다(Day5에서 배운 String의 불변성과 같은 성질 — `LocalDate`도 불변 객체입니다). `until(..., ChronoUnit.DAYS)`는 두 날짜 사이의 일수를 계산해주는데, 월 단위는 `ChronoUnit.MONTHS`, 년 단위는 `ChronoUnit.YEARS`로 바꾸면 됩니다.

---

## 6. LocalTime으로 시각 다루기

`LocalTime`은 날짜 없이 **시각만** 표현하는 클래스입니다.

| 메서드 | 설명 | 예시 |
|---|---|---|
| `LocalTime.now()` | 현재 시각 생성 | `LocalTime.now()` |
| `LocalTime.of(시, 분)` | 특정 시각 생성 | `LocalTime.of(9, 30)` |
| `plusHours(n)` / `plusMinutes(n)` | n시간/n분 더하기 | `time.plusMinutes(50)` |
| `isBefore(시각)` / `isAfter(시각)` | 두 시각의 순서 비교 | `a.isBefore(b)` |

```java
import java.time.LocalTime;

public class LocalTimeExample {
    public static void main(String[] args) {
        LocalTime classStartTime = LocalTime.of(9, 0);   // 수업 시작 시각 9:00
        LocalTime classEndTime = classStartTime.plusMinutes(50); // 50분 수업

        System.out.println("수업 시작: " + classStartTime);
        System.out.println("수업 종료: " + classEndTime);

        LocalTime checkTime = LocalTime.of(9, 30);
        boolean isDuringClass = checkTime.isAfter(classStartTime) && checkTime.isBefore(classEndTime);
        System.out.println("9:30은 수업 중인가? " + isDuringClass);
    }
}
```

**설명**: `classStartTime.plusMinutes(50)`도 `LocalDate`와 마찬가지로 원본을 바꾸지 않고 새 객체를 반환하는 불변 객체입니다. `isAfter()`와 `isBefore()`를 `&&`(Day2 복습)로 묶으면 "특정 시각이 두 시각 사이에 있는지" 검사할 수 있습니다.

---

## 7. 종합 예제: 학사관리 도메인에 함께 적용

```java
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class Student {
    private String name;
    private LocalDate enrollmentDate;
    private BigDecimal tuition;

    public Student(String name, LocalDate enrollmentDate, BigDecimal tuition) {
        this.name = name;
        this.enrollmentDate = enrollmentDate;
        this.tuition = tuition;
    }

    public long getEnrolledDays() {
        return enrollmentDate.until(LocalDate.now(), ChronoUnit.DAYS);
    }

    public BigDecimal getTuitionWithDiscount(BigDecimal discountRate) {
        BigDecimal discount = tuition.multiply(discountRate);
        return tuition.subtract(discount);
    }
}
```

```java
public class StudentFinanceExample {
    public static void main(String[] args) {
        Student student1 = new Student(
            "홍길동",
            LocalDate.of(2023, 3, 2),
            new BigDecimal("3500000")
        );

        System.out.println("재학 일수: " + student1.getEnrolledDays() + "일");
        System.out.println("할인 적용 등록금: " + student1.getTuitionWithDiscount(new BigDecimal("0.1")));
    }
}
```

**설명**: `Student` 클래스는 이제 필드 타입으로 `String`, `int` 같은 기본형뿐 아니라 `LocalDate`, `BigDecimal` 같은 표준 라이브러리 클래스도 사용합니다. 이렇게 잘 만들어진 클래스를 필드 타입으로 활용하면, 날짜 계산이나 정확한 금액 계산을 직접 구현할 필요 없이 안전하게 가져다 쓸 수 있습니다.

---

## 자주 하는 실수

1. **BigDecimal을 double로 생성**
   ```java
   new BigDecimal(0.1); // 이미 오차가 있는 double 값을 그대로 가져옴
   new BigDecimal("0.1"); // 올바른 방법
   ```

2. **BigDecimal에 사칙연산자를 직접 사용**
   ```java
   BigDecimal result = a + b; // 컴파일 에러! BigDecimal은 +, -, *, / 사용 불가
   BigDecimal result = a.add(b); // 올바른 방법
   ```

3. **BigDecimal을 equals()로 값 비교**
   ```java
   new BigDecimal("1.0").equals(new BigDecimal("1.00")); // false (자릿수까지 비교)
   new BigDecimal("1.0").compareTo(new BigDecimal("1.00")) == 0; // true (값만 비교, 올바른 방법)
   ```

4. **LocalDate/LocalTime의 plus 계열 메서드가 원본을 바꿀 거라 착각**
   ```java
   LocalDate date = LocalDate.now();
   date.plusDays(7); // 반환값을 버려서 아무 효과 없음! (String의 불변성과 같은 원리, Day5 복습)
   date = date.plusDays(7); // 올바른 방법: 반환값을 다시 대입해야 반영됨
   ```

---

## 핵심 요약

| 항목 | 핵심 내용 |
|---|---|
| BigDecimal이 필요한 이유 | double은 소수 계산 시 오차가 생겨 금액 계산에 부적합 |
| BigDecimal 생성 | 문자열 생성자 사용 권장 (`new BigDecimal("0.1")`) |
| BigDecimal 연산 | 연산자 대신 메서드 사용 (`add`, `subtract`, `multiply`, `divide`) |
| BigDecimal 비교 | 값 비교는 `equals()`가 아니라 `compareTo() == 0` |
| LocalDate | 날짜만 표현, `of()`로 생성, `plusDays()`/`until()`로 연산, 불변 객체 |
| LocalTime | 시각만 표현, `of()`로 생성, `plusMinutes()`/`isBefore()`로 연산, 불변 객체 |
