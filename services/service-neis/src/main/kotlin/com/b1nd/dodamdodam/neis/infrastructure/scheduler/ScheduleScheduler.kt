package com.b1nd.dodamdodam.neis.infrastructure.scheduler

import com.b1nd.dodamdodam.neis.domain.schedule.service.ScheduleService
import com.b1nd.dodamdodam.neis.infrastructure.neis.NeisScheduleClient
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.YearMonth

@Component
class ScheduleScheduler(
    private val neisScheduleClient: NeisScheduleClient,
    private val scheduleService: ScheduleService,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Scheduled(cron = "0 0 3 2 3 *")
    @Transactional
    fun fetchYearlySchedules() {
        val year = LocalDate.now().year
        syncYearly(year)
    }

    private fun syncYearly(year: Int) {
        try {
            val startDate = LocalDate.of(year, 3, 1)
            val endDate = YearMonth.of(year + 1, 2).atEndOfMonth()

            scheduleService.deleteAllByStartAtBetween(startDate, endDate)

            for (month in 3..12) {
                val schedules = neisScheduleClient.fetchMonthlySchedules(YearMonth.of(year, month))
                schedules.forEach { scheduleService.create(it.title, it.date, it.date, it.targets) }
            }
            for (month in 1..2) {
                val schedules = neisScheduleClient.fetchMonthlySchedules(YearMonth.of(year + 1, month))
                schedules.forEach { scheduleService.create(it.title, it.date, it.date, it.targets) }
            }
        } catch (e: Exception) {
            log.error("학사일정 동기화 실패: {}", year, e)
        }
    }
}
