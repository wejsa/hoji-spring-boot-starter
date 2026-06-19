package com.hoji.smoke

import com.hoji.common.core.error.GlobalExceptionHandler
import com.hoji.common.jpa.sequence.SequenceRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext
import org.springframework.data.domain.AuditorAware
import org.springframework.security.web.SecurityFilterChain

/**
 * 3개 스타터(core/jpa/security)를 단일 `@SpringBootTest` 컨텍스트에 조립해 auto-configuration이
 * 충돌 없이 동시 기동하고 각 모듈의 핵심 빈이 등록되는지 검증하는 통합 스모크 테스트.
 *
 * 소비 서비스가 세 모듈을 함께 의존했을 때의 조립(composition) 정합성을 회귀로 고정한다 — 서블릿 웹
 * 컨텍스트(core/security) + 인메모리 JPA 인프라(jpa)가 한 컨텍스트에서 양립함을 확인한다.
 */
@SpringBootTest
class StarterCompositionSmokeTest(
    @Autowired private val context: ApplicationContext,
) {

    @Test
    fun `3개 스타터 auto-config가 단일 컨텍스트에서 충돌 없이 기동된다`() {
        assertThat(context).isNotNull()
    }

    @Test
    fun `core 전역 예외 핸들러 빈이 등록된다`() {
        assertThat(context.getBeansOfType(GlobalExceptionHandler::class.java)).isNotEmpty()
    }

    @Test
    fun `jpa 감사 AuditorAware 빈이 등록된다`() {
        assertThat(context.getBeansOfType(AuditorAware::class.java)).isNotEmpty()
    }

    @Test
    fun `jpa 시퀀스 조회 빈이 등록된다`() {
        assertThat(context.getBeansOfType(SequenceRepository::class.java)).isNotEmpty()
    }

    @Test
    fun `security 기본 시큐리티 필터체인 빈이 등록된다`() {
        assertThat(context.getBeansOfType(SecurityFilterChain::class.java)).hasSize(1)
    }
}
