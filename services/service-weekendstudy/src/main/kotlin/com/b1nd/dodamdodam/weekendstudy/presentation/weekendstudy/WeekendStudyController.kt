package com.b1nd.dodamdodam.weekendstudy.presentation.weekendstudy

import com.b1nd.dodamdodam.core.common.data.InfinityScrollPageResponse
import com.b1nd.dodamdodam.core.common.data.Response
import com.b1nd.dodamdodam.core.security.annotation.authentication.UserAccess
import com.b1nd.dodamdodam.core.security.passport.enumerations.RoleType
import com.b1nd.dodamdodam.weekendstudy.application.weekendstudy.WeekendStudyUseCase
import com.b1nd.dodamdodam.weekendstudy.application.weekendstudy.data.request.AssignRoomRequest
import com.b1nd.dodamdodam.weekendstudy.application.weekendstudy.data.request.PersonalWeekendStudyApplyRequest
import com.b1nd.dodamdodam.weekendstudy.application.weekendstudy.data.request.ProjectWeekendStudyApplyRequest
import com.b1nd.dodamdodam.weekendstudy.application.weekendstudy.data.request.RejectWeekendStudyRequest
import com.b1nd.dodamdodam.weekendstudy.application.weekendstudy.data.response.PersonalWeekendStudyResponse
import com.b1nd.dodamdodam.weekendstudy.application.weekendstudy.data.response.ProjectWeekendStudyResponse
import com.b1nd.dodamdodam.weekendstudy.application.weekendstudy.data.response.WeekendStudyApplicationResponse
import com.b1nd.dodamdodam.weekendstudy.domain.weekendstudy.enumeration.WeekendStudyType
import jakarta.validation.Valid
import org.springframework.data.domain.Pageable
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate
import java.util.UUID

@RestController
class WeekendStudyController(
    private val weekendStudyUseCase: WeekendStudyUseCase,
) {
    @UserAccess(roles = [RoleType.STUDENT])
    @PostMapping("/personal")
    fun applyPersonal(@RequestBody request: PersonalWeekendStudyApplyRequest): Response<Any> =
        weekendStudyUseCase.applyPersonal(request)

    @UserAccess(roles = [RoleType.STUDENT])
    @PostMapping("/project")
    fun applyProject(@RequestBody @Valid request: ProjectWeekendStudyApplyRequest): Response<Any> =
        weekendStudyUseCase.applyProject(request)

    @UserAccess(roles = [RoleType.STUDENT])
    @GetMapping("/my/personal")
    fun getMyPersonal(): Response<List<PersonalWeekendStudyResponse>> =
        weekendStudyUseCase.getMyPersonal()

    @UserAccess(roles = [RoleType.STUDENT])
    @GetMapping("/my/project")
    fun getMyProject(): Response<List<ProjectWeekendStudyResponse>> =
        weekendStudyUseCase.getMyProject()

    @UserAccess(roles = [RoleType.STUDENT])
    @DeleteMapping("/{id}")
    fun cancel(@PathVariable("id") id: UUID): Response<Any> =
        weekendStudyUseCase.cancel(id)

    @UserAccess(roles = [RoleType.WEEKEND_MANAGER])
    @GetMapping("/applications")
    fun getApplications(
        @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") date: LocalDate,
        @RequestParam type: WeekendStudyType,
        pageable: Pageable,
    ): Response<InfinityScrollPageResponse<WeekendStudyApplicationResponse>> =
        weekendStudyUseCase.getApplications(date, type, pageable)

    @UserAccess(roles = [RoleType.WEEKEND_MANAGER])
    @PatchMapping("/{id}/allow")
    fun allow(@PathVariable("id") id: UUID): Response<Any> =
        weekendStudyUseCase.allow(id)

    @UserAccess(roles = [RoleType.WEEKEND_MANAGER])
    @PatchMapping("/{id}/reject")
    fun reject(@PathVariable("id") id: UUID, @RequestBody @Valid request: RejectWeekendStudyRequest): Response<Any> =
        weekendStudyUseCase.reject(id, request)

    @UserAccess(roles = [RoleType.WEEKEND_MANAGER])
    @PatchMapping("/{id}/pending")
    fun pending(@PathVariable("id") id: UUID): Response<Any> =
        weekendStudyUseCase.pending(id)

    @UserAccess(roles = [RoleType.WEEKEND_MANAGER])
    @PatchMapping("/{id}/room")
    fun assignRoom(@PathVariable("id") id: UUID, @RequestBody request: AssignRoomRequest): Response<Any> =
        weekendStudyUseCase.assignRoom(id, request)

    @UserAccess(roles = [RoleType.WEEKEND_MANAGER])
    @DeleteMapping("/{id}/room")
    fun unassignRoom(@PathVariable("id") id: UUID): Response<Any> =
        weekendStudyUseCase.unassignRoom(id)
}
