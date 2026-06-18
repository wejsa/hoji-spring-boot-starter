package com.hoji.common.security

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.mock.web.MockFilterChain
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.security.core.context.SecurityContextHolder
import java.nio.charset.StandardCharsets
import java.security.MessageDigest

/**
 * [ApiKeyAuthFilter] 인증 로직 단위 검증.
 *
 * spring-test의 mock 서블릿 객체로 보안 체인 없이 필터 단독을 구동하여, 키 추출/매칭/역할 부여와
 * deny-by-default(빈 키) 동작을 독립 검증한다.
 */
class ApiKeyAuthFilterTest {

    private val properties = ApiKeyProperties(
        apiKeys = listOf(
            ApiKeyProperties.ApiKeyEntry(clientId = "svc", key = "secret-key", roles = listOf("SERVICE")),
            ApiKeyProperties.ApiKeyEntry(clientId = "adm", key = "admin-key", roles = listOf("ADMIN", "SERVICE")),
            ApiKeyProperties.ApiKeyEntry(clientId = "disabled", key = "", roles = listOf("SERVICE")),
        ),
    )
    private val filter = ApiKeyAuthFilter(properties)

    @AfterEach
    fun clearContext() = SecurityContextHolder.clearContext()

    private fun runFilter(request: MockHttpServletRequest): MockFilterChain {
        val chain = MockFilterChain()
        filter.doFilter(request, MockHttpServletResponse(), chain)
        return chain
    }

    @Test
    fun `키가 없으면 인증이 설정되지 않는다`() {
        val chain = runFilter(MockHttpServletRequest())
        assertThat(SecurityContextHolder.getContext().authentication).isNull()
        assertThat(chain.request).isNotNull // 체인은 그대로 진행
    }

    @Test
    fun `잘못된 키는 인증이 설정되지 않는다`() {
        val request = MockHttpServletRequest()
        request.addHeader(ApiKeyAuthFilter.HEADER_NAME, "wrong-key")
        runFilter(request)
        assertThat(SecurityContextHolder.getContext().authentication).isNull()
    }

    @Test
    fun `유효한 헤더 키는 인증과 역할을 설정한다`() {
        val request = MockHttpServletRequest()
        request.addHeader(ApiKeyAuthFilter.HEADER_NAME, "admin-key")
        runFilter(request)

        val auth = SecurityContextHolder.getContext().authentication
        assertThat(auth).isNotNull
        assertThat(auth!!.name).isEqualTo("adm")
        assertThat(auth.authorities.map { it.authority })
            .containsExactlyInAnyOrder("ROLE_ADMIN", "ROLE_SERVICE")
    }

    @Test
    fun `쿼리 파라미터 apikey fallback으로 인증된다`() {
        val request = MockHttpServletRequest()
        request.setParameter(ApiKeyAuthFilter.QUERY_PARAM, "secret-key")
        runFilter(request)

        val auth = SecurityContextHolder.getContext().authentication
        assertThat(auth).isNotNull
        assertThat(auth!!.name).isEqualTo("svc")
        assertThat(auth.authorities.map { it.authority }).containsExactly("ROLE_SERVICE")
    }

    @Test
    fun `빈 key 항목은 빈 요청과 매칭되지 않는다 (deny-by-default 회귀)`() {
        val request = MockHttpServletRequest()
        request.addHeader(ApiKeyAuthFilter.HEADER_NAME, "") // 빈 키 제시
        runFilter(request)
        assertThat(SecurityContextHolder.getContext().authentication).isNull()
    }

    @Test
    fun `MessageDigest isEqual 동등성 경로 검증`() {
        val a = "secret-key".toByteArray(StandardCharsets.UTF_8)
        val same = "secret-key".toByteArray(StandardCharsets.UTF_8)
        val different = "secret-bad".toByteArray(StandardCharsets.UTF_8)
        assertThat(MessageDigest.isEqual(a, same)).isTrue()
        assertThat(MessageDigest.isEqual(a, different)).isFalse()
    }
}
