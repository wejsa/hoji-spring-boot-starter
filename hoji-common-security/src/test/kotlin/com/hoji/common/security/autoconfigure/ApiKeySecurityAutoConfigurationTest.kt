package com.hoji.common.security.autoconfigure

import com.hoji.common.security.ApiKeyAuthFilter
import com.hoji.common.security.ApiKeyAuthorizeRulesCustomizer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.SpringBootConfiguration
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.web.SecurityFilterChain
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

/**
 * [ApiKeySecurityAutoConfiguration] 풀체인 슬라이스 검증.
 *
 * 라이브러리에는 부트 앱이 없으므로 테스트 전용 [TestBootConfig](컴포넌트 스캔 없이 auto-config만 활성화) +
 * 더미 컨트롤러 + 역할 규칙 customizer 빈으로 보안 체인을 구성해 401/200/health/fallback/오버라이드를 검증한다.
 * 모든 경로는 중립 placeholder 경로(api 하위, actuator-health)만 사용한다(도메인 결합 0).
 */
@SpringBootTest(
    classes = [TestBootConfig::class, ApiKeySecurityTestConfig::class],
    webEnvironment = SpringBootTest.WebEnvironment.MOCK,
    properties = [
        "hoji.security.api-keys[0].client-id=internal-service",
        "hoji.security.api-keys[0].key=VALID-KEY-123",
        "hoji.security.api-keys[0].roles[0]=SERVICE",
    ],
)
@AutoConfigureMockMvc
class ApiKeySecurityAutoConfigurationTest(
    @Autowired private val mockMvc: MockMvc,
) {

    @Test
    fun `키가 없으면 401`() {
        mockMvc.get("/api/ping").andExpect { status { isUnauthorized() } }
    }

    @Test
    fun `잘못된 키면 401`() {
        mockMvc.get("/api/ping") {
            header(ApiKeyAuthFilter.HEADER_NAME, "WRONG-KEY")
        }.andExpect { status { isUnauthorized() } }
    }

    @Test
    fun `유효 헤더 키면 역할 매핑 적용되어 200`() {
        mockMvc.get("/api/ping") {
            header(ApiKeyAuthFilter.HEADER_NAME, "VALID-KEY-123")
        }.andExpect { status { isOk() } }
    }

    @Test
    fun `쿼리 파라미터 키 fallback도 200`() {
        mockMvc.get("/api/ping") {
            param(ApiKeyAuthFilter.QUERY_PARAM, "VALID-KEY-123")
        }.andExpect { status { isOk() } }
    }

    @Test
    fun `health 엔드포인트는 무인증 통과`() {
        mockMvc.get("/actuator/health").andExpect { status { isOk() } }
    }
}

/**
 * 소비자가 자체 [SecurityFilterChain]을 등록하면 기본 체인이 생성되지 않음을 검증
 * ([ConditionalOnMissingBean] 오버라이드 회귀).
 */
@SpringBootTest(
    classes = [TestBootConfig::class, OverrideSecurityConfig::class],
    webEnvironment = SpringBootTest.WebEnvironment.MOCK,
)
class ApiKeySecurityOverrideTest(
    @Autowired private val context: ApplicationContext,
) {

    @Test
    fun `소비자 SecurityFilterChain 등록 시 기본 apiKeyFilterChain 미생성`() {
        assertThat(context.containsBean("apiKeyFilterChain")).isFalse()
        assertThat(context.getBeansOfType(SecurityFilterChain::class.java)).hasSize(1)
    }
}

/** 컴포넌트 스캔 없이 auto-config만 트리거하는 테스트 전용 부트 설정. */
@SpringBootConfiguration
@EnableAutoConfiguration
class TestBootConfig

/** 더미 컨트롤러 + 역할 기반 라우트 규칙 customizer(확장점 동작 검증용). */
@Configuration
class ApiKeySecurityTestConfig {

    @Bean
    fun pingController(): PingController = PingController()

    @Bean
    fun serviceRoleRules(): ApiKeyAuthorizeRulesCustomizer =
        ApiKeyAuthorizeRulesCustomizer { registry ->
            registry.requestMatchers("/api/**").hasRole("SERVICE")
        }
}

/** 소비자 오버라이드 회귀 검증용: 모든 요청 permit 하는 자체 체인. */
@Configuration
class OverrideSecurityConfig {

    @Bean
    fun customFilterChain(http: HttpSecurity): SecurityFilterChain =
        http.authorizeHttpRequests { it.anyRequest().permitAll() }.build()
}

/** 인증 필요한 api 경로와 무인증 허용 actuator-health 더미 핸들러. */
@RestController
class PingController {

    @GetMapping("/api/ping")
    fun ping(): String = "pong"

    @GetMapping("/actuator/health")
    fun health(): String = "UP"
}
