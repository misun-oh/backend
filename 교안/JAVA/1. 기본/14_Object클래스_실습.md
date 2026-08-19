# Day 14. Object 클래스 - 실습

> 아래 문제는 교안에서 다룬 toString(), equals(), hashCode() 오버라이딩만으로 풀 수 있습니다.

---

## 기본

### 문제 1. 기본 toString() 확인하기
`Professor` 클래스(필드: `name`, `professorId`)를 만들고, `toString()`을 오버라이딩하지 않은 상태로 객체를 `println`으로 출력해보세요. 어떤 형태로 출력되는지 확인하고 주석으로 남기세요.

**정답 예시(주석 형태)**
```java
// 출력 결과 예: Professor@1b6d3586
// 클래스명@해시코드 형태로 출력됨 (Object의 기본 toString() 동작)
```

---

### 문제 2. toString() 오버라이딩하기
문제 1의 `Professor` 클래스에 `toString()`을 오버라이딩해서 "교수[이름=OOO, 교수번호=OOO]" 형태로 출력되게 만드세요.

**Output**
```
교수[이름=김교수, 교수번호=P001]
```

---

## 응용

### 문제 3. 기본 equals()의 한계 확인
`Department` 클래스(필드: `departmentName`)를 만들고, `toString()`은 오버라이딩하지 않은 상태에서, 내용이 같은 두 `Department` 객체를 만들어 `equals()`로 비교해보세요.

**Output**
```
false
```

---

### 문제 4. equals() 오버라이딩하기
문제 3의 `Department` 클래스에 `equals()`를 오버라이딩해서 `departmentName`이 같으면 같은 학과로 판단하도록 만드세요. 다시 두 객체를 비교해보세요.

**Output**
```
true
```

---

## 도전

### 문제 5. equals()와 hashCode() 함께 재정의
문제 4의 `Department` 클래스에 `hashCode()`도 `departmentName` 기준으로 재정의하세요. 내용이 같은 두 객체의 `hashCode()` 값이 서로 같은지 확인해서 출력하세요.

**Output**
```
hashCode가 같은가? true
```

---

### 문제 6. 종합 - 학생 클래스에 toString, equals, hashCode 모두 적용
`Student` 클래스(필드: `name`, `studentId`)에 `toString()`(형식: "학생[이름=OOO, 학번=OOO]"), `equals()`(학번 기준), `hashCode()`(학번 기준)를 모두 재정의하세요. 학생 객체 2개(같은 학번, 다른 이름 실수로 입력됐다고 가정)를 만들어 `println`으로 각각 출력하고, `equals()`로 비교한 결과도 출력하세요.

**Output**
```
학생[이름=홍길동, 학번=S001]
학생[이름=홍길똥, 학번=S001]
같은 학생인가? true
```
