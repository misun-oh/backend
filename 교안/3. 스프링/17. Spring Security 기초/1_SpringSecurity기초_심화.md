# Day 17. Spring Security 기초 — 심화 (경험자용)

## 1. 필터 체인 상세 (주요 필터 순서)

`DisableEncodeUrlFilter` → `WebAsyncManagerIntegrationFilter` → `SecurityContextHolderFilter`
→ `HeaderWriterFilter` → `CsrfFilter` → `LogoutFilter`
→ `UsernamePasswordAuthenticationFilter`(폼) / `BearerTokenAuthenticationFilter`(JWT)
→ `RequestCacheAwareFilter` → `SecurityContextHolderAwareRequestFilter`
→ `AnonymousAuthenticationFilter` → `SessionManagementFilter`
→ `ExceptionTranslationFilter`(401/403 처리, 로그인 페이지로 리다이렉트)
→ `AuthorizationFilter`(인가).

커스텀 필터: `http.addFilterBefore(myFilter, UsernamePasswordAuthenticationFilter.class)` (Day 19 JWT).

## 2. `AuthenticationManager` / `AuthenticationProvider`

- `DaoAuthenticationProvider` = `UserDetailsService` + `PasswordEncoder`.
- 다중 인증(폼 + LDAP + OAuth2): 여러 `AuthenticationProvider` 를 `AuthenticationManager` 에.
- 프로그래매틱 로그인: `authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(...))`.

## 3. `DelegatingPasswordEncoder`

`PasswordEncoderFactories.createDelegatingPasswordEncoder()` — 해시에 `{bcrypt}`, `{argon2}` 접두어로
알고리즘을 식별. 마이그레이션(옛 해시 → 새 알고리즘) 시 유용. 신규는 Argon2id 권장(메모리-하드).
`BCryptPasswordEncoder` 단독도 실무에서 여전히 널리 쓰임(cost 12 정도).

## 4. 인가 표현식 & 메서드 보안

```java
@EnableMethodSecurity            // @PreAuthorize 활성
...
@PreAuthorize("hasRole('ADMIN')")
public void deleteEmp(Long id) {}

@PreAuthorize("hasRole('ADMIN') or #username == authentication.name")
public MemberDto getMember(String username) {}

@PostAuthorize("returnObject.owner == authentication.name")
```
`@PreAuthorize` 는 AOP(Day 6) — 내부 호출·public 규칙 동일. 컨트롤러 인가는 `authorizeHttpRequests`,
서비스 도메인 규칙은 메서드 보안으로 이중.

## 5. 세션 관리

```java
http.sessionManagement(s -> s
    .maximumSessions(1)                       // 동시 로그인 1개
    .maxSessionsPreventsLogin(false)          // 새 로그인 시 기존 세션 만료
    .expiredUrl("/login?expired"));
```
- 세션 고정 공격 방지: 로그인 성공 시 세션 ID 교체(기본 `changeSessionId`).
- 분산 환경: `spring-session-jdbc`/`redis` 로 세션 외부화(스티키 세션 불필요).
- 세션 타임아웃: `server.servlet.session.timeout=30m`.

## 6. 인가 실패 처리

- 미인증(401) → `AuthenticationEntryPoint` → 폼은 로그인 페이지, API는 401 JSON.
- 인증됐으나 권한 없음(403) → `AccessDeniedHandler` → `/error/403` 또는 JSON.
```java
http.exceptionHandling(e -> e
    .authenticationEntryPoint((req,res,ex) -> res.sendError(401))
    .accessDeniedHandler((req,res,ex) -> res.sendError(403)));
```

## 7. `SecurityContext` 접근

- 컨트롤러: `@AuthenticationPrincipal UserDetails user` (Day 18).
- 아무 데서나: `SecurityContextHolder.getContext().getAuthentication()`.
- 비동기/스레드: `SecurityContextHolder` 는 `ThreadLocal`. `@Async` 는 전파 안 됨 →
  `DelegatingSecurityContextExecutor` 또는 `MODE_INHERITABLETHREADLOCAL`.

## 8. 커스텀 `UserDetails`

`User` 대신 `Member` 를 감싼 `MemberPrincipal implements UserDetails` 를 반환하면
`@AuthenticationPrincipal MemberPrincipal me` 로 `me.getMember().getName()` 등 도메인 정보 접근(Day 18).

## 9. 테스트

```java
@WebMvcTest(EmpViewController.class)
class SecuredTest {
    @Autowired MockMvc mvc;

    @Test void 미인증은_로그인으로() throws Exception {
        mvc.perform(get("/emps")).andExpect(status().is3xxRedirection())
           .andExpect(redirectedUrlPattern("**/login"));
    }
    @Test @WithMockUser(roles = "USER")
    void 인증되면_200() throws Exception {
        mvc.perform(get("/emps")).andExpect(status().isOk());
    }
    @Test @WithMockUser(roles = "USER")
    void ADMIN_전용은_403() throws Exception {
        mvc.perform(get("/depts")).andExpect(status().isForbidden());
    }
}
```
`@WithMockUser`, `@WithUserDetails`, `SecurityMockMvcRequestPostProcessors.user(...)`, `.csrf()`.
