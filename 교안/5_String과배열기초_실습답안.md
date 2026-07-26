# Day 5. String과 배열 기초 - 실습 답안

---

## 기본

### 문제 1. 문자열 정보 출력

```java
public class Practice1 {
    public static void main(String[] args) {
        String 문장 = "Hello Java";

        System.out.println("길이: " + 문장.length());
        System.out.println("첫 글자: " + 문장.charAt(0));
    }
}
```

**설명**: `length()`는 공백을 포함한 전체 글자 수를 반환합니다(`"Hello Java"`는 공백 포함 10글자). `charAt(0)`은 인덱스 0번, 즉 첫 번째 글자를 반환합니다.

**틀리기 쉬운 포인트**: 배열의 길이는 `배열.length`(괄호 없음)이지만 문자열의 길이는 `문자열.length()`(괄호 있음)입니다. 이 둘을 혼동하기 쉽습니다.

---

### 문제 2. 배열 선언과 순회 (향상된 for문)

```java
public class Practice2 {
    public static void main(String[] args) {
        int[] 숫자들 = {10, 20, 30, 40, 50};

        for (int 숫자 : 숫자들) {
            System.out.println(숫자);
        }
    }
}
```

**설명**: 향상된 for문은 `숫자들` 배열의 요소를 처음부터 끝까지 하나씩 `숫자` 변수에 담아 반복합니다. 인덱스를 직접 관리할 필요가 없어 코드가 간결합니다.

**틀리기 쉬운 포인트**: 향상된 for문 안에서 `숫자 = 99;`처럼 값을 바꿔도 원본 배열 `숫자들`은 바뀌지 않습니다. `숫자`는 배열 요소의 "복사본"이기 때문입니다.

---

## 응용

### 문제 3. 이메일에서 아이디 추출

```java
import java.util.Scanner;

public class Practice3 {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        String 이메일 = sc.nextLine();

        int 위치 = 이메일.indexOf("@");         // @ 문자가 있는 위치를 찾음
        String 아이디 = 이메일.substring(0, 위치); // 처음부터 @ 직전까지 잘라냄

        System.out.println("아이디: " + 아이디);
    }
}
```

**설명**: `indexOf("@")`로 `@`의 위치(인덱스)를 알아낸 뒤, `substring(0, 위치)`로 문자열의 시작부터 그 위치 직전까지 잘라냅니다. `substring`의 끝 인덱스는 포함되지 않으므로 `@` 자체는 결과에 들어가지 않습니다.

**틀리기 쉬운 포인트**: `indexOf`가 찾는 문자가 없으면 `-1`을 반환하는데, 이 경우 `substring(0, -1)`은 예외를 발생시킵니다. 실무에서는 `위치 != -1`인지 먼저 확인하는 것이 안전합니다(이번 문제는 `@`가 항상 있다고 가정).

---

### 문제 4. 대소문자 무시 비교

```java
import java.util.Scanner;

public class Practice4 {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        String 단어1 = sc.nextLine();
        String 단어2 = sc.nextLine();

        if (단어1.equalsIgnoreCase(단어2)) {
            System.out.println("같은 단어입니다");
        } else {
            System.out.println("다른 단어입니다");
        }
    }
}
```

**설명**: `equalsIgnoreCase()`는 대소문자 차이를 무시하고 내용만 비교합니다. `"JAVA"`와 `"java"`는 대소문자만 다르므로 `true`가 됩니다.

**틀리기 쉬운 포인트**: `.equals()`를 쓰면 대소문자까지 정확히 같아야 `true`가 되므로, 이 문제처럼 대소문자를 무시해야 하는 상황에서는 반드시 `.equalsIgnoreCase()`를 사용해야 합니다.

---

### 문제 5. 배열 합계와 평균

```java
public class Practice5 {
    public static void main(String[] args) {
        int[] 점수들 = {85, 90, 78, 92, 88};
        int 합계 = 0;

        for (int i = 0; i < 점수들.length; i++) {
            합계 += 점수들[i];
        }

        double 평균 = (double) 합계 / 점수들.length; // 정수 나눗셈 방지 위해 캐스팅 (Day1 복습)

        System.out.println("합계: " + 합계);
        System.out.printf("평균: %.2f%n", 평균);
    }
}
```

**설명**: 반복문으로 배열의 모든 요소를 `합계`에 누적한 뒤, `점수들.length`(배열 요소 개수)로 나눠 평균을 구합니다. 정수 나눗셈을 피하기 위해 `(double)`로 캐스팅합니다.

**틀리기 쉬운 포인트**: `합계 / 점수들.length`처럼 캐스팅을 빠뜨리면 정수 나눗셈이 되어 소수점이 사라진 평균값(`86` 등)이 나옵니다.

---

## 도전

### 문제 6. 콤마로 구분된 문자열 분리 (split)

```java
public class Practice6 {
    public static void main(String[] args) {
        String csv = "사과,바나나,포도,딸기";
        String[] 과일들 = csv.split(",");

        int 번호 = 1;
        for (String 과일 : 과일들) {
            System.out.println(번호 + ". " + 과일);
            번호++;
        }
    }
}
```

**설명**: `split(",")`은 콤마를 기준으로 문자열을 잘라 배열로 반환합니다. 향상된 for문은 배열 요소만 순회할 뿐 인덱스나 순번을 제공하지 않으므로, 번호를 매기려면 별도의 `번호` 변수를 직접 증가시켜야 합니다.

**틀리기 쉬운 포인트**: 향상된 for문 안에는 인덱스 변수(`i` 같은)가 없기 때문에, 순번이 필요한 경우 이 예제처럼 카운터 변수를 별도로 선언하고 반복마다 증가시켜야 합니다. 만약 인덱스 자체가 자주 필요하다면 기존 `for`문이 더 적합할 수 있습니다.

---

### 문제 7. 종합 - 최댓값 찾기와 문자열 가공

```java
public class Practice7 {
    public static void main(String[] args) {
        int[] 점수들 = {72, 95, 63, 88, 91};
        int 최댓값 = 점수들[0]; // 첫 번째 값을 기준값으로 시작

        for (int i = 1; i < 점수들.length; i++) {
            if (점수들[i] > 최댓값) {
                최댓값 = 점수들[i]; // 더 큰 값을 발견하면 갱신
            }
        }

        System.out.println("최고점: " + 최댓값 + "점입니다");
    }
}
```

**설명**: 최댓값을 찾는 기본 패턴은 "첫 번째 값을 임시 최댓값으로 정해두고, 나머지를 하나씩 비교하며 더 큰 값이 나오면 교체"하는 방식입니다. 그래서 반복문을 인덱스 `1`부터 시작합니다(0번은 이미 초기값으로 사용했으므로).

**틀리기 쉬운 포인트**: `최댓값`의 초기값을 `0`처럼 임의의 값으로 정하면, 배열의 모든 값이 그보다 작은 경우(예: 모든 점수가 음수) 잘못된 결과가 나올 수 있습니다. 배열의 첫 번째 요소로 초기화하는 것이 안전한 습관입니다.
