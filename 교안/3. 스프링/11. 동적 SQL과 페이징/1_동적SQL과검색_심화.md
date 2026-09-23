# Day 11-1. 동적 SQL과 검색 — 심화 (경험자용)

## 1. `<sql>` 조각 재사용 & 다중 매퍼 참조

```xml
<sql id="empSearchWhere"> <where> ... </where> </sql>
<!-- 다른 매퍼에서: -->
<include refid="com.example.hr.mapper.EmpMapper.empSearchWhere"/>
```
공통 컬럼/조인/WHERE를 하나로. 단, 결합도가 올라가니 남발 금지.

## 2. OGNL `test` 표현식 주의

- `test="active"` : Boolean 이면 값 그대로, 문자열 `"true"` 는 참 취급 안 됨 → `test="active == 'true'"` 또는 boolean으로.
- 숫자 리터럴: `test="type == 1"` OK. enum: `test="status.name() == 'ACTIVE'"` 또는 `status != null`.
- `test="list != null and list.size() > 0"` (`!list.isEmpty()` 는 OGNL 버전 따라).
- `and`/`or`/`not` 소문자.

## 3. 동적 SQL 테스트

- `@MybatisTest` 또는 `@SpringBootTest` 로 각 조건 조합을 케이스로:
  키워드만, 부서만, 둘 다, 아무것도, 재직만 → 각각 기대 건수 단언.
- SQL 로그(`logging.level.*.mapper=DEBUG`)로 조립된 WHERE 눈으로 확인.
- 검색 SQL과 카운트 SQL의 조건이 같은지(리팩터링 후) 회귀 테스트 (`2_페이징.md` 참고).

## 4. 대량 IN / 임시테이블

- `IN` 목록이 1000개↑면 파라미터·플랜이 나빠짐. 임시테이블 JOIN, 또는 `EXISTS` 서브쿼리,
  또는 배치로 나눠 질의.
- MySQL `range_optimizer_max_mem_size`, `eq_range_index_dive_limit` 영향.

## 5. N+1과 목록

목록에서 각 행마다 추가 조회(부서·직급 라벨)를 하면 N+1. 이 과정은 **조인 한 방 + DTO** 로 해결(Day 9).
집계(부서별 인원)는 `GROUP BY` + 조인으로 목록 쿼리 안에서.
