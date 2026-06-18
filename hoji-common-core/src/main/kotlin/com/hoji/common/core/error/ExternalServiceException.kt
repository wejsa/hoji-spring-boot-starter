package com.hoji.common.core.error

import org.springframework.http.HttpStatus

/**
 * 외부 연동 실패(502)를 표현하는 예외.
 *
 * 상위 의존(외부 서비스) 실패이므로 HTTP 상태는 [HttpStatus.BAD_GATEWAY]다.
 *
 * @param errorReason 소비자용 에러 코드(기본 [CommonErrorReason.EXTERNAL_ERROR]).
 */
class ExternalServiceException(
    errorReason: ErrorReason = CommonErrorReason.EXTERNAL_ERROR,
    message: String? = null,
    cause: Throwable? = null,
) : ApplicationException(errorReason, message, cause) {
    override val httpStatus: HttpStatus = HttpStatus.BAD_GATEWAY
}
