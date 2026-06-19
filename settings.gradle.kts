rootProject.name = "hoji-spring-boot-starter"

include(
    "hoji-common-core",
    "hoji-common-jpa",
    "hoji-common-security",
    // 미발행 통합 스모크 검증 모듈 (3 스타터 조립 기동 확인 — 발행 제외는 루트 build.gradle.kts 참조)
    "hoji-smoke-test",
)
