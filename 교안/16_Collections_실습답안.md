# Day 16. 컬렉션 프레임워크 (ArrayList, Set, HashMap) - 실습 답안

---

## 기본

### 문제 1. 배열의 불편함 재현하기

```java
public class Practice1 {
    public static void main(String[] args) {
        Student[] students = new Student[3];
        students[0] = new Student("홍길동", "S001");
        students[1] = new Student("김철수", "S002");
        students[2] = new Student("이영희", "S003");

        // students[3] = new Student("정수아", "S004");
        // 실행 에러 발생: ArrayIndexOutOfBoundsException
        // 이유: 배열의 크기가 3으로 고정되어 있어 인덱스 3이 존재하지 않음
    }
}
```

**설명**: 배열은 `new Student[3]`으로 만드는 순간 "딱 3자리"로 고정됩니다. 이 한계가 컬렉션 프레임워크(List/Set/Map)가 필요한 이유입니다.

**틀리기 쉬운 포인트**: 이 에러는 컴파일 시점이 아니라 **실행 시점**에 발생합니다.

---

### 문제 2. ArrayList로 학생 5명 담기

```java
import java.util.List;
import java.util.ArrayList;

public class Practice2 {
    public static void main(String[] args) {
        List<Student> students = new ArrayList<>();

        students.add(new Student("홍길동", "S001"));
        students.add(new Student("김철수", "S002"));
        students.add(new Student("이영희", "S003"));
        students.add(new Student("박민수", "S004"));
        students.add(new Student("최지우", "S005"));

        System.out.println("전체 학생 수: " + students.size());
    }
}
```

**설명**: `new Student[5]`처럼 크기를 미리 정할 필요 없이, `add()`를 호출할 때마다 리스트가 알아서 하나씩 늘어납니다.

**틀리기 쉬운 포인트**: `List<Student> students = new List<>();`처럼 인터페이스를 직접 생성하려 하면 컴파일 에러가 납니다.

---

### 문제 3. HashSet으로 중복 신청 방지

```java
import java.util.Set;
import java.util.HashSet;

public class Practice3 {
    public static void main(String[] args) {
        Set<String> registeredIds = new HashSet<>();

        System.out.println("S001 추가됨? " + registeredIds.add("S001"));
        System.out.println("S002 추가됨? " + registeredIds.add("S002"));
        System.out.println("S001 추가됨? " + registeredIds.add("S001"));

        System.out.println("최종 신청 인원: " + registeredIds.size());
    }
}
```

**설명**: `add()`는 이미 있는 값을 다시 넣으려 하면 아무 일도 하지 않고 `false`를 반환합니다. 이 반환값을 그대로 출력하면 "추가되었는지 아닌지"를 바로 알 수 있습니다.

**틀리기 쉬운 포인트**: `List`의 `add()`는 항상 값을 추가하고 `true`를 반환하지만, `Set`의 `add()`는 중복일 때만 다르게 동작합니다. 같은 메서드 이름이라도 구현체(정확히는 인터페이스 계열)에 따라 의미가 다를 수 있다는 점에 주의해야 합니다.

---

## 응용

### 문제 4. ArrayList에서 추가/삭제

```java
import java.util.List;
import java.util.ArrayList;

public class Practice4 {
    public static void main(String[] args) {
        List<Student> students = new ArrayList<>();
        students.add(new Student("홍길동", "S001"));
        students.add(new Student("김철수", "S002"));
        students.add(new Student("이영희", "S003"));
        students.add(new Student("박민수", "S004"));
        students.add(new Student("최지우", "S005"));

        students.add(new Student("정수아", "S006"));
        students.remove(2); // 인덱스 2(이영희) 삭제

        System.out.println("최종 학생 수: " + students.size());
        for (Student Student : students) {
            System.out.println(Student.getName());
        }
    }
}
```

**설명**: `remove(2)`를 호출하면 인덱스 2번(이영희)이 삭제되고, 뒤에 있던 요소들이 자동으로 한 칸씩 앞으로 당겨집니다.

**틀리기 쉬운 포인트**: `remove(2)`는 "값이 2인 요소"가 아니라 "인덱스 2번 위치의 요소"를 삭제한다는 뜻입니다.

---

### 문제 5. HashMap으로 학번 조회

```java
import java.util.Map;
import java.util.HashMap;

public class Practice5 {
    public static void main(String[] args) {
        Map<String, Student> studentMap = new HashMap<>();
        studentMap.put("S001", new Student("홍길동", "S001"));
        studentMap.put("S002", new Student("김철수", "S002"));
        studentMap.put("S003", new Student("이영희", "S003"));

        System.out.println("S002 학생 이름: " + studentMap.get("S002").getName());
        System.out.println("S999가 존재하는가? " + studentMap.containsKey("S999"));
    }
}
```

**설명**: `get("S002")`는 List처럼 반복문으로 하나씩 비교할 필요 없이, 키만 알면 즉시 해당 값을 찾아줍니다. `containsKey()`는 그 키가 Map에 존재하는지만 확인하고 boolean을 반환합니다.

**틀리기 쉬운 포인트**: `containsKey()`와 `contains()`(List/Set에서 쓰는)를 혼동하기 쉽습니다. `Map`은 키를 확인할 때 `containsKey()`, 값을 확인할 때는 `containsValue()`를 따로 사용합니다.

---

### 문제 6. HashMap 전체 순회

```java
import java.util.Map;
import java.util.HashMap;

public class Practice6 {
    public static void main(String[] args) {
        Map<String, Student> studentMap = new HashMap<>();
        studentMap.put("S001", new Student("홍길동", "S001"));
        studentMap.put("S002", new Student("김철수", "S002"));
        studentMap.put("S003", new Student("이영희", "S003"));

        for (String studentId : studentMap.keySet()) {
            System.out.println(studentId + " - " + studentMap.get(studentId).getName());
        }
    }
}
```

**설명**: `studentMap.keySet()`은 Map에 담긴 모든 키를 `Set<String>`으로 꺼내줍니다. 이 키들을 향상된 for문으로 순회하면서, 각 키로 `get()`을 호출해 값을 찾는 것이 Map을 순회하는 표준 방식입니다.

**틀리기 쉬운 포인트**: `for (String studentId : studentMap)`처럼 Map을 직접 순회하려 하면 컴파일 에러가 납니다. Map은 Collection이 아니므로(교안 3번 다이어그램 복습) 반드시 `keySet()`(또는 `values()`, `entrySet()`)을 거쳐야 합니다.

---

## 도전

### 문제 7. Set으로 학과 목록 중복 제거

```java
import java.util.Set;
import java.util.HashSet;

public class Practice7 {
    public static void main(String[] args) {
        String[] departmentArray = {"컴퓨터공학과", "경영학과", "컴퓨터공학과", "전자공학과", "경영학과"};
        Set<String> uniqueDepartments = new HashSet<>();

        for (String Department : departmentArray) {
            uniqueDepartments.add(Department); // 이미 있으면 자동으로 무시됨
        }

        System.out.println("서로 다른 학과 수: " + uniqueDepartments.size());
        for (String Department : uniqueDepartments) {
            System.out.println(Department);
        }
    }
}
```

**설명**: 배열에는 "컴퓨터공학과"와 "경영학과"가 각각 두 번씩 등장하지만, `Set`에 넣으면 중복이 자동으로 걸러져 서로 다른 값 3개만 남습니다. 이것이 "중복 제거가 필요할 때 Set을 쓰는" 대표적인 활용 사례입니다.

**틀리기 쉬운 포인트**: `Set`은 순서를 보장하지 않으므로, 출력되는 순서가 배열에 넣은 순서와 다를 수 있습니다. 순서가 중요한 데이터라면 `Set`이 아니라 `List`(중복 허용) 또는 `LinkedHashSet`(순서를 유지하는 Set, 이번 과정 범위 밖)을 고려해야 합니다.

---

### 문제 8. Map의 값 덮어쓰기 확인

```java
import java.util.Map;
import java.util.HashMap;

public class Practice8 {
    public static void main(String[] args) {
        Map<String, Student> studentMap = new HashMap<>();
        studentMap.put("S001", new Student("홍길동", "S001"));
        studentMap.put("S001", new Student("홍길똥", "S001")); // 같은 키로 다시 put

        System.out.println("Map 크기: " + studentMap.size());
        System.out.println("S001 조회 결과: " + studentMap.get("S001").getName());
    }
}
```

**설명**: 같은 키("S001")로 두 번 `put()`하면, 첫 번째 값(홍길동)은 사라지고 두 번째 값(홍길똥)으로 덮어씌워집니다. Map은 "키는 유일해야 한다"는 규칙을 갖고 있어서, 같은 키에 새 값을 넣는 것은 "추가"가 아니라 "교체"로 취급됩니다.

**틀리기 쉬운 포인트**: `List`의 `add()`는 같은 값을 여러 번 넣으면 그만큼 개수가 늘어나지만, `Map`의 `put()`은 키가 같으면 개수가 늘지 않고 값만 바뀝니다. 이 둘의 동작 차이를 정확히 구분해야 합니다.

---

### 문제 9. 종합 - List, Set, Map 함께 사용하기

```java
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;

public class Practice9 {
    public static void main(String[] args) {
        List<Student> studentList = new ArrayList<>();
        Set<String> studentIdSet = new HashSet<>();
        Map<String, Student> studentIdMap = new HashMap<>();

        String[][] data = {
            {"홍길동", "S001"}, {"김철수", "S002"}, {"이영희", "S003"},
            {"박민수", "S004"}, {"최지우", "S005"}
        };

        for (String[] row : data) {
            Student newStudent = new Student(row[0], row[1]);
            studentList.add(newStudent);       // List: 순서대로 전체 명단 관리
            studentIdSet.add(row[1]);        // Set: 중복 학번 체크용
            studentIdMap.put(row[1], newStudent);  // Map: 학번으로 즉시 조회용
        }

        System.out.println("[전체 명단]");
        for (Student Student : studentList) {
            System.out.println(Student.getName());
        }

        System.out.println("[학번으로 즉시 조회] S003: " + studentIdMap.get("S003").getName());
    }
}
```

**설명**: 이 문제는 같은 데이터를 세 가지 컬렉션에 동시에 저장해서, 각자의 강점을 활용하는 실무 패턴을 보여줍니다. "전체 명단을 순서대로 보여줘야 할 때"는 `List`, "이 학번이 이미 존재하는지 빠르게 확인해야 할 때"는 `Set`, "이 학번의 학생 정보를 즉시 찾아야 할 때"는 `Map`을 사용합니다. 하나의 컬렉션만으로는 세 가지 요구사항을 모두 효율적으로 만족시키기 어렵기 때문에, 실무에서는 이렇게 여러 컬렉션을 목적에 맞게 함께 쓰는 경우가 흔합니다.

**틀리기 쉬운 포인트**: 세 컬렉션에 각각 `new Student(...)`을 따로 만들어 넣으면, 겉보기엔 같은 데이터 같아도 서로 다른 객체가 되어버려 나중에 한쪽 데이터를 수정해도 다른 쪽에는 반영되지 않습니다. 이 답안처럼 `newStudent`이라는 변수에 객체를 한 번만 만들어서, 그 **같은 참조**를 세 컬렉션에 나눠 넣는 것이 올바른 방식입니다(Day4에서 배운 참조형의 특성이 그대로 적용됩니다).
