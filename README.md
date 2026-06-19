# hoji-spring-boot-starter v0.1.0

내부 서비스 공통 도메인 비결합(domain-agnostic) 빌딩블록을 일반화한 멀티모듈 Spring Boot 3 스타터 라이브러리. `core`/`jpa`/`security` 3개 모듈을 auto-configuration으로 제공하며, 소비 서비스는 의존성 추가 + 설정(yml)만으로 기능을 사용한다.

---

## 프로젝트 개요

| 항목 | 값 |
|------|-----|
| **기술 스택** | Spring Boot 3 (Kotlin 2.0+ / Java 21 / Gradle 8.x Kotlin DSL) |
| **인프라** | 없음 (Maven Central / `publishToMavenLocal` 발행형 라이브러리) |

---

## 모듈 구성

| 모듈 | 책임 | 핵심 의존성 |
|---|---|---|
| `hoji-common-core` | 순수 유틸(날짜·문자열·난수·파일·zip) + 예외 모델 + 전역 예외 핸들러 + 페이지 요청 | kotlin-stdlib, spring-web, spring-data-commons, zip4j |
| `hoji-common-jpa` | JPA 감사 베이스 엔티티 + 컨버터(YesNo/Base64) + 시퀀스 조회 | spring-data-jpa, jakarta.persistence, spring-jdbc |
| `hoji-common-security` | API-Key 인증 필터 + 설정 프로퍼티 + 기본 시큐리티 체인(Security 6) | spring-boot-starter-security, jakarta.servlet |

> 의존 방향: `common-jpa` → `common-core`, `common-security` 독립. 순환 금지.

---

## 에이전트 팀

| 에이전트 | 역할 |
|---------|------|
| code-reviewer | PR 다관점 통합 리뷰 가이드 (필수) |
| qa | PR 생성 후 테스트 품질 분석 (활성) |

---

## 시작하기

```bash
claude            # Claude Code 실행
/aick-status      # 상태 확인
/aick-backlog     # 백로그 확인
/aick-plan        # 다음 작업 가져오기 (HOJI-001부터)
```

빌드 (구현 후):

```bash
./gradlew build
./gradlew publishToMavenLocal   # 소비 서비스에서 mavenLocal()로 테스트
```

---

## 발행 및 소비

### 로컬 발행 (publishToMavenLocal)

```bash
./gradlew publishToMavenLocal
```

3개 라이브러리 모듈이 각각 `~/.m2/repository/com/hoji/{module}/0.1.0-SNAPSHOT/`에 발행된다 — `*.jar`, `*-sources.jar`, `*-javadoc.jar`, `*.pom`. 통합 스모크 모듈(`hoji-smoke-test`)은 검증 전용이라 발행되지 않는다.

> POM에는 `api(...)` 의존성이 compile scope로 기록되어 소비 서비스가 transitive로 확보한다 — 예: `hoji-common-jpa`를 의존하면 `hoji-common-core`가 자동으로 따라온다.

### 소비 서비스 설정

```kotlin
// build.gradle.kts (소비 서비스)
repositories { mavenLocal(); mavenCentral() }

dependencies {
    implementation("com.hoji:hoji-common-core:0.1.0-SNAPSHOT")
    implementation("com.hoji:hoji-common-jpa:0.1.0-SNAPSHOT")
    implementation("com.hoji:hoji-common-security:0.1.0-SNAPSHOT")
}
```

```yaml
# application.yml (소비 서비스) — 최소 활성 설정
hoji:
  security:
    api-keys:                 # 비어 있으면 deny-by-default (모든 요청 401)
      - client-id: sample-service
        key: ${SAMPLE_API_KEY}
        roles: [SERVICE]
  jpa:
    audit:
      actor: ${spring.application.name:SYSTEM}   # @CreatedBy/@LastModifiedBy actor
```

### 통합 스모크 검증

`hoji-smoke-test` 모듈이 3개 스타터를 단일 `@SpringBootTest` 컨텍스트에 조립해 auto-configuration 동시 기동과 핵심 빈 등록(전역 예외 핸들러 · AuditorAware · SequenceRepository · SecurityFilterChain)을 회귀로 검증한다.

```bash
./gradlew :hoji-smoke-test:test    # 조립 스모크만 실행
./gradlew build                    # 4개 모듈 전체 빌드 + 테스트
```

#### 수동 샘플 앱 스모크 체크리스트

별도 Boot 3 앱에서 mavenLocal 좌표로 실제 소비를 확인할 때:

- [ ] `./gradlew publishToMavenLocal`로 3개 모듈 발행 확인 (`~/.m2/.../com/hoji/`)
- [ ] 소비 앱에 `repositories { mavenLocal() }` + 3개 좌표 의존성 추가
- [ ] 앱 기동 시 3개 auto-config 로딩 (충돌 · 빈 누락 없음)
- [ ] API 키 헤더 없이 요청 → 401 (deny-by-default)
- [ ] 유효 키 헤더로 요청 → 200, principal = `client-id`
- [ ] 감사 컬럼(`@CreatedBy`)에 `actor` 값 기록 확인

---

## 디렉토리 구조

```
hoji-spring-boot-starter/
├── .claude/state/
│   ├── project.json      # 프로젝트 설정
│   └── backlog.json      # 백로그 (7 task / 3 phase)
├── docs/
│   └── requirements/     # 요구사항 문서 (REQUIREMENTS.md, common-*.md)
├── CLAUDE.md             # AI 에이전트 지시문
├── VERSION               # 프로젝트 버전
└── README.md             # 이 파일
```

---

## 주요 명령어

| 명령어 | 설명 |
|--------|------|
| `/aick-status` | 프로젝트 상태 확인 |
| `/aick-backlog` | 백로그 조회/관리 |
| `/aick-feature` | 새 기능 기획 |
| `/aick-plan` | 설계 + 스텝 계획 수립 |
| `/aick-impl` | 코드 구현 (스텝별) |
| `/aick-review-pr` | PR 리뷰 |
| `/aick-merge-pr` | PR 머지 |

---

## Git 브랜치 전략

```
main (운영)
  └── develop (개발 통합)
        ├── feature/HOJI-XXX-stepN
        └── bugfix/HOJI-XXX-버그명
```

---

<!-- CUSTOM_SECTION_START -->
## 버전 정책 (최신 베이스라인)

| 항목 | 버전 | 비고 |
|---|---|---|
| Java | 21 LTS | Boot 3 최소 17, LTS 권장 21 |
| Kotlin | 2.0+ | `jvmToolchain(21)`, `-Xjsr305=strict` |
| Spring Boot | 3.3+ (권장 3.4.x/3.5.x) | `javax.*`→`jakarta.*`, Security 6 |
| Gradle | 8.x (Kotlin DSL) | |
| 의존성 저장소 | Maven Central | |

> ⚠️ 원본 레거시 코드는 Boot 2.1.8 / Kotlin 1.3.61 / `javax.*` 기반. 가져올 때 마이그레이션 규칙(jakarta, Security 6, zip4j 2.x, Sort API)과 버그 수정 B1~B5를 반드시 적용한다 — 상세는 `CLAUDE.md` CUSTOM 섹션 참조.
<!-- CUSTOM_SECTION_END -->

## 라이선스

MIT License
