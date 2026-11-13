package com.b1nd.dodamdodam.neis.infrastructure.comcigan

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import java.nio.charset.Charset
import java.security.MessageDigest
import java.time.LocalDate
import java.util.Base64
import java.util.HexFormat
import javax.crypto.Cipher
import javax.crypto.Mac
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

/** The student page supplies these values; do not pin rotating field numbers or keys. */
internal class ComciganProtocol(html: String, private val objectMapper: ObjectMapper) {
    private val script = unpackComciganScripts(html, objectMapper)
    val route = extract("""url:\s*['"](\./\d+)\?['"]""")
    val searchPrefix = extract("""btoa\(['"](\d+l)['"]""")
    val timetablePrefix = extract("""sc_data\(['"](\d+_)['"]""")
    val token = extract("""window\.__HourRequestToken\s*=\s*['"]([^'"]+)['"]""")
    val originalField = "자료" + extract("""원자료=Q자료\(자료\.자료(\d+)""")
    val dailyField = "자료" + extract("""일일자료=Q자료\(자료\.자료(\d+)""")
    val teacherField = "자료" + extract("""자료\.자료(\d+)\[th]""")
    val subjectField = "자료" + extract("""자료\.자료(\d+)\[sb]""")
    val weeksField = "자료" + extract("""일자설정하기\(H시간표\.자료(\d+)""")
    val startDateField = "자료" + extract("""요일출력하기\(QnmsbChange\(자료\.자료(\d+)""")
    val schoolCodeDivisor = extract("""Math\.floor\(학교명단\[i]\[0]/(\d+)\)\+2""").toInt()
    private val textKey = extract("""var 키\s*=\s*String\.fromCharCode\(([\d,\s]+)\)""")
        .split(',').map { it.trim().toInt().toByte() }.toByteArray()
    private val responseKey: ByteArray

    init {
        val match = Regex("""return\s+((?:a\[\d+]\+?)+);\s*}\)\(\[([^]]+)]""").find(script)
            ?: error("컴시간 응답 키 형식 변경")
        val parts = Regex("""['"]([0-9a-f]+)['"]""").findAll(match.groupValues[2]).map { it.groupValues[1] }.toList()
        val hex = Regex("""a\[(\d+)]""").findAll(match.groupValues[1])
            .joinToString("") { parts[it.groupValues[1].toInt()] }
        responseKey = HexFormat.of().parseHex(hex)
        require(responseKey.size == 32 && textKey.isNotEmpty() && schoolCodeDivisor > 0) { "컴시간 키 형식 변경" }
    }

    fun decodeText(value: String): String {
        if (value.isEmpty()) return ""
        val bytes = Base64.getDecoder().decode(value)
        return String(ByteArray(bytes.size) { i -> (bytes[i].toInt() xor textKey[i % textKey.size].toInt()).toByte() }, EUC_KR)
    }

    fun decodeResponse(body: String): JsonNode {
        val parts = body.trimEnd('\u0000').split(':')
        require(parts.size == 4 && parts[0] in setOf("enc1", "enc5")) { "컴시간 암호화 응답 형식 변경" }
        fun decode(value: String): ByteArray = if (parts[0] == "enc5") Base64.getDecoder().decode(value) else HexFormat.of().parseHex(value)
        val iv = decode(parts[1])
        val ciphertext = decode(parts[2])
        val tag = decode(parts[3])
        require(iv.size == 16 && tag.size == 32 && ciphertext.isNotEmpty() && ciphertext.size % 16 == 0) { "컴시간 암호화 응답 길이 오류" }
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(MessageDigest.getInstance("SHA-256").digest(responseKey), "HmacSHA256"))
        val expected = mac.doFinal(parts.take(3).joinToString(":").toByteArray(Charsets.UTF_8))
        check(MessageDigest.isEqual(expected, tag)) { "컴시간 응답 무결성 검증 실패" }
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.DECRYPT_MODE, SecretKeySpec(responseKey, "AES"), IvParameterSpec(iv))
        return objectMapper.readTree(String(cipher.doFinal(ciphertext), Charsets.UTF_8).trimEnd('\u0000'))
    }

    private fun extract(pattern: String): String = Regex(pattern).find(script)?.groupValues?.get(1)
        ?: error("컴시간 학생 페이지 형식 변경: $pattern")

    companion object {
        private val EUC_KR = Charset.forName("EUC-KR")
    }
}

/** Decode the page's literal XOR blocks without evaluating remote JavaScript. */
internal fun unpackComciganScripts(html: String, objectMapper: ObjectMapper): String {
    val block = Regex("""/\* hour-[\w-]+ \*/\(function\(a,k\).*?}\)\((\[\[.*?]]),(\d+)\);""", RegexOption.DOT_MATCHES_ALL)
    var script = html
    repeat(8) {
        if (!block.containsMatchIn(script)) return script
        script = block.replace(script) { match ->
            val key = match.groupValues[2].toInt()
            objectMapper.readTree(match.groupValues[1]).joinToString("") { chunk ->
                val text = chunk[1].asText()
                if (chunk[0].asInt() != 0) text else {
                    require(text.length % 4 == 0) { "컴시간 스크립트 인코딩 오류" }
                    text.chunked(4).mapIndexed { index, hex -> (hex.toInt(16) xor ((key + index * 13) and 255)).toChar() }.joinToString("")
                }
            }
        }
    }
    error("컴시간 스크립트 중첩 한도 초과")
}

internal fun buildTimetableParameter(prefix: String, schoolCode: Int, week: Int): String =
    "${prefix}${schoolCode}_0_${week}_enc5"

internal fun resolveWeek(
    json: JsonNode,
    date: LocalDate,
    weeksField: String = "일자자료",
    decode: (String) -> String = { it },
): Int {
    val weeks = json[weeksField]
    check(weeks != null && weeks.isArray && !weeks.isEmpty) { "컴시간 주차 자료 없음" }
    for (week in weeks) {
        val range = decode(week[1].asText()).split('~').map { it.trim() }
        check(range.size == 2) { "컴시간 주차 날짜 형식 변경" }
        fun parse(value: String) = LocalDate.parse(if (value.length == 8) "20$value" else value)
        if (date in parse(range[0])..parse(range[1])) return week[0].asInt()
    }
    // Never label a different week's lessons with the requested dates.
    error("컴시간에 $date 주차 시간표가 게시되지 않았어요.")
}
