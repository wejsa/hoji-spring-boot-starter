package com.hoji.common.core.error

import org.springframework.http.HttpStatus

/**
 * 잘못된 요청(400)을 표현하는 예외.
 *
 * @param errorReason 소비자용 에러 코드(기본 [CommonErrorReason.INVALID_INPUT]).
 */
class BadRequestException(
    errorReason: ErrorReason = CommonErrorReason.INVALID_INPUT,
    message: String? = null,
    cause: Throwable? = null,
) : ApplicationException(errorReason, message, cause) {
    override val httpStatus: HttpStatus = HttpStatus.BAD_REQUEST
}
