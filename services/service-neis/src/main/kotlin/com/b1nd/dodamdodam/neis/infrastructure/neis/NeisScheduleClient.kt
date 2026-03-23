package com.b1nd.dodamdodam.neis.infrastructure.neis

import com.b1nd.dodamdodam.neis.domain.schedule.enums.ScheduleTarget
import com.b1nd.dodamdodam.neis.infrastructure.config.NeisProperties
import com.b1nd.dodamdodam.neis.infrastructure.neis.data.NeisScheduleApiResponse
import com.b1nd.dodamdodam.neis.infrastructure.neis.data.NeisScheduleRow
import com.b1nd.dodamdodam.neis.infrastructure.neis.data.ParsedSchedule
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@Component
class NeisScheduleClient(
    private val restTemplate: RestTemplate,
    private val neisProperties: NeisProperties,
) {
    private val log = LoggerFactory.getLogger(javaClass)
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd")

    fun fetchMonthlySchedules(yearMonth: YearMonth): List<ParsedSchedule> {
        val startDate = yearMonth.atDay(1).format(dateFormatter)
        val endDate = yearMonth.atEndOfMonth().format(dateFormatter)

        val uri = UriComponentsBuilder
            .fromHttpUrl("https://open.neis.go.kr/hub/SchoolSchedule")
            .queryParam("KEY", neisProperties.apiKey)
            .queryParam("Type", "json")
            .queryParam("pIndex", 1)
            .queryParam("pSize", 100)
            .queryParam("ATPT_OFCDC_SC_CODE", neisProperties.eduOfficeCode)
            .queryParam("SD_SCHUL_CODE", neisProperties.schoolCode)
            .queryParam("AA_FROM_YMD", startDate)
            .queryParam("AA_TO_YMD", endDate)
            .build()
            .toUri()

        val response = restTemplate.getForObject(uri, NeisScheduleApiResponse::class.java)
        val rows = response?.schoolSchedule
            ?.flatMap { it.row ?: emptyList() }
            ?: emptyList()
        return rows.mapNotNull { parseRow(it) }
    }

    private fun parseRow(row: NeisScheduleRow): ParsedSchedule? {
        val date = runCatching {
            LocalDate.parse(row.scheduleDate, dateFormatter)
        }.getOrElse {
            log.error("학사일정 날짜 파싱 실패: {}", row.scheduleDate, it)
            return null
        }

        val targets = mutableListOf<ScheduleTarget>()
        if (row.oneGradeEventYn == "Y") targets.add(ScheduleTarget.GRADE_1)
        if (row.twGradeEventYn == "Y") targets.add(ScheduleTarget.GRADE_2)
        if (row.threeGradeEventYn == "Y") targets.add(ScheduleTarget.GRADE_3)

        if (targets.isEmpty()) return null

        return ParsedSchedule(
            date = date,
            title = row.eventName.trim(),
            targets = targets,
        )
    }
}
