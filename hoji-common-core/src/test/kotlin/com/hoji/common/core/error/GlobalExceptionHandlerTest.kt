package com.hoji.common.core.error

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.springframework.core.MethodParameter
import org.springframework.validation.BeanPropertyBindingResult
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException

/**
 * [GlobalExceptionHandler]의 상태 매핑(B2)과 검증 변환 동작을 핸들러 직접 호출로 검증한다.
 *
 * 서블릿/검증 머신러리 의존 없이 결정적으로 검증하기 위해 MockMvc 대신 핸들러 메서드를 직접 호출한다.
 * @RestControllerAdvice 라우팅 자체는 Spring MVC가 보장하는 동작이므로 본 테스트는 핸들러 로직에 집중한다.
 */
class GlobalExceptionHandlerTest {

    private val handler = GlobalExceptionHandler()

    @Test
    fun `BadRequestException은 400으로 응답한다`() {
        val response = handler.handleApplicationException(BadRequestException(message = "잘못된 요청"))

        assertEquals(400, response.statusCode.value())
        assertEquals(CommonErrorReason.INVALID_INPUT.toReason(), response.body?.code)
        assertEquals("잘못된 요청", response.body?.message)
    }

    @Test
    fun `InternalProcessException은 500으로 응답한다 (B2)`() {
        val response = handler.handleApplicationException(InternalProcessException(message = "내부 오류"))

        assertEquals(500, response.statusCode.value())
        assertEquals(CommonErrorReason.INTERNAL_ERROR.toReason(), response.body?.code)
    }

    @Test
    fun `ExternalServiceException은 502로 응답한다`() {
        val response = handler.handleApplicationException(ExternalServiceException(message = "외부 연동 실패"))

        assertEquals(502, response.statusCode.value())
        assertEquals(CommonErrorReason.EXTERNAL_ERROR.toReason(), response.body?.code)
    }

    @Test
    fun `소비자 정의 ErrorReason도 예외의 httpStatus로 응답한다`() {
        val customReason = ErrorReason { "SVC-400-CUSTOM" }
        val response = handler.handleApplicationException(BadRequestException(errorReason = customReason))

        assertEquals(400, response.statusCode.value())
        assertEquals("SVC-400-CUSTOM", response.body?.code)
    }

    @Test
    fun `검증 실패는 400 + INVALID_INPUT으로 변환한다`() {
        val response = handler.handleValidation(validationException("name", "비어 있을 수 없습니다"))

        assertEquals(400, response.statusCode.value())
        assertEquals(CommonErrorReason.INVALID_INPUT.toReason(), response.body?.code)
        assertNotNull(response.body?.message)
        assertEquals("name: 비어 있을 수 없습니다", response.body?.message)
    }

    /** 머신러리(hibernate-validator) 없이 [MethodArgumentNotValidException]을 결정적으로 구성한다. */
    private fun validationException(field: String, defaultMessage: String): MethodArgumentNotValidException {
        val bindingResult = BeanPropertyBindingResult(Any(), "form")
        bindingResult.addError(FieldError("form", field, defaultMessage))
        val methodParameter = MethodParameter(this::class.java.getDeclaredMethod("dummyHandlerMethod", String::class.java), 0)
        return MethodArgumentNotValidException(methodParameter, bindingResult)
    }

    /** [MethodParameter] 구성용 참조 대상 (실행되지 않음). */
    @Suppress("unused")
    private fun dummyHandlerMethod(form: String) = Unit
}
