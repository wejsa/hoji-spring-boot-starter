package com.hoji.common.jpa.converter

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class YesNoConverterTest {
    private val converter = YesNoConverter()

    @Test
    fun `true는 Y로, false는 N으로 인코딩된다`() {
        assertThat(converter.convertToDatabaseColumn(true)).isEqualTo("Y")
        assertThat(converter.convertToDatabaseColumn(false)).isEqualTo("N")
    }

    @Test
    fun `null 속성은 N으로 인코딩된다`() {
        assertThat(converter.convertToDatabaseColumn(null)).isEqualTo("N")
    }

    @Test
    fun `Y는 대소문자 무관하게 true로 디코딩된다`() {
        assertThat(converter.convertToEntityAttribute("Y")).isTrue()
        assertThat(converter.convertToEntityAttribute("y")).isTrue()
    }

    @Test
    fun `N과 null과 기타 값은 false로 디코딩된다`() {
        assertThat(converter.convertToEntityAttribute("N")).isFalse()
        assertThat(converter.convertToEntityAttribute(null)).isFalse()
        assertThat(converter.convertToEntityAttribute("x")).isFalse()
    }
}
