package com.hoji.common.jpa.sequence

import org.springframework.jdbc.support.incrementer.OracleSequenceMaxValueIncrementer
import org.springframework.transaction.annotation.Transactional
import javax.sql.DataSource

/**
 * Oracle 시퀀스 기반 [SequenceRepository] 구현.
 *
 * 표준 [javax.sql.DataSource]에만 의존해(특정 커넥션 풀 구현체 비결합) spring-jdbc의
 * [OracleSequenceMaxValueIncrementer]에 채번을 위임한다. 호출마다 incrementer를 생성하므로
 * 무상태(thread-safe)이며, 시퀀스명은 호출 시점에 결정된다.
 *
 * Oracle 외 DB에서는 해당 DB의 `DataFieldMaxValueIncrementer` 구현을 사용하는 별도
 * [SequenceRepository] 빈으로 교체한다.
 *
 * @property dataSource 시퀀스 조회에 사용할 표준 데이터소스
 */
class OracleSequenceRepository(
    private val dataSource: DataSource,
) : SequenceRepository {
    @Transactional(readOnly = true)
    override fun getNextLong(sequenceName: String): Long =
        OracleSequenceMaxValueIncrementer(dataSource, sequenceName).nextLongValue()
}
