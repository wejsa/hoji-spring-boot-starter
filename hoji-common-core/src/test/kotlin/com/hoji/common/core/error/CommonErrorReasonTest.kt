package com.hoji.common.core.error

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus

class CommonErrorReasonTest {

    @Test
    fun `각 코드의 toReason 매핑`() {
        assertEquals("COMMON-400-INVALID-INPUT", CommonErrorReason.INVALID_INPUT.toReason())
        assertEquals("COMMON-500-INTERNAL", CommonErrorReason.INTERNAL_ERROR.toReason())
        assertEquals("COMMON-502-EXTERNAL", CommonErrorReason.EXTERNAL_ERROR.toReason())
    }

    @Test
    fun `소비자 정의 ErrorReason도 사용할 수 있다 (fun interface)`() {
        val custom = ErrorReason { "MY-001" }

        assertEquals("MY-001", custom.toReason())
    }

    @Test
    fun `예외별 httpStatus와 errorReason 기본값`() {
        assertEquals(HttpStatus.BAD_REQUEST, BadRequestException().httpStatus)
        assertEquals(HttpStatus.BAD_GATEWAY, ExternalServiceException().httpStatus)
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, InternalProcessException().httpStatus)
        assertEquals(CommonErrorReason.INVALID_INPUT, BadRequestException().errorReason)
    }

    @Test
    fun `toErrorResponse는 코드와 메시지를 담는다`() {
        val response = BadRequestException(CommonErrorReason.INVALID_INPUT, "bad").toErrorResponse()

        assertEquals("COMMON-400-INVALID-INPUT", response.code)
        assertEquals("bad", response.message)
    }
}
