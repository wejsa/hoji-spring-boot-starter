package com.hoji.smoke

import org.springframework.boot.autoconfigure.SpringBootApplication

/**
 * 통합 스모크 검증 전용 Spring Boot 애플리케이션.
 *
 * `com.hoji.smoke` 패키지만 컴포넌트 스캔하며, 3개 스타터(core/jpa/security)의 auto-configuration은
 * 각 모듈의 `AutoConfiguration.imports`를 통해 로딩된다([StarterCompositionSmokeTest]가 조립 기동을 검증).
 * 라이브러리 발행물에는 포함되지 않는 테스트 전용 진입점이다(미발행 모듈).
 */
@SpringBootApplication
class SmokeApplication
