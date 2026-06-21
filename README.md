# hoji-spring-boot-starter

> 도메인에 결합되지 않은(domain-agnostic) 공통 빌딩블록을 **의존성 추가 + `application.yml` 설정만으로** 쓰게 해 주는 멀티모듈 Spring Boot 3 스타터 라이브러리.

![version](https://img.shields.io/badge/version-0.1.0-blue)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3%2B-6DB33F)
![Kotlin](https://img.shields.io/badge/Kotlin-2.0%2B-7F52FF)
![Java](https://img.shields.io/badge/Java-21%20LTS-orange)
![License](https://img.shields.io/badge/license-Apache%202.0-green)

`core` / `jpa` / `security` 3개 모듈을 Spring Boot **auto-configuration**으로 제공합니다. 소비 서비스는 빈을 직접 등록할 필요 없이 의존성만 추가하면 공통 기능(예외 모델·감사 엔티티·API-Key 인증·유틸)이 켜지고, 필요할 때 **동일 타입 빈을 정의하는 것만으로 언제든 오버라이드**할 수 있습니다.

---

## 목차

- [왜 쓰나](#왜-쓰나)
- [모듈 한눈에](#모듈-한눈에)
- [빠른 시작](#빠른-시작)
- [기능 카탈로그](#기능-카탈로그)
  - [common-core — 예외 모델 · 페이지 · 유틸](#common-core)
  - [common-jpa — 감사 엔티티 · 컨버터 · 시퀀스](#common-jpa)
  - [common-security — API-Key 인증](#common-security)
- [설정 레퍼런스](#설정-레퍼런스)
- [오버라이드 / 확장 지점](#오버라이드--확장-지점)
- [동작 검증 상태](#동작-검증-상태)
- [빌드 & 발행](#빌드--발행)
- [호환성](#호환성)
- [라이선스](#라이선스)

---

## 왜 쓰나

| 특징 | 설명 |
|------|------|
| **도메인 비결합** | 라이브러리는 특정 업무 도메인을 알지 않습니다. 에러 코드 카탈로그·감사 actor·라우트 인가 규칙 등 "서비스마다 다른 것"은 모두 **확장점(interface/bean)으로 외부화**되어 소비자가 주입합니다. |
| **설정만으로 켜짐** | 각 모듈은 `META-INF/spring/...AutoConfiguration.imports`로 자동 등록됩니다. 의존성 추가 + `yml` 설정이면 끝 — `@Import`나 수동 빈 등록 불필요. |
| **안전한 오버라이드** | 모든 기본 빈은 `@ConditionalOnMissingBean` 백오프를 씁니다. 소비자가 같은 타입 빈을 정의하면 라이브러리 기본값은 조용히 물러납니다. |
| **모듈 독립 사용** | 필요한 모듈만 골라 의존할 수 있습니다. `core`만, 또는 `security`만 써도 됩니다(`jpa`는 `core`에 의존). |

---

## 모듈 한눈에

| 모듈 (Maven 좌표) | 책임 | 자동 등록(AutoConfiguration) |
|---|---|---|
| **`com.hoji:hoji-common-core`** | 예외 모델 + 전역 예외 핸들러 + 페이지 요청 값객체 + 순수 유틸(날짜·문자열·난수·파일·AES zip) | `CoreErrorAutoConfiguration` |
| **`com.hoji:hoji-common-jpa`** | JPA 감사 베이스 엔티티 + actor 외부화 + 컨버터(YesNo/Base64) + 시퀀스 채번 | `JpaAuditingAutoConfiguration`, `SequenceAutoConfiguration` |
| **`com.hoji:hoji-common-security`** | API-Key 인증 필터 + 설정 바인딩 + stateless 기본 보안 체인(Spring Security 6) | `ApiKeySecurityAutoConfiguration` |

> **의존 방향**: `hoji-common-jpa` → `hoji-common-core` (Base64 유틸 재사용, `api` scope라 transitive로 따라옴). `hoji-common-security`는 독립. 순환 없음.

---

## 빠른 시작

### 1. 의존성 추가

현재 버전(`0.1.0`)은 `publishToMavenLocal`로 로컬 발행해 사용합니다(Maven Central 발행은 후속).

```bash
# 라이브러리 리포지토리에서 1회 발행
./gradlew publishToMavenLocal
```

```kotlin
// 소비 서비스의 build.gradle.kts
repositories {
    mavenLocal()      // 로컬 발행본 해석
    mavenCentral()
}

dependencies {
    implementation("com.hoji:hoji-common-core:0.1.0")
    implementation("com.hoji:hoji-common-jpa:0.1.0")       // core를 transitive로 끌어옴
    implementation("com.hoji:hoji-common-security:0.1.0")
    // 필요한 모듈만 골라 써도 됩니다.
}
```

> 세 모듈을 함께 쓸 때 필요한 Spring Boot 스타터(`spring-boot-starter-web`, `-data-jpa`, `-security`, `-validation`)는 소비 서비스가 자신의 스택에 맞게 추가합니다. 라이브러리는 이들을 강제하지 않습니다(필요 클래스가 클래스패스에 있을 때만 auto-config가 켜지는 `@ConditionalOnClass` 방식).

### 2. 최소 설정 (`application.yml`)

```yaml
spring:
  application:
    name: my-service          # 감사 actor 기본값으로도 쓰임

hoji:
  security:
    api-keys:                  # 비어 있으면 deny-by-default — 모든 보호 요청이 401
      - client-id: my-service
        key: ${MY_API_KEY}     # 비밀키는 환경변수로 주입
        roles: [SERVICE]
  jpa:
    audit:
      actor: ${spring.application.name:SYSTEM}   # @CreatedBy/@LastModifiedBy에 채워질 값
```

### 3. 추가하면 바로 켜지는 것

| 의존 모듈 | 컨텍스트 기동 시 자동으로 |
|---|---|
| `core` | `@RestControllerAdvice` 전역 예외 핸들러가 등록 — `ApplicationException` 계층과 `@Valid` 실패가 일관된 `ErrorResponse`로 매핑됩니다. |
| `jpa` | `@EnableJpaAuditing` 활성화 + 기본 `AuditorAware<String>` 등록 — 베이스 엔티티 상속만으로 감사 컬럼이 자동 기록됩니다. DataSource가 단일이면 `SequenceRepository` 빈도 등록. |
| `security` | stateless API-Key 보안 체인 등록 — `/actuator/health` 외 모든 요청은 유효 키가 있어야 통과(401 deny-by-default). |

---

## 기능 카탈로그

<a name="common-core"></a>
### common-core — 예외 모델 · 페이지 · 유틸

#### 1) 에러 모델 (도메인 비결합)

핵심 아이디어: **에러 코드 카탈로그는 소비자가 소유**합니다. 라이브러리는 코드 문자열을 모르고, `ErrorReason` 계약과 HTTP 상태만 책임집니다.

```kotlin
// 1. 소비자가 자신의 에러 코드를 정의 (enum이 ErrorReason 구현)
enum class ShopErrorReason(private val code: String) : ErrorReason {
    WIDGET_NOT_FOUND("SHOP-404-WIDGET-NOT-FOUND"),
    ;
    override fun toReason(): String = code
}

// 2. ApplicationException을 상속해 코드 + HTTP 상태를 묶음
class WidgetNotFoundException(message: String) :
    ApplicationException(ShopErrorReason.WIDGET_NOT_FOUND, message) {
    override val httpStatus = HttpStatus.NOT_FOUND
}

// 3. 컨트롤러/서비스에서 그냥 throw — 전역 핸들러가 응답으로 변환
throw WidgetNotFoundException("widget 42 not found")
```

응답 바디(`ErrorResponse`):

```json
{ "code": "SHOP-404-WIDGET-NOT-FOUND", "message": "widget 42 not found" }
```

**기본 제공 예외** — 코드 카탈로그를 따로 만들기 전에도 바로 쓸 수 있는 라이브러리 기본값(`CommonErrorReason`):

| 예외 | HTTP 상태 | 기본 코드 |
|---|---|---|
| `BadRequestException` | **400** | `COMMON-400-INVALID-INPUT` |
| `InternalProcessException` | **500** | `COMMON-500-INTERNAL` |
| `ExternalServiceException` | **502** | `COMMON-502-EXTERNAL` |

- `@Valid`/Bean Validation 실패(`MethodArgumentNotValidException`)는 자동으로 **400 + `COMMON-400-INVALID-INPUT`** 로 변환되고, 바디 `message`에 `field: 사유` 요약이 담깁니다.
- 로깅은 상태 계열로 분리됩니다 — **4xx는 `warn`, 5xx는 `error`**.
- `InternalProcessException.of(...)`는 `InternalProcessException`을 반환하는 팩토리이며 500으로 응답합니다.

> **확장**: 소비자가 추가한 임의의 `ApplicationException` 하위 타입도 같은 핸들러가 각자의 `httpStatus`로 처리합니다. 핸들러를 직접 두고 싶으면 자신의 `@RestControllerAdvice`(또는 동일 타입 빈)를 등록하면 라이브러리 기본 핸들러는 물러납니다.

#### 2) 페이지네이션 — `CustomPageRequest`

쿼리 파라미터를 Spring Data `PageRequest`로 안전하게 변환하는 값 객체. 크기 상한·정렬 토큰 해석·기본 정렬을 한곳에서 처리합니다.

```kotlin
val pageRequest = CustomPageRequest(
    page = 2,                                  // 음수는 0으로 보정
    size = 5000,                               // MAX_ROW_SIZE(3000) 초과 → 3000으로 보정
    orders = listOf("name:asc", "createdAt"),  // "prop:asc"/"prop:desc", 방향 생략 시 DESC
).of()                                          // → org.springframework.data.domain.PageRequest

repository.findAll(pageRequest)
```

| 상수 | 값 | 의미 |
|---|---|---|
| `MAX_ROW_SIZE` | `3000` | 페이지 크기 상한(초과 시 절단) |
| `DEFAULT_PAGE_SIZE` | `10` | 기본 페이지 크기 |
| `DEFAULT_SORT_PROPERTY` | `"createdAt"` | `orders`가 비었을 때 기본 정렬(DESC) |

#### 3) 유틸리티 (순수 함수 — 빈 등록 없이 import 후 바로)

| 영역 | 대표 API | 예시 |
|---|---|---|
| **Base64** | `String.encodeBase64()` / `String.decodeBase64()` / `ByteArray.encodeBase64ToString()` | `"hello".encodeBase64()` → `"aGVsbG8="` |
| **Hex** | `byteArrayToHexString(bytes)` / `byteArrayToHexStringV2(bytes)` / `stringHexToByteArray(list)` | 바이트 ↔ 16진수 |
| **마스킹** | `String.mask(nth, toValue, except)` | `"1234567890".mask(4, "****", null)` → `"123****567890"` |
| **난수** | `generateRandom(length)` / `generateRandomByte(length)` | `SecureRandom` 기반. `generateRandom(8)` → `"04915532"` 같은 8자리 숫자열 |
| **날짜** | `LocalDateTime.convertToYyyyMMddHHmmss()` 등 포맷터 + `convertFromYyyyMMddHHmmss(s)` 파서 | `dt.convertToYyyyMMddHHmmss()` → `"20260620134507"` |
| **파일** | `File.getFilesUnder(loc)` / `File.move(to, overwrite)` / `FileOutputStream.writeLine(s)` | 디렉토리 순회·이동·라인 쓰기 |
| **AES Zip** | `compressZipFile(dirLocation, zipName, password: CharArray)` | zip4j 2.x · **AES-256** 암호화. 비밀번호는 보안상 `CharArray` |

```kotlin
val zip: File = compressZipFile("/tmp/payload", "bundle", "p@ssw0rd".toCharArray())
// → /tmp/payload/bundle.zip  (AES-256 암호화)
```

---

<a name="common-jpa"></a>
### common-jpa — 감사 엔티티 · 컨버터 · 시퀀스

#### 1) 감사 베이스 엔티티 (생성/수정 자동 기록)

엔티티가 베이스를 상속하면 Spring Data JPA Auditing이 persist/update 시점에 시각(과 actor)을 자동으로 채웁니다. 수기 `@PrePersist`/`@PreUpdate`가 필요 없습니다.

| 베이스 클래스 | 기록 필드 (컬럼) |
|---|---|
| `BaseDomain` | `createdAt`(`CREATED_AT`), `updatedAt`(`UPDATED_AT`) |
| `SystemBaseDomain` | `createdAt`(`SYS_CRE_DTTM`), `createdId`(`SYS_CRE_ID` · `@CreatedBy`), `updatedAt`(`SYS_UPD_DTTM`), `updatedId`(`SYS_UPD_ID` · `@LastModifiedBy`) |

```kotlin
@Entity
@Table(name = "WIDGET")
class Widget(
    @Id @Column(name = "ID") val id: Long,
    @Column(name = "NAME") var name: String,
) : SystemBaseDomain()    // createdAt/createdId/updatedAt/updatedId 자동 채움
```

**actor 외부화** — 누가 만들었는지(`@CreatedBy`)는 서비스마다 다르므로 `AuditorAware<String>`로 외부화됩니다. 라이브러리 기본 빈은 다음 우선순위로 actor를 결정합니다:

```
hoji.jpa.audit.actor  →  spring.application.name  →  "SYSTEM"
```

요청 컨텍스트의 사용자 ID 등 동적 actor가 필요하면 자신의 `AuditorAware<String>` 빈을 등록하면 됩니다(기본 빈은 자동 백오프):

```kotlin
@Bean
fun auditorAware(): AuditorAware<String> =
    AuditorAware { Optional.of(SecurityContextHolder.getContext().authentication?.name ?: "SYSTEM") }
```

> 컬럼명(`SYS_CRE_DTTM` 등)은 범용 관례입니다. 다른 명명이 필요하면 위 클래스를 참고해 자신의 베이스 엔티티를 정의하세요. 소비자가 직접 `@EnableJpaAuditing`을 둔다면 이중 활성화를 피하기 위해 `spring.autoconfigure.exclude`로 `JpaAuditingAutoConfiguration`을 제외하세요.

#### 2) JPA 속성 컨버터

| 컨버터 | 매핑 | 동작 |
|---|---|---|
| `YesNoConverter` | `Boolean` ↔ `"Y"`/`"N"` | 쓰기: `true`→`Y`, `false`/`null`→`N`. 읽기: `"Y"`(대소문자 무관)→`true`, 그 외→`false` |
| `Base64Converter` | `String` ↔ Base64 컬럼 | 저장 시 인코딩, 조회 시 복원. `null`은 그대로 통과(null-safe) |

```kotlin
@Convert(converter = YesNoConverter::class)
@Column(name = "ACTIVE_YN")
var active: Boolean = true        // DB에는 'Y'/'N'로 저장

@Convert(converter = Base64Converter::class)
@Column(name = "SECRET")
var secret: String = ""           // DB에는 Base64로 저장
```

(보조: `YesNoType` enum — `Y(true)`/`N(false)`, `YesNoType.of(boolean)`.)

#### 3) 시퀀스 채번 — `SequenceRepository`

DB 시퀀스에서 다음 값을 조회하는 벤더 비결합 인터페이스입니다.

```kotlin
interface SequenceRepository {
    fun getNextLong(sequenceName: String): Long
}
```

```kotlin
val id = sequenceRepository.getNextLong("WIDGET_SEQ")   // select WIDGET_SEQ.nextval ...
```

- 기본 구현(`OracleSequenceRepository`)은 표준 `javax.sql.DataSource`만 의존합니다(특정 커넥션 풀 벤더에 결합하지 않음). 트랜잭션 경계가 적용된 프록시 빈으로 등록됩니다.
- `@ConditionalOnSingleCandidate(DataSource)` — DataSource가 정확히 하나일 때 자동 등록되며, 소비자가 자신의 `SequenceRepository` 빈을 두면 백오프합니다. 다른 DB 방언이 필요하면 인터페이스를 직접 구현하세요.

---

<a name="common-security"></a>
### common-security — API-Key 인증

서비스 간 호출에 적합한 **stateless API-Key 인증**을 기본 보안 체인으로 제공합니다(Spring Security 6).

#### 1) 동작 방식

1. 요청에서 키를 추출: **헤더 `X-API-KEY`** (1순위) → **쿼리 파라미터 `apikey`** (2순위).
2. 설정된 키 목록과 **상수 시간 비교**(`MessageDigest.isEqual`)로 매칭.
3. 매칭되면 인증 성공 — `principal` = `client-id`, 권한 = `ROLE_<role>` (예: `roles: [SERVICE]` → `ROLE_SERVICE`).
4. 세션은 `STATELESS`, CSRF/HTTP Basic/폼 로그인은 비활성. 미인증 요청은 리다이렉트 없이 **401**.

```
요청 인가 순서:  /actuator/health 허용  →  소비자 커스터마이저 규칙  →  anyRequest().authenticated()
```

#### 2) 설정 (`ApiKeyProperties` · prefix `hoji.security`)

```yaml
hoji:
  security:
    api-keys:
      - client-id: billing-service     # 인증 성공 시 principal 이름 (비밀 아님)
        key: ${BILLING_API_KEY}        # 비밀키 — 비어 있으면 그 항목은 비활성(deny-by-default)
        roles: [SERVICE, ADMIN]        # ROLE_ 접두는 필터가 부여 → ROLE_SERVICE, ROLE_ADMIN
```

> **deny-by-default**: `api-keys`가 비어 있거나 `key`가 비면 어떤 요청과도 매칭되지 않아 보호 경로는 모두 401입니다. 비밀키는 코드가 아닌 환경변수로 주입하세요.

호출 예:

```bash
curl -H "X-API-KEY: $BILLING_API_KEY" https://my-service/api/widgets/42
```

#### 3) 공개 경로 등 라우트 규칙 추가 — `ApiKeyAuthorizeRulesCustomizer`

라우트 인가 규칙은 서비스마다 다르므로 외부화되어 있습니다. `@Component`로 하나 이상 등록하면 `health permit`과 `anyRequest authenticated` 사이에 적용됩니다.

```kotlin
@Component
class PublicRoutesCustomizer : ApiKeyAuthorizeRulesCustomizer {
    override fun customize(
        registry: AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry,
    ) {
        registry.requestMatchers("/api/public/**").permitAll()
    }
}
```

#### 4) 전체 오버라이드

보안 체인 전체를 직접 제어하려면 자신의 `SecurityFilterChain` 빈을 등록하세요 — 라이브러리 기본 체인은 `@ConditionalOnMissingBean(SecurityFilterChain)`으로 백오프합니다.

---

## 설정 레퍼런스

| 키 | 기본값 | 설명 |
|---|---|---|
| `hoji.security.api-keys` | `[]` | 허용 API 키 목록. 비면 전면 deny. |
| `hoji.security.api-keys[].client-id` | `""` | 인증 성공 시 principal 이름(로깅/감사용). |
| `hoji.security.api-keys[].key` | `""` | 비밀 API 키. 비어 있으면 해당 항목 비활성. |
| `hoji.security.api-keys[].roles` | `[SERVICE]` | 부여할 역할(런타임에 `ROLE_` 접두가 붙음). |
| `hoji.jpa.audit.actor` | `spring.application.name` → `SYSTEM` | `@CreatedBy`/`@LastModifiedBy`에 기록할 actor 문자열. |

---

## 오버라이드 / 확장 지점

라이브러리의 모든 결정은 "소비자가 원하면 가져갈 수 있게" 설계되어 있습니다.

| 지점 | 기본 동작 | 오버라이드 / 확장 방법 |
|---|---|---|
| **전역 예외 핸들러** | `ApplicationException`·`@Valid` → `ErrorResponse` 매핑 | 자신의 `@RestControllerAdvice` 등록 시 백오프 |
| **에러 코드** | `CommonErrorReason` 3종 | `ErrorReason` `fun interface`를 소비자 enum으로 구현 |
| **감사 actor** | `hoji.jpa.audit.actor` → app name → `SYSTEM` | 자신의 `AuditorAware<String>` 빈 등록 |
| **JPA Auditing** | `@EnableJpaAuditing` 자동 켜짐 | `spring.autoconfigure.exclude`로 `JpaAuditingAutoConfiguration` 제외 |
| **시퀀스 채번** | `OracleSequenceRepository` (단일 DataSource 시) | 자신의 `SequenceRepository` 빈 등록 |
| **라우트 인가** | `health` 외 전부 인증 | `ApiKeyAuthorizeRulesCustomizer` 빈 1개 이상 등록 |
| **보안 체인 전체** | API-Key stateless 체인 | 자신의 `SecurityFilterChain` 빈 등록 시 전체 대체 |

---

## 동작 검증 상태

별도 외부 소비 서비스(`com.acme:shop`)를 만들어 mavenLocal 발행본을 의존하고, 3개 auto-configuration을 한 컨텍스트에 조립한 뒤 모든 공개 기능을 end-to-end로 행사하는 시뮬레이션을 수행했습니다 — **12/12 통과**.

| 모듈 | 검증 항목 | 결과 |
|---|---|---|
| security | 공개 경로 permit / 키 없으면 401 / 유효 키 200(principal·ROLE) / 잘못된 키 401 | ✅ |
| core | `ApplicationException` → 선언 상태(404) + `ErrorResponse` / `@Valid` → 400 `COMMON-400-INVALID-INPUT` | ✅ |
| jpa | `SequenceRepository` 트랜잭션 프록시 + 단조 증가 / 감사 컬럼 자동 채움 / YesNo·Base64 컨버터 / 인증 POST end-to-end | ✅ |
| core | Base64·mask·random·date 유틸 / `CustomPageRequest` 상한·정렬 / AES zip | ✅ |

라이브러리 리포지토리 자체에도 미발행 통합 스모크 모듈(`hoji-smoke-test`)이 있어 3개 스타터의 동시 기동과 핵심 빈 등록을 회귀로 검증합니다:

```bash
./gradlew :hoji-smoke-test:test    # 조립 스모크
./gradlew build                    # 4개 모듈 전체 빌드 + 테스트
```

---

## 빌드 & 발행

```bash
./gradlew build                  # 3개 라이브러리 모듈 컴파일 + 테스트 + smoke
./gradlew publishToMavenLocal    # ~/.m2/repository/com/hoji/{module}/0.1.0/ 로 발행
```

발행물에는 각 모듈의 `*.jar`, `*-sources.jar`, `*-javadoc.jar`, `*.pom`이 포함됩니다. POM에는 `<licenses>`(Apache-2.0)와 `api(...)` 의존성이 compile scope로 기록되어 소비 서비스가 transitive로 확보합니다(예: `hoji-common-jpa` 의존 시 `hoji-common-core` 자동 포함). 통합 스모크 모듈(`hoji-smoke-test`)은 검증 전용이라 발행되지 않습니다.

---

## 호환성

| 항목 | 버전 | 비고 |
|---|---|---|
| Java | 21 LTS | Boot 3 최소 17, LTS 권장 21 (`jvmToolchain(21)`) |
| Kotlin | 2.0+ | `-Xjsr305=strict` |
| Spring Boot | 3.3+ (권장 3.4.x / 3.5.x) | `jakarta.*`, Spring Security 6 |
| Gradle | 8.x (Kotlin DSL) | `java-library` + `maven-publish` |

---

## 라이선스

Apache License 2.0 — 전문은 저장소 루트의 [`LICENSE`](./LICENSE) 참조. 발행 POM의 `<licenses>` 메타데이터와 동일합니다.

---

<sub>🛠️ 이 프로젝트는 <b><a href="https://github.com/wejsa/ai-crew-kit">AI Crew Kit</a></b>로 개발되었습니다 — 계획 → 구현 → 리뷰 → 머지 워크플로우를 자동화하는 개발 프레임워크.</sub>
