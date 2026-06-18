package com.hoji.common.core.error

import org.springframework.http.HttpStatus

/**
 * 내부 처리 오류(500)를 표현하는 예외.
 *
 * 상태코드는 [HttpStatus.INTERNAL_SERVER_ERROR](B2 — 과거 400을 반환하던 버그 수정).
 *
 * @param errorReason 소비자용 에러 코드(기본 [CommonErrorReason.INTERNAL_ERROR]).
 */
class InternalProcessException(
    errorReason: ErrorReason = CommonErrorReason.INTERNAL_ERROR,
    message: String? = null,
    cause: Throwable? = null,
) : ApplicationException(errorReason, message, cause) {
    override val httpStatus: HttpStatus = HttpStatus.INTERNAL_SERVER_ERROR

    companion object {
        /**
         * [InternalProcessException] 인스턴스를 생성하는 팩토리.
         *
         * B1: 반환타입은 [InternalProcessException]이다(과거 [BadRequestException]을 반환하던 버그 수정).
         */
        fun of(
            errorReason: ErrorReason = CommonErrorReason.INTERNAL_ERROR,
            message: String? = null,
            cause: Throwable? = null,
        ): InternalProcessException = InternalProcessException(errorReason, message, cause)
    }
}
