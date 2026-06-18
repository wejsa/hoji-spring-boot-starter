package com.hoji.common.core.error

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

/**
 * 라이브러리 예외 계층을 HTTP 응답으로 변환하는 도메인 비결합 전역 예외 핸들러.
 *
 * [ApplicationException]과 그 하위 예외는 각자의 [ApplicationException.httpStatus]로 응답하므로
 * 단일 핸들러가 [BadRequestException]·[ExternalServiceException]·[InternalProcessException] 및
 * 소비 서비스가 추가한 임의의 [ApplicationException] 하위 타입을 모두 처리한다.
 * Bean Validation 실패([MethodArgumentNotValidException])는 400 + [CommonErrorReason.INVALID_INPUT]로 변환한다.
 *
 * B4: 상태코드 계열에 따라 4xx는 `warn`, 5xx는 `error`로 분리 로깅한다(과거 전부 `error`로 남기던 동작 수정).
 */
@RestControllerAdvice
class GlobalExceptionHandler {

    private val log = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    /**
     * 라이브러리 예외 계층을 응답으로 변환한다.
     *
     * 응답 상태는 [ApplicationException.httpStatus], 바디는 [ApplicationException.toErrorResponse]를 따른다.
     * B2: [InternalProcessException]은 500으로 응답한다(과거 400 버그 수정 — 예외 자신의 `httpStatus`가 결정).
     *
     * @param ex 라이브러리 예외 계층 인스턴스
     * @return 예외의 HTTP 상태와 [ErrorResponse] 바디를 담은 응답
     */
    @ExceptionHandler(ApplicationException::class)
    fun handleApplicationException(ex: ApplicationException): ResponseEntity<ErrorResponse> {
        logByStatus(ex, ex.httpStatus)
        return ResponseEntity.status(ex.httpStatus).body(ex.toErrorResponse())
    }

    /**
     * Bean Validation 실패를 400 + [CommonErrorReason.INVALID_INPUT]로 변환한다.
     *
     * 원본 도메인 enum 의존을 제거하고 라이브러리 기본 코드([CommonErrorReason])로 대체했다.
     *
     * @param ex 검증 실패 예외
     * @return 400 상태와 필드 오류 요약을 담은 응답
     */
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(ex: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        val detail =
            ex.bindingResult.fieldErrors.joinToString(", ") { fieldError ->
                "${fieldError.field}: ${fieldError.defaultMessage}"
            }
        log.warn("입력 검증 실패: {}", detail)
        val body = ErrorResponse(CommonErrorReason.INVALID_INPUT.toReason(), detail.ifEmpty { null })
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body)
    }

    private fun logByStatus(ex: Throwable, status: HttpStatus) {
        if (status.is5xxServerError) {
            log.error("처리 중 오류 (status={}): {}", status.value(), ex.message, ex)
        } else {
            log.warn("요청 처리 실패 (status={}): {}", status.value(), ex.message)
        }
    }
}
