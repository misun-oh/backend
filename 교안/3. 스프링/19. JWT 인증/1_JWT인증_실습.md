# Day 19. JWT 인증 — 실습

준비: `jjwt-api` / `jjwt-impl` / `jjwt-gson`(0.13.0) 추가 — Boot 4는 Jackson 3라 `jjwt-jackson` 대신 `jjwt-gson`. `application.yml` 에
`jwt.secret`(32바이트 이상), `jwt.access-exp-min: 30`.

## 문제 1. `JwtProvider`

교안 4절의 `JwtProvider` 를 만들고, 단위 테스트로:
- `createAccessToken("admin", ["ROLE_ADMIN"])` 가 `a.b.c` 형태 문자열
- `parse(token)` 의 `getSubject()` 가 `"admin"`, `roles` 클레임이 `["ROLE_ADMIN"]`
- 시크릿을 다르게 만든 `JwtProvider` 로 parse → `SignatureException`(또는 `JwtException`)
- 만료 시간을 `-1분` 으로 만든 토큰 parse → `ExpiredJwtException`

## 문제 2. 로그인 API

- `LoginRequest(username, password)`, `TokenResponse(accessToken, refreshToken)`
- `AuthenticationManager` 빈 노출
- `POST /api/auth/login` → 인증 성공 시 access 토큰 반환, 실패 시 401

가입한 계정으로 `curl -X POST /api/auth/login -d '{"username":"...","password":"..."}'` → 토큰 확인.

## 문제 3. `JwtAuthFilter` + stateless 체인

- `JwtAuthFilter extends OncePerRequestFilter` (교안 6절)
- `/api/**` 전용 `SecurityFilterChain`(`@Order(1)`, `securityMatcher("/api/**")`, `STATELESS`,
  `csrf().disable()`, `/api/auth/**` permitAll, `addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)`)
- Day 17 폼 로그인 체인은 `@Order(2)` 로 유지

## 문제 4. 흐름 검증 (MockMvc)

`@SpringBootTest @AutoConfigureMockMvc` 로:
- `POST /api/auth/login` → 200 + `$.accessToken`
- 그 토큰으로 `GET /api/emps` (`Authorization: Bearer ...`) → 200
- 토큰 없이 `GET /api/emps` → 401
- 잘못된 토큰(`Bearer abc.def.ghi`) → 401
- `ADMIN` 아닌 토큰으로 `DELETE /api/emps/{id}` (ADMIN 필요) → 403

## 문제 5. 만료와 재발급 (개념 + 최소 구현)

- `access-exp-min: 0` (즉시 만료 근처)로 낮춰, 로그인 직후 잠깐 뒤 `GET /api/emps` → 401 확인.
- `POST /api/auth/refresh` 를 최소한으로 구현(refresh 토큰 검증 → 새 access 발급).
  refresh 저장/회전은 개념만 주석으로 설명.

## 문제 6. (개념) 판단

다음 각 상황에서 **세션 vs JWT** 중 무엇이 적합한지 한 줄 근거와 함께 고르세요.

1. 사내 관리자 웹(Thymeleaf), 서버 1대, "부정 사용자 즉시 로그아웃" 이 중요
2. React 웹 + iOS/Android 앱이 같은 REST API를 사용, 서버 4대
3. 파트너사에 사원 데이터 조회 API를 개방
4. 이 과정 미니 프로젝트(사원관리, 주로 화면) — 무엇을 골랐고 왜?
