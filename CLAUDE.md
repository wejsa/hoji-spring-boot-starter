# hoji-spring-boot-starter

## 프로젝트 개요

내부 서비스 공통 도메인 비결합(domain-agnostic) 빌딩블록을 일반화한 멀티모듈 Spring Boot 3 스타터 라이브러리. `core`/`jpa`/`security` 3개 모듈을 auto-configuration으로 제공하며, 소비 서비스는 의존성 추가 + 설정(yml)만으로 기능을 사용한다.

### 기술 스택
- **Backend**: Spring Boot 3 (Kotlin) — Kotlin 2.0+, Java 21 LTS, Gradle 8.x (Kotlin DSL)
- **Frontend**: 없음 (라이브러리)
- **Database**: 없음 — `common-jpa`는 DB 비결합 빌딩블록(베이스 엔티티/컨버터/시퀀스)만 제공, 소비 서비스가 실제 DB 보유
- **Cache / Message Queue**: 없음 (비목표 — 후속 우선순위)
- **Infrastructure**: 없음 — Maven Central / `publishToMavenLocal` 발행형 라이브러리
- **배포 형태**: 멀티모듈 Gradle, `java-library` + `maven-publish`, `bootJar` off / `jar` on

---

## 프레임워크 역할 경계

이 프로젝트는 **AI Crew Kit** 프레임워크 기반입니다.

| 프레임워크가 하는 것 | Claude가 하는 것 |
|---------------------|-----------------|
| 워크플로우 자동화 (plan→impl→review→merge) | 코드 작성 (모든 언어, 프로토콜, 패턴) |
| 품질 게이트 (빌드/테스트/리뷰 통과 필수) | 기술 판단 (아키텍처, 라이브러리 선택) |
| 팀 컨벤션 SSOT (코딩 스타일, 보안 규칙) | 구현 지식 (프레임워크 문서에 없는 기술도 구현) |

**원칙**: 프레임워크는 "어떤 프로세스로 만드는지"를 관리하고, Claude는 "어떻게 짜는지"를 담당합니다.

---

## 프레임워크 번들 경로 해석 (필수)

스킬·에이전트가 참조하는 **빌트인 읽기 전용 리소스**(스키마·템플릿·체크리스트·컨벤션 등)는 설치 방식에 따라 위치가 다릅니다. 스킬 본문은 이를 `${CLAUDE_PLUGIN_ROOT}/.claude/<경로>` 형태로 표기합니다:

- **플러그인 설치**: 로드 시 `${CLAUDE_PLUGIN_ROOT}`가 플러그인 설치 경로(절대경로)로 **치환**됩니다 → 그 경로를 그대로 Read.
- **clone/seed 설치**: `${CLAUDE_PLUGIN_ROOT}`가 **치환되지 않고 리터럴 문자열로 남습니다.** 이 경우 `${CLAUDE_PLUGIN_ROOT}/` 접두를 **제거**하고 **프로젝트 로컬** 경로(`.claude/<경로>`)를 Read하세요.

> **판정 규칙**: 경로에 `${CLAUDE_PLUGIN_ROOT}` 문자열이 그대로 보이면(=치환 안 됨, clone/seed 모드) → `.claude/<경로>`로 읽는다. 절대경로로 치환돼 있으면(=플러그인 모드) → 그 경로로 읽는다.

> **예외 — 프로젝트 상태는 항상 로컬**: `.claude/state/`, `.claude/temp/`, `.claude/plans/`, `project.json`, `CLAUDE.md`, `README.md`, `.claude/settings.json`은 프로젝트별 읽기/쓰기 대상이므로 **항상 프로젝트 로컬 경로**를 쓰며 `${CLAUDE_PLUGIN_ROOT}` 접두를 붙이지 않습니다.

---

## 30초 요약 (Quick Reference)

| 하고 싶은 것 | 명령 |
|-------------|------|
| 다음 작업 시작 | "다음 작업 가져와줘" → `/aick-plan` → `/aick-impl` |
| 소규모 수정 | "OO 고쳐줘" → `/aick-impl --micro "설명"` |
| PR 리뷰 + 머지 | "PR 123 리뷰해줘" → 자동 체이닝 |
| 상태 확인 | "상태 확인해줘" → `/aick-status` |

### 3가지 주의사항
1. **계획 승인 전 코드 작성 금지** — plan → 승인 → impl 순서 필수
2. **빌드/테스트 통과 필수** — PR 생성 전 자동 검증
3. **자동 체이닝 중 멈추지 않음** — impl → review → merge 자동 진행

---

## 상태 관리 (Git 기반 SSOT)

```
.claude/state/              # Git 관리
├── project.json            # 프로젝트 설정 (스택, 에이전트)
├── backlog.json            # 백로그 + 상태 + Phase
└── completed.json          # 완료 이력

.claude/temp/               # 임시 파일 (Git 제외)
└── {taskId}-plan.md        # Task별 상세 계획
```

**Git clone/pull이 곧 동기화입니다.**

---

## 에이전트

품질 분석 전담 (구현·기획은 메인 세션 담당):

- **code-reviewer** (필수): `aick-review-pr`의 다관점 통합 리뷰 가이드
- **qa** (활성): `aick-impl`이 PR 생성 후 백그라운드로 테스트 품질 분석

비활성: **db-designer** — 본 프로젝트는 스키마 설계가 아닌 베이스 엔티티/컨버터/시퀀스 빌딩블록이라 불필요. 필요 시 `project.json`의 `agents.enabled`에 추가.

---

## 주요 스킬 (프로파일: full)

| 스킬 | 용도 |
|------|------|
| `/aick-status` | 프로젝트 상태 확인 |
| `/aick-feature` | 새 기능 기획 + 백로그 등록 |
| `/aick-plan` | 설계 분석 + 스텝 분리 계획 |
| `/aick-impl` | 스텝별 구현 + PR 생성 |
| `/aick-review-pr` | PR 다관점 통합 리뷰 |
| `/aick-fix` | CRITICAL 이슈 자동 수정 |
| `/aick-merge-pr` | PR Squash 머지 + 상태 갱신 |
| `/aick-backlog` | 백로그 조회/관리 |
| `/aick-estimate` | 작업 복잡도 추정 |
| `/aick-hotfix` | main 긴급 수정 |
| `/aick-rollback` | 릴리스 롤백 |
| `/aick-retro` | 완료 Task 회고 |
| `/aick-report` | 메트릭 리포트 |
| `/aick-health-check` | 코드베이스 건강 검진 |
| `/aick-docs` | 공통 컨벤션·참고자료 |

---

## 자연어 명령어

| 자연어 | 매핑 스킬 |
|--------|----------|
| "다음 작업 가져와줘" / "계획 세워줘" | `/aick-plan` |
| "개발 진행해줘" / "구현해줘" | `/aick-impl` |
| "OO 고쳐줘" (소규모) | `/aick-impl --micro "OO"` |
| "PR {N} 리뷰해줘" | `/aick-review-pr {N}` |
| "PR {N} 머지해줘" | `/aick-merge-pr {N}` |
| "다음 스텝 진행해줘" | `/aick-impl --next` |
| "상태 확인해줘" | `/aick-status` |
| "긴급 수정해줘" | `/aick-hotfix` |

---

## 자동 연속 실행 규칙 (필수)

**현재 프로필: standard**

스킬 완료 후 조건 충족 시 **반드시** 다음 스킬을 자동 호출한다. 멈추거나 질문하지 않는다.

1. `aick-plan` 승인 → `aick-impl` 자동 호출
2. `aick-impl` PR 생성 → `aick-review-pr {prNumber} --auto-fix` 자동 호출
3. `aick-review-pr` 결과:
   - **APPROVED** → `aick-merge-pr {prNumber}` 자동 호출
   - **CRITICAL 발견** → `aick-fix` 자동 호출 → 재리뷰 (루프 가드 최대 2회)
4. `aick-merge-pr` 머지:
   - 남은 스텝 있음 → `aick-impl --next` 자동 호출
   - 마지막 스텝 → Task 완료, `workflowState=null`, 종료

### --all 옵션
`/aick-impl --all` 사용 시 모든 스텝을 사용자 개입 없이 연속 실행.

### 루프 가드
- `aick-fix` → `aick-review-pr` 루프: **최대 2회**. 3회째 CRITICAL 발견 시 REQUEST_CHANGES 출력 후 **즉시 중단**.
- 카운트 기준: 같은 PR에 대한 `aick-fix` 호출 횟수

### 중단 조건 (이 경우에만 멈추고 사용자에게 보고)
- CRITICAL 이슈 auto-fix 실패
- 빌드 실패 (3회 재시도 후)
- 라인 수 제한 초과 (프로필별 상이)
- `aick-fix` → `aick-review-pr` 루프 2회 초과

### 금지 사항
- 자동 호출 대상인데 "진행할까요?" 질문하며 멈추기 **금지**
- `Skill tool` 없이 직접 실행 **금지**
- 에러/REQUEST_CHANGES 외 상황에서 멈추기 **금지**

---

## 워크플로우 상태 추적 프로토콜 (필수)

체이닝 스킬(plan, impl, review-pr, fix, merge-pr) 진입/완료 시 해당 Task의 `workflowState`를 업데이트한다.

**필드 타입·소유권 (PreToolUse 머지 게이트 전제조건):**
- `prNumber` / `fixLoopCount`: **정수 또는 `null`** — 따옴표 금지. PR 생성 시 `prNumber`에 정수 PR 번호 기록.
- `lastReviewDecision`: `APPROVED` / `COMMENT` / `REQUEST_CHANGES` / `null`. `aick-review-pr`가 매 리뷰마다 소유·갱신. `REQUEST_CHANGES`면 `gh pr merge` 차단.

> ⚠️ **부분 갱신 시 기존 필드를 드롭하지 말 것.** `workflowState`는 항상 전체 객체로 다시 쓴다. **Task 완료 시:** `workflowState: null`.

---

## 에러 복구 프로토콜 (필수)

스킬 실행 중 에러 발생 시 아래 형식으로 안내한다:

```
❌ [{에러 유형}]: {구체적 원인}

📋 현재 상태: Task {TASK-ID} Step {N}/{M} · 브랜치 {현재 브랜치}

🔧 복구 방법:
   1. [권장] {가장 안전한 방법}
   2. {대안} / 3. [최후수단] {리셋}
```

| 에러 유형 | 자동 복구 | 수동 복구 |
|----------|----------|----------|
| 빌드 실패 (1-2회) | 자동 재시도 | - |
| 빌드 실패 (3회) | - | [권장] 로그 확인 후 수정 / `--retry` / [최후수단] `--skip` |
| JSON 파싱 에러 | `git checkout` 복원 | [권장] 자동 복원 수락 |
| Git push 충돌 | `pull --rebase` | [권장] 자동 rebase 수락 |
| gh auth 만료 | `gh auth refresh` 안내 | [권장] `! gh auth refresh` |
| 컨텍스트 압축 | workflowState 복원 | [권장] `/aick-status` |

자동 복구 가능한 에러는 자동 시도 후 결과를 보고한다. 미존재 시 3회 재시도 후 사용자 보고.

---

## 스킬 진입 시 경량 점검 프로토콜 (필수)

체이닝 스킬(plan, impl, review-pr, merge-pr) 진입 시 MUST-EXECUTE-FIRST 완료 후 다음 3가지를 확인한다.

1. **PR-backlog 상태 일치**: step.prNumber 있고 status=="pr_created"면 `gh pr view {N} --json state,mergedAt` → MERGED는 done+currentStep 증가, CLOSED는 pending+prNumber 제거, OPEN은 유지. 네트워크 실패 시 스킵(경고).
2. **Stale workflow 감지**: `workflowState.updatedAt < now-30분 && status=="in_progress"` → AskUserQuestion(이어서/처음부터/다른 Task).
3. **Intent 파일 복구**: `.claude/temp/{taskId}-complete-intent.json` 존재 시 미완료 작업 복구 후 삭제.

---

## Git 브랜치 전략

```
main (운영)
  ├── hotfix/HOT-NNN-긴급수정 (main 분기 → main PR)
  └── develop (개발 통합)
        ├── feature/HOJI-XXX-stepN (스텝별 개발)
        └── bugfix/HOJI-XXX-버그명
```

### PR 규칙
- PR은 **develop** 브랜치로 생성, 리뷰 승인 후 **Squash 머지**
- **스텝별 PR 생성** (500라인 미만 단위) — `/aick-impl` 스텝 완료 시 자동 처리
- 리뷰 `/aick-review-pr {번호}`, 머지 `/aick-merge-pr {번호}`

### 커밋 메시지 (conventional)
```
<type>: <description>
예: feat: HOJI-002 Step 1 - common-core util/page
types: feat, fix, refactor, docs, test, chore
```

---

## Git 워크트리 프로토콜

워크트리 감지: `git rev-parse --git-dir` ≠ `git rev-parse --git-common-dir`

| 작업 | 일반 모드 | 워크트리 모드 |
|------|----------|-------------|
| develop 동기화 | `git checkout develop && git pull` | `git fetch origin develop && git merge origin/develop` |
| push | `git push origin develop` | `git push -u origin HEAD` |
| PR merge | `gh pr merge --squash --delete-branch` | `gh pr merge --squash` (NEVER --delete-branch) |
| 브랜치 생성 | `git checkout -b feature/...` | 불필요 (현재 워크트리 브랜치 사용) |

> 네이티브 worktree 사용 시 `.claude/worktrees/`는 `.gitignore`로 추적 제외 (상태 파일 경합 방지).

---

## Task 개발 규칙

### 작업 ID 체계
```
HOJI-{번호}    예: HOJI-001, HOJI-007
```

### 스텝 분리 / 라인 수 제한
- 기본 제한: **500라인 미만** (스텝별 자동 조정 50~1000). aick-plan이 스텝 특성에 따라 자동 설정.
- 제한값 결정: `step.prLineLimit > conventions.prLineLimit > 500`

| 프로필 | 진행 | 경고 | 강력 경고 | 차단 |
|--------|------|------|----------|------|
| standard | <limit×0.6 | limit×0.6~limit | limit~limit×1.4 | >limit×1.4 |

---

## 공통 컨벤션 참조

구현·계획 시 아래 트리거에 해당하면 해당 컨벤션을 참조한다.

| 트리거 (파일/작업) | 참조 |
|-------------------|------|
| `*.kt` (Kotlin 코드) | Kotlin 공식 코딩 컨벤션, `-Xjsr305=strict`, KDoc 필수 |
| `*AutoConfiguration.kt` / `META-INF/spring/...AutoConfiguration.imports` | Spring Boot 3 auto-config — `@ConditionalOnMissingBean`/`@ConditionalOnProperty`로 opt-in/override |
| `build.gradle.kts` / `settings.gradle.kts` | Gradle 8.x Kotlin DSL, `java-library` + `maven-publish`, BOM 의존성 관리 |
| `*Test.kt` | JUnit5 + spring-boot-starter-test, `@DataJpaTest`/`@WebMvcTest` 슬라이스, spring-security-test |
| 요구사항 상세 | `docs/requirements/REQUIREMENTS.md` + `docs/requirements/common-*.md` (모듈별 참조 매핑·필수 변경) |

---

## 테스트 규칙
- 단위 테스트: **80%+**, 통합 테스트: 주요 플로우 100%
- 각 모듈 docs의 "수용 기준/테스트" 절 항목을 충족 (B1~B5 회귀 테스트 포함)

---

## 산출물 저장 위치

```
docs/
├── requirements/   # 요구사항 문서 (REQUIREMENTS.md, common-*.md)
├── api-specs/      # API 명세
├── architecture/   # 아키텍처 문서
└── reports/        # 메트릭/회고 리포트
```

---

<!-- CUSTOM_SECTION_START -->
## 🔑 프로젝트 핵심 구현 규칙 (impl 시 반드시 적용)

> 원본 레거시 코드(로컬 경로는 `.claude/settings.local.json`의 additionalDirectories 참조; Boot 2.1.8 / Kotlin 1.3.61 / `javax.*`)의 로직을 가져오되 아래를 **반드시** 적용한다. 모듈별 파일 매핑·필수 변경은 로컬 스펙(`.claude/temp/`) 참조.

### 마이그레이션 규칙 (원본 → 최신)
| # | 변환 |
|---|---|
| M1 | `javax.persistence.*` → `jakarta.persistence.*` (단 `javax.sql.DataSource`는 **유지**) |
| M2 | `javax.servlet.*` → `jakarta.servlet.*` (필터) |
| M3 | Spring Security 6 — `WebSecurityConfigurerAdapter` **제거** → `SecurityFilterChain` 빈, `authorizeHttpRequests()`/`requestMatchers()` 람다 DSL |
| M4 | auto-config 등록은 `spring.factories` **아님** → `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` |
| M5 | zip4j 1.3.3 → 2.11.x (`ZipFile`+`model.enums.*`, 비밀번호 `CharArray`) |
| M6 | `Sort(Direction, props)` 제거 → `Sort.by(...)` / `Sort.Order` |
| M7 | Kotlin `toUpperCase()`→`uppercase()` |
| M8 | `@ConfigurationProperties` data class + 생성자 바인딩 |
| M9 | 수기 `@PrePersist` → Spring Data JPA Auditing(`@EnableJpaAuditing`, `@CreatedDate` 등) |

### 결합 제거(decoupling) — 강제
1. 모든 패키지 `com.hoji.common.*` — 원본 도메인 단어 **금지** (패키지/네이밍 완전 중립화)
2. `ErrorReason`은 **인터페이스**, 380+ enum 카탈로그 미포함 (라이브러리는 `CommonErrorReason`만)
3. 시큐리티 라우트 규칙 외부화 — `ApiKeyAuthorizeRulesCustomizer` 확장점만 제공
4. 감사 actor 외부화 — 원본 actor enum 제거, `AuditorAware<String>`로 대체
5. 벤더 결합 제거 — 시퀀스는 `javax.sql.DataSource` (HikariDataSource 아님)
6. 도메인 함수 제외 (원본 도메인 전용 함수·마스킹 규칙 등)

### 버그 수정 (반드시 반영)
| ID | 조치 |
|---|---|
| B1 | `InternalProcessException.of()` → `InternalProcessException` 반환 (원본은 BadRequest 반환) |
| B2 | `InternalProcessException` 핸들러 → **500** 반환 (원본은 400) |
| B3 | `FileUtil`의 `fun FileOutputStream.close() = this.close()` **삭제** |
| B4 | 예외 로깅 4xx→`warn`, 5xx→`error` 분리 (원본은 전부 error) |
| B5 | `ApiKeyProperties` prefix `security` → `hoji.security` |

### 모듈 의존 방향 (순환 금지)
- `common-jpa` → `common-core` (Base64 유틸 재사용)
- `common-security` 독립
- 구현 순서: 스캐폴딩(001) → core(002·003) → jpa(004·005) → security(006) → 발행/통합(007)

### Definition of Done
- `./gradlew build` 성공(3모듈 컴파일+테스트), 각 모듈 `AutoConfiguration.imports` 존재, 도메인 단어 0, B1~B5 반영, public 클래스 KDoc, `publishToMavenLocal` 후 샘플 앱 기동 확인.
<!-- CUSTOM_SECTION_END -->
