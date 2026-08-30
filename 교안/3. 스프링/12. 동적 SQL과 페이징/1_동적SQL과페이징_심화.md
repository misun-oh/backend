# Day 12. 동적 SQL과 페이징 — 심화 (경험자용)

## 1. OFFSET 페이징의 한계와 키셋(cursor) 페이징

`LIMIT n OFFSET m` 은 m이 커질수록 앞의 m행을 읽고 버려서 느려진다(딥 페이징).
정렬 키가 유일하면 **키셋 페이징**:

```sql
-- 다음 페이지: 마지막으로 본 행의 (hire_date, emp_id) 이후
WHERE (e.HIRE_DATE, e.EMP_ID) < (#{lastHireDate}, #{lastEmpId})
ORDER BY e.HIRE_DATE DESC, e.EMP_ID DESC
LIMIT #{size}
```
- 장점: 항상 인덱스로 시작점 점프 → 일정한 속도. 무한 스크롤에 적합.
- 단점: "5페이지로 바로 가기" 불가, 정렬 바꾸면 커서 무효. 관리자 목록(페이지 번호 필요)은 OFFSET 유지.

## 2. COUNT 최적화

- 조인이 결과 수에 영향 없으면 COUNT는 `FROM 기준테이블` 만.
- `WHERE` 가 인덱스를 타게. `LEFT JOIN ... IS NULL` 같은 건 COUNT에서 서브쿼리로.
- 전체 건수가 꼭 필요 없으면(무한 스크롤) COUNT 생략. "다음 있음?"만: `LIMIT size+1` 로 한 건 더 읽어 판단.
- 매우 큰 테이블은 근사치(`information_schema` 통계, 별도 집계 테이블).

## 3. 스프링 데이터 `Pageable` / PageHelper

- `Pageable`(`spring-data-commons`)을 컨트롤러 파라미터로 받으면 `?page=0&size=10&sort=salary,desc` 자동 바인딩.
  MyBatis에는 기본 연동이 없어 `pageable.getPageNumber()/getPageSize()/getSort()` 를 직접 꺼내 씀.
- **PageHelper**(`pagehelper-spring-boot-starter`): `PageHelper.startPage(page, size)` 한 줄로 다음
  쿼리에 자동 LIMIT + COUNT. 편하지만 ThreadLocal 기반이라 "startPage 후 반드시 바로 그 쿼리 하나"
  규칙을 어기면 엉뚱한 쿼리에 페이징이 붙는 함정. 팀 합의하에. (Boot 4 지원 버전인지 릴리스 노트 확인)

## 4. `<sql>` 조각 재사용 & 다중 매퍼 참조

```xml
<sql id="empSearchWhere"> <where> ... </where> </sql>
<!-- 다른 매퍼에서: -->
<include refid="com.example.hr.mapper.EmpMapper.empSearchWhere"/>
```
공통 컬럼/조인/WHERE를 하나로. 단, 결합도가 올라가니 남발 금지.

## 5. OGNL `test` 표현식 주의

- `test="active"` : Boolean 이면 값 그대로, 문자열 `"true"` 는 참 취급 안 됨 → `test="active == 'true'"` 또는 boolean으로.
- 숫자 리터럴: `test="type == 1"` OK. enum: `test="status.name() == 'ACTIVE'"` 또는 `status != null`.
- `test="list != null and list.size() > 0"` (`!list.isEmpty()` 는 OGNL 버전 따라).
- `and`/`or`/`not` 소문자.

## 6. 동적 SQL 테스트

- `@MybatisTest` 또는 `@SpringBootTest` 로 각 조건 조합을 케이스로:
  키워드만, 부서만, 둘 다, 아무것도, 재직만 → 각각 기대 건수 단언.
- SQL 로그(`logging.level.*.mapper=DEBUG`)로 조립된 WHERE 눈으로 확인.
- COUNT 와 데이터 쿼리의 조건이 같은지(리팩터링 후) 회귀 테스트.

## 7. 대량 IN / 임시테이블

- `IN` 목록이 1000개↑면 파라미터·플랜이 나빠짐. 임시테이블 JOIN, 또는 `EXISTS` 서브쿼리,
  또는 배치로 나눠 질의.
- MySQL `range_optimizer_max_mem_size`, `eq_range_index_dive_limit` 영향.

## 8. N+1과 목록

목록에서 각 행마다 추가 조회(부서·직급 라벨)를 하면 N+1. 이 과정은 **조인 한 방 + DTO** 로 해결(Day 11).
집계(부서별 인원)는 `GROUP BY` + 조인으로 목록 쿼리 안에서.
