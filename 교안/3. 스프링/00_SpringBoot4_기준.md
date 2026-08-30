# Spring Boot 4.0 기준 (이 교안의 버전·API 기준선)

이 교안은 **Spring Boot 4.0.x + JDK 17 + Gradle 8.14.x** 를 기준으로 합니다.
Boot 3.x 자료를 볼 때 달라지는 점을 여기 모읍니다. 각 챕터의 코드는 이 문서와 일치해야 합니다.

> 확인 시점: 2026-08. 최신 4.0 패치는 **4.0.8**. (4.1.x 도 안정판이나, 이 교안은 4.0 라인 고정)

---

## 1. 버전 매트릭스

| 항목 | 버전 | 비고 |
|---|---|---|
| Spring Boot | **4.0.8** (4.0.x 최신) | Spring Framework 7 · Spring Security 7 기반 |
| JDK | **17 이상** | Boot 4 최소 = 17. 3.x와 동일 |
| Gradle | **8.14.x** (또는 9.x) | Boot 4 최소 = 8.14. 래퍼는 `gradle-8.14.3-bin.zip` |
| `io.spring.dependency-management` | `1.1.7` | 그대로 |
| mybatis-spring-boot-starter | **4.0.1** | Boot 4.0 지원. MyBatis 3.5 / MyBatis-Spring 4.0. **BOM이 관리 안 하므로 버전 명시** |
| JUnit (Jupiter/Platform) | **6.0.x** | `spring-boot-starter-test` 가 가져옴. **버전 안 박음** |
| Jackson | **3.x** (Boot 4 기본) | 그룹·패키지 `com.fasterxml.jackson` → `tools.jackson` |
| JWT (jjwt) | `io.jsonwebtoken:jjwt-*` `0.13.x` | Day 19에서 명시적으로 추가 (BOM 관리 아님) |

---

## 2. `build.gradle`

```groovy
plugins {
    id 'java'
    id 'org.springframework.boot' version '4.0.8'
    id 'io.spring.dependency-management' version '1.1.7'
}

group = 'com.example'
version = '0.0.1-SNAPSHOT'

java {
    toolchain { languageVersion = JavaLanguageVersion.of(17) }
}

repositories { mavenCentral() }

dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-webmvc'      // ← 3.x: starter-web
    implementation 'org.springframework.boot:spring-boot-starter-thymeleaf'
    implementation 'org.springframework.boot:spring-boot-starter-validation'  // Day 14
    implementation 'org.springframework.boot:spring-boot-starter-security'    // Day 17~
    implementation 'org.mybatis.spring.boot:mybatis-spring-boot-starter:4.0.1' // Day 9~

    compileOnly 'org.projectlombok:lombok'
    annotationProcessor 'org.projectlombok:lombok'
    developmentOnly 'org.springframework.boot:spring-boot-devtools'
    runtimeOnly 'com.mysql:mysql-connector-j'

    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    testImplementation 'org.springframework.security:spring-security-test'    // Day 17~18
    testRuntimeOnly 'org.junit.platform:junit-platform-launcher'
}

tasks.named('test') { useJUnitPlatform() }
```

### 3.x → 4.0 스타터 이름 대응

| 3.x | 4.0 |
|---|---|
| `spring-boot-starter-web` | **`spring-boot-starter-webmvc`** |
| `spring-boot-starter-test` | 그대로 (JUnit 6·Mockito·AssertJ 포함) |
| `spring-boot-starter-validation` | 그대로 (여전히 명시 필요) |
| `spring-boot-starter-security` | 그대로 |
| `spring-boot-starter-thymeleaf` | 그대로 |
| `spring-boot-starter-jdbc` | 그대로 |

> 옛 이름(`starter-web` 등)도 당분간 동작하지만 deprecated. 새 이름을 씁니다.
> Initializr 가 4.0.x 로 생성하면 이미 `starter-webmvc` 로 나옵니다.

---

## 3. 테스트 (Day 10 · 15 · 18)

| 3.x | 4.0 | import |
|---|---|---|
| `@MockBean` | **`@MockitoBean`** | `org.springframework.test.context.bean.override.mockito.MockitoBean` |
| `@SpyBean` | **`@MockitoSpyBean`** | `...bean.override.mockito.MockitoSpyBean` |
| `@SpringBootTest` + MockMvc 자동 | `@SpringBootTest` + **`@AutoConfigureMockMvc` 명시** | `org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc` |
| `@WebMvcTest` | 그대로 (MockMvc 자동설정 유지) | — |
| "JUnit 5" | **"JUnit 6"** (Jupiter) — 패키지 `org.junit.jupiter.api.*` 동일, Java 17 필요 | — |

- `spring-boot-starter-test` 가 JUnit 6 세트를 BOM 버전으로 가져옴. `junit-jupiter` 버전 **직접 지정 금지**.
- `@Transactional` 테스트 자동 롤백 — 변화 없음.

---

## 4. Jackson 3 (Day 15)

- 기본이 **Jackson 3**. 클래스(프로그램적 사용)의 패키지가 바뀜:
  - `com.fasterxml.jackson.databind.ObjectMapper` → **`tools.jackson.databind.ObjectMapper`**
  - `Jackson2ObjectMapperBuilderCustomizer` → `JsonMapperBuilderCustomizer`
  - `@JsonComponent` → `@JacksonComponent`
- **애노테이션은 그대로** (`com.fasterxml.jackson.annotation.*`):
  `@JsonInclude` · `@JsonFormat` · `@JsonProperty` · `@JsonIgnore` — import 변화 없음.
  → DTO에 애노테이션만 붙이는 이 교안 방식은 **대부분 영향 없음**.
- 프로퍼티: `spring.jackson.read.*` / `write.*` → `spring.jackson.json.read.*` / `json.write.*`
- 급하면 `spring.jackson.use-jackson2-defaults=true` 로 2 동작 유지(임시).

---

## 5. Spring Security 7 (Day 17~19)

- `http.authorizeRequests()` / `antMatchers()` / `.and()` — **전부 제거**(6.x에서 deprecated 되던 것).
  → **람다 DSL + `authorizeHttpRequests` + `requestMatchers`** 만 사용:
  ```java
  @Bean
  SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
      http
          .authorizeHttpRequests(auth -> auth
              .requestMatchers("/css/**", "/js/**", "/login", "/signup").permitAll()
              .requestMatchers("/depts/**", "/emps/*/delete").hasRole("ADMIN")
              .anyRequest().authenticated())
          .formLogin(form -> form.loginPage("/login").permitAll())
          .logout(out -> out.logoutSuccessUrl("/login?logout"))
          .csrf(Customizer.withDefaults());
      return http.build();
  }
  ```
- `@EnableWebSecurity` · `@EnableMethodSecurity` — 그대로.
- `PasswordEncoder`(BCrypt) · `UserDetailsService` · `InMemoryUserDetailsManager` — 그대로.
- 이 교안의 `SecurityConfig.java` 는 **이미 람다 DSL** 이라 그대로 SS7에서 동작.
- (remember-me/세션을 Jackson으로 직렬화할 때만) `SecurityJackson2Modules` → `SecurityJacksonModules`.
- **Thymeleaf `sec:` (Day 18)**: `thymeleaf-extras-springsecurity6` 를 명시 추가(스타터가 안 끌고 옴).
  버전은 부트 BOM. `sec:` 가 렌더링에서 무시되면 SS7 대응 아티팩트가 따로 나왔는지 릴리스 노트 확인.
- **JWT (Day 19)**: `jjwt` 가 아직 Jackson 3 미지원 → `jjwt-jackson` **대신 `jjwt-gson`**(0.13.0). Gson transitively 포함.

---

## 6. 기타

- **패키지 이동 (모듈화)**: Boot 4는 모듈을 `org.springframework.boot.<module>` 로 쪼갬.
  - `@SpringBootApplication` · `@EnableAutoConfiguration` → **그대로** `org.springframework.boot.autoconfigure.*`
  - 자동구성 **지원 클래스**(예: `DataSourceAutoConfiguration`)는 모듈별 패키지로 이동 가능 → `exclude=` 에 쓸 때 import 확인.
- **Jakarta EE 11 / Servlet 6.1**: `jakarta.*` 네임스페이스는 3.x에서 이미 씀 — 코드 변화 없음.
  `jakarta.validation.constraints.*` (`@NotBlank` 등) 그대로.
- `application.yml` 의 낯선 프로퍼티 경고를 잡으려면 개발 중에만
  `developmentOnly 'org.springframework.boot:spring-boot-properties-migrator'` 추가(끝나면 제거).
- Flyway/Liquibase 등은 별도 스타터 필요해졌지만 이 교안은 안 씀.

---

## 7. 왜 4.0 인가 (수강생 설명용)

- Boot 3.x 는 지원 종료가 진행 중, **3.5가 3.x의 마지막 마이너**.
- Boot 4.0 은 **Java 17 그대로**, Gradle 8.14 그대로 — 진입 장벽이 낮음.
- MyBatis·Security·Thymeleaf 생태계가 Boot 4 대응을 마침(mybatis-spring-boot-starter 4.0.x).
- 바뀌는 건 대부분 **이름**(`starter-webmvc`, `@MockitoBean`)이라 개념 학습에는 영향이 작음.
