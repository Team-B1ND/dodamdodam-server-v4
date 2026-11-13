package com.b1nd.dodamdodam.neis.infrastructure.comcigan

import com.sun.net.httpserver.HttpServer
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable
import java.net.InetSocketAddress
import java.net.URLDecoder
import java.nio.charset.Charset
import java.time.LocalDate
import java.util.Base64
import java.util.concurrent.CopyOnWriteArrayList

class ComciganClientTest {
    private val mapper = ComciganFixtures.mapper
    private val monday = LocalDate.of(2026, 11, 9)
    private val protocol get() = ComciganProtocol(ComciganFixtures.html, mapper)

    @Test
    fun `컴시간 요청 파라미터에 주차와 암호화 버전을 포함한다`() {
        assertEquals("83044_20999_0_2_enc5", buildTimetableParameter("83044_", 20999, 2))
    }

    @Test
    fun `중첩된 스크립트를 실행 없이 풀고 암호화 응답을 검증한다`() {
        assertEquals(ComciganFixtures.script, unpackComciganScripts(ComciganFixtures.pack(ComciganFixtures.pack(ComciganFixtures.script)), mapper))
        assertEquals("한글 학교", protocol.decodeText(ComciganFixtures.text("한글 학교")))
        val encoded = ComciganFixtures.response(mapOf("message" to "시간표"))
        assertEquals("시간표", protocol.decodeResponse(encoded)["message"].asText())
        val parts = encoded.trimEnd('\u0000').split(':').toMutableList()
        val tag = Base64.getDecoder().decode(parts[3]).apply { this[0] = (this[0].toInt() xor 1).toByte() }
        parts[3] = Base64.getEncoder().encodeToString(tag)
        assertThrows(IllegalStateException::class.java) { protocol.decodeResponse(parts.joinToString(":")) }
    }

    @Test
    fun `게시되지 않은 주차는 현재 주차로 대체하지 않는다`() {
        val data = ComciganFixtures.timetable()
        assertEquals(2, resolveWeek(data, monday.plusWeeks(1), protocol.weeksField, protocol::decodeText))
        assertThrows(IllegalStateException::class.java) {
            resolveWeek(data, monday.plusWeeks(2), protocol.weeksField, protocol::decodeText)
        }
    }

    @Test
    fun `학년 반과 세 자리 교사 및 그룹 과목과 변경 표시를 해석한다`() {
        val rows = parseComciganTimeTables(ComciganFixtures.timetable(), protocol, monday, ComciganProperties("학교"))
        val own = rows.filter { it.grade == 2 && it.room == 3 }
        assertEquals(2, own.size)
        assertEquals(listOf(1, 2), own.map { it.period })
        assertTrue(own.all { it.date == monday && it.subject == "웹프" && it.teacher == "김경" })
        assertTrue(own[0].isReplaced)
        assertFalse(own[1].isReplaced)
        assertEquals(1, rows.count { it.grade == 1 && it.room == 1 })
    }

    @Test
    fun `변경알림 모드가 없으면 원 시간표와 비교하고 열람제한일을 지킨다`() {
        val data = ComciganFixtures.timetable().put("변경알림", 0)
        val rows = parseComciganTimeTables(data, protocol, monday, ComciganProperties("학교")).filter { it.grade == 2 }
        assertFalse(rows[0].isReplaced)
        assertTrue(rows[1].isReplaced)
        data.put("열람제한일", "2026-11-09")
        assertTrue(parseComciganTimeTables(data, protocol, monday, ComciganProperties("학교")).isEmpty())
    }

    @Test
    fun `실제 HTTP 흐름으로 정확한 학교를 선택하고 요청한 다음 주 데이터를 가져온다`() {
        withServer { client, parameters ->
            val rows = client.fetchWeeklyTimeTables(monday.plusWeeks(1))
            assertTrue(rows.isNotEmpty())
            assertTrue(rows.all { it.date == monday.plusWeeks(1) })
            assertEquals(listOf("83845l%EB%8C%80%EA%B5%AC%EC%86%8C%ED%94%84%ED%8A%B8%EC%9B%A8%EC%96%B4%EA%B3%A0%EB%93%B1%ED%95%99%EA%B5%90_enc5", "83044_20999_0_1_enc5", "83044_20999_0_2_enc5"), parameters)
        }
    }

    @Test
    fun `토큰 만료는 페이지를 다시 읽어 한 번 재시도한다`() {
        withServer(expiredOnce = true) { client, parameters ->
            assertFalse(client.fetchDailyTimeTables(monday).isEmpty())
            assertEquals(2, parameters.count { it.startsWith("83845l") })
        }
    }

    @Test
    fun `다른 주차 응답을 요청 날짜로 저장하지 않는다`() {
        withServer(wrongWeek = true) { client, _ ->
            assertThrows(IllegalStateException::class.java) { client.fetchWeeklyTimeTables(monday.plusWeeks(1)) }
        }
    }

    @Test
    fun `주말에는 외부 API를 호출하지 않는다`() {
        val client = ComciganClient(ComciganProperties("학교", baseUrl = "http://127.0.0.1:1/st"), mapper)
        assertTrue(client.fetchDailyTimeTables(monday.plusDays(5)).isEmpty())
        assertThrows(IllegalArgumentException::class.java) { client.fetchWeeklyTimeTables(monday.plusDays(1)) }
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "COMCIGAN_LIVE_TEST", matches = "true")
    fun `실제 컴시간 2학년 3반 시간표가 제보 화면과 일치한다`() {
        val rows = ComciganClient(ComciganProperties("대구소프트웨어고등학교"), mapper).fetchWeeklyTimeTables(monday)
        val own = rows.filter { it.grade == 2 && it.room == 3 }
        assertEquals(33, own.size)
        assertEquals(listOf("웹프", "웹프", "네트", "네트", "한국"), own.filter { it.date == monday.plusDays(4) }.map { it.subject })
        assertEquals(listOf("자율", "확통", "디자", "디자", "국어", "체육2", "한국"), own.filter { it.date == monday }.map { it.subject })
        assertEquals(listOf(2, 3, 4), own.filter { it.date == monday.plusDays(2) && it.isReplaced }.map { it.period })
    }

    private fun withServer(expiredOnce: Boolean = false, wrongWeek: Boolean = false, test: (ComciganClient, List<String>) -> Unit) {
        val server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        val parameters = CopyOnWriteArrayList<String>()
        server.createContext("/st") { exchange ->
            val body = ComciganFixtures.html.toByteArray(Charset.forName("EUC-KR"))
            exchange.sendResponseHeaders(200, body.size.toLong())
            exchange.responseBody.use { it.write(body) }
        }
        server.createContext("/900886") { exchange ->
            val parameter = String(Base64.getDecoder().decode(URLDecoder.decode(exchange.requestURI.rawQuery, Charsets.UTF_8)))
            parameters.add(parameter)
            val denied = exchange.requestHeaders.getFirst("X-Hour-Token") != "test-token" || (expiredOnce && parameters.size == 1)
            val body = if (denied) "HOUR_TOKEN_REQUIRED" else ComciganFixtures.response(
                if (parameter.startsWith("83845l")) ComciganFixtures.schools()
                else ComciganFixtures.timetable(if (parameter.endsWith("_2_enc5") && !wrongWeek) "2026-11-16" else "2026-11-09")
            )
            val bytes = body.toByteArray()
            exchange.sendResponseHeaders(if (denied) 403 else 200, bytes.size.toLong())
            exchange.responseBody.use { it.write(bytes) }
        }
        server.start()
        try {
            test(ComciganClient(ComciganProperties("대구소프트웨어고등학교", baseUrl = "http://127.0.0.1:${server.address.port}/st"), mapper), parameters)
        } finally {
            server.stop(0)
        }
    }

    @Test
    fun `일반 문자열과 XOR 문자열이 섞인 스크립트를 복호화한다`() {
        val html = "<script>/* hour-obfuscated-v2 */(function(a,k){var s='';(0,eval)(s);})(" +
            "[[1,\"var x=1;\"],[0,\"C7C3B8AC0043C7EAB84B00A50093009C00E000BC00BD00BF\"]],83);</script>"

        val result = unpackComciganScripts(html, mapper)

        assertEquals("<script>var x=1;자료.자료122[th]</script>", result)
    }
}
