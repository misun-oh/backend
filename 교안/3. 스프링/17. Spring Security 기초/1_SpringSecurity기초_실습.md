# Day 17. Spring Security 기초 — 실습

준비: `spring-boot-starter-security` 추가, `MEMBER`/`MEMBER_ROLE` 테이블 생성(교안 4절).

## 문제 1. 최소 설정 + 관찰

`starter-security` 만 추가하고 앱을 실행하세요.
- `/emps` 접속 시 어떤 화면이 뜨나요?
- 콘솔에서 `Using generated security password:` 를 찾아, 사용자 `user` + 그 비번으로 로그인해 보세요.

## 문제 2. `SecurityFilterChain`

`SecurityConfig` 를 만들어:
- `/`, `/login`, `/signup`, `/css/**`, `/js/**`, `/uploads/**` → `permitAll`
- `/depts/**` → `hasRole("ADMIN")`
- 나머지 → `authenticated`
- 폼 로그인: `loginPage("/login")`, 성공 시 `/emps`, 실패 시 `/login?error`
- 로그아웃: `/logout` → `/login?logout`
- `PasswordEncoder` 빈 = `BCryptPasswordEncoder`

## 문제 3. 회원가입

- `SignupForm`(username, password, name — 검증 애노테이션)
- `MemberMapper`(existsByUsername, insert(useGeneratedKeys), insertRole, findByUsername, findRoles)
- `MemberService.signup(form)` : 중복 체크 → `passwordEncoder.encode` → `MEMBER` INSERT → `MEMBER_ROLE`('USER')
- `GET /signup` 폼, `POST /signup` 처리 → `redirect:/login?signup`

가입 후 **DB의 `PASSWORD` 컬럼 값**을 확인하고, `1234` 같은 평문이 아니라 `$2a$...` 인지 확인하세요.

## 문제 4. `UserDetailsService`

`DbUserDetailsService implements UserDetailsService` 를 만들어 `MEMBER` + `MEMBER_ROLE` 로
`UserDetails` 를 구성(권한에 `ROLE_` 접두어). 가입한 계정으로 `/login` 로그인 → `/emps` 진입 확인.

## 문제 5. 권한 분리

- `USER` 계정과 `ADMIN` 계정(직접 `MEMBER_ROLE` 에 'ADMIN' INSERT)을 하나씩 만들기
- `USER` 로 `/depts` 접근 → 403
- `ADMIN` 으로 `/depts` 접근 → 200
- `login.html` 에 `${param.error}`, `${param.logout}`, `${param.signup}` 메시지 표시

## 문제 6. (진단) 이 설정의 문제

```java
http.authorizeHttpRequests(auth -> auth
    .anyRequest().authenticated()
    .requestMatchers("/login", "/css/**").permitAll()
);
```
로그인 자체가 안 되고 무한 리다이렉트가 발생한다. 왜인가? 고치세요.
