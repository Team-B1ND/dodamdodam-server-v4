package com.b1nd.dodamdodam.nightstudy.presentation.team

import com.b1nd.dodamdodam.core.common.data.InfinityScrollPageResponse
import com.b1nd.dodamdodam.core.common.data.Response
import com.b1nd.dodamdodam.core.security.annotation.authentication.UserAccess
import com.b1nd.dodamdodam.core.security.passport.enumerations.RoleType
import com.b1nd.dodamdodam.nightstudy.application.team.TeamUseCase
import com.b1nd.dodamdodam.nightstudy.application.team.data.request.CreateTeamRequest
import com.b1nd.dodamdodam.nightstudy.application.team.data.request.InviteTeamRequest
import com.b1nd.dodamdodam.nightstudy.application.team.data.request.UpdateTeamRequest
import com.b1nd.dodamdodam.nightstudy.application.team.data.response.GetMyTeamResponse
import com.b1nd.dodamdodam.nightstudy.application.team.data.response.GetTeamMembersResponse
import jakarta.validation.Valid
import org.springframework.data.domain.Pageable
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/teams")
class TeamController(
    private val teamUseCase: TeamUseCase
) {

    @UserAccess(roles = [RoleType.STUDENT])
    @PostMapping
    fun createTeam(@RequestBody @Valid request: CreateTeamRequest): Response<Any> =
        teamUseCase.createNightStudyTeam(request)

    @UserAccess(roles = [RoleType.STUDENT])
    @GetMapping
    fun getMyTeam(pageable: Pageable): Response<InfinityScrollPageResponse<GetMyTeamResponse>> =
        teamUseCase.getMyTeam(pageable)

    @UserAccess(roles = [RoleType.STUDENT])
    @GetMapping("/{publicId}")
    fun getTeamMembers(@PathVariable("publicId") publicId: UUID): Response<List<GetTeamMembersResponse>> =
        teamUseCase.getTeamMembers(publicId)

    @UserAccess(roles = [RoleType.STUDENT])
    @PatchMapping
    fun updateTeam(@RequestBody @Valid request: UpdateTeamRequest): Response<Any> =
        teamUseCase.updateTeam(request)

    @UserAccess(roles = [RoleType.STUDENT])
    @DeleteMapping("/{publicId}")
    fun deleteTeam(@PathVariable("publicId") publicId: UUID): Response<Any> =
        teamUseCase.deleteTeam(publicId)

    @UserAccess(roles = [RoleType.STUDENT])
    @PostMapping("/invite")
    fun inviteToTeam(@RequestBody @Valid request: InviteTeamRequest): Response<Any> =
        teamUseCase.inviteToTeam(request)

    @UserAccess(roles = [RoleType.STUDENT])
    @PatchMapping("/invite/accept/{publicId}")
    fun acceptInvitation(@PathVariable("publicId") publicId: UUID): Response<Any> =
        teamUseCase.acceptInvitation(publicId)

    @UserAccess(roles = [RoleType.STUDENT])
    @DeleteMapping("/invite/reject/{publicId}")
    fun rejectInvitation(@PathVariable("publicId") publicId: UUID): Response<Any> =
        teamUseCase.rejectInvitation(publicId)
}