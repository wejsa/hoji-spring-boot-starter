package com.hoji.common.core.error

import org.springframework.http.HttpStatus

/**
 * 라이브러리 예외 계층의 추상 베이스.
 *
 * 각 구현은 [errorReason](소비자용 에러 코드)과 [httpStatus](응답 HTTP 상태)를 가진다.
 * 에러 코드와 HTTP 상태는 독립적이다 — 코드 카탈로그는 소비자 책임, HTTP 상태는 예외 종류가 결정한다.
 *
 * @property errorReason 소비자에게 노출할 에러 코드 계약.
 */
abstract class ApplicationException(
    val errorReason: ErrorReason,
    message: String? = null,
    cause: Throwable? = null,
) : RuntimeException(message ?: errorReason.toReason(), cause) {

    /** 응답에 사용할 HTTP 상태. */
    abstract val httpStatus: HttpStatus

    /** 본 예외를 응답 바디 [ErrorResponse]로 변환한다. */
    fun toErrorResponse(): ErrorResponse = ErrorResponse(errorReason.toReason(), message)
}
