package com.hoji.common.jpa.converter

/**
 * Boolean 의미를 Y/N으로 명시적으로 표현하는 범용 enum.
 *
 * @property value Y는 `true`, N은 `false`에 대응한다.
 */
enum class YesNoType(val value: Boolean) {
    Y(true),
    N(false),
    ;

    companion object {
        /** [value]에 대응하는 [YesNoType]을 반환한다(`true`→[Y], `false`→[N]). */
        fun of(value: Boolean): YesNoType = if (value) Y else N
    }
}
