# Day 9. MyBatis 시작 & DB 설정 — 심화 (경험자용)

## 1. HikariCP 튜닝 포인트

| 속성 | 의미 | 감각 |
|---|---|---|
| `maximum-pool-size` | 풀 최대 커넥션 | (코어수 × 2) + 디스크수 근처에서 시작. 무작정 크게 X |
| `minimum-idle` | 유휴 최소 유지 | 보통 max와 같게(고정 풀) |
| `connection-timeout` | 커넥션 대기 타임아웃 | 3~5초. 넘으면 예외 → 빠른 실패 |
| `max-lifetime` | 커넥션 최대 수명 | DB `wait_timeout` 보다 짧게(예: 25~30분) |
| `keepalive-time` | 유휴 커넥션 살아있는지 확인 | `max-lifetime` 보다 작게 |
| `leak-detection-threshold` | 커넥션 반납 안 됨 감지 | 개발/의심 시 5000~20000ms |

풀 고갈(`Connection is not available`)은 대개 **트랜잭션이 너무 길거나** 커넥션을 안 닫는(MyBatis에선
드묾) 것. 슬로우 쿼리·외부 API를 트랜잭션 안에서 호출하지 않기(Day 13).

## 2. 여러 DataSource

읽기/쓰기 분리, 멀티 DB일 때:
- `@ConfigurationProperties("spring.datasource.write")` / `read` 로 각각 `DataSource` 빈,
  `@Primary` 지정.
- `SqlSessionFactory`·`SqlSessionTemplate`·`@MapperScan(sqlSessionTemplateRef=...)` 을 소스별로.
- `AbstractRoutingDataSource` + `@Transactional(readOnly=true)` 감지로 라우팅.

## 3. `resultMap` 을 언제 쓰나 (Day 11 예고)

- 단순 컬럼→필드는 `resultType` + camelCase 매핑으로 충분.
- 커스텀 매핑(컬럼명↔필드명 다름, `<association>`으로 1:1 조인, `<collection>`으로 1:N, 중첩 객체,
  `typeHandler`)가 필요하면 `resultMap`.

## 4. `TypeHandler`

- `boolean active` ↔ `CHAR(1) 'Y'/'N'` 을 SQL `CASE` 대신 핸들러로:
```java
@MappedTypes(Boolean.class)
public class YNBooleanTypeHandler extends BaseTypeHandler<Boolean> {
    public void setNonNullParameter(PreparedStatement ps, int i, Boolean p, JdbcType t) throws SQLException {
        ps.setString(i, Boolean.TRUE.equals(p) ? "N" : "Y"); // 주의: ENT_YN 은 "퇴사여부"
    }
    public Boolean getNullableResult(ResultSet rs, String col) throws SQLException {
        return "N".equals(rs.getString(col));
    }
    // ...
}
```
`mybatis.type-handlers-package` 로 등록. 도메인 규칙(활성=ENT_YN='N')을 한 곳에 모을 수 있다.

## 5. `#{}` 고급

- `#{name, jdbcType=VARCHAR}` — null 파라미터에 jdbcType 필요할 때(오라클 등).
- `#{amount, typeHandler=com...MoneyHandler}`.
- 컬렉션 파라미터는 `<foreach>` (Day 12).
- 파라미터가 2개 이상이면 `@Param("id") Long id, @Param("name") String name` 로 이름 지정.

## 6. `${}` 를 안전하게 쓰는 유일한 방법

정렬·동적 컬럼은 **enum + switch** 로:
```java
public enum EmpSort {
    HIRE_DATE("HIRE_DATE"), SALARY("SALARY"), NAME("EMP_NAME");
    final String column; EmpSort(String c){ this.column = c; }
}
```
XML `ORDER BY ${sort.column} ${direction}` — `sort` 는 enum 이라 임의 문자열이 못 들어옴,
`direction` 은 `"ASC"|"DESC"` 만 허용하도록 서비스에서 강제.

## 7. MyBatis vs Spring Data JDBC vs JdbcTemplate

- **JdbcTemplate**: SQL 문자열 + RowMapper. MyBatis보다 저수준, 동적 SQL 불편.
- **MyBatis**: XML/애노테이션 SQL + 동적 SQL(`<if>`), 결과 매핑 강력. SQL 완전 제어. 이 과정 선택.
- **Spring Data JDBC**: 애그리거트 중심의 가벼운 ORM. 복잡 조인엔 약함.
- **JPA**: 별도 과정.

## 8. 매퍼 테스트 & 로깅

- `logging.level.com.example.hr.mapper=DEBUG` → 실행 SQL, `==> Parameters:`, `<== Total:` 가 보임.
- `@MybatisTest`(슬라이스) 또는 `@SpringBootTest` + `@Transactional`(자동 롤백)로 실제 DB 검증(Day 10).
- `p6spy` 를 붙이면 `#{}` 바인딩이 채워진 **완성 SQL**을 로그로 볼 수 있어 디버깅이 편하다.
