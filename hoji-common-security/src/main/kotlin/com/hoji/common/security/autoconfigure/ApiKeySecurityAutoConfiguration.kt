package com.hoji.common.security.autoconfigure

import com.hoji.common.security.ApiKeyAuthFilter
import com.hoji.common.security.ApiKeyAuthorizeRulesCustomizer
import com.hoji.common.security.ApiKeyProperties
import org.springframework.beans.factory.ObjectProvider
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.http.HttpStatus
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.HttpStatusEntryPoint
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

/**
 * API-Key 기반 stateless 보안 체인을 자동 구성한다(Spring Security 6).
 *
 * [SecurityFilterChain]이 클래스패스에 있을 때만 활성화되며([ConditionalOnClass]), 소비 서비스가 자체
 * [SecurityFilterChain] 빈을 정의하지 않은 경우에만([ConditionalOnMissingBean]) 기본 체인을 등록한다 —
 * 소비자는 동일 타입 빈 등록만으로 체인 전체를 오버라이드한다. [ApiKeyProperties]는 자동으로 활성화된다.
 *
 * 기본 체인은 CSRF/HTTP Basic/폼 로그인을 비활성화하고 세션을 [SessionCreationPolicy.STATELESS]로 둔다.
 * [ApiKeyAuthFilter]를 [UsernamePasswordAuthenticationFilter] 앞에 배치해 키를 인증으로 변환하며, 인가 규칙은
 * `health permit → 등록된 [ApiKeyAuthorizeRulesCustomizer] → anyRequest().authenticated()` 순서로
 * 라이브러리가 보장한다(라우트 규칙 자체는 결합 제거를 위해 소비자가 주입). 미인증 요청은
 * [HttpStatusEntryPoint]로 **401**을 반환한다(폼/Basic 리다이렉트 대신 API 친화적 응답).
 *
 * `before = [SecurityAutoConfiguration::class]`로 Spring Boot 기본 보안 자동구성([SecurityAutoConfiguration])보다
 * 먼저 평가되도록 하여 기본 체인 백오프를 보장한다. 클래스 리터럴 참조는 jpa 모듈의 순서 지정 방식과 일관되며,
 * Spring Boot 업그레이드로 참조 클래스가 이동/개명되면 컴파일 시점에 즉시 드러난다(문자열 FQN의 silent-rot 회피).
 */
@AutoConfiguration(before = [SecurityAutoConfiguration::class])
@ConditionalOnClass(SecurityFilterChain::class)
@EnableWebSecurity
@EnableConfigurationProperties(ApiKeyProperties::class)
class ApiKeySecurityAutoConfiguration {

    /**
     * API-Key 인증을 적용한 기본 보안 체인.
     *
     * @param http 보안 체인 빌더
     * @param properties 허용 API 키 설정([ApiKeyAuthFilter]에 주입)
     * @param customizers 소비자가 등록한 라우트 규칙 확장점(없으면 health 외 전부 인증 요구)
     * @return 구성된 [SecurityFilterChain]
     */
    @Bean
    @ConditionalOnMissingBean(SecurityFilterChain::class)
    fun apiKeyFilterChain(
        http: HttpSecurity,
        properties: ApiKeyProperties,
        customizers: ObjectProvider<ApiKeyAuthorizeRulesCustomizer>,
    ): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .httpBasic { it.disable() }
            .formLogin { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .exceptionHandling { it.authenticationEntryPoint(HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)) }
            .addFilterBefore(ApiKeyAuthFilter(properties), UsernamePasswordAuthenticationFilter::class.java)
            .authorizeHttpRequests { auth ->
                auth.requestMatchers("/actuator/health", "/actuator/health/**").permitAll()
                customizers.orderedStream().forEach { customizer -> customizer.customize(auth) }
                auth.anyRequest().authenticated()
            }
        return http.build()
    }
}
