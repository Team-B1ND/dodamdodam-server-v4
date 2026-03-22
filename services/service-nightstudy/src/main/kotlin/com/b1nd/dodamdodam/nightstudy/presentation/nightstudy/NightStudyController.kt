package com.b1nd.dodamdodam.nightstudy.presentation.nightstudy

import com.b1nd.dodamdodam.core.common.data.Response
import com.b1nd.dodamdodam.core.security.annotation.authentication.UserAccess
import com.b1nd.dodamdodam.core.security.passport.enumerations.RoleType
import com.b1nd.dodamdodam.nightstudy.application.nightstudy.NightStudyUseCase
import com.b1nd.dodamdodam.nightstudy.application.nightstudy.data.request.ApplyPersonalNightStudyRequest
import com.b1nd.dodamdodam.nightstudy.application.nightstudy.data.request.ApplyProjectNightStudyRequest
import com.b1nd.dodamdodam.nightstudy.application.nightstudy.data.response.PersonalNightStudyResponse
import com.b1nd.dodamdodam.nightstudy.application.nightstudy.data.response.ProjectNightStudyResponse
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.enumeration.NightStudyStatusType
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/night-study")
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
    fun cancelNightStudy(@PathVariable id: Long): Response<Any> =
        nightStudyUseCase.cancelNightStudy(id)
}
