# Day 19. JWT 인증 — 심화 (경험자용)

## 1. 알고리즘: HS256 vs RS256/ES256

- **HS256**(HMAC): 대칭키. 발급·검증이 같은 시크릿. 단일 서비스에 단순.
- **RS256/ES256**(RSA/EC): 개인키로 서명, **공개키로 검증**. 인증 서버 1곳 발급, 여러 리소스 서버가
  공개키(JWKS)로 검증. 마이크로서비스·SSO(OIDC)에 적합.
- 키 롤링: `kid`(헤더)로 키 식별, JWKS 엔드포인트에서 여러 키 노출.

## 2. Spring Security OAuth2 Resource Server (권장)

직접 필터를 짜는 대신 표준 지원:
```groovy
implementation 'org.springframework.boot:spring-boot-starter-oauth2-resource-server'
```
```java
http.oauth2ResourceServer(o -> o.jwt(j -> j.jwtAuthenticationConverter(conv)));
```
```yaml
spring.security.oauth2.resourceserver.jwt:
  secret-key: ${JWT_SECRET}          # HS256
  # 또는 issuer-uri / jwk-set-uri    # RS256
```
- `JwtDecoder` 자동 구성, `Jwt` principal, `@AuthenticationPrincipal Jwt jwt`.
- 커스텀 클레임 → 권한 매핑: `JwtGrantedAuthoritiesConverter`(`claimName("roles")`, prefix `"ROLE_"`).
- 교안 6절의 수제 필터는 "원리 이해용", 실무는 이 스타터.

## 3. Refresh 토큰 회전(rotation)

- Refresh 사용 시마다 **새 Refresh 발급 + 기존 폐기**. 탈취된 옛 토큰 재사용 감지 → 그 사용자 전체 세션 무효화.
- 저장: `REFRESH_TOKEN(member_id, token_hash, expires_at, device, revoked)`. 토큰은 해시로 저장.
- 쿠키에 Refresh: `HttpOnly; Secure; SameSite=Strict; Path=/api/auth/refresh`.
  Access는 자바스크립트 메모리(리로드 시 refresh로 복구).

## 4. 로그아웃 / 강제 만료 전략

stateless의 근본 한계 대응:
1. **짧은 Access(5~15분)** + Refresh 폐기 — 가장 흔함.
2. **블랙리스트**: 로그아웃한 `jti` 를 Redis에 `exp` 까지 TTL 저장. 필터에서 조회 → 사실상 상태 有.
3. **토큰 버전**: `MEMBER.token_version` 을 클레임에 포함. 비번 변경·강제 로그아웃 시 버전 증가 →
   구버전 토큰 거부. DB 조회 1회 추가.

## 5. 클레임 설계

- 넣을 것: `sub`(식별자), `roles`/`scope`, `exp`, `iat`, `jti`(무효화용), `iss`/`aud`(검증 강화).
- 넣지 말 것: 개인정보, 자주 바뀌는 값(권한이 실시간이면 토큰이 stale). 그런 건 매 요청 DB 조회.
- 크기: 헤더로 매 요청 전송되므로 페이로드 작게(수 KB면 과함).

## 6. 시계 오차 / 검증 옵션

```java
Jwts.parser().verifyWith(key)
    .clockSkewSeconds(30)          // 서버 시계 차이 허용
    .requireIssuer("hr")
    .requireAudience("hr-web")
    .build().parseSignedClaims(token);
```

## 7. 보안 함정

- **`alg: none` 공격**: 라이브러리가 서명 없는 토큰을 수락하면 위조 가능. jjwt 0.11+/`parseSignedClaims` 는 방어.
- **키 혼동(RS→HS)**: RS256 공개키를 HS256 시크릿으로 오용. 파서에 알고리즘 고정.
- **XSS로 토큰 탈취**: Access를 `localStorage` 에 두면 XSS 시 유출. 메모리 + Refresh 쿠키(HttpOnly)가 상대적으로 안전.
- **CSRF**: Bearer 헤더는 브라우저가 자동 첨부 안 하므로 CSRF 안전. 단 Refresh를 쿠키로 두면 그 엔드포인트는 CSRF 고려.

## 8. 테스트

```java
@SpringBootTest @AutoConfigureMockMvc
class JwtFlowTest {
    @Autowired MockMvc mvc; @Autowired ObjectMapper om;

    @Test void 로그인_후_토큰으로_API접근() throws Exception {
        String res = mvc.perform(post("/api/auth/login").contentType(APPLICATION_JSON)
                .content("""{"username":"admin","password":"1234"}"""))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        String token = om.readTree(res).get("accessToken").asText();

        mvc.perform(get("/api/emps").header("Authorization", "Bearer " + token))
           .andExpect(status().isOk());

        mvc.perform(get("/api/emps"))                       // 토큰 없이
           .andExpect(status().isUnauthorized());
    }
}
```
`SecurityMockMvcRequestPostProcessors.jwt()` (resource server 사용 시) 로 목 토큰 주입도 가능.
