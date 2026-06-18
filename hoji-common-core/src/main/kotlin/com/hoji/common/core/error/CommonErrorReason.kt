package com.hoji.common.core.error

/**
 * 라이브러리가 코드를 직접 써야 하는 지점(예: 입력 검증 실패)의 기본 [ErrorReason] 값.
 *
 * 소비 서비스는 본 enum을 무시하고 자신의 [ErrorReason] 구현을 사용해도 된다.
 */
enum class CommonErrorReason(private val reason: String) : ErrorReason {
    /** 입력 검증 실패. */
    INVALID_INPUT("COMMON-400-INVALID-INPUT"),

    /** 내부 처리 오류. */
    INTERNAL_ERROR("COMMON-500-INTERNAL"),

    /** 외부 연동 오류. */
    EXTERNAL_ERROR("COMMON-502-EXTERNAL"),
    ;

    override fun toReason(): String = reason
}
