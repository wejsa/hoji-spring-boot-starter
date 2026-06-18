package com.hoji.common.jpa.converter

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class Base64ConverterTest {
    private val converter = Base64Converter()

    @Test
    fun `속성을 인코딩 후 디코딩하면 원본과 같다`() {
        val original = "hello 한글 123"
        val encoded = converter.convertToDatabaseColumn(original)
        assertThat(encoded).isNotEqualTo(original)
        assertThat(converter.convertToEntityAttribute(encoded)).isEqualTo(original)
    }

    @Test
    fun `null 속성과 null 컬럼은 null로 통과한다`() {
        assertThat(converter.convertToDatabaseColumn(null)).isNull()
        assertThat(converter.convertToEntityAttribute(null)).isNull()
    }

    @Test
    fun `빈 문자열도 round-trip 된다`() {
        val encoded = converter.convertToDatabaseColumn("")
        assertThat(converter.convertToEntityAttribute(encoded)).isEqualTo("")
    }
}
