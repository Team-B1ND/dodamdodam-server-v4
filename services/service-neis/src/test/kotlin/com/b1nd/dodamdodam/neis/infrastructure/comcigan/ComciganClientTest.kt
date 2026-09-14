package com.b1nd.dodamdodam.neis.infrastructure.comcigan

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDate

class ComciganClientTest {
    private val objectMapper = ObjectMapper()

    @Test
    fun `컴시간 요청 파라미터는 수정일 다음에 주차를 배치한다`() {
        val parameter = buildTimetableParameter(prefix = "73629_", schoolCode = 20999, week = 2)

        assertEquals("73629_20999_0_2", parameter)
    }

    @Test
    fun `날짜에 해당하는 컴시간의 1부터 시작하는 주차 값을 반환한다`() {
        val response = objectMapper.readTree(
            """
            {
              "오늘r": 1,
              "일자자료": [
                [1, "26-09-07 ~ 26-09-12"],
                [2, "26-09-14 ~ 26-09-19"]
              ]
            }
            """.trimIndent()
        )

        assertEquals(1, resolveWeek(response, LocalDate.of(2026, 9, 10)))
        assertEquals(2, resolveWeek(response, LocalDate.of(2026, 9, 14)))
    }
}
