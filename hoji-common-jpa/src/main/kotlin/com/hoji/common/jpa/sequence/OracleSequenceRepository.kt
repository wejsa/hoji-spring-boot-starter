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
 * [Transactional]은 **클래스 레벨**에 둔다 — Spring Boot는 `proxyTargetClass=true`(기본값)로 CGLIB
 * 클래스 프록시를 생성하는데, Kotlin 클래스는 기본 `final`이라 메서드 레벨 애너테이션만으로는
 * `kotlin-spring`(all-open) 플러그인이 클래스를 열어주지 않아 프록시 생성이 실패한다(통합 스모크로 회귀 고정).
 *
 * @property dataSource 시퀀스 조회에 사용할 표준 데이터소스
 */
@Transactional(readOnly = true)
class OracleSequenceRepository(
    private val dataSource: DataSource,
) : SequenceRepository {
    override fun getNextLong(sequenceName: String): Long =
        OracleSequenceMaxValueIncrementer(dataSource, sequenceName).nextLongValue()
}
