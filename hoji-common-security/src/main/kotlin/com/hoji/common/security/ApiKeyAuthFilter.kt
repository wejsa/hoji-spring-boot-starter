package com.hoji.common.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.filter.OncePerRequestFilter
import java.nio.charset.StandardCharsets
import java.security.MessageDigest

/**
 * API-Key 기반 stateless 인증 필터.
 *
 * 요청에서 키를 추출(헤더 [HEADER_NAME] 1순위, 쿼리 파라미터 [QUERY_PARAM] 2순위)하여
 * [ApiKeyProperties]의 활성 키와 **constant-time**([MessageDigest.isEqual])으로 비교한다. 일치하는 항목이
 * 있으면 해당 [ApiKeyProperties.ApiKeyEntry.clientId]를 principal로, `ROLE_` 접두를 붙인 역할을 권한으로 갖는
 * 인증을 [SecurityContextHolder]에 설정한다.
 *
 * 키가 없거나 일치하지 않으면 인증을 설정하지 않고 체인을 그대로 진행한다 — 인가 판단(401 등)은 보안 체인의
 * `anyRequest().authenticated()`(deny-by-default)에 위임한다. 빈 키 항목은 매칭 대상에서 제외된다.
 *
 * 필터는 무상태이며 주입된 [ApiKeyProperties]만 참조하므로 thread-safe 하다.
 *
 * @property properties 허용 API 키 설정
 */
class ApiKeyAuthFilter(
    private val properties: ApiKeyProperties,
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val presented = resolveKey(request)
        if (presented != null && SecurityContextHolder.getContext().authentication == null) {
            matchEntry(presented)?.let { entry ->
                val authorities = entry.roles.map { SimpleGrantedAuthority("ROLE_$it") }
                SecurityContextHolder.getContext().authentication =
                    UsernamePasswordAuthenticationToken(entry.clientId, null, authorities)
            }
        }
        filterChain.doFilter(request, response)
    }

    /** 헤더 우선, 없으면 쿼리 파라미터에서 키를 읽는다. 빈 문자열은 미제시로 취급한다. */
    private fun resolveKey(request: HttpServletRequest): String? =
        request.getHeader(HEADER_NAME)?.takeIf { it.isNotEmpty() }
            ?: request.getParameter(QUERY_PARAM)?.takeIf { it.isNotEmpty() }

    /** 제시된 키를 활성(비어있지 않은) 항목과 constant-time 비교하여 일치 항목을 찾는다. */
    private fun matchEntry(presented: String): ApiKeyProperties.ApiKeyEntry? {
        val presentedBytes = presented.toByteArray(StandardCharsets.UTF_8)
        return properties.apiKeys.firstOrNull { entry ->
            entry.key.isNotEmpty() &&
                MessageDigest.isEqual(presentedBytes, entry.key.toByteArray(StandardCharsets.UTF_8))
        }
    }

    companion object {
        /** API 키 전달 헤더명(1순위). */
        const val HEADER_NAME = "X-API-KEY"

        /** API 키 전달 쿼리 파라미터명(2순위 fallback). */
        const val QUERY_PARAM = "apikey"
    }
}
