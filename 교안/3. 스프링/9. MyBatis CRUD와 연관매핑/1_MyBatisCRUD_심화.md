# Day 9. MyBatis CRUD & 연관 매핑 — 심화 (경험자용)

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

### 2.1 그런데 1:N + 페이징이 같이 있으면 — 위 가이드가 뒤집힌다

위 가이드("목록이면 조인")는 **N:1**(사원 여러 명 → 부서 하나씩)에만 맞습니다. 반대로 **1:N**을
페이징되는 목록에서 함께 조회하면(예: 사원 목록 화면에 사원마다 **권한 목록**(`MEMBER_ROLE`,
Day 17에서 실제 추가)을 같이 보여주고 싶을 때), **조인 한 방은 오히려 페이징을 깨뜨립니다.**

```
사원 A - 권한 2개, 사원 B - 권한 1개, 사원 C - 권한 3개 를 LIMIT 3 으로 조회하면?

조인 한 방(잘못된 방법):
  A-권한1, A-권한2, B-권한1  ← 딱 3행에서 LIMIT 걸림 → 사원은 A, B 두 명뿐. C는 통째로 누락!

중첩 select(올바른 방법):
  A, B, C  ← 사원 테이블만 LIMIT 3 → 정확히 3명. 권한은 사원별로 따로 조회해서 채움
```

조인 결과의 "행"과 "사원 수"가 같지 않기 때문에, **`<if>`/`<where>`(동적 조건)와 `LIMIT`/`OFFSET`은
반드시 사원(1쪽) 쪽 쿼리에만 걸고, N쪽(권한)은 중첩 select로 따로 채웁니다.**

```xml
<resultMap id="empWithRolesMap" type="Emp">
  <id     property="empId"   column="EMP_ID"/>
  <result property="empName" column="EMP_NAME"/>
  <!-- N쪽은 중첩 select로 — 페이징이 걸린 위 쿼리와는 별개로 사원마다 한 번씩 실행 -->
  <collection property="roles" column="EMP_ID" select="findRolesByEmpId"/>
</resultMap>

<select id="findRolesByEmpId" resultType="string">
  SELECT ROLE_NAME FROM MEMBER_ROLE WHERE EMP_ID = #{empId}
</select>

<select id="findPage" resultMap="empWithRolesMap">
  SELECT EMP_ID, EMP_NAME
  FROM EMP
  <where>
    <if test="keyword != null and keyword != ''">
      AND EMP_NAME LIKE CONCAT('%', #{keyword}, '%')
    </if>
  </where>
  ORDER BY EMP_ID
  LIMIT #{pageSize} OFFSET #{offset}
</select>
```

- `<where>`/`<if>`·`LIMIT`/`OFFSET` 은 전부 `findPage`(사원 쪽)에만 있습니다 — 행 뻥튀기 걱정이 없습니다.
- 결과로 나온 사원마다 `findRolesByEmpId` 가 한 번씩 더 실행됩니다(N+1). 페이지당 10~20명 수준이면
  실무에서도 흔히 감수하는 비용입니다.
- **상세 화면(단건)** 처럼 페이징이 없다면 1절의 `<collection>`(조인 한 방)을 그대로 써도 됩니다 —
  행이 늘어나도 어차피 그 사원 한 명 것이라 문제가 안 됩니다. 페이징 유무가 기준입니다.
- `<collection>`·`<association>` 은 **`<resultMap>` 안에서만** 쓸 수 있는 자식 요소입니다.
  `resultType`(자동 매핑)에는 중첩을 지시할 자리가 없어서, 중첩이 필요한 순간 `resultMap`을
  명시적으로 씁니다.

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

## 4. 동적 SQL 조립 도구 — `<set>`, `<trim>`, `<bind>`

### `<set>` — 부분 수정(null 필드는 건드리지 않음)

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
`<set>` 이 `SET` 키워드를 앞에 붙이고, `<if>` 들이 붙인 결과의 **마지막 콤마를 자동으로 정리**합니다.
`<if>` 가 하나도 안 걸리면(수정할 필드가 없으면) `SET` 자체가 안 붙어 SQL 문법 오류가 나므로,
서비스에서 "수정할 필드가 하나도 없음"을 미리 걸러 줘야 합니다.

### `<trim>` — `<where>`/`<set>`이 사실 이걸로 만들어진 것

`<where>`(3절 이전에 이미 씀)와 `<set>`은 사실 **`<trim>` 을 미리 설정해 둔 특수한 형태**입니다.
`<trim>` 은 자식 내용의 **앞/뒤에 뭘 붙일지**, **앞/뒤의 뭘 지울지**를 직접 지정하는 범용 도구입니다.

| 속성 | 뜻 |
|---|---|
| `prefix` | 자식 내용 앞에 붙일 문자열 |
| `prefixOverrides` | 자식 내용 맨 앞에 이 문자열이 있으면 지움(`\|` 로 여러 개) |
| `suffix` | 자식 내용 뒤에 붙일 문자열 |
| `suffixOverrides` | 자식 내용 맨 뒤에 이 문자열이 있으면 지움 |

```xml
<!-- <where> 의 실체 -->
<trim prefix="WHERE" prefixOverrides="AND |OR ">
  <if test="keyword != null">AND EMP_NAME LIKE #{keyword}</if>
  <if test="deptId  != null">AND DEPT_ID = #{deptId}</if>
</trim>

<!-- <set> 의 실체 -->
<trim prefix="SET" suffixOverrides=",">
  <if test="empName != null">EMP_NAME = #{empName},</if>
</trim>
```

`<where>`/`<set>`으로 대부분 충분하지만, `SELECT` 목록처럼 **앞에 붙는 콤마를 지워야 하는** 경우
(`prefixOverrides=","`)처럼 둘 다 안 맞는 모양이 필요할 때 `<trim>`을 직접 씁니다.

### `<bind>` — 검색어를 SQL 함수 없이 안전하게 조립

지금까지 `LIKE` 검색은 `CONCAT('%', #{keyword}, '%')` 처럼 **DB 함수**로 앞뒤에 `%`를 붙였습니다.
`CONCAT`은 MySQL·Oracle엔 있지만 DB마다 이름·문법이 다릅니다(SQL Server는 `+` 연산자).
`<bind>`는 자바 쪽(OGNL)에서 문자열을 미리 조립해 **DB 함수 없이** 넘기는 방법입니다.

```xml
<select id="search" resultType="Emp">
  <bind name="keywordPattern" value="'%' + keyword + '%'"/>
  SELECT * FROM EMP
  WHERE EMP_NAME LIKE #{keywordPattern}
</select>
```

- `value` 는 **OGNL 표현식** — 파라미터 객체의 필드(`keyword`)를 자바 문자열처럼 `+` 로 이어 붙입니다.
- 결과(`keywordPattern`)를 `#{}` 로 바인딩하므로 **여전히 파라미터 바인딩**이고(SQL 인젝션 안전),
  DB 함수에 의존하지 않아 **어떤 DB로 바꿔도 그대로** 동작합니다.
- 검색어에 `%`·`_` 같은 LIKE 와일드카드 문자가 그대로 들어있으면 의도치 않게 넓게 검색될 수 있는데,
  `<bind>` 안에서 이스케이프 처리(`replace`)까지 OGNL로 해결할 수도 있습니다(실무에선 서비스단에서
  이스케이프하는 편이 더 흔합니다).

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

`ENT_YN 'Y'/'N'` ↔ `boolean active`, `enum` ↔ `code` 등은 `BaseTypeHandler` 로(Day 7 심화).
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
