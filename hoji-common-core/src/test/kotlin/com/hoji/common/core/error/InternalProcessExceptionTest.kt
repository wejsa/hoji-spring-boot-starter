package com.hoji.common.core.error

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus

class InternalProcessExceptionTest {

    @Test
    fun `of()는 InternalProcessException을 반환한다 (B1 회귀)`() {
        val exception = InternalProcessException.of()

        assertInstanceOf(InternalProcessException::class.java, exception)
    }

    @Test
    fun `of()는 전달한 errorReason과 message를 보존한다`() {
        val reason = CommonErrorReason.INTERNAL_ERROR
        val exception = InternalProcessException.of(reason, "boom")

        assertSame(reason, exception.errorReason)
        assertEquals("boom", exception.message)
    }

    @Test
    fun `httpStatus는 500이다 (B2)`() {
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, InternalProcessException().httpStatus)
    }

    @Test
    fun `message 미지정 시 errorReason 코드를 메시지로 사용한다`() {
        val exception = InternalProcessException()

        assertEquals(CommonErrorReason.INTERNAL_ERROR.toReason(), exception.message)
    }
}
