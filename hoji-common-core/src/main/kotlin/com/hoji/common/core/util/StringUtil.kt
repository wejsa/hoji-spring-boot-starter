package com.hoji.common.core.util

import java.util.Base64
import kotlin.experimental.and

/**
 * 문자열 인코딩(Base64/Hex)·마스킹 유틸. 도메인 비결합 — 순수 JVM 기반.
 */

/** UTF-8 바이트를 Base64로 인코딩한다. */
fun String.encodeBase64(): String = Base64.getEncoder().encodeToString(this.toByteArray())

/** Base64 문자열을 디코딩해 UTF-8 문자열로 복원한다. */
fun String.decodeBase64(): String = String(Base64.getDecoder().decode(this))

/** Base64 문자열을 디코딩해 원본 바이트 배열로 복원한다. */
fun String.decodeBase64ToByteArray(): ByteArray = Base64.getDecoder().decode(this)

/** 바이트 배열을 Base64 문자열로 인코딩한다. */
fun ByteArray.encodeBase64ToString(): String = Base64.getEncoder().encodeToString(this)

/** 2자리 16진수 문자열 리스트를 바이트 배열로 변환한다. */
fun stringHexToByteArray(valueList: List<String>): ByteArray {
    val output = ByteArray(valueList.size)
    for (i in valueList.indices) {
        output[i] = ((Character.digit(valueList[i][0], 16) shl 4) +
            Character.digit(valueList[i][1], 16)).toByte()
    }
    return output
}

/** 바이트 배열을 2자리 대문자 16진수 문자열 리스트로 변환한다. */
fun byteArrayToHexString(valueArray: ByteArray): List<String> {
    val output: MutableList<String> = ArrayList()
    for (input in valueArray) {
        output.add(String.format("%02X", input and 0xff.toByte()))
    }
    return output
}

/** 바이트 배열을 하나의 대문자 16진수 문자열로 변환한다. */
fun byteArrayToHexStringV2(valueArray: ByteArray): String {
    val output = StringBuilder(2 * valueArray.size)
    for (input in valueArray) {
        output.append(String.format("%02X", input and 0xff.toByte()))
    }
    return output.toString()
}

/**
 * [nth]번째(1-base) 문자를 [toValue]로 치환해 마스킹한다.
 *
 * 문자열이 [except]와 같거나 [nth]가 길이를 초과하면 원본을 그대로 반환한다.
 */
fun String.mask(nth: Int, toValue: String, except: String?): String {
    if ((except != null && this == except) || nth > this.length) return this
    return "${this.substring(0, nth - 1)}$toValue${this.substring(nth)}"
}
