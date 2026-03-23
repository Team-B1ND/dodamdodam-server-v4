package com.b1nd.dodamdodam.neis.infrastructure.scheduler

import com.b1nd.dodamdodam.neis.domain.schedule.enums.ScheduleType
import com.b1nd.dodamdodam.neis.domain.schedule.service.ScheduleService
import com.b1nd.dodamdodam.neis.infrastructure.neis.NeisScheduleClient
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.YearMonth

@Component
class ScheduleScheduler(
    private val neisScheduleClient: NeisScheduleClient,
    private val scheduleService: ScheduleService,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Scheduled(cron = "0 0 3 L-2 * *")
    @Transactional
    fun fetchNextMonthSchedules() {
        val nextMonth = YearMonth.now().plusMonths(1)
        syncSchedules(nextMonth)
    }

    @Scheduled(cron = "0 0 4 1 * *")
    @Transactional
    fun fetchCurrentMonthSchedules() {
        syncSchedules(YearMonth.now())
    }

    private fun syncSchedules(yearMonth: YearMonth) {
        try {
            val startOfMonth = yearMonth.atDay(1)
            val endOfMonth = yearMonth.atEndOfMonth()

            scheduleService.deleteAllNeisSchedulesByMonth(startOfMonth, endOfMonth)

            val schedules = neisScheduleClient.fetchMonthlySchedules(yearMonth)
            schedules.forEach { parsed ->
                scheduleService.create(parsed.title, parsed.date, parsed.date, ScheduleType.NEIS, parsed.targets)
            }
        } catch (e: Exception) {
            log.error("학사일정 동기화 실패: {}", yearMonth, e)
        }
    }
}
