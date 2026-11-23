package com.b1nd.dodamdodam.weekendstudy.presentation.attendance

import com.b1nd.dodamdodam.core.common.data.InfinityScrollPageResponse
import com.b1nd.dodamdodam.core.common.data.Response
import com.b1nd.dodamdodam.core.security.annotation.authentication.UserAccess
import com.b1nd.dodamdodam.core.security.passport.enumerations.RoleType
import com.b1nd.dodamdodam.weekendstudy.application.attendance.WeekendStudyAttendanceUseCase
import com.b1nd.dodamdodam.weekendstudy.application.attendance.data.request.CheckWeekendStudyAttendanceRequest
import com.b1nd.dodamdodam.weekendstudy.application.attendance.data.response.WeekendStudyAttendanceSummaryResponse
import org.springframework.data.domain.Pageable
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.web.bind.annotation.*
import java.time.LocalDate
import java.util.*

@RestController
@RequestMapping("/attendance")
class WeekendStudyAttendanceController(
    private val attendanceUseCase: WeekendStudyAttendanceUseCase,
) {
    @UserAccess(roles = [RoleType.WEEKEND_MANAGER])
    @PatchMapping("/{userId}")
    fun check(
        @PathVariable userId: UUID,
        @RequestBody request: CheckWeekendStudyAttendanceRequest,
    ): Response<Any> =
        attendanceUseCase.check(userId, request)

    @UserAccess(roles = [RoleType.WEEKEND_MANAGER])
    @GetMapping
    fun getSummaries(
        @DateTimeFormat(pattern = "yyyy-MM-dd")
        @RequestParam startAt: LocalDate,
        @DateTimeFormat(pattern = "yyyy-MM-dd")
        @RequestParam endAt: LocalDate,
        pageable: Pageable,
    ): Response<InfinityScrollPageResponse<WeekendStudyAttendanceSummaryResponse>> =
        attendanceUseCase.getSummaries(startAt, endAt, pageable)
}
