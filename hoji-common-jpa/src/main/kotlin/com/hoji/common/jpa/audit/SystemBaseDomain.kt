package com.hoji.common.jpa.audit

import jakarta.persistence.Column
import jakarta.persistence.EntityListeners
import jakarta.persistence.MappedSuperclass
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

/**
 * 생성/수정 시각과 actor(생성자/수정자 식별 문자열)를 자동 기록하는 시스템 감사 베이스 엔티티.
 *
 * 시각은 [createdAt]/[updatedAt]에, actor는 [createdId]/[updatedId]에 Spring Data JPA Auditing이
 * 채운다(수기 `@PrePersist`/`@PreUpdate` 대체 — M9). actor 값은 소비 서비스가 등록한
 * `AuditorAware<String>` 빈이 제공한다 — 원본의 actor enum 결합을 제거해 도메인 비결합으로 외부화한다.
 * Auditing 활성화(`@EnableJpaAuditing`)와 `AuditorAware` 빈은 소비 서비스 또는 라이브러리
 * 자동설정(HOJI-005)이 담당한다.
 */
@MappedSuperclass
@EntityListeners(AuditingEntityListener::class)
abstract class SystemBaseDomain {
    /** 최초 저장 시각(이후 불변 — `updatable = false`). */
    @CreatedDate
    @Column(name = "SYS_CRE_DTTM", updatable = false)
    lateinit var createdAt: LocalDateTime

    /** 생성 actor(이후 불변 — `updatable = false`). */
    @CreatedBy
    @Column(name = "SYS_CRE_ID", updatable = false)
    lateinit var createdId: String

    /** 최종 수정 시각. */
    @LastModifiedDate
    @Column(name = "SYS_UPD_DTTM")
    lateinit var updatedAt: LocalDateTime

    /** 최종 수정 actor. */
    @LastModifiedBy
    @Column(name = "SYS_UPD_ID")
    lateinit var updatedId: String
}
