package com.b1nd.dodamdodam.neis.infrastructure.scheduler

import com.b1nd.dodamdodam.neis.application.timetable.TimeTableUseCase
import org.slf4j.LoggerFactory
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId

@Component
class TimeTableScheduler(
    private val timeTableUseCase: TimeTableUseCase,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Scheduled(cron = "0 0 18 * * FRI", zone = "Asia/Seoul")
    fun fetchNextWeekTimeTables() {
        syncWeekly(today().with(DayOfWeek.MONDAY).plusWeeks(1))
    }

    // Recover missing days after an outage/deployment, as well as today's changes.
    @EventListener(ApplicationReadyEvent::class)
    @Scheduled(cron = "0 40 8 * * MON-FRI", zone = "Asia/Seoul")
    fun fetchTodayTimeTables() {
        syncWeekly(today().with(DayOfWeek.MONDAY))
    }

    private fun syncWeekly(mondayDate: LocalDate) {
        try {
            // The use case owns the transaction, including commit; failures are caught here.
            timeTableUseCase.syncWeeklyTimeTables(mondayDate)
            log.info("주간 시간표 동기화 완료: {}", mondayDate)
        } catch (e: Exception) {
            log.error("주간 시간표 동기화 실패: {}", mondayDate, e)
        }
    }

    private fun today() = LocalDate.now(ZoneId.of("Asia/Seoul"))
}
