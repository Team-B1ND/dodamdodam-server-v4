package com.b1nd.dodamdodam.neis.application.timetable

import com.b1nd.dodamdodam.core.common.data.Response
import com.b1nd.dodamdodam.core.security.passport.holder.PassportHolder
import com.b1nd.dodamdodam.core.security.passport.requireUserId
import com.b1nd.dodamdodam.neis.application.timetable.data.response.TimeTableResponse
import com.b1nd.dodamdodam.neis.application.timetable.data.toResponse
import com.b1nd.dodamdodam.neis.domain.timetable.service.TimeTableService
import com.b1nd.dodamdodam.neis.infrastructure.comcigan.ComciganClient
import com.b1nd.dodamdodam.neis.infrastructure.user.UserQueryClient
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.DayOfWeek
import java.time.LocalDate

@Component
@Transactional(rollbackFor = [Exception::class])
class TimeTableUseCase(
    private val timeTableService: TimeTableService,
    private val comciganClient: ComciganClient,
    private val userQueryClient: UserQueryClient,
) {
    @Transactional(readOnly = true)
    fun getMyWeeklyTimeTables(): Response<List<TimeTableResponse>> {
        val userId = PassportHolder.current().requireUserId()
        val user = runBlocking { userQueryClient.getUser(userId.toString()) }
        val student = user.student

        val today = LocalDate.now()
        val monday = today.with(DayOfWeek.MONDAY)
        val friday = today.with(DayOfWeek.FRIDAY)

        val timeTables = timeTableService.getWeeklyTimeTablesByRoom(monday, friday, student.grade, student.room)
        return Response.ok("내 시간표를 조회했어요.", timeTables.map { it.toResponse() })
    }

    @Transactional(readOnly = true)
    fun getTimeTablesByDate(date: LocalDate, grade: Int, room: Int): Response<List<TimeTableResponse>> {
        val timeTables = timeTableService.getTimeTablesByDateAndRoom(date, grade, room)
        return Response.ok("시간표를 조회했어요.", timeTables.map { it.toResponse() })
    }

    @Transactional(readOnly = true)
    fun getAllTimeTablesByDate(date: LocalDate): Response<List<TimeTableResponse>> {
        val timeTables = timeTableService.getTimeTablesByDate(date)
        return Response.ok("시간표를 조회했어요.", timeTables.map { it.toResponse() })
    }

    fun syncWeeklyTimeTables(mondayDate: LocalDate): Response<Any> {
        val timeTables = comciganClient.fetchWeeklyTimeTables(mondayDate)
        timeTables.forEach {
            timeTableService.saveOrUpdate(it.date, it.grade, it.room, it.period, it.subject, it.teacher)
        }
        return Response.ok("${mondayDate} 주간 시간표 동기화가 완료되었어요.")
    }

    fun syncDailyTimeTables(date: LocalDate): Response<Any> {
        val timeTables = comciganClient.fetchDailyTimeTables(date)
        timeTables.forEach {
            timeTableService.saveOrUpdate(it.date, it.grade, it.room, it.period, it.subject, it.teacher)
        }
        return Response.ok("${date} 시간표 동기화가 완료되었어요.")
    }
}
