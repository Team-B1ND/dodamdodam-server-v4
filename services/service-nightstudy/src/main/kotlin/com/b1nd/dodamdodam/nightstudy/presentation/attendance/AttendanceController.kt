package com.b1nd.dodamdodam.nightstudy.presentation.attendance

import com.b1nd.dodamdodam.core.common.data.Response
import com.b1nd.dodamdodam.core.security.annotation.authentication.UserAccess
import com.b1nd.dodamdodam.core.security.passport.enumerations.RoleType
import com.b1nd.dodamdodam.nightstudy.application.nightstudy.NightStudyUseCase
import com.b1nd.dodamdodam.nightstudy.application.nightstudy.data.request.UpdateNightStudyAttendanceRequest
import com.b1nd.dodamdodam.nightstudy.application.nightstudy.data.response.NightStudyAttendanceCountResponse
import com.b1nd.dodamdodam.nightstudy.application.nightstudy.data.response.NightStudyAttendanceResponse
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate
import java.util.UUID

@RestController
@RequestMapping("/attendance")
class AttendanceController(
    private val nightStudyUseCase: NightStudyUseCase,
) {

    @UserAccess(roles = [RoleType.DORMITORY_MANAGER])
    @GetMapping("/count")
    fun countAttendance(
        @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") date: LocalDate?,
        @RequestParam period: Int,
    ): Response<NightStudyAttendanceCountResponse> =
        nightStudyUseCase.countAttendance(date ?: LocalDate.now(), period)

    @UserAccess(roles = [RoleType.DORMITORY_MANAGER])
    @PatchMapping("/{userId}")
    fun updateAttendance(
        @PathVariable userId: UUID,
        @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") date: LocalDate?,
        @RequestParam period: Int,
        @RequestBody request: UpdateNightStudyAttendanceRequest
    ): Response<NightStudyAttendanceResponse> =
        nightStudyUseCase.updateAttendance(userId, date ?: LocalDate.now(), period, request)

    @UserAccess(roles = [RoleType.DORMITORY_MANAGER])
    @GetMapping("/{userId}")
    fun getAttendance(
        @PathVariable userId: UUID,
        @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") date: LocalDate?,
        @RequestParam period: Int,
    ): Response<NightStudyAttendanceResponse> =
        nightStudyUseCase.getAttendance(userId, date ?: LocalDate.now(), period)
}
