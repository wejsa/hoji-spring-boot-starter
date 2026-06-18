package com.hoji.common.jpa.audit

import com.hoji.common.jpa.JpaAuditingTestConfig.Companion.TEST_ACTOR
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager

/** [SystemBaseDomain] 상속 검증용 테스트 전용 엔티티. */
@Entity
class SystemBaseDomainTestEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    var name: String = "",
) : SystemBaseDomain()

/**
 * [SystemBaseDomain]의 JPA Auditing 동작 검증.
 *
 * persist 시 시각과 actor(createdId/updatedId)가 `AuditorAware<String>` 값으로 채워짐을 확인한다.
 */
@DataJpaTest
class SystemBaseDomainTest @Autowired constructor(
    private val em: TestEntityManager,
) {
    @Test
    fun `persist 시 시각과 actor가 채워진다`() {
        val saved = em.persistFlushFind(SystemBaseDomainTestEntity(name = "a"))

        assertThat(saved.createdAt).isNotNull()
        assertThat(saved.updatedAt).isNotNull()
        assertThat(saved.createdId).isEqualTo(TEST_ACTOR)
        assertThat(saved.updatedId).isEqualTo(TEST_ACTOR)
    }

    @Test
    fun `update 시 updatedId가 actor로 갱신되고 createdId는 불변이다`() {
        val saved = em.persistFlushFind(SystemBaseDomainTestEntity(name = "a"))
        val createdId = saved.createdId

        saved.name = "b"
        em.persistAndFlush(saved)
        em.clear()
        val reloaded = em.find(SystemBaseDomainTestEntity::class.java, saved.id)

        assertThat(reloaded.createdId).isEqualTo(createdId)
        assertThat(reloaded.updatedId).isEqualTo(TEST_ACTOR)
    }
}
