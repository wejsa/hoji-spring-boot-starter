package com.hoji.common.jpa.autoconfigure

import com.hoji.common.jpa.sequence.OracleSequenceRepository
import com.hoji.common.jpa.sequence.SequenceRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.boot.autoconfigure.AutoConfigurations
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType
import java.util.function.Supplier
import javax.sql.DataSource

/**
 * [SequenceAutoConfiguration] 와이어링 검증.
 *
 * DataSource 단일 후보 조건([ConditionalOnSingleCandidate])과 소비자 오버라이드 조건
 * ([ConditionalOnMissingBean])을 [ApplicationContextRunner]로 독립 검증한다 — 패키지 스캔이나
 * 기존 슬라이스 테스트 설정과 충돌하지 않는다. 단일 DataSource 후보는 임베디드 H2로 명시 등록한다.
 */
class SequenceAutoConfigurationTest {

    private val runner = ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(SequenceAutoConfiguration::class.java))

    /** 채번 빈 구성만 검증하므로 비어 있는 임베디드 H2 데이터소스로 충분하다(실제 시퀀스 조회 미수행). */
    private fun embeddedDataSource(): DataSource =
        EmbeddedDatabaseBuilder().generateUniqueName(true).setType(EmbeddedDatabaseType.H2).build()

    @Test
    fun `DataSource 단일 후보면 기본 OracleSequenceRepository가 등록된다`() {
        runner
            .withBean(DataSource::class.java, Supplier { embeddedDataSource() })
            .run { context ->
                assertThat(context).hasSingleBean(SequenceRepository::class.java)
                assertThat(context.getBean(SequenceRepository::class.java))
                    .isInstanceOf(OracleSequenceRepository::class.java)
            }
    }

    @Test
    fun `DataSource가 없으면 SequenceRepository가 등록되지 않는다`() {
        runner.run { context ->
            assertThat(context).doesNotHaveBean(SequenceRepository::class.java)
        }
    }

    @Test
    fun `소비자가 SequenceRepository를 등록하면 기본 빈은 등록되지 않는다`() {
        runner
            .withBean(DataSource::class.java, Supplier { embeddedDataSource() })
            .withBean(SequenceRepository::class.java, Supplier { CustomSequenceRepository() })
            .run { context ->
                assertThat(context).hasSingleBean(SequenceRepository::class.java)
                assertThat(context.getBean(SequenceRepository::class.java))
                    .isInstanceOf(CustomSequenceRepository::class.java)
            }
    }

    /** 소비자 오버라이드 검증용 더미 구현. */
    private class CustomSequenceRepository : SequenceRepository {
        override fun getNextLong(sequenceName: String): Long = 0L
    }
}
