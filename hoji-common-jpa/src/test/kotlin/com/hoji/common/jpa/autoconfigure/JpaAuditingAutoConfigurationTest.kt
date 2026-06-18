package com.hoji.common.jpa.autoconfigure

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.boot.autoconfigure.AutoConfigurations
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration
import org.springframework.boot.test.context.assertj.AssertableApplicationContext
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import org.springframework.data.domain.AuditorAware
import java.util.Optional
import java.util.function.Supplier

/**
 * [JpaAuditingAutoConfiguration] 와이어링 검증.
 *
 * `@EnableJpaAuditing` 기동에 EntityManagerFactory가 필요하므로 DataSource/Hibernate 자동설정 + H2로
 * 최소 JPA 컨텍스트를 띄운다(엔티티 0개여도 부팅 정상). 기본 actor 우선순위
 * (`hoji.jpa.audit.actor` → `spring.application.name` → `"SYSTEM"`)와 소비자 오버라이드
 * ([ConditionalOnMissingBean])를 [ApplicationContextRunner]로 독립 검증한다.
 */
class JpaAuditingAutoConfigurationTest {

    private val runner = ApplicationContextRunner()
        .withConfiguration(
            AutoConfigurations.of(
                DataSourceAutoConfiguration::class.java,
                HibernateJpaAutoConfiguration::class.java,
                JpaAuditingAutoConfiguration::class.java,
            ),
        )
        .withPropertyValues("spring.datasource.url=jdbc:h2:mem:auditing;DB_CLOSE_DELAY=-1")

    /** 컨텍스트의 단일 [AuditorAware] 빈이 보고하는 현재 auditor 값을 반환한다. */
    @Suppress("UNCHECKED_CAST")
    private fun auditorOf(context: AssertableApplicationContext): String =
        (context.getBean(AuditorAware::class.java) as AuditorAware<String>).currentAuditor.orElseThrow()

    @Test
    fun `설정이 없으면 기본 actor는 SYSTEM이다`() {
        runner.run { context ->
            assertThat(context).hasSingleBean(AuditorAware::class.java)
            assertThat(auditorOf(context)).isEqualTo("SYSTEM")
        }
    }

    @Test
    fun `hoji_jpa_audit_actor 설정이 actor를 결정한다`() {
        runner.withPropertyValues("hoji.jpa.audit.actor=BATCH").run { context ->
            assertThat(auditorOf(context)).isEqualTo("BATCH")
        }
    }

    @Test
    fun `actor 미설정 시 spring_application_name으로 폴백한다`() {
        runner.withPropertyValues("spring.application.name=svc").run { context ->
            assertThat(auditorOf(context)).isEqualTo("svc")
        }
    }

    @Test
    fun `소비자가 AuditorAware를 등록하면 그 빈이 우선한다`() {
        runner
            .withBean(AuditorAware::class.java, Supplier { AuditorAware { Optional.of("CUSTOM") } })
            .run { context ->
                assertThat(context).hasSingleBean(AuditorAware::class.java)
                assertThat(auditorOf(context)).isEqualTo("CUSTOM")
            }
    }
}
