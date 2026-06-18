package com.hoji.common.jpa.sequence

/**
 * DB 시퀀스에서 다음 채번 값을 조회하는 도메인 비결합 확장점.
 *
 * 시퀀스명은 [String]으로 외부화되어 소비 서비스가 자체 enum/상수로 관리한다 — 라이브러리는
 * 특정 시퀀스 카탈로그에 결합되지 않는다. 기본 구현은 Oracle 시퀀스에 위임하며([OracleSequenceRepository]),
 * 타 DB는 동일 인터페이스의 빈을 등록해 교체할 수 있다.
 */
interface SequenceRepository {
    /**
     * [sequenceName] 시퀀스의 다음 값을 반환한다.
     *
     * @param sequenceName 조회 대상 DB 시퀀스명
     * @return 시퀀스의 다음 채번 값
     */
    fun getNextLong(sequenceName: String): Long
}
