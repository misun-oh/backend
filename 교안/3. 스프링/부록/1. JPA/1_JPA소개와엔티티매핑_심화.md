# 부록 1 심화. 영속성 컨텍스트와 매핑의 함정

본문에서 다루지 않은, 경험자를 위한 심화 내용입니다. 초심자는 이후 추가될 JPA 다음 챕터
(영속성 컨텍스트 정식 챕터)에서 다시 만나도 늦지 않습니다.

## 1. 영속성 컨텍스트 — JPA가 실제로 대신 해주는 일

`EntityManager`(Spring Data JPA 내부적으로 사용)는 엔티티를 **영속성 컨텍스트**라는
1차 캐시에 담아 관리합니다. `JpaRepository.save()`/`findById()` 뒤에서 벌어지는 일:

```java
Emp emp = empRepository.findById(1L).get();   // ① SELECT 실행, 영속성 컨텍스트에 저장
emp.setSalary(emp.getSalary() + 100000);       // ② 필드만 바꿈. SQL 없음!
// 메서드(트랜잭션) 종료 시점에 자동으로 UPDATE 실행 — "변경 감지(Dirty Checking)"
```

- **1차 캐시**: 같은 트랜잭션 안에서 같은 PK를 두 번 조회하면 SQL 없이 캐시에서 반환 →
  같은 PK로 조회한 두 참조는 `==` 로 같다(동일성 보장).
- **변경 감지(Dirty Checking)**: `save()`를 다시 호출하지 않아도, 트랜잭션 커밋 시점에
  "조회 시점 스냅샷"과 현재 필드를 비교해 바뀐 컬럼만 `UPDATE`. MyBatis에서는 `update()`를
  **명시적으로 호출**해야 했던 것과 대비됩니다.
- **쓰기 지연**: `INSERT`/`UPDATE` SQL을 즉시 안 날리고 모아뒀다가 트랜잭션 커밋(또는
  `flush()`) 시점에 한 번에 내보냅니다.

**주의**: 이 모든 게 `@Transactional` **범위 안에서만** 성립합니다. 트랜잭션 밖에서 지연
로딩 필드(`emp.getDept().getDeptName()`)를 건드리면 `LazyInitializationException`이
납니다 — 영속성 컨텍스트가 이미 닫혀서 추가 쿼리를 못 날리기 때문. Controller까지 세션을
열어두는 옵션이 `spring.jpa.open-in-view`(기본 `true`)인데, 트랜잭션 경계가 흐려지고
DB 커넥션을 오래 붙잡는 부작용이 있어 **실무에서는 `false`로 끄고 Service 계층에서
필요한 데이터를 다 채워서 반환**하는 걸 권장합니다.

## 2. 연관관계의 주인 — 실제로 무시되는 예

```java
Dept dept = deptRepository.findById(1L).get();
Emp emp = new Emp(...);
dept.getEmps().add(emp);     // Dept.emps 는 mappedBy 쪽 — 이것만으로는 DB 반영 안 됨!
deptRepository.save(dept);   // EMP.DEPT_ID 는 여전히 null
```

```java
emp.setDept(dept);           // 주인(Emp.dept, @JoinColumn)쪽에 설정
empRepository.save(emp);     // 비로소 EMP.DEPT_ID 가 채워짐
```

양방향으로 둘 다 세팅하고 싶다면 편의 메서드를 만드는 것이 관례입니다.

```java
public void addEmp(Emp emp) {
    this.emps.add(emp);
    emp.setDeptInternal(this);   // 주인 쪽도 같이 맞춰줌 — 무한루프 주의(둘 다 서로 호출 X)
}
```

## 3. `equals`/`hashCode` 함정

Lombok `@Data`나 `@EqualsAndHashCode`를 엔티티에 그대로 붙이면 위험합니다.

- 모든 필드를 비교 대상에 넣으면, **연관관계 필드(`dept`)까지 비교**하다가 지연 로딩을
  강제로 초기화하거나 무한 루프(`Emp.equals` → `Dept.equals` → `Emp.equals` …)에 빠질 수 있습니다.
- `@GeneratedValue`로 키가 자동 생성되는 엔티티는 **저장 전에는 `id`가 `null`**이라서,
  `id` 기반 `equals`도 컬렉션에 넣었다 저장한 뒤 `id`가 채워지면 `hashCode`가 바뀌는
  문제가 있습니다(`HashSet`에서 못 찾게 됨).

**실무 관례**: 엔티티는 Lombok `@Getter`만 쓰고, `equals`/`hashCode`가 꼭 필요하면 PK만
비교하되 `Objects.equals`로 `null` 안전하게, 그리고 컬렉션에는 저장(영속화) 후에만 넣습니다.
확신이 없으면 **엔티티에 `equals`/`hashCode`를 아예 재정의하지 않는** 것이 더 안전한 기본값입니다.

## 4. `Y`/`N` 컬럼을 boolean으로 — `@Converter`

`ENT_YN CHAR(1)` 을 `boolean active` 로 자연스럽게 매핑하려면 변환기를 만듭니다.

```java
@Converter(autoApply = true)
public class YnConverter implements AttributeConverter<Boolean, String> {
    @Override
    public String convertToDatabaseColumn(Boolean attr) {
        return (attr != null && attr) ? "Y" : "N";
    }
    @Override
    public Boolean convertToEntityAttribute(String db) {
        return "Y".equals(db);
    }
}
```

```java
@Convert(converter = YnConverter.class)   // autoApply=true면 생략 가능
@Column(name = "ENT_YN")
private boolean active;
```

## 5. JPQL과 `@Query`

메서드 이름 규칙(`findByDeptDeptId`)만으로 부족한 조건은 JPQL을 직접 씁니다. **JPQL은
테이블이 아니라 엔티티/필드를 대상으로** 쓰는 쿼리 언어입니다.

```java
public interface EmpRepository extends JpaRepository<Emp, Long> {

    @Query("select e from Emp e where e.salary >= :min order by e.salary desc")
    List<Emp> findHighPaid(@Param("min") int min);

    // 연관 엔티티까지 한 번에 가져오는 fetch join (N+1 예방 — 다음 챕터에서 본격 학습)
    @Query("select e from Emp e join fetch e.dept where e.dept.deptId = :deptId")
    List<Emp> findByDeptIdWithDept(@Param("deptId") Long deptId);

    // 정말 복잡한 통계·튜닝이 필요하면 네이티브 SQL도 탈출구로 열려있다
    @Query(value = "SELECT * FROM EMP WHERE SALARY >= :min", nativeQuery = true)
    List<Emp> findHighPaidNative(@Param("min") int min);
}
```

MyBatis의 동적 SQL(`<if>`/`<where>`, Day 11)에 익숙하다면, JPA 쪽 대응은 `Specification`
또는 QueryDSL이지만 이 과정 범위 밖입니다 — 검색 조건이 아주 복잡하면 그 부분만 MyBatis로
남겨두는 실무 선택도 흔합니다(6절 비교표 참고).

## 6. Cascade와 `orphanRemoval`

```java
@OneToMany(mappedBy = "dept", cascade = CascadeType.ALL, orphanRemoval = true)
private List<Emp> emps = new ArrayList<>();
```

- `cascade = ALL`: `Dept`를 저장/삭제하면 연관된 `Emp`도 함께 저장/삭제.
- `orphanRemoval = true`: 컬렉션에서 `Emp`를 제거하면(참조를 끊으면) 그 `Emp` 행 자체를 삭제.
- **위험**: 부서 삭제 시 소속 사원이 통째로 삭제되는 건 업무 요구사항과 안 맞을 수 있음
  (보통은 사원을 다른 부서로 옮기거나 삭제를 막아야 함). Cascade는 "부모 없이 존재할 수
  없는 자식"(예: 주문–주문항목)에는 적합하지만, **Emp–Dept 처럼 자식이 독립적으로
  의미 있는 관계에는 신중하게** 적용합니다.

## 7. `open-in-view` 끄고 개발할 때 체크리스트

- Controller/View(Thymeleaf)에서 지연 로딩 필드에 처음 접근하면 예외가 납니다 →
  Service 계층에서 **필요한 연관 데이터를 DTO에 미리 담아서** 반환.
- 테스트에서도 `@Transactional` 없이 지연 로딩 필드를 건드리면 같은 예외가 재현됩니다 —
  오히려 운영과 같은 조건으로 버그를 일찍 발견할 수 있어 권장.
