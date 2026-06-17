package com.b1nd.dodamdodam.nightstudy.presentation.nightstudy

import com.b1nd.dodamdodam.core.common.data.Response
import com.b1nd.dodamdodam.core.security.annotation.authentication.UserAccess
import com.b1nd.dodamdodam.core.security.passport.enumerations.RoleType
import com.b1nd.dodamdodam.nightstudy.application.nightstudy.NightStudyUseCase
import com.b1nd.dodamdodam.nightstudy.application.nightstudy.data.request.AssignRoomRequest
import com.b1nd.dodamdodam.nightstudy.application.nightstudy.data.request.PersonalNightStudyApplyRequest
import com.b1nd.dodamdodam.nightstudy.application.nightstudy.data.request.ProjectNightStudyApplyRequest
import com.b1nd.dodamdodam.nightstudy.application.nightstudy.data.request.RejectNightStudyRequest
import com.b1nd.dodamdodam.nightstudy.application.nightstudy.data.request.UpdateNightStudyAttendanceRequest
import com.b1nd.dodamdodam.nightstudy.application.nightstudy.data.response.NightStudyApplicationResponse
import com.b1nd.dodamdodam.nightstudy.application.nightstudy.data.response.NightStudyApprovedCountResponse
import com.b1nd.dodamdodam.nightstudy.application.nightstudy.data.response.NightStudyAttendanceCountResponse
import com.b1nd.dodamdodam.nightstudy.application.nightstudy.data.response.NightStudyAttendanceResponse
import com.b1nd.dodamdodam.nightstudy.application.nightstudy.data.response.PersonalNightStudyResponse
import com.b1nd.dodamdodam.nightstudy.application.nightstudy.data.response.ProjectNightStudyResponse
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.enumeration.NightStudyStatusType
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.enumeration.NightStudyType
import com.b1nd.dodamdodam.core.common.data.InfinityScrollPageResponse
import org.springframework.data.domain.Pageable
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.web.bind.annotation.*
import java.time.LocalDate
import java.util.UUID

@RestController
class NightStudyController(
    private val nightStudyUseCase: NightStudyUseCase,
) {
    @UserAccess(roles = [RoleType.STUDENT])
    @PostMapping("/personal")
    fun applyPersonalNightStudy(@RequestBody request: PersonalNightStudyApplyRequest): Response<Any> =
        nightStudyUseCase.applyPersonalNightStudy(request)

    @UserAccess(roles = [RoleType.STUDENT])
    @PostMapping("/project")
    fun applyProjectNightStudy(@RequestBody request: ProjectNightStudyApplyRequest): Response<Any> =
        nightStudyUseCase.applyProjectNightStudy(request)

    @UserAccess(roles = [RoleType.STUDENT])
    @GetMapping("/my/personal")
    fun getMyPersonalNightStudy(): Response<List<PersonalNightStudyResponse>> =
        nightStudyUseCase.getMyPersonalNightStudy()

    @UserAccess(roles = [RoleType.STUDENT])
    @GetMapping("/my/project")
    fun getMyProjectNightStudy(): Response<List<ProjectNightStudyResponse>> =
        nightStudyUseCase.getMyProjectNightStudy()

    @UserAccess(roles = [RoleType.STUDENT])
    @DeleteMapping("/{id}")
    fun cancelNightStudy(@PathVariable id: UUID): Response<Any> =
        nightStudyUseCase.cancelNightStudy(id)

    @UserAccess(roles = [RoleType.DORMITORY_MANAGER])
    @GetMapping("/applications")
    fun findAllByType(@RequestParam type: NightStudyType, @RequestParam(required = false) keyword: String?, @RequestParam(required = false) status: NightStudyStatusType?, pageable: Pageable): Response<InfinityScrollPageResponse<NightStudyApplicationResponse>> =
        nightStudyUseCase.searchAllByType(type, keyword, status, pageable)

    @UserAccess(roles = [RoleType.DORMITORY_MANAGER])
    @GetMapping("/applications/count")
    fun countApproved(): Response<NightStudyApprovedCountResponse> =
        nightStudyUseCase.countApproved()

    @UserAccess(roles = [RoleType.DORMITORY_MANAGER])
    @GetMapping("/applications/{id}")
    fun findById(@PathVariable id: UUID): Response<NightStudyApplicationResponse> =
        nightStudyUseCase.findById(id)

    @UserAccess(roles = [RoleType.DORMITORY_MANAGER])
    @PatchMapping("/applications/{id}/allow")
    fun allow(@PathVariable id: UUID): Response<Any> =
        nightStudyUseCase.allow(id)

    @UserAccess(roles = [RoleType.DORMITORY_MANAGER])
    @PatchMapping("/applications/{id}/reject")
    fun reject(@PathVariable id: UUID, @RequestBody request: RejectNightStudyRequest): Response<Any> =
        nightStudyUseCase.reject(id, request.rejectionReason)

    @UserAccess(roles = [RoleType.DORMITORY_MANAGER])
    @PatchMapping("/applications/{id}/pending")
    fun pending(@PathVariable id: UUID): Response<Any> =
        nightStudyUseCase.pending(id)

    @UserAccess(roles = [RoleType.DORMITORY_MANAGER])
    @PatchMapping("/applications/{id}/room")
    fun assignRoom(@PathVariable id: UUID, @RequestBody request: AssignRoomRequest): Response<Any> =
        nightStudyUseCase.assignRoom(id, request.roomId)

    @UserAccess(roles = [RoleType.DORMITORY_MANAGER])
    @DeleteMapping("/applications/{id}/room")
    fun unassignRoom(@PathVariable id: UUID): Response<Any> =
        nightStudyUseCase.unassignRoom(id)

    @UserAccess(roles = [RoleType.DORMITORY_MANAGER])
    @GetMapping("/attendance/count")
    fun countAttendance(
        @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") date: LocalDate?,
        @RequestParam period: Int,
    ): Response<NightStudyAttendanceCountResponse> =
        nightStudyUseCase.countAttendance(date ?: LocalDate.now(), period)

    @UserAccess(roles = [RoleType.DORMITORY_MANAGER])
    @PatchMapping("/attendance/{userId}")
    fun updateAttendance(
        @PathVariable userId: UUID,
        @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") date: LocalDate?,
        @RequestParam period: Int,
        @RequestBody request: UpdateNightStudyAttendanceRequest
    ): Response<NightStudyAttendanceResponse> =
        nightStudyUseCase.updateAttendance(userId, date ?: LocalDate.now(), period, request)

    @UserAccess(roles = [RoleType.DORMITORY_MANAGER])
    @GetMapping("/attendance/{userId}")
    fun getAttendance(
        @PathVariable userId: UUID,
        @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") date: LocalDate?,
        @RequestParam period: Int,
    ): Response<NightStudyAttendanceResponse> =
        nightStudyUseCase.getAttendance(userId, date ?: LocalDate.now(), period)
}
