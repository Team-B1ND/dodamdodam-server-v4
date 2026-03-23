package com.b1nd.dodamdodam.nightstudy.presentation.nightstudy

import com.b1nd.dodamdodam.core.common.data.Response
import com.b1nd.dodamdodam.core.security.annotation.authentication.UserAccess
import com.b1nd.dodamdodam.core.security.passport.enumerations.RoleType
import com.b1nd.dodamdodam.nightstudy.application.nightstudy.NightStudyUseCase
import com.b1nd.dodamdodam.nightstudy.application.nightstudy.data.request.ApplyPersonalNightStudyRequest
import com.b1nd.dodamdodam.nightstudy.application.nightstudy.data.request.ApplyProjectNightStudyRequest
import com.b1nd.dodamdodam.nightstudy.application.nightstudy.data.request.RejectNightStudyRequest
import com.b1nd.dodamdodam.nightstudy.application.nightstudy.data.response.ApplicationResponse
import com.b1nd.dodamdodam.nightstudy.application.nightstudy.data.response.PersonalNightStudyResponse
import com.b1nd.dodamdodam.nightstudy.application.nightstudy.data.response.ProjectNightStudyResponse
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.enumeration.NightStudyStatusType
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.enumeration.NightStudyType
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
class NightStudyController(
    private val nightStudyUseCase: NightStudyUseCase,
) {
    @UserAccess(roles = [RoleType.STUDENT])
    @PostMapping("/personal")
    fun applyPersonalNightStudy(@RequestBody request: ApplyPersonalNightStudyRequest): Response<Any> =
        nightStudyUseCase.applyPersonalNightStudy(request)

    @UserAccess(roles = [RoleType.STUDENT])
    @PostMapping("/project")
    fun applyProjectNightStudy(@RequestBody request: ApplyProjectNightStudyRequest): Response<Any> =
        nightStudyUseCase.applyProjectNightStudy(request)

    @UserAccess(roles = [RoleType.STUDENT])
    @GetMapping("/my/personal")
    fun getMyPersonalNightStudy(@RequestParam status: NightStudyStatusType): Response<List<PersonalNightStudyResponse>> =
        nightStudyUseCase.getMyPersonalNightStudy(status)

    @UserAccess(roles = [RoleType.STUDENT])
    @GetMapping("/my/project")
    fun getMyProjectNightStudy(@RequestParam status: NightStudyStatusType): Response<List<ProjectNightStudyResponse>> =
        nightStudyUseCase.getMyProjectNightStudy(status)

    @UserAccess(roles = [RoleType.STUDENT])
    @DeleteMapping("/{id}")
    fun cancelNightStudy(@PathVariable id: UUID): Response<Any> =
        nightStudyUseCase.cancelNightStudy(id)

    @UserAccess(roles = [RoleType.DORMITORY_MANAGER])
    @GetMapping("/applications")
    fun findAllByType(@RequestParam type: NightStudyType): Response<List<ApplicationResponse>> =
        nightStudyUseCase.findAllByType(type)

    @UserAccess(roles = [RoleType.DORMITORY_MANAGER])
    @GetMapping("/applications/{id}")
    fun findById(@PathVariable id: UUID): Response<ApplicationResponse> =
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
}

