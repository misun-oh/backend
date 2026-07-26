# Day 10. 클래스 - this, static, 캡슐화 - 실습 답안

---

## 기본

### 문제 1. this로 필드 초기화하기

```java
// 강좌.java
public class 강좌 {
    private String 강좌명;
    private int 정원;

    public 강좌(String 강좌명, int 정원) {
        this.강좌명 = 강좌명;
        this.정원 = 정원;
    }

    public String get강좌명() {
        return 강좌명;
    }

    public int get정원() {
        return 정원;
    }
}
```

```java
// Practice1.java
public class Practice1 {
    public static void main(String[] args) {
        강좌 강좌1 = new 강좌("자바프로그래밍", 30);
        System.out.println("강좌명: " + 강좌1.get강좌명() + ", 정원: " + 강좌1.get정원());
    }
}
```

**설명**: 매개변수 이름(`강좌명`, `정원`)을 필드와 똑같이 지었기 때문에, `this.강좌명 = 강좌명;`처럼 `this`를 붙여야 왼쪽(필드)과 오른쪽(매개변수)이 구분됩니다.

**틀리기 쉬운 포인트**: `this` 없이 `강좌명 = 강좌명;`이라고만 쓰면, 자바는 이를 매개변수를 자기 자신에게 대입하는 것으로 처리해서 필드는 초기화되지 않은 상태(`null`)로 남습니다.

---

### 문제 2. setter로 값 변경하기

```java
public class 강좌 {
    private String 강좌명;
    private int 정원;

    public 강좌(String 강좌명, int 정원) {
        this.강좌명 = 강좌명;
        this.정원 = 정원;
    }

    public int get정원() {
        return 정원;
    }

    public void set정원(int 정원) {
        this.정원 = 정원;
    }
}
```

```java
public class Practice2 {
    public static void main(String[] args) {
        강좌 강좌1 = new 강좌("자바프로그래밍", 30);
        System.out.println("변경 전 정원: " + 강좌1.get정원());

        강좌1.set정원(40);
        System.out.println("변경 후 정원: " + 강좌1.get정원());
    }
}
```

**설명**: `set정원(40)`을 호출하면 `강좌1` 객체의 `정원` 필드가 `private`로 숨겨져 있어도, `public` setter 메서드를 통해 안전하게 값을 바꿀 수 있습니다.

**틀리기 쉬운 포인트**: `강좌1.정원 = 40;`처럼 필드에 직접 대입하려고 하면 `private`이므로 컴파일 에러가 납니다. 반드시 `public`으로 열어둔 setter를 거쳐야 합니다.

---

## 응용

### 문제 3. setter에 검증 로직 추가하기

```java
// 계좌.java
public class 계좌 {
    private int 잔액;

    public 계좌(int 잔액) {
        this.잔액 = 잔액;
    }

    public int get잔액() {
        return 잔액;
    }

    public void set잔액(int 잔액) {
        if (잔액 < 0) {
            System.out.println("잔액은 음수가 될 수 없습니다");
            return; // 잘못된 값이면 아래 대입문을 실행하지 않고 메서드 종료
        }
        this.잔액 = 잔액;
    }
}
```

```java
// Practice3.java
public class Practice3 {
    public static void main(String[] args) {
        계좌 계좌1 = new 계좌(10000);
        계좌1.set잔액(-5000); // 검증 로직에 걸려 값이 바뀌지 않음

        System.out.println("현재 잔액: " + 계좌1.get잔액());
    }
}
```

**설명**: `set잔액()` 안에서 `잔액 < 0`을 먼저 확인하고, 조건에 걸리면 `return;`으로 메서드를 즉시 종료해 `this.잔액 = 잔액;`이 실행되지 않게 막았습니다. 이것이 캡슐화의 핵심 이점으로, 필드를 `public`으로 열어뒀다면 이런 검증이 불가능합니다.

**틀리기 쉬운 포인트**: `return;`을 빠뜨리면 "잔액은 음수가 될 수 없습니다"라는 메시지만 출력하고, 바로 아래 줄에서 `this.잔액 = 잔액;`이 그대로 실행되어 결국 잘못된 값이 저장되어 버립니다.

---

### 문제 4. static으로 전체 개수 세기

```java
// 상품.java
public class 상품 {
    private String 이름;
    static int 전체상품수 = 0;

    public 상품(String 이름) {
        this.이름 = 이름;
        전체상품수++;
    }
}
```

```java
// Practice4.java
public class Practice4 {
    public static void main(String[] args) {
        상품 상품1 = new 상품("노트북");
        상품 상품2 = new 상품("마우스");
        상품 상품3 = new 상품("키보드");

        System.out.println("전체 상품 수: " + 상품.전체상품수);
    }
}
```

**설명**: `전체상품수`는 `static`이므로 `상품1`, `상품2`, `상품3` 모두가 공유하는 하나의 값입니다. 생성자가 실행될 때마다(즉 객체가 하나씩 만들어질 때마다) `전체상품수++`가 실행되어 값이 누적됩니다.

**틀리기 쉬운 포인트**: `전체상품수`를 `static` 없이 일반 필드로 선언하면, 각 객체가 독립적으로 `0`부터 시작하는 자기만의 카운터를 가지게 되어 "전체" 개수를 셀 수 없게 됩니다.

---

## 도전

### 문제 5. final 상수 활용

```java
// 원.java
public class 원 {
    static final double PI = 3.14159;

    private double 반지름;

    public 원(double 반지름) {
        this.반지름 = 반지름;
    }

    public double 넓이구하기() {
        return 반지름 * 반지름 * PI;
    }
}
```

```java
// Practice5.java
public class Practice5 {
    public static void main(String[] args) {
        원 원1 = new 원(5);
        System.out.printf("넓이: %.2f%n", 원1.넓이구하기());
    }
}
```

**설명**: `PI`는 `static final`로 선언되어 모든 `원` 객체가 공유하며 절대 값이 바뀌지 않는 상수입니다. `넓이구하기()` 메서드 내부에서 필드(`반지름`)와 상수(`PI`)를 함께 사용해 계산합니다.

**틀리기 쉬운 포인트**: `PI`처럼 프로그램 전체에서 고정된 값은 `static final`로 선언해 재사용하는 것이 안전합니다. 만약 `final` 없이 선언하면 실수로 `PI = 3.14;`처럼 다른 값으로 바뀌어도 컴파일 에러가 나지 않아 버그의 원인이 될 수 있습니다.

---

### 문제 6. 종합 - 캡슐화된 학과 클래스 + static 카운터

```java
// 학과.java
public class 학과 {
    static int 전체학과수 = 0;

    private String 학과명;
    private int 정원;

    public 학과(String 학과명, int 정원) {
        this.학과명 = 학과명;
        this.정원 = 정원;
        전체학과수++;
    }

    public String get학과명() {
        return 학과명;
    }

    public int get정원() {
        return 정원;
    }

    public void set정원(int 정원) {
        if (정원 <= 0) {
            System.out.println("정원은 0보다 커야 합니다");
            return;
        }
        this.정원 = 정원;
    }
}
```

```java
// Practice6.java
public class Practice6 {
    public static void main(String[] args) {
        학과 학과1 = new 학과("컴퓨터공학과", 40);
        학과 학과2 = new 학과("경영학과", 50);

        학과1.set정원(-10); // 검증 로직에 걸려 정원이 바뀌지 않음

        System.out.println("전체 학과 수: " + 학과.전체학과수);
        System.out.println(학과1.get학과명() + " 정원: " + 학과1.get정원());
    }
}
```

**설명**: 이 문제는 이번 챕터에서 배운 4가지 개념(this, private/캡슐화, setter 검증, static)을 모두 결합합니다. 생성자에서 `this`로 필드를 초기화하는 동시에 `static` 카운터(`전체학과수`)를 증가시키고, `set정원()`은 잘못된 값을 걸러내는 캡슐화된 설계를 보여줍니다.

**틀리기 쉬운 포인트**: `전체학과수++`를 생성자 안에 넣지 않고 `main`에서 직접 관리하려고 하면, 객체가 생성될 때마다 정확히 세는 것을 깜빡하기 쉽습니다. "객체가 생성될 때 자동으로 실행되어야 하는 로직"은 생성자 안에 넣는 것이 안전하고 자연스럽습니다.
