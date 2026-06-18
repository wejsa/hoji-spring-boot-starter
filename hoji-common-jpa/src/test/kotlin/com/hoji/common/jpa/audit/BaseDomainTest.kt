package com.hoji.common.jpa.audit

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager

/** [BaseDomain] 상속 검증용 테스트 전용 엔티티. */
@Entity
class BaseDomainTestEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    var name: String = "",
) : BaseDomain()

/**
 * [BaseDomain]의 JPA Auditing 동작 검증.
 *
 * persist 시 createdAt/updatedAt이 채워지고, update 시 updatedAt만 갱신되며 createdAt은 불변임을 확인한다.
 */
@DataJpaTest
class BaseDomainTest @Autowired constructor(
    private val em: TestEntityManager,
) {
    @Test
    fun `persist 시 createdAt과 updatedAt이 채워진다`() {
        val saved = em.persistFlushFind(BaseDomainTestEntity(name = "a"))

        assertThat(saved.createdAt).isNotNull()
        assertThat(saved.updatedAt).isNotNull()
    }

    @Test
    fun `update 시 updatedAt만 갱신되고 createdAt은 불변이다`() {
        val saved = em.persistFlushFind(BaseDomainTestEntity(name = "a"))
        val createdAt = saved.createdAt
        val updatedAt = saved.updatedAt
        Thread.sleep(10) // auditing 타임스탬프 차이 보장

        saved.name = "b"
        em.persistAndFlush(saved)
        em.clear()
        val reloaded = em.find(BaseDomainTestEntity::class.java, saved.id)

        assertThat(reloaded.createdAt).isEqualTo(createdAt)
        assertThat(reloaded.updatedAt).isAfter(updatedAt)
    }
}
