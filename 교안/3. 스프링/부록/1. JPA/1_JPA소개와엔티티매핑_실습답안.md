# 부록 1. JPA 소개와 엔티티 매핑 — 실습 답안

---

## 문제 1. `Job` 엔티티 매핑

```java
package com.example.hr.jpa.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "JOB")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Job {

    @Id
    @Column(name = "JOB_CODE", length = 2)
    private String jobCode;

    @Column(name = "JOB_NAME", length = 35)
    private String jobName;
}
```

`@GeneratedValue`를 안 쓰는 이유: `JOB_CODE`는 `'J1'`처럼 **의미를 가진 코드값**을 애플리케이션이나
초기 데이터로 직접 지정하는 자연 키(natural key)라서, DB의 `AUTO_INCREMENT`에 채번을 맡기지 않습니다.

---

## 문제 2. `Emp` ↔ `Job` 연관관계

```java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "JOB_CODE")
private Job job;
```

```java
@Test
@Transactional   // 트랜잭션 안에서 지연 로딩 필드를 건드려야 LazyInitializationException이 안 남
void 사원의_직급명을_조회한다() {
    Emp emp = empRepository.findAll().get(0);
    assertThat(emp.getJob().getJobName()).isNotBlank();
}
```

---

## 문제 3. 쿼리 메서드

```java
public interface EmpRepository extends JpaRepository<Emp, Long> {
    List<Emp> findByDeptDeptId(Long deptId);
    List<Emp> findBySalaryGreaterThanEqual(int salary);
}
```

```java
@Test
void 급여_500만_이상_조회() {
    List<Emp> result = empRepository.findBySalaryGreaterThanEqual(5_000_000);
    assertThat(result).allMatch(e -> e.getSalary() >= 5_000_000);
}
```

`GreaterThanEqual`처럼 메서드 이름의 술어(predicate)를 Spring Data JPA가 파싱해서
`WHERE SALARY >= ?` 조건을 자동 생성합니다.

---

## 문제 4. 부서별 평균 급여

```java
@Query("select e.dept.deptName, avg(e.salary) from Emp e group by e.dept.deptName")
List<Object[]> avgSalaryByDept();
```

```java
@Test
void 부서별_평균급여() {
    empRepository.avgSalaryByDept()
            .forEach(row -> System.out.println(row[0] + " : " + row[1]));
}
```

**비교**: MyBatis(Day 11)는 `SELECT d.DEPT_TITLE, AVG(e.SALARY) FROM EMP e JOIN DEPT d ...
GROUP BY d.DEPT_TITLE` 를 XML에 **그대로** 적습니다. JPQL은 테이블/컬럼이 아니라 엔티티/필드
이름(`Emp`, `e.dept.deptName`)을 쓰고, 실행 시점에 Hibernate가 실제 SQL로 번역합니다 — 문법은
비슷해 보여도 "누가 SQL로 번역하느냐"가 다릅니다.

---

## 문제 5. 변경 감지 확인

```java
@Test
@Transactional
@Commit   // 테스트 기본 롤백을 끄고 실제로 커밋해서 확인 (org.springframework.test.annotation.Commit)
void 변경감지로_급여가_수정된다() {
    Emp emp = empRepository.findById(100L).get();
    emp.setSalary(emp.getSalary() + 100000);
    // save() 호출 없음
}

@Test
void 급여가_실제로_바뀌었는지_별도_확인() {
    Emp emp = empRepository.findById(100L).get();
    // 위 테스트가 커밋됐다면 +100000 된 값이 보임
}
```

`save()`를 안 불러도 `UPDATE`가 나가는 이유: `findById()`로 조회한 `Emp`는 영속성 컨텍스트가
**계속 관리하는 객체**입니다. 트랜잭션을 커밋(또는 `flush()`)하는 시점에, 조회 당시 찍어둔
스냅샷과 현재 필드값을 비교해서 달라진 컬럼만 자동으로 `UPDATE` SQL을 만듭니다(변경 감지).
`save()`는 **새 객체(비영속)를 영속 상태로 만들 때**만 꼭 필요하고, 이미 영속 상태인 객체를
수정할 때는 필요 없습니다.

---

## 문제 6. 양방향 매핑 실수 재현

```java
@Test
void mappedBy쪽만_수정하면_DB에_반영안됨() {
    Dept dept = deptRepository.findById(1L).get();
    Emp newEmp = Emp.builder().empName("테스트").dept(null).build();  // 주인 쪽 dept를 일부러 안 채움
    dept.getEmps().add(newEmp);           // mappedBy 쪽에만 추가
    deptRepository.save(dept);

    // newEmp 는 dept.getEmps() 컬렉션에는 있지만 DB의 EMP_ID는 아직 채번 전, DEPT_ID는 null로 저장(또는 저장 자체가 안 될 수도 있음 — cascade 미설정 시 저장조차 안 됨)
}
```

`Dept.emps`는 `mappedBy = "dept"`가 붙은, **연관관계의 주인이 아닌 쪽**입니다. JPA는
외래 키(`EMP.DEPT_ID`) 값을 결정할 때 **주인 쪽 필드(`Emp.dept`, `@JoinColumn`)만** 봅니다.
`Dept.emps`에 아무리 객체를 추가해도 그건 자바 컬렉션 안에서만 일어난 일이고, `Emp.dept`를
직접 설정하지 않는 한 `DEPT_ID` 컬럼은 갱신되지 않습니다(게다가 `cascade` 설정이 없으면
`newEmp` 자체가 저장되지도 않습니다). 항상 **주인 쪽(`emp.setDept(dept)`)을 통해** 관계를
맺어야 합니다.
