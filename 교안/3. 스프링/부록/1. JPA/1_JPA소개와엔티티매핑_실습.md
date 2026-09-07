# 부록 1. JPA 소개와 엔티티 매핑 — 실습

3절에서 만든 학습용 JPA 프로젝트(`com.example.hr.jpa`)에 이어서 진행합니다.
각 문제는 **테스트로 검증**합니다(`@SpringBootTest @Transactional`).

## 문제 1. `Job` 엔티티 매핑

`JOB` 테이블(`JOB_CODE CHAR(2)`, `JOB_NAME VARCHAR(35)`)을 엔티티로 매핑하세요.

- PK는 `JOB_CODE`(문자열) — `@GeneratedValue`를 쓰지 않는 이유를 한 줄로 적으세요.
- 필드명 `jobCode`, `jobName`.

## 문제 2. `Emp` ↔ `Job` 연관관계 추가

`Emp`에 `Job`으로의 `@ManyToOne`을 추가하세요(`@JoinColumn(name = "JOB_CODE")`,
`fetch = FetchType.LAZY`). 본문의 `Emp`↔`Dept` 매핑과 같은 패턴입니다.

테스트: 아무 사원이나 조회해 `emp.getJob().getJobName()` 이 예외 없이 나오는지 확인
(단, 트랜잭션 안에서 확인 — 심화 1절 참고).

## 문제 3. `EmpRepository` 쿼리 메서드

메서드 이름 규칙만으로 아래 두 메서드를 완성하세요(구현 코드 없이 선언만).

- `List<Emp> findByDeptDeptId(Long deptId)` — 이미 본문에 있음, 실행해서 SQL 로그 확인
- `List<Emp> findBySalaryGreaterThanEqual(int salary)` — 급여 이상 조회

테스트: `findBySalaryGreaterThanEqual(5000000)` 결과가 모두 급여 500만 이상.

## 문제 4. JPQL로 부서별 평균 급여

`@Query` 로 JPQL을 작성하세요.

```java
@Query("select e.dept.deptName, avg(e.salary) from Emp e group by e.dept.deptName")
List<Object[]> avgSalaryByDept();
```

테스트를 돌려 결과 리스트를 콘솔에 출력하고, MyBatis(Day 11 동적 SQL)로 같은 통계를
냈다면 SQL이 어떻게 달랐을지 한 줄로 비교하세요.

## 문제 5. 변경 감지 확인

```java
@Test
@Transactional
void 변경감지로_급여가_수정된다() {
    Emp emp = empRepository.findById(100L).get();
    emp.setSalary(emp.getSalary() + 100000);
    // empRepository.save(emp) 를 호출하지 않는다!
}
```

이 테스트가 끝난 뒤 `EMP` 테이블의 급여가 실제로 바뀌어 있는지 **새 트랜잭션**으로 다시
조회해 확인하세요(힌트: `@Transactional` 테스트는 기본적으로 롤백되므로, 저장 여부를
보려면 `TestEntityManager.flush()` + 롤백 전 상태를 로그로 찍거나, 별도 조회 테스트로
확인). `save()`를 호출하지 않았는데도 `UPDATE` SQL이 나가는 이유를 설명하세요.

## 문제 6. (개념) 양방향 매핑 실수 재현

`Dept.emps`(`@OneToMany(mappedBy = "dept")`) 컬렉션에만 새 `Emp`를 추가하고
`deptRepository.save(dept)`만 호출했을 때, 새 `Emp`의 `DEPT_ID`가 어떻게 되는지
예측한 뒤 실제로 테스트로 확인하세요. 왜 그런 결과가 나오는지 심화 2절 용어(연관관계의
주인)를 써서 설명하세요.
