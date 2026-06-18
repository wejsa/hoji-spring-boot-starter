package com.hoji.common.jpa

import org.springframework.boot.SpringBootConfiguration
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.data.domain.AuditorAware
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import java.util.Optional

/**
 * `@DataJpaTest` 슬라이스 부트스트랩용 테스트 설정.
 *
 * 라이브러리 모듈에는 `@SpringBootApplication`이 없어 `@DataJpaTest`가 컨텍스트를 찾지 못하므로
 * 테스트 전용 `@SpringBootConfiguration`을 둔다. 프로덕션 감사 자동설정(HOJI-005)이 아직 없으므로
 * `@EnableJpaAuditing` + `AuditorAware<String>`를 여기서 로컬로 와이어링한다.
 */
@SpringBootConfiguration
@EnableAutoConfiguration
@EnableJpaAuditing
class JpaAuditingTestConfig {
    /** 테스트용 고정 actor — `@CreatedBy`/`@LastModifiedBy` 채움 검증용. */
    @Bean
    fun auditorAware(): AuditorAware<String> = AuditorAware { Optional.of(TEST_ACTOR) }

    companion object {
        const val TEST_ACTOR: String = "TEST-ACTOR"
    }
}
