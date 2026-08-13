# Day 5. String과 배열 기초

> 참고 교재: 이것이 자바다 CHAPTER 5(참조 타입) 일부 범위
> 이전 챕터: Day 4에서 참조형과 null, ==/equals를 배웠습니다.

## 학습목표
- String의 불변성(immutable) 개념을 설명할 수 있다
- 자주 쓰는 String 메서드를 활용해 문자열을 다룰 수 있다
- 1차원 배열을 선언·초기화하고 향상된 for문으로 순회할 수 있다

---

## 1. String의 불변성 (Immutable)

`String` 객체는 한 번 생성되면 **내용을 바꿀 수 없습니다**. `+=`처럼 문자열을 바꾸는 것처럼 보이는 코드도, 실제로는 새로운 String 객체를 만들어 변수가 그 객체를 다시 가리키게 되는 것입니다.

```java
public class 불변성예제 {
    public static void main(String[] args) {
        String 이름 = "홍길동";
        이름 = 이름 + "님"; // "홍길동"이 바뀌는 게 아니라, "홍길동님"이라는 새 객체가 생성됨

        System.out.println(이름); // 홍길동님
    }
}
```

**설명**: 기존 `"홍길동"` 문자열 객체는 그대로 있고, `이름` 변수가 새로 만들어진 `"홍길동님"` 객체를 가리키도록 바뀐 것입니다. 이 특성 때문에 문자열을 반복문 안에서 많이 연결하면 매번 새 객체가 생성되어 비효율적일 수 있습니다(이 문제를 해결하는 `StringBuilder`는 이후 심화 과정에서 다룹니다).

---

## 2. 자주 쓰는 String 메서드

| 메서드 | 설명 | 예시 |
|---|---|---|
| `length()` | 문자열의 길이(글자 수) 반환 | `"hello".length()` → `5` |
| `charAt(idx)` | 특정 위치의 문자 하나 반환 (0부터 시작) | `"hello".charAt(1)` → `'e'` |
| `substring(시작)` | 시작 위치부터 끝까지 잘라냄 | `"hello".substring(2)` → `"llo"` |
| `substring(시작,끝)` | 시작~끝-1 위치까지 잘라냄 | `"hello".substring(1,3)` → `"el"` |
| `indexOf(문자열)` | 특정 문자열이 처음 나오는 위치 (없으면 -1) | `"hello".indexOf("l")` → `2` |
| `replace(old,new)` | old를 new로 모두 교체 | `"hello".replace("l","L")` → `"heLLo"` |
| `split(구분자)` | 구분자로 문자열을 잘라 배열로 반환 | `"a,b,c".split(",")` → `["a","b","c"]` |
| `trim()` | 앞뒤 공백 제거 | `"  hi  ".trim()` → `"hi"` |
| `equals(대상)` | 내용이 같은지 비교 (Day4 복습) | `"a".equals("a")` → `true` |
| `equalsIgnoreCase(대상)` | 대소문자 무시하고 비교 | `"A".equalsIgnoreCase("a")` → `true` |
| `toUpperCase()` | 모두 대문자로 변환 | `"abc".toUpperCase()` → `"ABC"` |
| `toLowerCase()` | 모두 소문자로 변환 | `"ABC".toLowerCase()` → `"abc"` |

```java
public class String메서드예제 {
    public static void main(String[] args) {
        String 문장 = "  Hello Java World  ";

        System.out.println(문장.length());              // 20 (앞뒤 공백 포함)
        System.out.println(문장.trim());                 // "Hello Java World" (앞뒤 공백 제거)
        System.out.println(문장.trim().charAt(0));       // 'H' (0번째 글자)
        System.out.println(문장.trim().substring(6));    // "Java World"
        System.out.println(문장.trim().substring(0, 5)); // "Hello"
        System.out.println(문장.trim().indexOf("Java")); // 6 (Java가 시작하는 위치)
        System.out.println(문장.trim().replace("Java", "Python")); // Hello Python World
        System.out.println(문장.trim().toUpperCase());   // HELLO JAVA WORLD

        String csv = "사과,바나나,포도";
        String[] 과일들 = csv.split(","); // ["사과", "바나나", "포도"]로 나눔
        System.out.println(과일들[0]);    // 사과
        System.out.println(과일들.length); // 3

        System.out.println("Java".equals("java"));            // false (대소문자 다름)
        System.out.println("Java".equalsIgnoreCase("java"));   // true (대소문자 무시)
    }
}
```

**설명**: 메서드들을 `.`으로 이어서 연속 호출(`문장.trim().charAt(0)`)할 수 있습니다. 앞의 메서드가 반환한 결과에 다시 메서드를 호출하는 방식입니다. `split()`의 결과는 배열이라는 점도 눈여겨보세요 — 다음 항목에서 배열을 자세히 다룹니다.

---

## 3. 1차원 배열 선언과 초기화

같은 자료형의 값 여러 개를 하나의 변수로 관리할 때 배열을 사용합니다.

```java
public class 배열선언예제 {
    public static void main(String[] args) {
        // 방법 1: 크기를 지정하고 나중에 값 대입
        int[] 점수 = new int[3];
        점수[0] = 90;
        점수[1] = 85;
        점수[2] = 78;

        // 방법 2: 선언과 동시에 값 초기화
        int[] 나이들 = {20, 21, 22, 23};

        // 방법 3: 문자열 배열
        String[] 이름들 = {"홍길동", "김철수", "이영희"};

        System.out.println(점수[0]);      // 90 (인덱스는 0부터 시작)
        System.out.println(나이들.length); // 4 (배열의 길이)
        System.out.println(이름들[2]);     // 이영희
    }
}
```

**설명**: 배열의 인덱스(위치 번호)는 항상 `0`부터 시작합니다. 크기가 `n`인 배열의 유효한 인덱스는 `0`부터 `n-1`까지입니다. `배열이름.length`(괄호 없음)로 배열의 전체 길이를 알 수 있습니다.

---

## 4. 향상된 for문 (for-each)

배열의 모든 요소를 처음부터 끝까지 순회할 때, 인덱스 없이 더 간단하게 쓸 수 있는 반복문입니다.

```java
public class 향상된for예제 {
    public static void main(String[] args) {
        int[] 점수들 = {90, 85, 78, 92};

        // 기존 for문
        for (int i = 0; i < 점수들.length; i++) {
            System.out.println(점수들[i]);
        }

        // 향상된 for문 (for-each): 인덱스 없이 값만 바로 꺼내옴
        for (int 점수 : 점수들) {
            System.out.println(점수);
        }
    }
}
```

**구조**: `for (자료형 변수명 : 배열) { ... }` — 배열의 각 요소를 순서대로 변수에 담아 반복합니다.

**주의**: 향상된 for문은 인덱스를 알 수 없고, 배열의 값을 읽기만 할 뿐 원본 배열의 값을 바꾸는 용도로는 적합하지 않습니다. 인덱스가 필요하거나 값을 수정해야 하면 기존 `for`문을 사용해야 합니다.

---

## 자주 하는 실수

1. **배열 인덱스 범위 초과 (ArrayIndexOutOfBoundsException)**
   ```java
   int[] 배열 = new int[3]; // 유효 인덱스: 0, 1, 2
   배열[3] = 10; // 실행 에러! 인덱스 3은 존재하지 않음
   ```

2. **substring의 끝 인덱스는 포함되지 않음**
   ```java
   "hello".substring(1, 3); // "el" (인덱스 1, 2만 포함, 3은 미포함)
   ```

3. **String이 불변이라는 것을 잊고 원본이 바뀔 거라 착각**
   ```java
   String s = "hello";
   s.toUpperCase();          // 반환값을 사용하지 않으면 아무 효과 없음
   System.out.println(s);    // 여전히 "hello"
   s = s.toUpperCase();      // 반환값을 다시 대입해야 반영됨
   ```

4. **`length`(배열)와 `length()`(문자열/메서드) 혼동**
   ```java
   int[] arr = {1,2,3};
   arr.length();      // 컴파일 에러! 배열은 괄호 없는 필드
   arr.length;         // 올바른 표현

   String s = "hi";
   s.length;            // 컴파일 에러! 문자열은 메서드이므로 괄호 필요
   s.length();          // 올바른 표현
   ```

---

## 핵심 요약

| 항목 | 핵심 내용 |
|---|---|
| String 불변성 | 문자열은 한 번 생성되면 내용이 바뀌지 않고, 변경 시 새 객체가 생성됨 |
| 자주 쓰는 String 메서드 | length, charAt, substring, indexOf, replace, split, trim, equals, equalsIgnoreCase, toUpperCase, toLowerCase |
| 배열 선언 | `자료형[] 변수명 = new 자료형[크기];` 또는 `{값1, 값2, ...}` |
| 배열 인덱스 | 0부터 시작, `배열.length`로 길이 확인(괄호 없음) |
| 향상된 for문 | `for (자료형 변수 : 배열) { }` — 인덱스 없이 값만 순회, 읽기 전용 용도 |
