package com.b1nd.dodamdodam.neis.presentation.schedule

import com.b1nd.dodamdodam.core.security.annotation.authentication.UserAccess
import com.b1nd.dodamdodam.core.security.passport.enumerations.RoleType
import com.b1nd.dodamdodam.neis.application.schedule.ScheduleUseCase
import com.b1nd.dodamdodam.neis.application.schedule.data.request.CreateScheduleRequest
import com.b1nd.dodamdodam.neis.application.schedule.data.request.UpdateScheduleRequest
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/schedule")
class ScheduleController(
    private val scheduleUseCase: ScheduleUseCase,
) {
    @UserAccess(roles = [RoleType.ADMIN, RoleType.TEACHER])
    @PostMapping
    fun createSchedule(@RequestBody request: CreateScheduleRequest) =
        scheduleUseCase.createSchedule(request)

    @UserAccess(roles = [RoleType.ADMIN, RoleType.TEACHER])
    @PatchMapping
    fun updateSchedule(@RequestBody request: UpdateScheduleRequest) =
        scheduleUseCase.updateSchedule(request)

    @UserAccess(roles = [RoleType.ADMIN, RoleType.TEACHER])
    @DeleteMapping("/{publicId}")
    fun deleteSchedule(@PathVariable publicId: UUID) =
        scheduleUseCase.deleteSchedule(publicId)

    @UserAccess
    @GetMapping
    fun getSchedules(@RequestParam year: Int, @RequestParam month: Int) =
        scheduleUseCase.getSchedulesByMonth(year, month)

    @UserAccess(roles = [RoleType.ADMIN])
    @PostMapping("/sync")
    fun syncSchedules(@RequestParam year: Int) =
        scheduleUseCase.syncSchedules(year)
}
