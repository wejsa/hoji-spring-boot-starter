package com.hoji.common.jpa.converter

import com.hoji.common.core.util.decodeBase64
import com.hoji.common.core.util.encodeBase64
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

/**
 * `String` 속성을 Base64로 인코딩해 컬럼에 저장하고 조회 시 복원하는 컨버터.
 *
 * `null` 속성/컬럼 값은 `null`로 통과시킨다(null-safe). core의 Base64 유틸을 재사용한다.
 */
@Converter
class Base64Converter : AttributeConverter<String, String> {
    /** 속성 문자열을 Base64 컬럼 값으로 인코딩한다. `null`은 그대로 반환. */
    override fun convertToDatabaseColumn(attribute: String?): String? = attribute?.encodeBase64()

    /** Base64 컬럼 값을 원본 문자열로 디코딩한다. `null`은 그대로 반환. */
    override fun convertToEntityAttribute(dbData: String?): String? = dbData?.decodeBase64()
}
