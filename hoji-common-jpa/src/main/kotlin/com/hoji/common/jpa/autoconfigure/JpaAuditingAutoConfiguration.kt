package com.hoji.common.jpa.autoconfigure

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.data.domain.AuditorAware
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import java.util.Optional

/**
 * Spring Data JPA Auditing을 자동 활성화하고 actor 외부화 기본 빈을 등록한다.
 *
 * JPA가 클래스패스에 있을 때만([ConditionalOnClass]) `@EnableJpaAuditing`을 켜
 * [com.hoji.common.jpa.audit.BaseDomain]/[com.hoji.common.jpa.audit.SystemBaseDomain]의
 * `@CreatedDate`/`@LastModifiedDate`/`@CreatedBy`/`@LastModifiedBy`가 persist/update 시점에 자동으로
 * 채워지게 한다(수기 `@PrePersist` 대체 — M9).
 *
 * actor(생성자/수정자 식별 문자열)는 원본 도메인 enum 결합을 제거해 [String]으로 외부화한다(D4).
 * 기본 [AuditorAware] 빈은 다음 우선순위로 actor를 결정한다:
 * `hoji.jpa.audit.actor` → `spring.application.name` → `"SYSTEM"`.
 *
 * 소비 서비스가 자체 [AuditorAware] 빈을 등록하면 그 빈이 우선한다([ConditionalOnMissingBean]) —
 * 예: 요청 컨텍스트의 사용자 ID를 actor로 쓰는 경우. 소비자가 직접 `@EnableJpaAuditing`을 둔다면
 * 이중 활성화를 피하기 위해 `spring.autoconfigure.exclude`로 본 자동설정을 제외한다.
 */
@AutoConfiguration
@ConditionalOnClass(name = ["jakarta.persistence.EntityManager"])
@EnableJpaAuditing
class JpaAuditingAutoConfiguration {

    /**
     * 외부화된 actor를 공급하는 기본 [AuditorAware] 빈.
     *
     * @param actor `hoji.jpa.audit.actor` → `spring.application.name` → `"SYSTEM"` 순으로 바인딩된 actor 식별자
     * @return 항상 [actor]를 현재 auditor로 반환하는 [AuditorAware]
     */
    @Bean
    @ConditionalOnMissingBean(AuditorAware::class)
    fun auditorAware(
        @Value("\${hoji.jpa.audit.actor:\${spring.application.name:SYSTEM}}") actor: String,
    ): AuditorAware<String> = AuditorAware { Optional.of(actor) }
}
