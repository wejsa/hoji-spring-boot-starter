package com.hoji.common.core.util

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class RandomUtilTest {

    @Test
    fun `generateRandom - 지정 길이의 숫자 문자열`() {
        val result = generateRandom(12)
        assertEquals(12, result.length)
        assertTrue(result.all { it.isDigit() }, "숫자(0-9)로만 구성되어야 한다: $result")
    }

    @Test
    fun `generateRandom - 길이 0이면 빈 문자열`() {
        assertEquals("", generateRandom(0))
    }

    @Test
    fun `generateRandomByte - 지정 길이의 바이트 배열`() {
        assertEquals(16, generateRandomByte(16).size)
    }
}
