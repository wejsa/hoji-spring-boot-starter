package com.hoji.smoke

import com.hoji.common.jpa.sequence.SequenceRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.aop.support.AopUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

/**
 * 3개 스타터를 조립한 통합 컨텍스트에서 핵심 빈의 **행위**를 실제로 행사해 회귀를 고정하는 스모크 테스트.
 *
 * [StarterCompositionSmokeTest]가 빈 *존재*를 단언하는 것과 달리, 본 테스트는 실제 호출 경로를 통과시켜
 * 빈 존재만으로는 드러나지 않는 런타임 결함(트랜잭션 프록시 미생성·인가 백오프 등)을 잡는다(회고 L-004):
 *  - **jpa 시퀀스**: [SequenceRepository]를 실제 호출해 `OracleSequenceRepository`의 CGLIB `@Transactional`
 *    프록시 + 채번 SQL 왕복이 살아있음을 확인(L-001 final+CGLIB / L-002 auto-config 순서 회귀를 행위로 고정).
 *  - **security**: API 키 미제시 요청이 deny-by-default로 401을 받는지 확인(인증 엔트리포인트 동작).
 *
 * 시큐리티 필터 체인이 [MockMvc]에 적용되도록 `@AutoConfigureMockMvc`(기본 addFilters=true)를 사용한다.
 * 채번 대상 `HOJI_SMOKE_SEQ` 시퀀스는 `schema.sql`이 기동 시 생성하며, H2는 `MODE=Oracle`로 기동한다.
 */
@SpringBootTest
@AutoConfigureMockMvc
class StarterBehaviorSmokeTest(
    @Autowired private val sequenceRepository: SequenceRepository,
    @Autowired private val mockMvc: MockMvc,
) {

    @Test
    fun `시퀀스 빈은 트랜잭션 프록시이며 실제 채번 호출이 단조 증가한다`() {
        // 빈 존재가 아니라 프록시가 실제로 감싸졌는지 — Kotlin final 클래스 + CGLIB 제약(L-001) 회귀 고정
        assertThat(AopUtils.isAopProxy(sequenceRepository)).isTrue()

        // 트랜잭션 프록시 + DataSource 채번 SQL 왕복을 실제로 행사 — 단조 증가로 실 호출을 단언
        val first = sequenceRepository.getNextLong(SEQUENCE_NAME)
        val second = sequenceRepository.getNextLong(SEQUENCE_NAME)
        assertThat(second).isGreaterThan(first)
    }

    @Test
    fun `API 키 없는 비허용 경로 요청은 deny-by-default로 401을 받는다`() {
        mockMvc.get("/smoke/secured").andExpect {
            status { isUnauthorized() }
        }
    }

    private companion object {
        /** `schema.sql`이 생성하는 채번 대상 시퀀스명. */
        const val SEQUENCE_NAME = "HOJI_SMOKE_SEQ"
    }
}
