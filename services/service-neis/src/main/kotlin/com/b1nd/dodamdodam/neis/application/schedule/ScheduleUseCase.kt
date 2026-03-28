package com.b1nd.dodamdodam.neis.application.schedule

import com.b1nd.dodamdodam.core.common.data.Response
import com.b1nd.dodamdodam.neis.application.schedule.data.request.CreateScheduleRequest
import com.b1nd.dodamdodam.neis.application.schedule.data.request.UpdateScheduleRequest
import com.b1nd.dodamdodam.neis.application.schedule.data.response.ScheduleResponse
import com.b1nd.dodamdodam.neis.application.schedule.data.toResponse
import com.b1nd.dodamdodam.neis.domain.schedule.service.ScheduleService
import com.b1nd.dodamdodam.neis.infrastructure.neis.NeisScheduleClient
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.YearMonth
import java.util.UUID

@Component
@Transactional(rollbackFor = [Exception::class])
class ScheduleUseCase(
    private val scheduleService: ScheduleService,
    private val neisScheduleClient: NeisScheduleClient,
) {
    fun createSchedule(request: CreateScheduleRequest): Response<ScheduleResponse> {
        val schedule = scheduleService.create(request.title, request.startAt, request.endAt, request.targets)
        return Response.created("학사일정이 등록되었어요.", schedule.toResponse(request.targets))
    }

    fun updateSchedule(request: UpdateScheduleRequest): Response<ScheduleResponse> {
        val schedule = scheduleService.update(request.publicId, request.title, request.startAt, request.endAt, request.targets)
        return Response.ok("학사일정이 수정되었어요.", schedule.toResponse(request.targets))
    }

    fun deleteSchedule(publicId: UUID): Response<Any> {
        scheduleService.delete(publicId)
        return Response.ok("학사일정이 삭제되었어요.")
    }

    @Transactional(readOnly = true)
    fun getSchedulesByMonth(year: Int, month: Int): Response<List<ScheduleResponse>> {
        val yearMonth = YearMonth.of(year, month)
        val startOfMonth = yearMonth.atDay(1)
        val endOfMonth = yearMonth.atEndOfMonth()

        val schedules = scheduleService.getSchedulesByMonth(startOfMonth, endOfMonth)
        val targetMap = scheduleService.getTargetsBySchedules(schedules)
        return Response.ok("학사일정을 조회했어요.", schedules.map {
            it.toResponse(targetMap[it.id] ?: emptyList())
        })
    }

    fun syncSchedules(year: Int): Response<Any> {
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

        return Response.ok("${year}학년도 학사일정 동기화가 완료되었어요.")
    }
}
