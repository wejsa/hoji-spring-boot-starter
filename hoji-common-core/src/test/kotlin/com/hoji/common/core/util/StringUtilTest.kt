package com.hoji.common.core.util

import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class StringUtilTest {

    @Test
    fun `base64 인코딩-디코딩 round-trip`() {
        val original = "hello world 한글 1234"
        assertEquals(original, original.encodeBase64().decodeBase64())
    }

    @Test
    fun `base64 known value`() {
        assertEquals("aGk=", "hi".encodeBase64())
    }

    @Test
    fun `ByteArray base64 round-trip`() {
        val bytes = byteArrayOf(0, 1, 2, 127, -1, -128)
        val encoded = bytes.encodeBase64ToString()
        assertArrayEquals(bytes, encoded.decodeBase64ToByteArray())
    }

    @Test
    fun `hex 변환 round-trip`() {
        val bytes = byteArrayOf(0x0A, 0xFF.toByte(), 0x10)
        assertEquals("0AFF10", byteArrayToHexStringV2(bytes))
        assertEquals(listOf("0A", "FF", "10"), byteArrayToHexString(bytes))
        assertArrayEquals(bytes, stringHexToByteArray(listOf("0A", "FF", "10")))
    }

    @Test
    fun `mask - nth 위치 치환`() {
        assertEquals("1234****678", "12345678".mask(5, "****", null))
    }

    @Test
    fun `mask - nth가 길이 초과면 원본 반환`() {
        assertEquals("123", "123".mask(99, "*", null))
    }

    @Test
    fun `mask - except와 일치하면 원본 반환`() {
        assertEquals("secret", "secret".mask(1, "*", "secret"))
    }
}
