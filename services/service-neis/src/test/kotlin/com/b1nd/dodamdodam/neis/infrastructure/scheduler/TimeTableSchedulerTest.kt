package com.b1nd.dodamdodam.neis.infrastructure.scheduler

import com.b1nd.dodamdodam.neis.application.timetable.TimeTableUseCase
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId

class TimeTableSchedulerTest {
    @Test
    fun `시작 및 일일 갱신은 이번 주 전체를 복구하고 외부 실패를 전파하지 않는다`() {
        val useCase = mock(TimeTableUseCase::class.java)
        val monday = LocalDate.now(ZoneId.of("Asia/Seoul")).with(DayOfWeek.MONDAY)
        `when`(useCase.syncWeeklyTimeTables(monday)).thenThrow(IllegalStateException("upstream unavailable"))
        assertDoesNotThrow { TimeTableScheduler(useCase).fetchTodayTimeTables() }
        verify(useCase).syncWeeklyTimeTables(monday)
    }
}
