# HOJI-007 회고 — 배포·통합 검증

> 생성: 2026-06-19 · 모드: 개별 Task 회고 (`/aick-retro`)

## 기본 정보

| 항목 | 값 |
|------|-----|
| Task ID | HOJI-007 |
| 제목 | 배포·통합 검증 (phase 4 운영/품질) |
| 유형 / 우선순위 | chore / low |
| 시작 (claim) | 2026-06-18T23:43:29Z |
| 계획 승인 | 2026-06-18T23:47:05Z |
| 완료 | 2026-06-19T00:25:01Z |
| 총 소요 | **약 41분 30초** |
| 스텝 수 | 2 (Step 1 발행 설정 / Step 2 스모크 모듈 + 가이드) |
| PR | #12 (Step 1), #13 (Step 2) — 둘 다 squash 머지 |
| 품질 점수(정성) | **92 / 100** |

## 타임라인

| 시각(UTC) | 이벤트 | 비고 |
|-----------|--------|------|
| 23:43:29 | claim | 잠금 + assignee |
| 23:47:05 | 계획 승인 | 설계+스텝 분리 ~3.5분 |
| 23:48:55 | PR #12 생성 | Step 1 (maven-publish 확정) |
| 23:55:27 | 리뷰 | COMMENT (자기 PR · CRITICAL 0) |
| 23:56:48 | 머지 | Step 1 완료 |
| 00:14:07 | PR #13 생성 | Step 2 (스모크 모듈 + README) |
| 00:20:52 | 리뷰 | COMMENT · T3 Full 3-agent · CRITICAL 0 |
| 00:23:29 | 머지 | Step 2 완료 |
| 00:25:01 | task_completed | 백로그 done + phase 4 done |

## 5축 분석

### 1. Speed
- **총 41.5분**으로 phase 4 마무리 Task치고 빠름. 계획(3.5분) → Step 1(8분) → Step 2(11분) + 상태 처리.
- **병목 구간**: Step 1 머지(23:56:48) ~ Step 2 PR 생성(00:14:07) 사이 **약 17분**. 이 구간이 실제 엔지니어링이 집중된 곳 — 스모크 모듈 작성 중 **빌드 실패 2회**를 만나 수정·재빌드(아래 Quality 참조). 리뷰/머지 대기 자체는 각 6~9분으로 짧음(자기 PR 자동 체이닝).

### 2. Quality
- **CRITICAL 0건** (양 스텝), **auto-fix 루프 0회** (fixLoopCount=0), **첫 리뷰 통과율 100%**.
- Step 2는 216줄(>200) → **T3 Full** 자동 분류, 3개 서브에이전트(architecture+security+test) 전원 CRITICAL 0. 5 MAJOR + 7 MINOR는 모두 **비차단 권고**.
- **핵심 성과 — 머지 전 실 결함 2건 포착**: 스모크 모듈이 컨텍스트 기동을 강제하면서 라이브러리 자체 버그를 드러냄:
  1. `SequenceAutoConfiguration` — `@ConditionalOnSingleCandidate(DataSource)`가 순서 미지정 시 0 후보로 평가 → 단일 DataSource 소비자에서도 채번 빈 누락. `@AutoConfiguration(after = DataSourceAutoConfiguration)`로 수정.
  2. `OracleSequenceRepository` — Kotlin `final` + Boot CGLIB(`proxyTargetClass=true`)에서 메서드 레벨 `@Transactional`이 all-open을 발동 못 시켜 프록시 생성 실패 → 컨텍스트 기동 불가. `@Transactional`을 **클래스 레벨**로 이동해 수정.
- **남은 비차단 권고**(머지 차단 아님): 스모크가 빈 *존재*만 단언(행위 미행사), auto-config 순서 지정 방식이 security 모듈과 불일치, README 하단 라이선스 표기(MIT) ↔ POM(Apache-2.0) 불일치.

### 3. Patterns
- **반복 가능성 높은 기술 함정 2종**(Kotlin/Spring): ① final 클래스 + CGLIB 프록시, ② 조건부 빈의 auto-config 평가 순서. 향후 jpa/security 외 모듈 추가 시 재발 위험 → 체크리스트 승격 후보.
- **자주 손댄 파일**: `build.gradle.kts`(양 스텝), `settings.gradle.kts`, `README.md`.
- **스킬 실행 순서**: plan → impl → review(--auto-fix) → merge → (마지막 스텝) 완료 처리. 전 사이클 표준 체이닝 이탈 없음.

### 4. Decisions
| 결정 | 트레이드오프 |
|------|-------------|
| 발행 제외 **별도 스모크 모듈** 신설 (각 모듈 테스트에 분산 X) | 모듈 1개 추가 비용 ↔ 소비자 관점 조립을 단일 컨텍스트로 회귀 고정. 실 결함 2건 포착으로 ROI 입증 |
| `@Transactional` **클래스 레벨** | 메서드 단위 세밀함 포기 ↔ Kotlin/CGLIB 프록시 제약 회피(기동 보장) |
| `@AutoConfiguration(after=클래스리터럴)` | security 모듈의 문자열 `beforeName` 방식과 불일치(기술 부채 소) ↔ 컴파일 타임 안전성 |
| publication 제외 가드(R3) | — 검증 전용 모듈이라 발행물 오염 방지 |

### 5. Lessons (Keep / Improve / Learn / Try)
- **Keep**: 발행 제외 통합 스모크 모듈 — 멀티모듈 라이브러리에서 머지 전 실 결함을 잡는 고ROI 안전망.
- **Improve**: 스모크가 빈 존재만 단언 → 프록시/트랜잭션 실제 호출 1건, deny-by-default 401 1건 등 **행위 검증** 추가.
- **Learn**: kotlin-spring all-open은 **클래스 자체에 stereotype**이 있을 때만 클래스를 연다. 메서드 레벨 `@Transactional`은 CGLIB 프록시 대상 클래스를 열지 못한다.
- **Learn**: `@ConditionalOnSingleCandidate`/`@ConditionalOnBean`은 **평가 시점까지 등록된 빈 정의**만 본다 → DataSource 등록 auto-config보다 뒤로 `after` 고정 필수.
- **Try**: auto-config 순서 지정 방식(클래스 리터럴 `after` vs 문자열 `beforeName`)을 모듈 간 통일.

## Action Items
1. (선택·권고) 스모크 테스트에 행위 검증 추가 — 트랜잭션 프록시 1건 + 보안 401 1건. → `/aick-feature`로 후속 Task화 가능.
2. (선택) auto-config 순서 지정 방식 모듈 간 일관화(jpa ↔ security).
3. (선택) README 라이선스 표기 MIT → Apache-2.0(POM 정합).
4. (체크리스트 승격 후보) "Kotlin final+CGLIB 프록시" / "조건부 빈 auto-config 순서" — 동일 함정 재발 시 `_base/checklists`에 반영 검토(현재 2회 미만이라 보류, 사용자 승인 시 반영).

---
_본 회고는 읽기 전용 분석이며 backlog/completed 상태를 변경하지 않습니다. 학습 항목은 `.claude/state/lessons-learned.json`에 반영됨._
