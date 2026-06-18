package com.hoji.common.core.error

/**
 * 에러 코드 계약(도메인 비결합).
 *
 * 라이브러리는 구체적인 에러 코드 카탈로그를 알지 않는다. 소비 서비스가 자신의 enum 등으로
 * 본 인터페이스를 구현하여 코드 문자열을 제공한다. 라이브러리 자체 기본값은 [CommonErrorReason] 참조.
 */
fun interface ErrorReason {
    /** 소비자에게 노출할 에러 코드 문자열을 반환한다. */
    fun toReason(): String
}
