# 부록 1. JPA 소개와 엔티티 매핑

| 항목 | 내용 |
|---|---|
| 선수학습 | Day 9(MyBatis CRUD·연관 매핑), Day 13(트랜잭션), Day 14(AOP) |
| 이번 챕터 | MyBatis 복습 → ORM 개념 → JPA/Hibernate란 → 의존성·설정 → `@Entity`로 EMP/DEPT 매핑 → `@ManyToOne`/`@OneToMany` 연관관계 → `JpaRepository` |
| 권장 진행 | 1일 (JPA 부록 1챕터 — 이 뒤로 영속성 컨텍스트·연관관계 심화·연관 쿼리 실습이 이어질 수 있음) |
| DB | 기존과 동일한 HR 스키마(`EMP`, `DEPT`). **단, 이 챕터의 예제는 별도 실습 프로젝트/패키지에서 진행**(3절 참고) |

> 이 챕터는 18일 본 과정과 별도로 편성된 **부록**의 첫 번째 챕터입니다(부록 2는 JWT 인증,
> 3~4는 Spring Security). Day 1~18 본 과정에서는 영속성 계층을 의도적으로 MyBatis로만
> 다뤘습니다(`00_커리큘럼개요.md` 참고). 여기서는 같은 EMP/DEPT 테이블을 **JPA로 다시**
> 다뤄보면서 MyBatis와 무엇이 다른지 비교합니다.

## 학습목표

- MyBatis(SQL을 직접 쓰는 방식)와 JPA(객체-테이블 매핑을 프레임워크에 맡기는 방식)의 차이를 설명한다.
- ORM이 무엇이고, 왜 등장했는지, "영속성 컨텍스트"가 대략 무엇을 하는 개념인지 안다.
- `spring-boot-starter-data-jpa` 를 추가하고 `application.yml`의 JPA 설정 항목을 이해한다.
- `@Entity`/`@Id`/`@GeneratedValue`/`@Column` 으로 `EMP`/`DEPT` 테이블을 엔티티 클래스로 매핑한다.
- `@ManyToOne`/`@OneToMany` 로 사원↔부서 연관관계를 맺고, 지연 로딩(LAZY)을 기본으로 쓴다.
- `JpaRepository` 인터페이스 하나로 기본 CRUD가 되는 것을 확인하고, Day 9의 MyBatis 코드와 나란히 비교한다.

---

## 1. 왜 JPA인가 — MyBatis를 다시 보며

Day 7~9에서 사원 목록 조회 하나를 만들려고 이렇게 했습니다.

```java
// mapper/EmpMapper.java
@Mapper
public interface EmpMapper {
    List<Emp> findAll();
    Emp findById(Long empId);
}
```

```xml
<select id="findAll" resultType="Emp">
  SELECT EMP_ID, EMP_NAME, EMAIL, DEPT_ID, SALARY, HIRE_DATE FROM EMP ORDER BY EMP_ID
</select>
```

테이블이 20개면 이런 인터페이스+XML 쌍이 20개, `INSERT`/`UPDATE`/`DELETE`까지 합치면
훨씬 더 많아집니다. **테이블 하나당 반복되는 뻔한 SQL**(단순 조회·단건 등록·단건 수정·삭제)이
누적되는 것이 MyBatis의 현실적인 단점입니다. (반대로 복잡한 조인·집계·튜닝은 MyBatis가 더 쉽습니다 — 6절에서 비교)

**ORM(Object-Relational Mapping)** 은 "테이블 = 클래스, 컬럼 = 필드, 행 = 객체"로
자바 객체와 관계형 테이블을 자동으로 연결해주는 기술입니다. 자바 진영의 ORM 표준
**명세**가 **JPA**(Jakarta Persistence API)이고, 그 명세를 실제로 구현한 라이브러리가
**Hibernate**입니다. 스프링 부트에서 `spring-boot-starter-data-jpa`를 추가하면
내부적으로 Hibernate가 JPA 구현체로 동작합니다.

```java
// JPA로 같은 조회
List<Emp> emps = empRepository.findAll();   // SQL을 직접 안 씀
```

`findAll()` 하나가 이미 구현되어 있습니다. `INSERT`/`UPDATE`/`DELETE`도 마찬가지입니다.
"SQL을 안 쓰고 객체를 다루듯 DB를 다룬다"가 JPA의 핵심 아이디어입니다.

---

## 2. 의존성과 설정

`build.gradle`:
```groovy
implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
runtimeOnly   'com.mysql:mysql-connector-j'
```

`spring-boot-starter-data-jpa` 안에 Hibernate(ORM 구현체) + Spring Data JPA(Repository 추상화) +
`spring-boot-starter-jdbc`가 함께 들어옵니다. Boot 4.0.x 기준 **Hibernate ORM 7.x / Jakarta
Persistence 3.2**가 딸려 옵니다 — 애노테이션 패키지는 `jakarta.persistence.*` (Boot 3부터 동일, Boot4에서 추가 변경 없음).

`application.yml`:
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/hr?serverTimezone=Asia/Seoul&characterEncoding=UTF-8
    username: hr
    password: hr1234
  jpa:
    hibernate:
      ddl-auto: validate        # 운영 습관을 처음부터: 아래 표 참고
    show-sql: true              # 콘솔에 실행 SQL 출력
    properties:
      hibernate:
        format_sql: true        # SQL을 보기 좋게 줄바꿈
    open-in-view: false         # 3절 실습 전에는 기본값(true) 그대로 둬도 무방, 심화에서 설명
```

| `ddl-auto` 값 | 동작 | 언제 쓰나 |
|---|---|---|
| `create` | 시작할 때 테이블 **삭제 후 재생성** | 학습용 임시 DB에서만 |
| `create-drop` | `create` + 종료 시 삭제 | 테스트 |
| `update` | 엔티티 기준으로 없는 컬럼/테이블만 추가 | 개인 연습용(운영 금지) |
| `validate` | 엔티티와 테이블이 **일치하는지 검사만**, 안 맞으면 기동 실패 | **실무 기본값** |
| `none` | 아무것도 안 함 | 마이그레이션 도구(Flyway 등)를 따로 쓸 때 |

> **주의**: `create`/`update`는 편해 보이지만, 실무 DB에 잘못 붙이면 컬럼이 통째로
> 사라지거나 바뀔 수 있습니다. 이 챕터의 실습 DB는 반드시 **학습용으로 새로 만든 스키마**를
> 쓰고, 기존 HR 데이터가 있는 스키마에는 `ddl-auto: validate`나 `none`만 씁니다.

---

## 3. 실습 프로젝트를 분리하는 이유

Day 5~18에서 이미 `com.example.hr.domain.Emp`, `com.example.hr.domain.Dept` 클래스를
MyBatis용으로 만들어 뒀습니다. 이 챕터의 `Emp`/`Dept`는 **JPA 애노테이션이 붙은 별개의
클래스**라서 같은 프로젝트·같은 패키지에 두면 클래스 이름이 충돌합니다.

두 가지 방법 중 하나를 고릅니다.

- **방법 A(권장, 이 챕터가 가정)**: [start.spring.io](https://start.spring.io) 로 학습용 새 프로젝트를
  하나 더 만든다(의존성: Web, Data JPA, MySQL Driver, Lombok). MyBatis 프로젝트를 건드리지 않고
  JPA만 독립적으로 연습.
- **방법 B**: 기존 hr 프로젝트에 이어서 하고 싶다면 패키지를 `com.example.hr.jpa.domain` 처럼
  분리해서 MyBatis 버전(`com.example.hr.domain`)과 나란히 둔다. (미니 프로젝트에는 둘을 섞지 않음)

---

## 4. `@Entity` 로 테이블 매핑

먼저 연관관계가 없는 `Dept` 부터.

```java
package com.example.hr.jpa.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "DEPT")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Dept {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)   // AUTO_INCREMENT
    @Column(name = "DEPT_ID")
    private Long deptId;

    @Column(name = "DEPT_TITLE", nullable = false, length = 35)
    private String deptName;

    @Column(name = "LOCATION_ID", length = 2)
    private String location;
}
```

| 애노테이션 | 의미 |
|---|---|
| `@Entity` | 이 클래스는 JPA가 관리하는 엔티티 — 테이블과 매핑된다 |
| `@Table(name=...)` | 매핑할 테이블 이름. 클래스명과 테이블명이 같으면 생략 가능 |
| `@Id` | 기본 키(PK) 필드 |
| `@GeneratedValue(strategy = IDENTITY)` | DB의 `AUTO_INCREMENT`(MySQL)에 키 생성을 위임 |
| `@Column(name=..., nullable=..., length=...)` | 컬럼명·제약조건. 필드명과 컬럼명이 같으면 `name` 생략 가능 |

이제 `Emp`. Day 5~9에서 쓰던 필드(`empId`, `empName`, `email`, `deptId`, `salary`,
`hireDate`, `active`)를 그대로 가져오되, `deptId`(Long) 대신 **연관관계 매핑**을 씁니다(5절).

```java
package com.example.hr.jpa.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "EMP")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Emp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "EMP_ID")
    private Long empId;

    @Column(name = "EMP_NAME", nullable = false, length = 20)
    private String empName;

    @Column(name = "EMAIL", length = 25)
    private String email;

    @Column(name = "SALARY")
    private int salary;

    @Column(name = "HIRE_DATE")
    private LocalDate hireDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DEPT_ID")
    private Dept dept;
}
```

`ENT_YN`(`'Y'`/`'N'`) 처럼 boolean이 아닌 컬럼을 boolean 필드로 받고 싶을 때는 변환기가
필요합니다 — 심화(`@Converter`)에서 다룹니다. 이 챕터에서는 컬럼 그대로 두거나 생략해도 됩니다.

---

## 5. 연관관계 매핑 — `@ManyToOne`/`@OneToMany`

Day 9에서 MyBatis는 이렇게 했습니다.

```xml
<!-- MyBatis: 조인 결과를 SQL에서 직접 조립 -->
<resultMap id="empWithDeptMap" type="Emp">
  <id property="empId" column="EMP_ID"/>
  <association property="dept" javaType="Dept">
    <id property="deptId" column="D_DEPT_ID"/>
    <result property="deptName" column="DEPT_TITLE"/>
  </association>
</resultMap>
```

JPA는 클래스에 **관계 자체를 선언**해두면, 이후 조회할 때 필요한 SQL(조인 또는 추가 쿼리)을
Hibernate가 대신 만들어 줍니다.

```java
// Emp(N) → Dept(1) : 사원 여러 명이 부서 하나에 속한다
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "DEPT_ID")   // EMP 테이블의 외래 키 컬럼
private Dept dept;
```

```java
// Dept(1) → Emp(N) : 부서 하나가 사원 여러 명을 갖는다 (양방향, 선택)
@OneToMany(mappedBy = "dept")
private List<Emp> emps = new ArrayList<>();
```

| 애노테이션 | 방향 | `mappedBy` |
|---|---|---|
| `@ManyToOne` | 외래 키를 **가진** 쪽(N쪽, `Emp`)에 붙인다 | 없음 — 이쪽이 연관관계의 **주인** |
| `@OneToMany` | 외래 키가 **없는** 쪽(1쪽, `Dept`)에 붙인다 | 상대方 필드 이름(`"dept"`)을 적어 "나는 주인이 아니다"를 표시 |

**연관관계의 주인**: 외래 키를 관리하는 쪽(`@JoinColumn`이 붙은 `Emp.dept`)만 DB에 실제로
`UPDATE`를 일으킵니다. `Dept.emps`에 아무리 값을 넣어도(`mappedBy`가 붙은 쪽) DB는 안 바뀝니다.
"주인이 아닌 쪽은 조회 전용"이라고 기억합니다. (상세한 이유는 심화)

`fetch = FetchType.LAZY`: 연관된 `Dept`를 **실제로 그 값을 꺼내 쓰는 시점**에 추가 쿼리로
가져옵니다(즉시 안 가져옴). `@ManyToOne`/`@OneToOne`은 기본값이 `EAGER`(즉시 로딩)이므로
**직접 `LAZY`로 지정하는 습관**을 들입니다 — 이유와 부작용(N+1)은 다음 챕터(영속성 컨텍스트·
연관관계 심화)에서 자세히 다룹니다.

---

## 6. `JpaRepository` — MyBatis Mapper와 나란히

```java
package com.example.hr.jpa.repository;

import com.example.hr.jpa.domain.Emp;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EmpRepository extends JpaRepository<Emp, Long> {

    // 메서드 이름만으로 쿼리가 생성된다 ("쿼리 메서드")
    List<Emp> findByDeptDeptId(Long deptId);
}
```

`@Mapper` 인터페이스처럼 구현체를 우리가 만들지 않는 것은 같지만, **XML이 아예 없습니다.**
`JpaRepository<Emp, Long>` 을 상속하는 순간 아래가 전부 공짜로 생깁니다.

| 메서드 | 하는 일 |
|---|---|
| `findAll()` | 전체 조회 (`SELECT * FROM EMP`) |
| `findById(Long id)` | PK로 단건 조회 (`Optional<Emp>` 반환) |
| `save(Emp emp)` | `empId`가 없으면 `INSERT`, 있으면 `UPDATE` |
| `deleteById(Long id)` | 삭제 |
| `count()` | 전체 건수 |

`findByDeptDeptId(Long deptId)` 처럼 **메서드 이름을 분석해서** Spring Data JPA가 자동으로
`WHERE dept.dept_id = ?` SQL을 만들어 줍니다(이름 규칙은 심화에서 더 다룸).

```java
@Service
@RequiredArgsConstructor
public class EmpJpaService {
    private final EmpRepository empRepository;

    public List<Emp> findAll() {
        return empRepository.findAll();          // MyBatis: empMapper.findAll()
    }

    public Emp register(String empName, String email, int salary, Dept dept) {
        Emp emp = Emp.builder()
                .empName(empName).email(email).salary(salary)
                .hireDate(LocalDate.now()).dept(dept)
                .build();
        return empRepository.save(emp);           // MyBatis: empMapper.insert(emp)
    }
}
```

Controller/Service 계층 구조(Day 5)는 MyBatis 때와 **완전히 동일**합니다. 바뀌는 것은
Repository/Mapper 구현 방식뿐입니다.

---

## 자주 하는 실수

- **`@Entity`에 기본 생성자가 없음** → Hibernate가 리플렉션으로 객체를 만들 때 필요합니다.
  Lombok `@NoArgsConstructor`를 꼭 붙입니다.
- **`@Id` 없이 엔티티 작성** → `Entity has no identifier` 예외. PK 필드에 `@Id` 필수.
- **`@ManyToOne`을 기본값(EAGER)으로 방치** → 목록 조회 한 번에 관련 엔티티를 계속
  추가 조회(N+1)하게 됨. `fetch = FetchType.LAZY`를 습관적으로 명시.
- **`Dept.emps` 컬렉션에만 사원을 추가하고 저장** → `mappedBy`가 붙은 쪽은 주인이 아니므로
  DB에 반영 안 됨. `Emp.setDept(dept)` 처럼 주인 쪽을 통해 관계를 맺어야 함.
- **운영 DB에 `ddl-auto: update`/`create`를 그대로 씀** → 컬럼·테이블이 예고 없이
  바뀌거나 사라짐. 실무는 `validate`/`none` + 별도 마이그레이션 도구.
- **MyBatis `Emp`와 JPA `Emp`를 같은 패키지에 둠** → 클래스 이름 충돌. 3절처럼 분리.

---

## 핵심 요약

| 요소 | 내용 |
|---|---|
| ORM | 객체 ↔ 테이블 자동 매핑. JPA = 명세, Hibernate = 구현체 |
| `spring-boot-starter-data-jpa` | Hibernate + Spring Data JPA 자동 구성 |
| `@Entity` / `@Table` | 클래스를 테이블에 매핑 |
| `@Id` / `@GeneratedValue(IDENTITY)` | PK, `AUTO_INCREMENT` 위임 |
| `@Column` | 컬럼명·제약조건 (필드명=컬럼명이면 생략 가능) |
| `@ManyToOne` + `@JoinColumn` | 외래 키를 가진 쪽 = 연관관계의 주인 |
| `@OneToMany(mappedBy=...)` | 주인이 아닌 쪽, 조회 전용 |
| `fetch = LAZY` | 실제 사용 시점에 추가 조회 (`@ManyToOne` 기본값은 EAGER — 직접 LAZY로 바꿀 것) |
| `JpaRepository<T, ID>` | 상속만으로 기본 CRUD + 쿼리 메서드 |
| `ddl-auto` | 학습용 `update`/`create` vs 실무 `validate`/`none` |

> 다음(JPA 2일차): 오늘 미뤄둔 이야기 — 영속성 컨텍스트(1차 캐시·변경 감지·쓰기 지연)가
> 실제로 무엇을 해주는지, `LAZY` 로딩이 부르는 N+1 문제와 `fetch join`/`@EntityGraph`로
> 해결하는 법, `@Transactional` 경계 안/밖에서 `LazyInitializationException`이 왜 나는지.
