package com.hoji.common.core.util

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date

/**
 * 날짜/시간 포맷 변환·파싱 유틸. 도메인 비결합 — 순수 [java.time] 기반.
 *
 * 확장 함수는 `LocalDateTime`/`LocalDate`를 흔히 쓰는 문자열 표현으로 변환하고,
 * top-level 함수는 문자열을 `LocalDate`/`LocalDateTime`으로 역변환한다.
 */

/** `yyMMdd` 문자열로 변환한다. */
fun LocalDateTime.convertToYyMMdd(): String = this.format(DateTimeFormatter.ofPattern("yyMMdd"))

/** `yyyyMMdd` 문자열로 변환한다. */
fun LocalDateTime.convertToYyyyMMdd(): String = this.format(DateTimeFormatter.ofPattern("yyyyMMdd"))

/** [separator]로 구분한 `yyyy{sep}MM{sep}dd` 문자열로 변환한다. */
fun LocalDateTime.convertToYyyyMMdd(separator: String): String =
    this.format(DateTimeFormatter.ofPattern("yyyy${separator}MM${separator}dd"))

/** `yyyyMMddHHmm` 문자열로 변환한다. */
fun LocalDateTime.convertToYyyyMMddHHmm(): String = this.format(DateTimeFormatter.ofPattern("yyyyMMddHHmm"))

/** `yyyyMMddHHmmss` 문자열로 변환한다. */
fun LocalDateTime.convertToYyyyMMddHHmmss(): String = this.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))

/** `MMdd` 문자열로 변환한다. */
fun LocalDateTime.convertToMMdd(): String = this.format(DateTimeFormatter.ofPattern("MMdd"))

/** `yyyyMMddHHmmss`를 (날짜 `yyyyMMdd`, 시각 `HHmmss`) 쌍으로 분리한다. */
fun LocalDateTime.pairOfYyyyMMddHHmmss(): Pair<String, String> =
    convertToYyyyMMddHHmmss().let { it.substring(0, 8) to it.substring(8, 14) }

/** 시스템 기본 시간대 기준 [Date]로 변환한다. */
fun LocalDateTime.convertToDate(): Date = Date.from(this.atZone(ZoneId.systemDefault()).toInstant())

/** `yyyyMMdd` 문자열로 변환한다. */
fun LocalDate.convertToYyyyMMdd(): String = this.format(DateTimeFormatter.ofPattern("yyyyMMdd"))

/** 해당 연도 1월 1일을 `yyyyMMdd` 문자열로 반환한다. */
fun LocalDate.getThisYearsFirstDayAsYyyyMMdd(): String =
    LocalDate.of(this.year, 1, 1).format(DateTimeFormatter.ofPattern("yyyyMMdd"))

/** [date]가 비었으면 오늘, 아니면 `yyyyMMdd`로 파싱한 [LocalDate]를 반환한다. */
fun getDate(date: String?): LocalDate =
    if (date.isNullOrEmpty()) LocalDate.now() else LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyyMMdd"))

/** `yyyyMMdd` 문자열을 자정(`00:00`) [LocalDateTime]으로 변환한다. */
fun convertFromYyyyMMdd(date: String): LocalDateTime =
    LocalDateTime.of(LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyyMMdd")), LocalTime.MIN)

/** `yyyy/MM/dd HH:mm:ss` 문자열을 [LocalDateTime]으로 파싱한다. */
fun convertFromYyyyMMddHHmmss(date: String): LocalDateTime =
    LocalDateTime.parse(date, DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"))

/** `yyyyMMdd` 문자열을 [LocalDate]로 파싱한다. */
fun convertFromYyyyMMddToLocalDate(date: String): LocalDate =
    LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyyMMdd"))

/** `yyMMdd` 문자열을 [LocalDate]로 파싱한다. */
fun convertFromYyMMddToLocalDate(date: String): LocalDate =
    LocalDate.parse(date, DateTimeFormatter.ofPattern("yyMMdd"))
