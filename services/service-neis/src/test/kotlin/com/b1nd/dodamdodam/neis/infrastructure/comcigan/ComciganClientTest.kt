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

    @Test
    fun `난독화된 컴시간 스크립트를 복호화해 원본 HTML 뒤에 붙인다`() {
        val html = "<script>/* hour-obfuscated-v2 */(function(a,k){var s='';(0,eval)(s);})(" +
            "[[1,\"var x=1;\"],[0,\"C7C3B8AC0043C7EAB84B00A50093009C00E000BC00BD00BF\"]],83);</script>"

        val result = deobfuscateScripts(html, objectMapper)

        assertEquals("$html\nvar x=1;자료.자료122[th]", result)
    }
}
