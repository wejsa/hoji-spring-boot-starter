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
