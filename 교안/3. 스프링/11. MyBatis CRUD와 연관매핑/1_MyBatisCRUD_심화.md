# Day 11. MyBatis CRUD & 연관 매핑 — 심화 (경험자용)

## 1. `<collection>` — 1:N

부서 상세에서 "그 부서 소속 사원 목록"을 함께:

```xml
<resultMap id="deptWithEmpsMap" type="Dept">
  <id     property="deptId"   column="DEPT_ID"/>
  <result property="deptName" column="DEPT_TITLE"/>
  <collection property="emps" ofType="Emp">
    <id     property="empId"   column="E_EMP_ID"/>
    <result property="empName" column="E_EMP_NAME"/>
  </collection>
</resultMap>

<select id="findDeptWithEmps" resultMap="deptWithEmpsMap">
  SELECT d.DEPT_ID, d.DEPT_TITLE,
         e.EMP_ID AS E_EMP_ID, e.EMP_NAME AS E_EMP_NAME
  FROM DEPT d LEFT JOIN EMP e ON e.DEPT_ID = d.DEPT_ID
  WHERE d.DEPT_ID = #{deptId}
</select>
```
조인 결과의 행이 부서×사원으로 늘어나지만, `<id>` 로 부서를 식별해 하나의 `Dept` + 사원 리스트로 접힌다.

## 2. 중첩 `select` vs 중첩 `resultMap` (N+1)

```xml
<!-- 중첩 select : 부서마다 사원 조회 SQL 추가 실행 → N+1 -->
<association property="dept" column="DEPT_ID" select="com.example.hr.mapper.DeptMapper.findById"/>
```
- 편하지만 목록 N건이면 부서 조회가 N번 더. `fetchType="lazy"` + 실제 접근 시점에 실행.
- 목록·성능이 중요하면 **조인 한 방(중첩 resultMap)**. 상세 1건이면 중첩 select도 무방.

## 3. 배치 INSERT / UPDATE

```java
// 방법 A: <foreach> 로 multi-row VALUES
<insert id="insertAll">
  INSERT INTO EMP (EMP_ID, EMP_NAME) VALUES
  <foreach collection="list" item="e" separator=",">
    (#{e.empId}, #{e.empName})
  </foreach>
</insert>
```
```java
// 방법 B: ExecutorType.BATCH SqlSession
try (SqlSession s = sqlSessionFactory.openSession(ExecutorType.BATCH)) {
    EmpMapper m = s.getMapper(EmpMapper.class);
    for (Emp e : list) m.insert(e);
    s.flushStatements();
}
```
대량이면 방법 A(패킷 크기 `max_allowed_packet` 주의) 또는 B. 스프링에서 배치 executor는
`MyBatisAutoConfiguration` 설정(`mybatis.executor-type=batch`) 또는 별도 template.

## 4. 동적 SET — `<set>`, `<trim>`

부분 수정(null 필드는 건드리지 않음):
```xml
<update id="patch">
  UPDATE EMP
  <set>
    <if test="empName != null">EMP_NAME = #{empName},</if>
    <if test="email   != null">EMAIL = #{email},</if>
    <if test="deptId  != null">DEPT_ID = #{deptId},</if>
  </set>
  WHERE EMP_ID = #{empId}
</update>
```
`<set>` 이 마지막 콤마를 정리하고, 아무 것도 없으면 오류 나므로 서비스에서 "수정할 필드 없음"을 먼저 거른다.

## 5. `@Options` / 애노테이션 매퍼

간단한 것은 XML 없이:
```java
@Insert("INSERT INTO EMP(EMP_ID, EMP_NAME) VALUES(#{empId}, #{empName})")
@Options(useGeneratedKeys = true, keyProperty = "empId")
int insert(Emp emp);

@Select("SELECT * FROM EMP WHERE EMP_ID = #{id}")
@Results(id = "empRs", value = {
    @Result(property = "empName", column = "EMP_NAME"),
})
Emp findById(Long id);
```
동적 SQL이 필요하면 `@SelectProvider` + SQL 빌더 클래스, 또는 그냥 XML. 팀 컨벤션 통일이 중요.

## 6. 타입 핸들러로 도메인 규칙 캡슐화

`ENT_YN 'Y'/'N'` ↔ `boolean active`, `enum` ↔ `code` 등은 `BaseTypeHandler` 로(Day 9 심화).
매퍼마다 `CASE WHEN` 을 반복하지 않게 된다. `@MappedTypes` + `type-handlers-package` 등록.

## 7. 낙관적 잠금(optimistic lock)

동시 수정 충돌 방지: `EMP` 에 `VERSION` 컬럼.
```xml
<update id="update">
  UPDATE EMP SET EMP_NAME=#{empName}, VERSION = VERSION + 1
  WHERE EMP_ID = #{empId} AND VERSION = #{version}
</update>
```
반환 행 수가 0이면 "그 사이 누가 먼저 수정함" → `ObjectOptimisticLockingException` 던지고 사용자에게 재시도 안내.

## 8. 테스트 팁

- 흐름 테스트(등록→수정→삭제)는 하나로 묶어도 되지만, 실패 지점 파악을 위해 단계별 단언.
- `assertThat(list).extracting("empName", "deptName")` 튜플 검증.
- 조인 매핑은 "행 수가 안 뻥튀기 되는지"(`<id>` 로 접히는지) 꼭 확인.
- `@Sql` 로 시드를 넣고 검증 후 `@Transactional` 롤백.
