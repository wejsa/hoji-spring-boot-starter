package com.hoji.common.core.autoconfigure

import com.hoji.common.core.error.GlobalExceptionHandler
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.context.annotation.Bean

/**
 * common-core 에러 처리 빈을 자동 구성한다.
 *
 * 웹 애플리케이션 컨텍스트에서만 활성화되며([ConditionalOnWebApplication]),
 * 소비 서비스가 동일 타입 빈을 정의하지 않았을 때만 기본 [GlobalExceptionHandler]를 등록한다
 * ([ConditionalOnMissingBean] — 소비자 오버라이드 허용).
 */
@AutoConfiguration
@ConditionalOnWebApplication
class CoreErrorAutoConfiguration {

    /** 소비 서비스가 자체 핸들러를 등록하지 않았을 때 사용할 기본 전역 예외 핸들러. */
    @Bean
    @ConditionalOnMissingBean
    fun globalExceptionHandler(): GlobalExceptionHandler = GlobalExceptionHandler()
}
