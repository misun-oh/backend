# Day 14. Object 클래스 - 실습 답안

---

## 기본

### 문제 1. 기본 toString() 확인하기

```java
// Professor.java
public class Professor {
    private String name;
    private String professorId;

    public Professor(String name, String professorId) {
        this.name = name;
        this.professorId = professorId;
    }
}
```

```java
// Practice1.java
public class Practice1 {
    public static void main(String[] args) {
        Professor professor1 = new Professor("김교수", "P001");
        System.out.println(professor1);
        // 출력 결과 예: Professor@1b6d3586
        // 클래스명@해시코드 형태로 출력됨 (Object의 기본 toString() 동작)
        // 해시코드 부분은 실행할 때마다, 환경마다 다른 값이 나올 수 있음
    }
}
```

**설명**: `Professor` 클래스는 `toString()`을 따로 작성하지 않았으므로, `Object`로부터 물려받은 기본 `toString()`이 그대로 실행됩니다. 이 기본 형태는 "이 객체가 어떤 클래스인지, 메모리상 어디 있는지"를 나타낼 뿐, 사람이 읽기에는 의미가 없는 정보입니다.

**틀리기 쉬운 포인트**: 해시코드 값은 실행할 때마다 달라질 수 있으므로, 이 값 자체를 테스트 결과로 정확히 예측하거나 비교하려 하면 안 됩니다. "형태가 클래스명@해시코드다"라는 사실만 확인하면 충분합니다.

---

### 문제 2. toString() 오버라이딩하기

```java
// Professor.java
public class Professor {
    private String name;
    private String professorId;

    public Professor(String name, String professorId) {
        this.name = name;
        this.professorId = professorId;
    }

    @Override
    public String toString() {
        return "교수[이름=" + name + ", 교수번호=" + professorId + "]";
    }
}
```

```java
// Practice2.java
public class Practice2 {
    public static void main(String[] args) {
        Professor professor1 = new Professor("김교수", "P001");
        System.out.println(professor1);
    }
}
```

**설명**: `toString()`을 오버라이딩하면, `println(professor1)`을 호출할 때 우리가 정의한 문자열이 출력됩니다. `println`은 내부적으로 항상 객체의 `toString()`을 호출하도록 구현되어 있기 때문에, 별도로 `.toString()`을 명시하지 않아도 오버라이딩한 내용이 자동으로 반영됩니다.

**틀리기 쉬운 포인트**: `toString()`의 반환형은 반드시 `String`이어야 합니다. `void`나 다른 타입으로 작성하면 오버라이딩이 아니라 컴파일 에러가 발생합니다(Object의 원래 시그니처와 일치하지 않으므로).

---

## 응용

### 문제 3. 기본 equals()의 한계 확인

```java
// Department.java
public class Department {
    private String departmentName;

    public Department(String departmentName) {
        this.departmentName = departmentName;
    }
}
```

```java
// Practice3.java
public class Practice3 {
    public static void main(String[] args) {
        Department department1 = new Department("컴퓨터공학과");
        Department department2 = new Department("컴퓨터공학과"); // 내용은 같지만 다른 객체

        System.out.println(department1.equals(department2));
    }
}
```

**설명**: `department1`과 `department2`는 `departmentName`이 똑같지만 `new`로 각각 따로 만들어졌기 때문에 서로 다른 객체입니다. `equals()`를 재정의하지 않았으므로 `Object`의 기본 동작(주소 비교)이 그대로 실행되어 `false`가 나옵니다.

**틀리기 쉬운 포인트**: "내용이 같으니까 당연히 true겠지"라고 생각하기 쉽지만, 자바의 기본 `equals()`는 절대 내용을 비교하지 않습니다. 내용 비교가 필요하면 반드시 직접 오버라이딩해야 합니다.

---

### 문제 4. equals() 오버라이딩하기

```java
// Department.java
public class Department {
    private String departmentName;

    public Department(String departmentName) {
        this.departmentName = departmentName;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Department)) return false;

        Department other = (Department) obj;
        return this.departmentName.equals(other.departmentName); // String의 equals()를 이용해 내용 비교 (Day4~5 복습)
    }
}
```

```java
// Practice4.java
public class Practice4 {
    public static void main(String[] args) {
        Department department1 = new Department("컴퓨터공학과");
        Department department2 = new Department("컴퓨터공학과");

        System.out.println(department1.equals(department2));
    }
}
```

**설명**: `this.departmentName.equals(other.departmentName)`에서 `.equals()`는 `String`의 `equals()`입니다(Day5에서 배운, 문자열 내용 비교). 이렇게 `Department`의 `equals()` 안에서 필드(`String` 타입인 `departmentName`)의 `equals()`를 활용해 내용을 비교하는 것이 일반적인 패턴입니다.

**틀리기 쉬운 포인트**: `this.departmentName == other.departmentName`처럼 `==`을 쓰면 다시 참조 비교로 돌아가 버립니다(Day4 복습). 문자열 필드를 비교할 때는 반드시 `.equals()`를 사용해야 합니다.

---

## 도전

### 문제 5. equals()와 hashCode() 함께 재정의

```java
// Department.java
public class Department {
    private String departmentName;

    public Department(String departmentName) {
        this.departmentName = departmentName;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Department)) return false;

        Department other = (Department) obj;
        return this.departmentName.equals(other.departmentName);
    }

    @Override
    public int hashCode() {
        return departmentName.hashCode(); // equals()가 비교에 사용한 필드와 동일한 필드로 hashCode 생성
    }
}
```

```java
// Practice5.java
public class Practice5 {
    public static void main(String[] args) {
        Department department1 = new Department("컴퓨터공학과");
        Department department2 = new Department("컴퓨터공학과");

        boolean sameHash = department1.hashCode() == department2.hashCode();
        System.out.println("hashCode가 같은가? " + sameHash);
    }
}
```

**설명**: `hashCode()`를 `equals()`와 동일한 필드(`departmentName`)를 기준으로 만들었기 때문에, 내용이 같은 두 객체는 `hashCode()`도 항상 같은 값을 반환합니다. `String`의 `hashCode()`는 문자열 내용을 기준으로 일정한 정수를 계산해주므로, 이를 그대로 활용했습니다.

**틀리기 쉬운 포인트**: 만약 `hashCode()`를 재정의하지 않고 `Object`의 기본 동작(객체 주소 기반)을 그대로 뒀다면, `department1`과 `department2`는 `equals()`로는 같다고 판단되지만 `hashCode()`는 서로 다른 값이 나왔을 것입니다. 이는 "equals가 true면 hashCode도 같아야 한다"는 규칙을 위반하는 상태입니다.

---

### 문제 6. 종합 - 학생 클래스에 toString, equals, hashCode 모두 적용

```java
// Student.java
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

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Student)) return false;

        Student other = (Student) obj;
        return this.studentId.equals(other.studentId); // 학번만 기준으로 같음을 판단 (이름은 비교하지 않음)
    }

    @Override
    public int hashCode() {
        return studentId.hashCode();
    }
}
```

```java
// Practice6.java
public class Practice6 {
    public static void main(String[] args) {
        Student student1 = new Student("홍길동", "S001");
        Student student2 = new Student("홍길똥", "S001"); // 이름은 다르지만 학번은 같음

        System.out.println(student1);
        System.out.println(student2);
        System.out.println("같은 학생인가? " + student1.equals(student2));
    }
}
```

**설명**: `equals()`를 "학번만 같으면 같은 학생"으로 정의했기 때문에, 이름이 다르게 입력된 `student2`라도 `studentId`이 `student1`과 같으므로 `equals()`는 `true`를 반환합니다. 이는 현실적으로도 타당한 설계입니다 — 학번은 학생을 고유하게 식별하는 값이므로, 설령 이름이 오타로 잘못 입력되었더라도 "같은 학생"으로 판단하는 것이 자연스럽습니다.

**틀리기 쉬운 포인트**: `equals()`에서 어떤 필드를 비교 기준으로 삼을지는 클래스 설계자가 직접 정하는 것입니다. 이 문제처럼 "학번만 비교"할 수도 있고, "이름과 학번을 모두 비교"하도록 만들 수도 있습니다. 어떤 필드가 객체를 구별하는 데 실제로 의미 있는지(학번처럼 고유한 값인지, 이름처럼 중복될 수 있는 값인지)를 고려해서 결정해야 합니다.
