package com.hoji.common.jpa.audit

import jakarta.persistence.Column
import jakarta.persistence.EntityListeners
import jakarta.persistence.MappedSuperclass
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

/**
 * 생성/수정 시각을 Spring Data JPA Auditing으로 자동 기록하는 베이스 엔티티.
 *
 * 소비 엔티티가 상속하면 [createdAt]/[updatedAt]이 persist/update 시점에 자동으로 채워진다
 * (수기 `@PrePersist`/`@PreUpdate` 대체 — M9). Auditing 활성화(`@EnableJpaAuditing`)는
 * 소비 서비스 또는 라이브러리 자동설정(HOJI-005)이 담당한다.
 *
 * 컬럼명(CREATED_AT/UPDATED_AT)은 범용 관례이며, 다른 명명이 필요한 소비자는 자체 베이스를 정의한다.
 */
@MappedSuperclass
@EntityListeners(AuditingEntityListener::class)
abstract class BaseDomain {
    /** 최초 저장 시각(이후 불변 — `updatable = false`). */
    @CreatedDate
    @Column(name = "CREATED_AT", updatable = false)
    lateinit var createdAt: LocalDateTime

    /** 최종 수정 시각. */
    @LastModifiedDate
    @Column(name = "UPDATED_AT")
    lateinit var updatedAt: LocalDateTime
}
