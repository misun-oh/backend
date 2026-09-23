# Day 11-1. 동적 SQL과 검색 — 실습

## 문제 1. 검색 조건 DTO

`dto/EmpSearchCond`(keyword, deptId, workingOnly) 를 만드세요. (`sort`/`page`/`size` 는
`2_페이징.md` 실습에서 이어서 추가합니다)

## 문제 2. 동적 검색 SQL

`EmpMapper.selectByCond(EmpSearchCond)` 를 만들고 `<where>` + `<if>` 로 keyword(이름·이메일·사번
LIKE), deptId(`=`), workingOnly(`ent_yn='N'`) 를 조립하세요. WHERE 조각은
`<sql id="searchWhere">` 로 따로 빼두세요 (다음 문서의 `countByCond` 와 공유할 예정입니다).

테스트(`@SpringBootTest`), `selectByCond(cond).size()` 로 검증:

| 조건 | 기대 건수 |
|---|---|
| 아무 조건 없음 | 21 |
| `workingOnly=true` | 20 (퇴사 1명 제외) |
| `deptId="D5"` | 5 |
| `keyword="김"` | 김민혜·김은민·김태일·김하나… (실제 데이터로 확인) |
| `deptId="D5"` + `workingOnly=true` | 5 |

## 문제 3. `<foreach>` IN

`EmpMapper.findByIds(@Param("ids") List<Integer> ids)` 를 `<foreach>` 로 만들고,
`ids = [200, 205, 999]` 로 조회 시 2건(200, 205)이 나오는지 테스트.
`ids = []` 로 호출하면? 서비스에서 어떻게 방어해야 하나?

## 문제 4. 화면 검색폼

`index.html` 에 검색폼(`method="get"`)을 붙이세요. `keyword`/`deptId`/`workingOnly` 로 검색했을 때
목록이 조건에 맞게 바뀌는지, 페이지를 새로고침해도 검색창에 값이 그대로 남아있는지 브라우저에서
확인하세요. (페이지네이션은 아직 없습니다 — `2_페이징.md` 실습에서 이어서)
