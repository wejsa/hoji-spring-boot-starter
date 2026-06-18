package com.hoji.common.core.error

/**
 * 예외 발생 시 응답 바디로 직렬화되는 에러 표현.
 *
 * @property code 소비자용 에러 코드 ([ErrorReason.toReason]).
 * @property message 사람이 읽을 수 있는 상세 메시지(없을 수 있음).
 */
data class ErrorResponse(
    val code: String,
    val message: String?,
)
