# Day 11. MyBatis CRUD & 연관 매핑 — 실습

각 문제는 **테스트로 검증**합니다(`@SpringBootTest @Transactional`).

## 문제 1. EmpMapper CRUD 완성

`EmpMapper` + XML 에 다음을 구현하세요.

- `int insert(Emp emp)` — HR 원본이므로 `EMP_ID` 는 파라미터로 받아 채움. `ENT_YN` 은 `'N'` 고정.
- `int update(Emp emp)` — 이름·이메일·부서·급여·입사일 수정.
- `int deleteById(Long empId)`

## 문제 2. CRUD 흐름 테스트

`EmpCrudTest.등록_수정_삭제_흐름()` 을 작성(교안 6절 참고). `EMP_ID = 900` 으로 등록 →
`findById(900)` 확인 → 이름 수정 → 재확인 → 삭제 → `findById(900)` 이 null.
테스트를 **연속 3번** 돌려 항상 통과하는지 확인하세요(롤백 검증).

## 문제 3. 목록에 부서명 붙이기

`EmpMapper.findList()` 를 `EMP LEFT JOIN DEPT LEFT JOIN JOB` 으로 만들어 `List<Emp>` 로 매핑하세요.
`Emp` 에 읽기 전용 필드 `deptName`, `jobName` 을 두고, SQL에서 `d.DEPT_TITLE AS deptName`,
`j.JOB_NAME AS jobName` 으로 채웁니다. (`resultType="Emp"` + `AS 별칭` 을 필드명과 일치)

테스트: 21건, 곽상혁의 `getDeptName()` 이 `"총무부"`, 모든 행의 `getEmpName()` 이 not blank.

`EmpServiceImpl.findAll()` 이 이 메서드를 쓰도록 바꿔, 화면(`/emps`) 목록에 부서명이 나오게 하세요.

## 문제 4. `<association>` 으로 상세

`EmpMapper.findByIdWithDept(Long empId)` 를 `<association property="dept">` 로 만들어
`Emp` 안에 `Dept dept` 가 채워지도록 하세요.

테스트: `findByIdWithDept(205L).getDept().getDeptName()` 이 `"해외영업1부"`.

## 문제 5. `@Param` 2개

`EmpMapper.updateSalary(@Param("empId") Long empId, @Param("salary") int salary)` 를 만들고
`WHERE EMP_ID = #{empId}` `SET SALARY = #{salary}`.

`@Param` 을 **일부러 하나 빼고** 실행해 보고 어떤 예외가 나는지 적으세요.

## 문제 6. (개념) `update` 가 0을 반환

`empMapper.update(emp)` 가 `0` 을 반환하는 경우는 어떤 상황인가? (2가지)
서비스에서 이 `0` 을 어떻게 처리해야 하나?
