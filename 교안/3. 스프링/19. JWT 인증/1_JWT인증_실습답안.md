# Day 19. JWT 인증 — 실습 답안

---

## 문제 1. `JwtProvider`

교안 4절 그대로. 테스트:

```java
class JwtProviderTest {

    JwtProvider provider = new JwtProvider("dev-secret-please-change-this-32bytes-min!!", 30);

    @Test void 생성_파싱() {
        String token = provider.createAccessToken("admin", List.of("ROLE_ADMIN"));
        assertThat(token.split("\\.")).hasSize(3);

        Claims c = provider.parse(token).getPayload();
        assertThat(c.getSubject()).isEqualTo("admin");
        assertThat(c.get("roles", List.class)).containsExactly("ROLE_ADMIN");
    }

    @Test void 다른_시크릿이면_서명오류() {
        String token = provider.createAccessToken("admin", List.of("ROLE_ADMIN"));
        JwtProvider other = new JwtProvider("another-secret-32bytes-1234567890abc!!", 30);
        assertThatThrownBy(() -> other.parse(token)).isInstanceOf(JwtException.class);
    }

    @Test void 만료된_토큰() {
        JwtProvider shortLived = new JwtProvider("dev-secret-please-change-this-32bytes-min!!", 0);
        // exp = now + 0분 → 곧 만료. 약간 대기하거나 clockSkew 0
        String token = shortLived.createAccessToken("admin", List.of("ROLE_ADMIN"));
        await().atMost(2, SECONDS).untilAsserted(() ->
            assertThatThrownBy(() -> shortLived.parse(token)).isInstanceOf(ExpiredJwtException.class));
    }
}
```

---

## 문제 2. 로그인 API

```java
public record LoginRequest(@NotBlank String username, @NotBlank String password) {}
public record TokenResponse(String accessToken, String refreshToken) {}

@Configuration
public class AuthConfig {
    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration c) throws Exception {
        return c.getAuthenticationManager();
    }
}

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final JwtProvider jwtProvider;

    @PostMapping("/login")
    public TokenResponse login(@RequestBody @Valid LoginRequest req) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.username(), req.password()));
        List<String> roles = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority).toList();
        return new TokenResponse(jwtProvider.createAccessToken(auth.getName(), roles), null);
    }
}
```
인증 실패 시 `AuthenticationException` → API 체인의 `authenticationEntryPoint` 또는
`@RestControllerAdvice` 에서 401.

---

## 문제 3. 필터 + stateless 체인

```java
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {
    private final JwtProvider jwtProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        String h = req.getHeader(HttpHeaders.AUTHORIZATION);
        if (h != null && h.startsWith("Bearer ")) {
            try {
                Claims c = jwtProvider.parse(h.substring(7)).getPayload();
                List<String> roles = c.get("roles", List.class);
                var authorities = roles.stream().map(SimpleGrantedAuthority::new).toList();
                SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken(c.getSubject(), null, authorities));
            } catch (JwtException e) {
                SecurityContextHolder.clearContext();
            }
        }
        chain.doFilter(req, res);
    }
}
```

```java
@Bean @Order(1)
SecurityFilterChain apiChain(HttpSecurity http, JwtAuthFilter jwtAuthFilter) throws Exception {
    http.securityMatcher("/api/**")
        .csrf(c -> c.disable())
        .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(a -> a
            .requestMatchers("/api/auth/**").permitAll()
            .requestMatchers(HttpMethod.DELETE, "/api/emps/**").hasRole("ADMIN")
            .anyRequest().authenticated())
        .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
        .exceptionHandling(e -> e
            .authenticationEntryPoint((rq, rs, ex) -> rs.sendError(401))
            .accessDeniedHandler((rq, rs, ex) -> rs.sendError(403)));
    return http.build();
}
// 폼 로그인 체인 = @Order(2), securityMatcher 없이 나머지 전부
```

---

## 문제 4. 흐름 검증

```java
@SpringBootTest
@AutoConfigureMockMvc
class JwtFlowTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;

    private String login(String u, String p) throws Exception {
        String body = mvc.perform(post("/api/auth/login").contentType(APPLICATION_JSON)
                .content(om.writeValueAsString(new LoginRequest(u, p))))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return om.readTree(body).get("accessToken").asText();
    }

    @Test void 로그인_토큰_API() throws Exception {
        String token = login("admin", "1234");
        mvc.perform(get("/api/emps").header("Authorization", "Bearer " + token))
           .andExpect(status().isOk());
    }
    @Test void 토큰없음_401() throws Exception {
        mvc.perform(get("/api/emps")).andExpect(status().isUnauthorized());
    }
    @Test void 잘못된토큰_401() throws Exception {
        mvc.perform(get("/api/emps").header("Authorization", "Bearer abc.def.ghi"))
           .andExpect(status().isUnauthorized());
    }
    @Test void USER가_DELETE_403() throws Exception {
        String token = login("user", "1234");
        mvc.perform(delete("/api/emps/205").header("Authorization", "Bearer " + token))
           .andExpect(status().isForbidden());
    }
}
```

---

## 문제 5. 만료와 재발급

- `jwt.access-exp-min: 0` → 로그인 직후 1~2초 뒤 `GET /api/emps` → **401**(`ExpiredJwtException` →
  필터가 인증 안 채움 → entryPoint 401).

```java
@PostMapping("/refresh")
public TokenResponse refresh(@RequestBody RefreshRequest req) {
    Claims c = jwtProvider.parse(req.refreshToken()).getPayload();   // 서명·만료 검증
    // (실무) 저장소에서 이 refresh 가 유효한지 조회 + 회전(새 refresh 발급, 기존 폐기)
    List<String> roles = c.get("roles", List.class);
    String access = jwtProvider.createAccessToken(c.getSubject(), roles);
    return new TokenResponse(access, req.refreshToken());
}
```
> 개념: refresh 토큰은 서버 DB/Redis에 저장했다가 로그아웃 시 삭제해야 "재발급 불가"가 된다.
> 저장 없이 서명만 검증하면 탈취된 refresh 를 막을 수 없다.

---

## 문제 6. 세션 vs JWT 판단

1. **세션** — 서버 1대라 세션 공유 부담 없고, "즉시 강제 로그아웃"은 세션 무효화 한 줄. JWT는 stateless라 즉시 폐기가 어렵다.
2. **JWT** — 웹·모바일이 같은 REST를 쓰고 서버 4대. 쿠키/세션 공유보다 헤더 토큰이 크로스도메인·확장에 유리.
3. **JWT(OAuth2)** — 외부 파트너에겐 세션 쿠키를 줄 수 없다. 만료·스코프가 담긴 토큰(가능하면 RS256 + JWKS).
4. **미니 프로젝트: 세션** — 주로 Thymeleaf 화면이고 단일 서버, Day 17~18에서 만든 폼 로그인·`sec:authorize`
   를 그대로 활용. API 일부만 있다면 그 경로만 JWT 체인을 추가로 둘 수 있다(8절의 이중 체인).
