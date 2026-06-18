package com.hoji.common.jpa.autoconfigure

import com.hoji.common.jpa.sequence.OracleSequenceRepository
import com.hoji.common.jpa.sequence.SequenceRepository
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate
import org.springframework.context.annotation.Bean
import javax.sql.DataSource

/**
 * 시퀀스 채번 빈을 자동 구성한다.
 *
 * 컨텍스트에 [DataSource] 단일 후보가 있을 때만([ConditionalOnSingleCandidate]), 그리고 소비 서비스가
 * 자체 [SequenceRepository]를 등록하지 않았을 때만([ConditionalOnMissingBean]) 기본
 * [OracleSequenceRepository]를 등록한다. 소비자는 동일 타입 빈 등록만으로 손쉽게 오버라이드한다.
 */
@AutoConfiguration
class SequenceAutoConfiguration {

    /** [DataSource] 단일 후보 + 소비자 미등록 시 사용할 기본 Oracle 시퀀스 채번 빈. */
    @Bean
    @ConditionalOnMissingBean(SequenceRepository::class)
    @ConditionalOnSingleCandidate(DataSource::class)
    fun oracleSequenceRepository(dataSource: DataSource): SequenceRepository =
        OracleSequenceRepository(dataSource)
}
