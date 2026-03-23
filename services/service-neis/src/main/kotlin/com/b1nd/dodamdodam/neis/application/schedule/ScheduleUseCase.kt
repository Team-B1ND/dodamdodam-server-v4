package com.b1nd.dodamdodam.neis.application.schedule

import com.b1nd.dodamdodam.core.common.data.Response
import com.b1nd.dodamdodam.neis.application.schedule.data.request.CreateScheduleRequest
import com.b1nd.dodamdodam.neis.application.schedule.data.response.PageResponse
import com.b1nd.dodamdodam.neis.application.schedule.data.response.ScheduleResponse
import com.b1nd.dodamdodam.neis.application.schedule.data.toResponse
import com.b1nd.dodamdodam.neis.domain.schedule.enums.ScheduleType
import com.b1nd.dodamdodam.neis.domain.schedule.service.ScheduleService
import com.b1nd.dodamdodam.neis.infrastructure.neis.NeisScheduleClient
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.YearMonth
import java.util.UUID

@Component
@Transactional(rollbackFor = [Exception::class])
class ScheduleUseCase(
    private val scheduleService: ScheduleService,
    private val neisScheduleClient: NeisScheduleClient,
) {
    fun createSchedule(request: CreateScheduleRequest): Response<ScheduleResponse> {
        val schedule = scheduleService.create(request.title, request.startDate, request.endDate, ScheduleType.ADMIN, request.targets)
        return Response.created("학사일정이 등록되었어요.", schedule.toResponse())
    }

    fun deleteSchedule(publicId: UUID): Response<Any> {
        scheduleService.delete(publicId)
        return Response.ok("학사일정이 삭제되었어요.")
    }

    @Transactional(readOnly = true)
    fun getSchedulesByMonth(year: Int, month: Int, pageable: Pageable): Response<PageResponse<ScheduleResponse>> {
        val yearMonth = YearMonth.of(year, month)
        val startOfMonth = yearMonth.atDay(1)
        val endOfMonth = yearMonth.atEndOfMonth()

        val schedules = scheduleService.getSchedulesByMonth(startOfMonth, endOfMonth, pageable)
        return Response.ok("학사일정을 조회했어요.", PageResponse.of(schedules.map { it.toResponse() }))
    }

    fun syncSchedules(yearMonth: YearMonth): Response<Any> {
        val startOfMonth = yearMonth.atDay(1)
        val endOfMonth = yearMonth.atEndOfMonth()

        scheduleService.deleteAllNeisSchedulesByMonth(startOfMonth, endOfMonth)

        val schedules = neisScheduleClient.fetchMonthlySchedules(yearMonth)
        schedules.forEach { parsed ->
            scheduleService.create(parsed.title, parsed.date, parsed.date, ScheduleType.NEIS, parsed.targets)
        }
        return Response.ok("${yearMonth} 학사일정 동기화가 완료되었어요.")
    }
}
