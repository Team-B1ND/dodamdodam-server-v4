package com.b1nd.dodamdodam.neis.infrastructure.scheduler

import com.b1nd.dodamdodam.neis.domain.timetable.service.TimeTableService
import com.b1nd.dodamdodam.neis.infrastructure.comcigan.ComciganClient
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.DayOfWeek
import java.time.LocalDate

@Component
class TimeTableScheduler(
    private val comciganClient: ComciganClient,
    private val timeTableService: TimeTableService,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Scheduled(cron = "0 0 18 * * FRI")
    @Transactional
    fun fetchNextWeekTimeTables() {
        val nextMonday = LocalDate.now().with(DayOfWeek.MONDAY).plusWeeks(1)
        syncWeekly(nextMonday)
    }

    @Scheduled(cron = "0 40 8 * * MON-FRI")
    @Transactional
    fun fetchTodayTimeTables() {
        syncDaily(LocalDate.now())
    }

    private fun syncWeekly(mondayDate: LocalDate) {
        try {
            val timeTables = comciganClient.fetchWeeklyTimeTables(mondayDate)
            timeTables.forEach {
                timeTableService.saveOrUpdate(it.date, it.grade, it.room, it.period, it.subject, it.teacher)
            }
        } catch (e: Exception) {
            log.error("다음 주 시간표 동기화 실패: {}", mondayDate, e)
        }
    }

    private fun syncDaily(date: LocalDate) {
        try {
            val timeTables = comciganClient.fetchDailyTimeTables(date)
            timeTables.forEach {
                timeTableService.saveOrUpdate(it.date, it.grade, it.room, it.period, it.subject, it.teacher)
            }
        } catch (e: Exception) {
            log.error("당일 시간표 동기화 실패: {}", date, e)
        }
    }
}
