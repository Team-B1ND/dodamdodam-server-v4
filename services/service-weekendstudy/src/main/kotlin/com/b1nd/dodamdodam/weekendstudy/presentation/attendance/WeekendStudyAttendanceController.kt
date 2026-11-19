package com.b1nd.dodamdodam.weekendstudy.presentation.attendance

import com.b1nd.dodamdodam.core.common.data.InfinityScrollPageResponse
import com.b1nd.dodamdodam.core.common.data.Response
import com.b1nd.dodamdodam.core.security.annotation.authentication.UserAccess
import com.b1nd.dodamdodam.core.security.passport.enumerations.RoleType
import com.b1nd.dodamdodam.weekendstudy.application.attendance.WeekendStudyAttendanceUseCase
import com.b1nd.dodamdodam.weekendstudy.application.attendance.data.request.CheckWeekendStudyAttendanceRequest
import com.b1nd.dodamdodam.weekendstudy.application.attendance.data.request.GetWeekendStudyAttendanceSummaryRequest
import com.b1nd.dodamdodam.weekendstudy.application.attendance.data.response.WeekendStudyAttendanceSummaryResponse
import jakarta.validation.Valid
import org.springframework.data.domain.Pageable
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

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
        @RequestBody request: GetWeekendStudyAttendanceSummaryRequest,
        pageable: Pageable,
    ): Response<InfinityScrollPageResponse<WeekendStudyAttendanceSummaryResponse>> =
        attendanceUseCase.getSummaries(request, pageable)
}
