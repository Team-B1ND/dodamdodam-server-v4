package com.b1nd.dodamdodam.neis.infrastructure.comcigan

import com.b1nd.dodamdodam.neis.infrastructure.comcigan.data.ParsedTimeTable
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Component
import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.Charset
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDate
import java.util.Base64

@Component
class ComciganClient(
    private val comciganProperties: ComciganProperties,
    private val objectMapper: ObjectMapper,
) {
    private val pageUri get() = URI.create(comciganProperties.baseUrl)
    private val httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build()

    fun fetchWeeklyTimeTables(mondayDate: LocalDate): List<ParsedTimeTable> {
        require(mondayDate.dayOfWeek == DayOfWeek.MONDAY) { "주간 시간표 기준일은 월요일이어야 해요." }
        // Fetch fresh page metadata per sync: tokens, keys and field numbers can rotate.
        // This also avoids making the whole NEIS service depend on Comcigan at bean creation.
        repeat(2) { attempt ->
            try {
                return fetchWeek(mondayDate)
            } catch (e: ComciganHttpException) {
                if (e.status != 403 || attempt != 0) throw e
            }
        }
        error("컴시간 요청 토큰 갱신 실패")
    }

    fun fetchDailyTimeTables(date: LocalDate): List<ParsedTimeTable> {
        if (date.dayOfWeek in setOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)) return emptyList()
        return fetchWeeklyTimeTables(date.with(DayOfWeek.MONDAY)).filter { it.date == date }
    }

    private fun fetchWeek(mondayDate: LocalDate): List<ParsedTimeTable> {
        val html = String(fetch(pageUri), Charset.forName("EUC-KR"))
        val protocol = ComciganProtocol(html, objectMapper)
        val endpoint = pageUri.resolve(protocol.route)
        fun request(parameter: String): JsonNode {
            val encoded = Base64.getEncoder().encodeToString(parameter.toByteArray(Charsets.UTF_8))
            val uri = URI.create("$endpoint?${encode(encoded)}")
            return protocol.decodeResponse(String(fetch(uri, protocol.token), Charsets.UTF_8))
        }
        val schools = request(protocol.searchPrefix + encode(comciganProperties.schoolName) + "_enc5")["학교검색"]
        check(schools != null && schools.isArray) { "컴시간 학교 검색 응답 오류" }
        val school = schools.firstOrNull {
            it[0].asInt() != 0 && protocol.decodeText(it[2].asText()) == comciganProperties.schoolName
        } ?: error("컴시간에서 '${comciganProperties.schoolName}' 학교를 찾을 수 없어요.")
        val schoolCode = protocol.decodeText(school[school[0].asInt() / protocol.schoolCodeDivisor + 2].asText()).toInt()
        val base = request(buildTimetableParameter(protocol.timetablePrefix, schoolCode, 1))
        val week = resolveWeek(base, mondayDate, protocol.weeksField, protocol::decodeText)
        val json = if (week == 1) base else request(buildTimetableParameter(protocol.timetablePrefix, schoolCode, week))
        val actualMonday = LocalDate.parse(protocol.decodeText(json[protocol.startDateField].asText()))
        check(actualMonday == mondayDate) { "컴시간 응답 주차 불일치: 요청 $mondayDate, 응답 $actualMonday" }
        return parseComciganTimeTables(json, protocol, mondayDate, comciganProperties)
    }

    private fun fetch(uri: URI, token: String? = null): ByteArray {
        val builder = HttpRequest.newBuilder(uri).timeout(Duration.ofSeconds(20))
            .header("User-Agent", "Mozilla/5.0").header("Referer", pageUri.toString())
        if (token != null) builder.header("X-Hour-Token", token)
        val response = httpClient.send(builder.GET().build(), HttpResponse.BodyHandlers.ofByteArray())
        if (response.statusCode() != 200) throw ComciganHttpException(response.statusCode())
        return response.body()
    }

    private fun encode(text: String) = URLEncoder.encode(text, Charsets.UTF_8).replace("+", "%20")
}

private class ComciganHttpException(val status: Int) : IllegalStateException("컴시간 HTTP 요청 실패: $status")

internal fun parseComciganTimeTables(
    json: JsonNode,
    protocol: ComciganProtocol,
    mondayDate: LocalDate,
    properties: ComciganProperties,
): List<ParsedTimeTable> {
    fun array(field: String): JsonNode = json[field].also {
        check(it != null && it.isArray) { "컴시간 시간표 자료 없음: $field" }
    }
    val subjects = array(protocol.subjectField)
    val teachers = array(protocol.teacherField)
    val daily = array(protocol.dailyField)
    val original = array(protocol.originalField)
    val divisor = json.path("분리").asInt(100)
    check(divisor > 0) { "컴시간 과목 분리 값 오류" }
    val explicitChanges = json.path("변경알림").asInt() == 1
    val limit = json.path("열람제한일").asText().takeIf { it.isNotBlank() }
        ?.let { runCatching { LocalDate.parse(it.take(10)) }.getOrNull() }?.takeIf { it.year >= 2014 }
    val result = mutableListOf<ParsedTimeTable>()
    for (grade in 1..minOf(properties.maxGrade, daily.size() - 1)) {
        for (room in 1..minOf(properties.maxRoom, daily[grade].size() - 1)) {
            val roomData = daily[grade][room]
            for (day in 1..minOf(5, roomData.size() - 1)) {
                val date = mondayDate.plusDays(day - 1L)
                if (limit != null && !date.isBefore(limit)) continue
                for (period in 1 until roomData[day].size()) {
                    val raw = roomData[day][period].asText()
                    val value = raw.removePrefix(">").toLongOrNull() ?: error("컴시간 교시 자료 형식 변경")
                    val teacherIndex = (value % 1000).toInt()
                    if (teacherIndex == 0) continue
                    val subjectIndex = ((value / 1000) % divisor).toInt()
                    check(teacherIndex in 1 until teachers.size() && subjectIndex in 1 until subjects.size()) { "컴시간 교사 또는 과목 인덱스 오류" }
                    val subject = protocol.decodeText(subjects[subjectIndex].asText())
                    val teacher = displayTeacherName(protocol.decodeText(teachers[teacherIndex].asText()))
                    check(subject.isNotBlank()) { "컴시간 과목명 누락" }
                    val originalValue = original.path(grade).path(room).path(day).path(period).asText("0").removePrefix(">").toLongOrNull() ?: 0L
                    val replaced = if (explicitChanges) raw.startsWith(">") else value != originalValue
                    result.add(ParsedTimeTable(date, grade, room, period, subject, teacher, replaced))
                }
            }
        }
    }
    return result
}

private fun displayTeacherName(name: String): String =
    if ((name.contains('*') && !name.startsWith('*') && !name.endsWith('*')) || (name.length == 2 && name[1] == '*')) name
    else name.replaceFirst("*", "")
