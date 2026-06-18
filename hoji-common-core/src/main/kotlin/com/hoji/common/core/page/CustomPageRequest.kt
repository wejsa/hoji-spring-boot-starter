package com.hoji.common.core.page

import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort

/**
 * 페이지네이션 요청 파라미터를 표현하는 도메인 비결합 값 객체.
 *
 * 소비 서비스는 [page]/[size]/[orders]를 받아 [of]로 Spring Data [PageRequest]를 생성한다.
 * 정렬 토큰이 비어 있으면 [defaultSortProperty] 내림차순으로 정렬한다.
 *
 * @property page 0-base 페이지 번호. 음수는 0으로 보정된다.
 * @property size 페이지 크기. [MAX_ROW_SIZE] 초과 시 [MAX_ROW_SIZE]로, 1 미만은 1로 보정된다.
 * @property orders `"property:asc"` / `"property:desc"` 형식의 정렬 토큰 목록. 방향 생략 시 내림차순.
 * @property defaultSortProperty [orders]가 비었을 때 사용할 기본 정렬 프로퍼티 (기본값 `createdAt`).
 */
data class CustomPageRequest(
    val page: Int = 0,
    val size: Int = DEFAULT_PAGE_SIZE,
    val orders: List<String> = emptyList(),
    val defaultSortProperty: String = DEFAULT_SORT_PROPERTY,
) {
    /**
     * 보정된 [page]/[size]와 해석된 정렬 규칙으로 [PageRequest]를 생성한다.
     *
     * @return 정렬 규칙이 적용된 [PageRequest]
     */
    fun of(): PageRequest =
        PageRequest.of(page.coerceAtLeast(0), size.coerceIn(1, MAX_ROW_SIZE), resolveSort())

    private fun resolveSort(): Sort {
        if (orders.isEmpty()) {
            return Sort.by(Sort.Direction.DESC, defaultSortProperty)
        }
        return Sort.by(orders.map(::toOrder))
    }

    private fun toOrder(token: String): Sort.Order {
        val parts = token.split(DELIMITER, limit = 2)
        val property = parts.first().trim()
        val direction =
            if (parts.size > 1 && parts[1].trim().uppercase() == ASC) {
                Sort.Direction.ASC
            } else {
                Sort.Direction.DESC
            }
        return Sort.Order(direction, property)
    }

    companion object {
        /** 페이지 크기 상한. */
        const val MAX_ROW_SIZE: Int = 3000

        /** 기본 페이지 크기. */
        const val DEFAULT_PAGE_SIZE: Int = 10

        /** 기본 정렬 프로퍼티. */
        const val DEFAULT_SORT_PROPERTY: String = "createdAt"

        private const val DELIMITER: String = ":"
        private const val ASC: String = "ASC"
    }
}
