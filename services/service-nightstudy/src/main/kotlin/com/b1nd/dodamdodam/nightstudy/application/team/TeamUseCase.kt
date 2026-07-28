package com.b1nd.dodamdodam.nightstudy.application.team

import com.b1nd.dodamdodam.core.common.data.InfinityScrollPageResponse
import com.b1nd.dodamdodam.core.common.data.Response
import com.b1nd.dodamdodam.core.security.passport.holder.PassportHolder
import com.b1nd.dodamdodam.core.security.passport.requireUserId
import com.b1nd.dodamdodam.grpc.user.UserResponse
import com.b1nd.dodamdodam.nightstudy.application.team.data.request.CreateTeamRequest
import com.b1nd.dodamdodam.nightstudy.application.team.data.request.InviteTeamRequest
import com.b1nd.dodamdodam.nightstudy.application.team.data.request.UpdateTeamRequest
import com.b1nd.dodamdodam.nightstudy.application.team.data.response.GetMyTeamResponse
import com.b1nd.dodamdodam.nightstudy.application.team.data.response.GetTeamMembersResponse
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.exception.NightStudyTeamInviteeNotFoundException
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.service.NightStudyTeamService
import com.b1nd.dodamdodam.nightstudy.infrastructure.user.client.UserQueryClient
import kotlinx.coroutines.runBlocking
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Component
@Transactional(rollbackFor = [Exception::class])
class TeamUseCase(
    private val nightStudyTeamService: NightStudyTeamService,
    private val userQueryClient: UserQueryClient,
) {

    fun createNightStudyTeam(request: CreateTeamRequest): Response<Any> {
        val userId = PassportHolder.current().requireUserId()
        nightStudyTeamService.createTeam(request.toEntity(), userId)
        return Response.created("팀을 생성했어요.")
    }

    @Transactional(readOnly = true)
    fun getMyTeam(pageable: Pageable): Response<InfinityScrollPageResponse<GetMyTeamResponse>> {
        val userId = PassportHolder.current().requireUserId()
        val teams = nightStudyTeamService.findByUserId(userId, pageable)
        return Response.ok(
            "전체 팀을 조회했어요.",
            InfinityScrollPageResponse(
                content = GetMyTeamResponse.fromList(teams.content),
                hasNext = teams.hasNext(),
            )
        )
    }

    @Transactional(readOnly = true)
    fun getTeamMembers(id: UUID?): Response<List<GetTeamMembersResponse>> {
        PassportHolder.current().requireUserId()
        val members = nightStudyTeamService.findByTeamMember(id)
        val userIds = members.map { it.user.toString() }.distinct()
        val usersMap = fetchUsersMap(userIds)

        return Response.ok(
            "팀 멤버를 조회했어요.",
            GetTeamMembersResponse.fromList(members, usersMap),
        )
    }

    fun updateTeam(request: UpdateTeamRequest): Response<Any> {
        val userId = PassportHolder.current().requireUserId()
        val team = nightStudyTeamService.findByLeaderId(request.publicId, userId)
        nightStudyTeamService.updateNightStudyTeam(team, request.toEntity())
        return Response.ok("팀을 수정했어요.")
    }

    fun deleteTeam(id: UUID): Response<Any> {
        val userId = PassportHolder.current().requireUserId()
        val team = nightStudyTeamService.findByLeaderId(id, userId)
        nightStudyTeamService.deleteNightStudyTeam(team)
        return Response.ok("팀을 삭제했어요.")
    }

    fun inviteToTeam(request: InviteTeamRequest): Response<Any> {
        val userId = PassportHolder.current().requireUserId()
        val team = nightStudyTeamService.findByLeaderId(request.publicId, userId)

        ensureUsersExist(request.members)
        nightStudyTeamService.inviteMembers(team, request.members)

        return Response.created("팀에 초대했어요.")
    }

    fun acceptInvitation(publicId: UUID): Response<Any> {
        val userId = PassportHolder.current().requireUserId()
        nightStudyTeamService.acceptInvitation(publicId, userId)
        return Response.ok("팀에 가입했어요.")
    }

    fun rejectInvitation(publicId: UUID): Response<Any> {
        val userId = PassportHolder.current().requireUserId()
        nightStudyTeamService.rejectInvitation(publicId, userId)
        return Response.ok("팀 초대를 거절했어요.")
    }

    private fun fetchUsersMap(userIds: List<String>): Map<String, UserResponse> {
        return if (userIds.isNotEmpty()) {
            runBlocking { userQueryClient.getUsers(userIds) }
                .usersList.associateBy { it.publicId }
        } else emptyMap()
    }

    private fun ensureUsersExist(members: List<UUID>) {
        val requested = members.map { it.toString() }.distinct()
        val found = fetchUsersMap(requested).keys
        if (!found.containsAll(requested)) {
            throw NightStudyTeamInviteeNotFoundException()
        }
    }
}
