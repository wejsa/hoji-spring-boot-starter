package com.hoji.common.jpa.converter

import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

/**
 * `Boolean` 속성을 `"Y"`/`"N"` 컬럼 값으로 매핑하는 컨버터.
 *
 * - 쓰기: `true`→`"Y"`, `false`/`null`→`"N"`.
 * - 읽기: `"Y"`(대소문자 무관)→`true`, 그 외(`null` 포함)→`false`.
 */
@Converter
class YesNoConverter : AttributeConverter<Boolean, String> {
    override fun convertToDatabaseColumn(attribute: Boolean?): String =
        YesNoType.of(attribute == true).name

    override fun convertToEntityAttribute(dbData: String?): Boolean =
        YesNoType.Y.name.equals(dbData, ignoreCase = true)
}
