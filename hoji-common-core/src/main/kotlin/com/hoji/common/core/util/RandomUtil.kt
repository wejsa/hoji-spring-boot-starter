package com.hoji.common.core.util

import java.security.SecureRandom

/**
 * 난수 생성 유틸. 도메인 비결합.
 *
 * 보안 컨텍스트(nonce 등)에서 예측 가능성을 제거하기 위해 비암호학적
 * [java.util.Random] 대신 CSPRNG인 [SecureRandom]을 사용한다.
 * 출력 길이/형식은 동일하므로 외부 와이어 호환에 영향이 없다.
 */
private val SECURE_RANDOM = SecureRandom()

/** [length] 자리의 숫자(0-9)로만 이루어진 난수 문자열을 생성한다. */
fun generateRandom(length: Int): String {
    val sb = StringBuilder()
    repeat(length) { sb.append(SECURE_RANDOM.nextInt(10)) }
    return sb.toString()
}

/** [length] 바이트의 난수 바이트 배열을 생성한다. */
fun generateRandomByte(length: Int): ByteArray =
    ByteArray(length).also { SECURE_RANDOM.nextBytes(it) }
