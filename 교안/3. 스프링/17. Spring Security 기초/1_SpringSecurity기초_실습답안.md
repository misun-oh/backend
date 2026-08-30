# Day 17. Spring Security 기초 — 실습 답안

---

## 문제 1. 최소 설정 관찰

- `/emps` 접속 → 스프링 시큐리티 **기본 로그인 폼**(`/login`, 시큐리티 자동 생성)으로 리다이렉트.
- 콘솔:
  ```
  Using generated security password: 3f2a1b9c-...
  ```
  사용자명 `user`, 이 UUID 비번으로 로그인하면 통과. (재기동마다 비번 바뀜 → 실서비스 불가, 그래서 2~4번)

---

## 문제 2. `SecurityFilterChain`

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
          .authorizeHttpRequests(auth -> auth
              .requestMatchers("/", "/login", "/signup", "/css/**", "/js/**", "/uploads/**").permitAll()
              .requestMatchers("/depts/**").hasRole("ADMIN")
              .anyRequest().authenticated())
          .formLogin(f -> f
              .loginPage("/login")
              .defaultSuccessUrl("/emps", true)
              .failureUrl("/login?error")
              .permitAll())
          .logout(l -> l
              .logoutUrl("/logout")
              .logoutSuccessUrl("/login?logout"));
        return http.build();
    }

    @Bean PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }
}
```

---

## 문제 3. 회원가입

```java
public record SignupForm(
    @NotBlank @Size(min = 4, max = 20) String username,
    @NotBlank @Size(min = 4) String password,
    @NotBlank String name) {}

@Mapper
public interface MemberMapper {
    boolean existsByUsername(String username);
    int insert(Member m);                    // useGeneratedKeys keyProperty="memberId"
    int insertRole(@Param("memberId") Long id, @Param("role") String role);
    Member findByUsername(String username);
    List<String> findRoles(Long memberId);
}

@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberMapper memberMapper;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public Long signup(SignupForm form) {
        if (memberMapper.existsByUsername(form.username()))
            throw new BusinessException("DUP_USERNAME", "이미 사용 중인 아이디입니다");
        Member m = Member.builder()
                .username(form.username())
                .password(passwordEncoder.encode(form.password()))
                .name(form.name())
                .enabled(true).build();
        memberMapper.insert(m);
        memberMapper.insertRole(m.getMemberId(), "USER");
        return m.getMemberId();
    }
}
```

가입 후 `SELECT PASSWORD FROM MEMBER` → `$2a$10$N9qo8uLOickgx2ZMRZoMy...` (60자, 매번 다름).

---

## 문제 4. `UserDetailsService`

```java
@Service
@RequiredArgsConstructor
public class DbUserDetailsService implements UserDetailsService {

    private final MemberMapper memberMapper;

    @Override
    public UserDetails loadUserByUsername(String username) {
        Member m = memberMapper.findByUsername(username);
        if (m == null) throw new UsernameNotFoundException(username);
        String[] auths = memberMapper.findRoles(m.getMemberId()).stream()
                .map(r -> "ROLE_" + r).toArray(String[]::new);
        return User.builder()
                .username(m.getUsername())
                .password(m.getPassword())
                .disabled(!m.isEnabled())
                .authorities(auths)
                .build();
    }
}
```

`@Bean` 으로 `DaoAuthenticationProvider` 를 명시 안 해도, `UserDetailsService` 빈 +
`PasswordEncoder` 빈이 있으면 부트가 자동 연결.

---

## 문제 5. 권한 분리

```sql
-- ADMIN 계정에 권한 추가 (가입 후)
INSERT INTO MEMBER_ROLE (MEMBER_ID, ROLE) VALUES (2, 'ADMIN');
```

- `USER` 로 로그인 → `GET /depts` → **403** (`SecurityConfig` 의 `hasRole("ADMIN")`).
- `ADMIN` 로그인 → `GET /depts` → **200**.

```html
<p class="form-hint" th:if="${param.error}">아이디 또는 비밀번호가 올바르지 않습니다</p>
<p class="form-hint" th:if="${param.logout}">로그아웃되었습니다</p>
<p class="form-hint" th:if="${param.signup}">가입이 완료되었습니다. 로그인하세요</p>
```

---

## 문제 6. 이 설정의 문제

```java
.anyRequest().authenticated()
.requestMatchers("/login", "/css/**").permitAll()
```

`authorizeHttpRequests` 는 **위에서 아래로 첫 매칭**을 적용한다. `anyRequest()` 가 먼저라
**모든 요청**(로그인 페이지·CSS 포함)이 `authenticated` 로 걸린다 → 미인증 사용자가 `/login` 에 가려면
로그인이 필요 → `/login` 으로 리다이렉트 → 다시 인증 필요 → **무한 리다이렉트**.

**고침**: 구체적인 규칙을 먼저, `anyRequest()` 를 마지막에.
```java
.requestMatchers("/login", "/css/**").permitAll()
.anyRequest().authenticated()
```
