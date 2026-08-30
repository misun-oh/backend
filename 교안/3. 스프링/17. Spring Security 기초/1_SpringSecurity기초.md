# Day 17. Spring Security 기초

| 항목 | 내용 |
|---|---|
| 선수학습 | Day 7(MVC·필터), Day 14(폼), Day 9(MyBatis) |
| 이번 챕터 | 시큐리티 필터 체인 → `SecurityFilterChain` 설정 → 폼 로그인/로그아웃 → `UserDetailsService` + `PasswordEncoder`(BCrypt) → 경로별 권한(`hasRole`) → 세션 → 회원가입 |
| 권장 진행 | 1일 |
| 의존성 | `implementation 'org.springframework.boot:spring-boot-starter-security'` |
| DB | `MEMBER`(로그인 계정), `MEMBER_ROLE`(권한) 테이블 추가 |
| 결과물 | 로그인해야 `/emps` 진입, 프로토타입의 로그인 모달이 실제 인증으로 |

## 학습목표

- 시큐리티가 서블릿 **필터 체인**으로 동작한다는 큰 그림을 안다.
- `SecurityFilterChain` 빈으로 경로별 인가·폼 로그인·로그아웃·CSRF를 설정한다.
- 비밀번호를 **BCrypt로 해시**해 저장하고, `UserDetailsService` 로 로그인 사용자를 로딩한다.
- `ROLE_` 접두어와 `hasRole`/`hasAuthority` 를 구분한다.
- 세션 기반 인증의 흐름(로그인 → JSESSIONID → 이후 요청)을 안다.
- 회원가입 → 로그인 → 보호된 페이지 접근을 구현한다.

---

## 1. 큰 그림 — 필터 체인

`spring-boot-starter-security` 만 추가해도 **모든 요청에 로그인**이 걸립니다(기본 폼 + 랜덤 비번 콘솔 출력).

동작 방식: 스프링 시큐리티는 하나의 서블릿 필터(`FilterChainProxy`) 안에 여러 **보안 필터**를 순서대로 둡니다.

```
요청 → [CSRF] → [로그인 처리(UsernamePasswordAuthenticationFilter)]
     → [세션에서 인증 복원(SecurityContextPersistenceFilter)]
     → [인가 검사(AuthorizationFilter)] → DispatcherServlet → 컨트롤러
```

- 인증(Authentication): "너 누구야?" — 로그인.
- 인가(Authorization): "이 페이지 볼 자격 있어?" — 권한 검사.
- 인증 결과는 `SecurityContext` 에 담기고, 세션(`HttpSession`)에 저장되어 다음 요청에 복원됩니다.

---

## 2. `SecurityFilterChain` 설정

```java
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/login", "/signup", "/css/**", "/js/**", "/uploads/**").permitAll()
                .requestMatchers("/depts/**").hasRole("ADMIN")
                .anyRequest().authenticated()               // 그 외 전부 로그인 필요
            )
            .formLogin(form -> form
                .loginPage("/login")                        // 우리가 만든 로그인 화면
                .loginProcessingUrl("/login")               // 폼 action (POST) — 시큐리티가 가로챔
                .usernameParameter("username")
                .passwordParameter("password")
                .defaultSuccessUrl("/emps", true)
                .failureUrl("/login?error")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
            )
            .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**"));   // API는 CSRF 제외(Day 19 JWT)

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

- `authorizeHttpRequests` : **위에서 아래로** 매칭, 첫 매칭 규칙 적용. 구체적인 경로를 먼저.
- `permitAll` : 인증 없이 허용(정적 리소스·로그인·회원가입).
- `formLogin.loginPage("/login")` : 이 URL을 우리가 `@Controller` 로 만들어야 함.
- `csrf` : 폼 기반은 CSRF 토큰이 기본 활성(3절). REST/JWT는 제외.

---

## 3. CSRF 토큰 (폼)

시큐리티는 `POST`/`PUT`/`DELETE` 요청에 **CSRF 토큰**을 요구합니다(다른 사이트가 우리 세션으로
몰래 요청하는 것 방지).

Thymeleaf + 스프링 시큐리티 연동이면 `<form>` 에 **자동으로 hidden 토큰이 삽입**됩니다(별도 태그 불필요).
`th:action` 을 쓴 `<form method="post">` 면 됩니다. `fetch` 로 POST 할 땐 메타 태그로 토큰을 읽어 헤더에.

```html
<meta name="_csrf" th:content="${_csrf.token}">
<meta name="_csrf_header" th:content="${_csrf.headerName}">
```

---

## 4. 사용자 저장 — `MEMBER` / `MEMBER_ROLE`

```sql
CREATE TABLE MEMBER (
  MEMBER_ID  BIGINT AUTO_INCREMENT PRIMARY KEY,
  USERNAME   VARCHAR(50)  NOT NULL UNIQUE,
  PASSWORD   VARCHAR(100) NOT NULL,          -- BCrypt 해시 ($2a$...)
  NAME       VARCHAR(50)  NOT NULL,
  ENABLED    CHAR(1)      NOT NULL DEFAULT 'Y',
  CREATED_AT DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE MEMBER_ROLE (
  MEMBER_ID BIGINT       NOT NULL,
  ROLE      VARCHAR(20)  NOT NULL,           -- 'USER', 'ADMIN' (접두어 ROLE_ 은 코드에서)
  PRIMARY KEY (MEMBER_ID, ROLE),
  CONSTRAINT FK_MR_MEMBER FOREIGN KEY (MEMBER_ID) REFERENCES MEMBER(MEMBER_ID)
);
```

---

## 5. 비밀번호 해시 — BCrypt

**절대 평문으로 저장하지 않습니다.** 회원가입 시:

```java
@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberMapper memberMapper;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public Long signup(SignupForm form) {
        if (memberMapper.existsByUsername(form.getUsername()))
            throw new BusinessException("DUP_USERNAME", "이미 사용 중인 아이디입니다");

        Member m = Member.builder()
                .username(form.getUsername())
                .password(passwordEncoder.encode(form.getPassword()))   // ★ 해시
                .name(form.getName())
                .enabled(true)
                .build();
        memberMapper.insert(m);
        memberMapper.insertRole(m.getMemberId(), "USER");
        return m.getMemberId();
    }
}
```

- `BCryptPasswordEncoder.encode("1234")` → `$2a$10$...`(60자, 매번 다름 — salt 포함).
- 로그인 시 시큐리티가 `passwordEncoder.matches(입력, 저장된해시)` 로 비교.
- BCrypt는 느리게 설계됨(무차별 대입 방어). cost(기본 10) 조정 가능.

---

## 6. `UserDetailsService` — 로그인 사용자 로딩

시큐리티가 "이 username 사용자 정보 줘" 할 때 호출.

```java
@Service
@RequiredArgsConstructor
public class DbUserDetailsService implements UserDetailsService {

    private final MemberMapper memberMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Member m = memberMapper.findByUsername(username);
        if (m == null) throw new UsernameNotFoundException(username);

        List<String> roles = memberMapper.findRoles(m.getMemberId());   // ["USER", "ADMIN"]

        return User.builder()
                .username(m.getUsername())
                .password(m.getPassword())                              // 저장된 해시 그대로
                .disabled(!m.isEnabled())
                .authorities(roles.stream()
                        .map(r -> "ROLE_" + r)                          // ★ ROLE_ 접두어
                        .toArray(String[]::new))
                .build();
    }
}
```

- 반환하는 `UserDetails.getPassword()` 는 **해시**. 시큐리티가 알아서 `matches` 비교.
- 권한 문자열에 `ROLE_` 를 붙여 저장 → 설정에서는 `hasRole("ADMIN")`(접두어 자동), 세밀한 권한은
  `hasAuthority("EMP_DELETE")` 처럼 `ROLE_` 없이.

---

## 7. 로그인 화면 & 흐름

```java
@Controller
public class AuthController {
    @GetMapping("/login")  public String login()  { return "auth/login"; }
    @GetMapping("/signup") public String signup(Model m) { m.addAttribute("form", new SignupForm()); return "auth/signup"; }

    @PostMapping("/signup")
    public String doSignup(@Valid @ModelAttribute("form") SignupForm form, BindingResult br) {
        if (br.hasErrors()) return "auth/signup";
        memberService.signup(form);
        return "redirect:/login?signup";
    }
    // 로그인 처리(POST /login)는 시큐리티가 가로채므로 우리 메서드 불필요
}
```

```html
<!-- auth/login.html -->
<form th:action="@{/login}" method="post">
  <input class="input" name="username" placeholder="아이디">
  <input class="input" name="password" type="password" placeholder="비밀번호">
  <button class="btn btn--primary" type="submit">로그인</button>
  <p class="form-hint" th:if="${param.error}">아이디 또는 비밀번호가 올바르지 않습니다</p>
  <p class="form-hint" th:if="${param.logout}">로그아웃되었습니다</p>
</form>
```

**세션 흐름**:
1. `POST /login` (username/password) → 시큐리티가 `UserDetailsService` 로 사용자 로딩 + `matches` 검증
2. 성공 → `SecurityContext` 에 인증 저장 → `HttpSession` 에 저장 → `Set-Cookie: JSESSIONID=...`
3. 이후 요청은 쿠키의 `JSESSIONID` 로 세션 조회 → 인증 복원 → 인가 검사

---

## 자주 하는 실수

- **비밀번호 평문 저장** → `passwordEncoder.encode()` 필수. 저장·비교 모두 인코더 경유.
- **`ROLE_` 접두어 혼동** → DB엔 `ADMIN`, 권한 부여 시 `ROLE_ADMIN`, 설정은 `hasRole("ADMIN")`.
  `hasRole` 은 `ROLE_` 자동 부여, `hasAuthority` 는 문자열 그대로.
- **로그인 페이지·CSS를 `permitAll` 안 함** → 로그인 화면이 로그인 필요 → 무한 리다이렉트.
- **`loginProcessingUrl` 과 폼 `action` 불일치** → 로그인 안 됨.
- **폼인데 CSRF 끄고 방치** → CSRF 공격 노출. 폼은 켜두고 API/JWT만 제외.
- **`@EnableWebSecurity` 빠뜨림**(부트 3에선 스타터만으로도 되지만 명시 권장) / `SecurityFilterChain` 빈 안 만듦 → 기본 설정(전부 인증).
- **`authorizeHttpRequests` 순서** → 넓은 규칙(`anyRequest`)이 먼저면 뒤 규칙 무시.

---

## 핵심 요약

| 요소 | 내용 |
|---|---|
| 필터 체인 | 요청이 보안 필터들을 거쳐 인증·인가 후 컨트롤러 |
| `SecurityFilterChain` 빈 | 경로 인가 + 폼 로그인 + 로그아웃 + CSRF |
| `PasswordEncoder`(BCrypt) | 회원가입 시 `encode`, 로그인 시 시큐리티가 `matches` |
| `UserDetailsService` | username → `UserDetails`(해시 비번 + `ROLE_*` 권한) |
| `hasRole("ADMIN")` | 내부적으로 `ROLE_ADMIN`. 세밀 권한은 `hasAuthority` |
| 세션 | 로그인 후 `JSESSIONID` 쿠키로 인증 유지 |
| CSRF | 폼은 활성(Thymeleaf 자동 토큰), API는 제외 |

> 다음(Day 18): 로그인 사용자를 화면·로직에서 쓰고, 권한별로 메뉴를 보이거나 숨긴다 — 인증 심화.
