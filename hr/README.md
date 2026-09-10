# hr — 사원관리 시스템

`교안/3. 스프링/0. 사원관리 시스템 설계/1_설계.md` 의 설계(유즈케이스 · 화면 흐름 · 클래스 다이어그램 · 시퀀스
다이어그램 · 패키지 구조)와 `교안/3. 스프링/2. 프로토타입/prototypes/hr/` 화면 디자인을 그대로 구현한
Spring Boot 프로젝트입니다.

## 기술 스택

- Java 17, Spring Boot 4.0.8 (Spring MVC + Thymeleaf)
- MyBatis (`mybatis-spring-boot-starter` 4.0.0) + MySQL 8
- Gradle(Groovy DSL), Lombok

## 실행 방법

1. MySQL에 `hr` 데이터베이스를 만듭니다.

   ```sql
   CREATE DATABASE hr CHARACTER SET utf8mb4;
   ```

2. `src/main/resources/application.yml` 의 `spring.datasource` 계정 정보(`username`/`password`)를
   본인 MySQL 환경에 맞게 수정합니다.
3. 앱을 실행하면(`./gradlew bootRun`) `spring.sql.init`(schema.sql + data.sql)이 테이블 생성과
   샘플 데이터(부서 9개, 직급 7개, 사원 21명 — SQL 과정의 HR 실습데이터와 동일) 입력을 자동으로 처리합니다.
4. `http://localhost:8080` 접속 → 로그인 화면으로 이동합니다.

   - 아이디 `admin` / 비밀번호 `1234` (고정 계정 — 아래 "로그인" 참고)

## 화면 ↔ 주소

| 화면 | 주소 | 컨트롤러 |
|---|---|---|
| 대시보드 | `GET /` | `HomeController` |
| 사원 목록·검색·페이징 | `GET /emps` | `EmpController` |
| 사원 등록 폼 | `GET /emps/new`, `POST /emps` | `EmpController` |
| 사원 상세 | `GET /emps/{id}` | `EmpController` |
| 사원 수정 폼 | `GET /emps/{id}/edit`, `POST /emps/{id}` | `EmpController` |
| 사원 삭제 | `POST /emps/{id}/delete` | `EmpController` |
| 부서 목록 | `GET /depts` | `DeptController` |
| 부서 추가·수정·삭제 | `POST /depts`, `POST /depts/{id}`, `POST /depts/{id}/delete` | `DeptController` |
| 로그인·로그아웃 | `GET/POST /login`, `GET /logout` | `LoginController` |

## 패키지 구조 (설계문서 7절과 동일)

```
com.example.hr
├── HrApplication.java
├── config/       WebConfig, LoginCheckInterceptor
├── common/       SessionConst, exception(NotFoundException, DeptInUseException, GlobalExceptionHandler)
├── domain/       Emp, Dept, Job — DB 테이블과 1:1
├── dto/          EmpSearchCond, EmpForm, DeptForm, DeptListRow, PageResult, DashboardStats, LoginForm, LoginMember
├── controller/   EmpController, DeptController, HomeController, LoginController
├── service/      *Service(인터페이스) + *ServiceImpl
└── mapper/       *Mapper 인터페이스 (SQL은 resources/mapper/*.xml)
```

## 테스트

- `./gradlew test` : DB 없이 도는 단위/슬라이스 테스트만 실행(도메인·DTO 순수 테스트, Mockito로
  매퍼를 흉내 낸 Service 테스트, `@WebMvcTest` + `@MockitoBean` Controller 테스트). CI·평소 개발에서 이걸 씁니다.
- `./gradlew integrationTest` : `@Tag("integration")`이 붙은 `EmpMapperTest`/`DeptMapperTest`를 실행합니다.
  **`hr` MySQL 데이터베이스가 떠 있어야 합니다** — `@SpringBootTest` + `@Transactional`로 진짜 SQL을
  검증하고, 각 테스트가 끝나면 자동 롤백되어 데이터가 더러워지지 않습니다.
- 자세한 내용과 작성법은 `교안/3. 스프링/8. JUnit 테스트/1_JUnit테스트.md`, `2_Mock테스트.md` 참고.

## 설계 문서와의 대응

- **부서 삭제 정책**: 설계 실습답안 문제6의 정책 ①(소속 사원이 있으면 삭제를 막고 안내) 을
  `DeptServiceImpl.remove()` + `DeptInUseException` 로 구현했습니다.
- **도메인 vs DTO**: `Emp.deptName`/`jobName`/`managerName` 은 조인으로 채우는 읽기 전용 필드,
  `DeptListRow.memberCount`/`avgSalary` 는 집계(GROUP BY) 결과라 전용 DTO로 분리했습니다(설계 5.2, 실습 문제5).
- **로그인(R6)**: 이 저장소의 설계는 Day 12(로그인과 권한)에서 실제 인증을 다루도록 되어 있습니다.
  지금은 회원 테이블 없이 고정 계정(`admin`/`1234`, `LoginServiceImpl`) + 세션 + `LoginCheckInterceptor`
  로 "로그인한 담당자만 사용" 요구사항만 최소로 만족시켜 두었습니다. 실제 인증(회원 테이블, 암호화,
  권한 분리)은 후속 챕터에서 교체하면 됩니다.
