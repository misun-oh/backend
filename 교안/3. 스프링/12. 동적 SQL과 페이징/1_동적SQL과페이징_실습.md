# Day 12. 동적 SQL과 페이징 — 실습

## 문제 1. 검색 조건 DTO

`dto/EmpSearchCond`(keyword, deptId, activeOnly, sort, page=1, size=10, `getOffset()`),
`dto/PageResult<T>`(content, totalElements, page, size, `totalPages/hasPrev/hasNext`),
`dto/EmpSort` enum(EMP_ID/HIRE_DATE/SALARY/NAME, column·direction) 을 만드세요.

## 문제 2. 동적 검색 SQL

`EmpMapper.findPage(EmpSearchCond)`, `long countPage(EmpSearchCond)` 를 만들고
`<where>` + `<if>` 로 keyword(이름·이메일 LIKE), deptId(`=`), activeOnly(`ENT_YN='N'`) 를 조립.
WHERE 조각은 `<sql id="searchWhere">` 로 두 쿼리가 공유.

테스트(`@SpringBootTest @Transactional`):

| 조건 | 기대 건수 |
|---|---|
| 아무 조건 없음 | 21 |
| `activeOnly=true` | 20 (퇴사 1명 제외) |
| `deptId="D5"` | 5 |
| `keyword="김"` | 김민혜·김은민·김태일·김하나… (실제 데이터로 확인) |
| `deptId="D5"` + `activeOnly=true` | 5 |

## 문제 3. 페이징

`EmpService.search(cond)` 가 `PageResult<Emp>` 를 반환하도록 구현.
`findPage` 에 `LIMIT #{size} OFFSET #{offset}` 추가.

테스트:
- `size=10, page=1` → `content` 10건, `totalElements` 21, `totalPages()` 3, `hasNext()` true
- `page=3` → `content` 1건, `hasNext()` false

## 문제 4. 정렬 (화이트리스트)

`ORDER BY e.${sort.column} ${sort.direction}` 로 정렬 적용.
- `sort=SALARY` → 급여 내림차순 첫 행이 곽상혁(8,000,000)인지
- 컨트롤러 파라미터 `?sort=DROP TABLE` 로 호출 시 무슨 일이 일어나는가? (enum 변환 실패)

## 문제 5. `<foreach>` IN

`EmpMapper.findByIds(@Param("ids") List<Long> ids)` 를 `<foreach>` 로 만들고,
`ids = [200, 205, 999]` 로 조회 시 2건(200, 205)이 나오는지 테스트.
`ids = []` 로 호출하면? 서비스에서 어떻게 방어해야 하나?

## 문제 6. 화면 검색폼 + 페이지네이션

`emp/list.html` 에 검색폼(`method="get"`)과 페이지네이션을 붙이세요.
페이지 링크에 현재 `keyword/deptId/activeOnly` 를 함께 실어, **2페이지로 가도 검색이 유지**되는지
브라우저에서 확인하세요.
