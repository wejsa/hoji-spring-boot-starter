package com.hoji.common.security

import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer

/**
 * API-Key 보안 체인의 라우트 인가 규칙을 소비 서비스가 주입하는 확장점.
 *
 * 라이브러리는 도메인 라우트 규칙을 포함하지 않는다(결합 제거). 기본 체인은 health 엔드포인트 permit과
 * `anyRequest().authenticated()` 사이 지점에서 등록된 모든 커스터마이저를 적용하므로, 소비자는 본 인터페이스
 * 구현 빈을 등록해 자신의 경로 규칙(예: 관리자 전용 경로는 특정 역할만 허용)을 선언한다.
 *
 * 여러 구현 빈을 등록하면 [org.springframework.core.annotation.Order]/[org.springframework.core.Ordered]
 * 순서대로 적용된다 — 더 구체적인 매처를 먼저 등록하도록 순서를 제어한다. 구현 빈이 없으면 기본 체인은
 * health 외 모든 요청을 인증만 요구한다(deny-by-default).
 *
 * 단일 추상 메서드를 가지므로 Kotlin/Java 람다로 간결하게 구현할 수 있다(`fun interface`).
 */
fun interface ApiKeyAuthorizeRulesCustomizer {
    /**
     * 인가 규칙을 [registry]에 등록한다.
     *
     * 구현체는 `anyRequest()`를 호출하지 않는다 — 종단 규칙은 라이브러리가 일괄로 적용한다.
     *
     * @param registry `authorizeHttpRequests` 매처 레지스트리(health permit 이후, anyRequest 이전 시점)
     */
    fun customize(
        registry: AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry,
    )
}
