package com.hoji.common.security

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * API-Key 인증 설정 바인딩.
 *
 * `hoji.security` prefix 아래 API 키 목록을 받는다(생성자 바인딩, 불변 data class). 라이브러리는
 * 도메인에 결합되지 않으므로 키 카탈로그를 소비 서비스가 환경변수로 외부 주입한다.
 *
 * **deny-by-default**: [ApiKeyEntry.key]가 비어 있는 항목은 비활성으로 간주되어 어떤 요청과도 매칭되지
 * 않는다([ApiKeyAuthFilter]가 빈 키를 스킵). 즉 키가 주입되지 않으면 인증이 통과하지 않는다.
 *
 * @property apiKeys 허용 API 키 목록(기본 빈 목록 → 전면 deny)
 */
@ConfigurationProperties(prefix = "hoji.security")
data class ApiKeyProperties(
    val apiKeys: List<ApiKeyEntry> = emptyList(),
) {
    /**
     * 단일 API 키 항목.
     *
     * @property clientId 호출자 식별값(로깅/감사용, 비밀 아님). 인증 성공 시 principal 이름으로 사용된다.
     * @property key 비밀 API 키. 환경변수로 주입하며, 비어 있으면 해당 항목은 비활성(deny-by-default).
     * @property roles 인증 성공 시 부여할 역할명 목록(`ROLE_` 접두 제외 — 필터가 부여 시 접두를 붙인다).
     */
    data class ApiKeyEntry(
        val clientId: String = "",
        val key: String = "",
        val roles: List<String> = listOf("SERVICE"),
    )
}
