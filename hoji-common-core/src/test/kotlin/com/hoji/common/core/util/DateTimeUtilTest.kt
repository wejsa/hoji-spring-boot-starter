package com.hoji.common.core.util

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime

class DateTimeUtilTest {

    private val dt = LocalDateTime.of(2026, 3, 7, 9, 8, 5)

    @Test
    fun `LocalDateTime 포맷 변환`() {
        assertEquals("260307", dt.convertToYyMMdd())
        assertEquals("20260307", dt.convertToYyyyMMdd())
        assertEquals("2026-03-07", dt.convertToYyyyMMdd("-"))
        assertEquals("202603070908", dt.convertToYyyyMMddHHmm())
        assertEquals("20260307090805", dt.convertToYyyyMMddHHmmss())
        assertEquals("0307", dt.convertToMMdd())
    }

    @Test
    fun `pairOfYyyyMMddHHmmss - 날짜와 시각 분리`() {
        assertEquals("20260307" to "090805", dt.pairOfYyyyMMddHHmmss())
    }

    @Test
    fun `LocalDate 포맷 변환과 연초 계산`() {
        val date = LocalDate.of(2026, 3, 7)
        assertEquals("20260307", date.convertToYyyyMMdd())
        assertEquals("20260101", date.getThisYearsFirstDayAsYyyyMMdd())
    }

    @Test
    fun `getDate - null이나 빈 문자열은 오늘`() {
        assertEquals(LocalDate.now(), getDate(null))
        assertEquals(LocalDate.now(), getDate(""))
    }

    @Test
    fun `getDate - yyyyMMdd 파싱`() {
        assertEquals(LocalDate.of(2026, 3, 7), getDate("20260307"))
    }

    @Test
    fun `문자열에서 역변환`() {
        assertEquals(LocalDateTime.of(2026, 3, 7, 0, 0), convertFromYyyyMMdd("20260307"))
        assertEquals(LocalDateTime.of(2026, 3, 7, 9, 8, 5), convertFromYyyyMMddHHmmss("2026/03/07 09:08:05"))
        assertEquals(LocalDate.of(2026, 3, 7), convertFromYyyyMMddToLocalDate("20260307"))
        assertEquals(LocalDate.of(2026, 3, 7), convertFromYyMMddToLocalDate("260307"))
    }
}
