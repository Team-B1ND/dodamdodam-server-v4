package com.b1nd.dodamdodam.weekendstudy.application.attendance

import com.b1nd.dodamdodam.core.common.data.InfinityScrollPageResponse
import com.b1nd.dodamdodam.core.common.data.Response
import com.b1nd.dodamdodam.core.security.passport.holder.PassportHolder
import com.b1nd.dodamdodam.core.security.passport.requireUserId
import com.b1nd.dodamdodam.weekendstudy.application.attendance.data.request.CheckWeekendStudyAttendanceRequest
import com.b1nd.dodamdodam.weekendstudy.application.attendance.data.response.WeekendStudyAttendanceStatusResponse
import com.b1nd.dodamdodam.weekendstudy.application.attendance.data.response.WeekendStudyAttendanceSummaryResponse
import com.b1nd.dodamdodam.weekendstudy.application.weekendstudy.data.toApplicantResponse
import com.b1nd.dodamdodam.weekendstudy.domain.attendance.enumeration.WeekendStudyAttendanceStatus
import com.b1nd.dodamdodam.weekendstudy.domain.attendance.service.WeekendStudyAttendanceService
import com.b1nd.dodamdodam.weekendstudy.domain.weekendstudy.service.WeekendStudyService
import com.b1nd.dodamdodam.weekendstudy.infrastructure.user.client.UserQueryClient
import kotlinx.coroutines.runBlocking
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.util.*

@Component
@Transactional(rollbackFor = [Exception::class])
class WeekendStudyAttendanceUseCase(
    private val weekendStudyService: WeekendStudyService,
    private val attendanceService: WeekendStudyAttendanceService,
    private val userQueryClient: UserQueryClient,
) {
    fun check(userId: UUID, request: CheckWeekendStudyAttendanceRequest): Response<Any> {
        val checkerId = PassportHolder.current().requireUserId()
        val weekendStudyPeriod = weekendStudyService.getPeriod(userId, request.date, request.period)
        attendanceService.check(weekendStudyPeriod, request.status, checkerId)
        return Response.ok("출석을 체크했어요.")
    }

    @Transactional(readOnly = true)
    fun getSummaries(startAt: LocalDate, endAt: LocalDate, pageable: Pageable): Response<InfinityScrollPageResponse<WeekendStudyAttendanceSummaryResponse>> {
        val countsByUser = attendanceService.countByUserAndStatus(startAt, endAt)
        val usersMap = fetchUsersMap(countsByUser.keys)

        val summaries = countsByUser.mapNotNull { (userId, counts) ->
            usersMap[userId.toString()]?.let { student ->
                WeekendStudyAttendanceSummaryResponse(
                    student = student,
                    attendance = counts[WeekendStudyAttendanceStatus.ATTENDANCE] ?: 0L,
                    late = counts[WeekendStudyAttendanceStatus.LATE] ?: 0L,
                    absent = counts[WeekendStudyAttendanceStatus.ABSENT] ?: 0L,
                )
            }
        }.sortedWith(compareBy(
            { it.student.student?.grade ?: Int.MAX_VALUE },
            { it.student.student?.room ?: Int.MAX_VALUE },
            { it.student.student?.number ?: Int.MAX_VALUE },
        ))

        val offset = pageable.offset.toInt()
        val pageSize = pageable.pageSize
        val sliced = summaries.drop(offset).take(pageSize)
        val hasNext = offset + pageSize < summaries.size

        return Response.ok(
            "주말 자습 출결 정보를 조회했어요.",
            InfinityScrollPageResponse(content = sliced, hasNext = hasNext)
        )
    }

    @Transactional(readOnly = true)
    fun getStatuses(date: LocalDate, period: Int?): Response<List<WeekendStudyAttendanceStatusResponse>> {
        val periods = attendanceService.getAllPeriodsWithAttendance(date, period)
        val usersMap = fetchUsersMap(periods.map { it.period.userId }.toSet())

        val statuses = periods.mapNotNull { (weekendStudyPeriod, attendance) ->
            usersMap[weekendStudyPeriod.userId.toString()]?.let { student ->
                WeekendStudyAttendanceStatusResponse(
                    student = student,
                    period = weekendStudyPeriod.period,
                    type = weekendStudyPeriod.weekendStudy.type,
                    status = attendance?.status,
                    checkedAt = attendance?.modifiedAt,
                )
            }
        }.sortedWith(compareBy(
            { it.student.student?.grade ?: Int.MAX_VALUE },
            { it.student.student?.room ?: Int.MAX_VALUE },
            { it.student.student?.number ?: Int.MAX_VALUE },
            { it.period },
        ))

        return Response.ok("주말 자습 출결 현황을 조회했어요.", statuses)
    }

    fun cancel(userId: UUID, date: LocalDate, period: Int): Response<Any> {
        val weekendStudyPeriod = weekendStudyService.getPeriod(userId, date, period)
        attendanceService.cancel(weekendStudyPeriod)
        return Response.ok("출결 체크를 취소했어요.")
    }

    private fun fetchUsersMap(userIds: Collection<UUID>) =
        if (userIds.isEmpty()) emptyMap()
        else runBlocking { userQueryClient.getUsers(userIds.map { it.toString() }) }.usersList
            .associate { it.publicId to it.toApplicantResponse() }
}
