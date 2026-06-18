package com.hoji.common.core.page

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.data.domain.Sort

class CustomPageRequestTest {

    @Test
    fun `size가 MAX_ROW_SIZE를 초과하면 MAX_ROW_SIZE로 보정된다`() {
        val request = CustomPageRequest(page = 0, size = CustomPageRequest.MAX_ROW_SIZE + 1)

        assertEquals(CustomPageRequest.MAX_ROW_SIZE, request.of().pageSize)
    }

    @Test
    fun `page가 음수면 0으로 보정된다`() {
        val request = CustomPageRequest(page = -5, size = 10)

        assertEquals(0, request.of().pageNumber)
    }

    @Test
    fun `size가 1 미만이면 1로 보정된다`() {
        val request = CustomPageRequest(page = 0, size = 0)

        assertEquals(1, request.of().pageSize)
    }

    @Test
    fun `orders의 asc desc 토큰을 방향별로 파싱한다`() {
        val request = CustomPageRequest(orders = listOf("name:asc", "age:desc"))

        val sort = request.of().sort
        assertEquals(Sort.Direction.ASC, sort.getOrderFor("name")?.direction)
        assertEquals(Sort.Direction.DESC, sort.getOrderFor("age")?.direction)
    }

    @Test
    fun `방향을 생략하면 내림차순으로 파싱한다`() {
        val request = CustomPageRequest(orders = listOf("name"))

        assertEquals(Sort.Direction.DESC, request.of().sort.getOrderFor("name")?.direction)
    }

    @Test
    fun `방향 토큰은 대소문자를 구분하지 않는다`() {
        val request = CustomPageRequest(orders = listOf("name:ASC"))

        assertEquals(Sort.Direction.ASC, request.of().sort.getOrderFor("name")?.direction)
    }

    @Test
    fun `orders가 비면 defaultSortProperty 내림차순으로 정렬한다`() {
        val order = CustomPageRequest().of().sort.getOrderFor("createdAt")

        assertEquals(Sort.Direction.DESC, order?.direction)
    }

    @Test
    fun `커스텀 defaultSortProperty를 사용한다`() {
        val order = CustomPageRequest(defaultSortProperty = "modifiedAt").of().sort.getOrderFor("modifiedAt")

        assertEquals(Sort.Direction.DESC, order?.direction)
    }
}
