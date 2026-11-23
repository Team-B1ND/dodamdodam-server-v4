package com.b1nd.dodamdodam.weekendstudy.presentation.deadline

import com.b1nd.dodamdodam.core.security.annotation.authentication.UserAccess
import com.b1nd.dodamdodam.core.security.passport.enumerations.RoleType
import com.b1nd.dodamdodam.weekendstudy.application.deadline.WeekendStudyDeadlineUseCase
import com.b1nd.dodamdodam.weekendstudy.application.deadline.data.request.UpdateDeadlineRequest
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/deadline")
class WeekendStudyDeadlineController(
    private val deadlineUseCase: WeekendStudyDeadlineUseCase,
) {
    @UserAccess
    @GetMapping
    fun getDeadline() =
        deadlineUseCase.getDeadline()

    @UserAccess(roles = [RoleType.WEEKEND_MANAGER])
    @PatchMapping
    fun updateDeadline(@RequestBody request: UpdateDeadlineRequest) =
        deadlineUseCase.updateDeadline(request)
}
