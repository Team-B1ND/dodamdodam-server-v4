package com.b1nd.dodamdodam.inapp.application.team

import com.b1nd.dodamdodam.core.common.data.Response
import com.b1nd.dodamdodam.core.redis.service.RedisService
import com.b1nd.dodamdodam.core.security.passport.holder.PassportHolder
import com.b1nd.dodamdodam.core.security.passport.requireUserId
import com.b1nd.dodamdodam.inapp.application.team.data.request.AcceptTeamInviteRequest
import com.b1nd.dodamdodam.inapp.application.team.data.request.AddTeamMemberRequest
import com.b1nd.dodamdodam.inapp.application.team.data.request.CreateTeamInviteRequest
import com.b1nd.dodamdodam.inapp.application.team.data.request.CreateTeamRequest
import com.b1nd.dodamdodam.inapp.application.team.data.request.EditTeamInfoRequest
import com.b1nd.dodamdodam.inapp.application.team.data.request.TransferOwnerRequest
import com.b1nd.dodamdodam.inapp.application.team.data.response.CreateTeamResponse
import com.b1nd.dodamdodam.inapp.application.team.data.response.MyTeamResponse
import com.b1nd.dodamdodam.inapp.application.team.data.response.TeamDetailResponse
import com.b1nd.dodamdodam.inapp.application.team.data.response.TeamInviteResponse
import com.b1nd.dodamdodam.inapp.application.team.data.response.TeamMemberResponse
import com.b1nd.dodamdodam.inapp.application.team.data.toMyTeamResponses
import com.b1nd.dodamdodam.inapp.application.team.data.toTeamDetailResponse
import com.b1nd.dodamdodam.inapp.application.team.data.toTeamMemberResponses
import com.b1nd.dodamdodam.inapp.domain.team.entity.TeamMemberEntity
import com.b1nd.dodamdodam.inapp.domain.team.exception.TeamAlreadyMemberException
import com.b1nd.dodamdodam.inapp.domain.team.exception.TeamInviteCodeNotFoundException
import com.b1nd.dodamdodam.inapp.domain.team.exception.TeamNotMemberException
import com.b1nd.dodamdodam.inapp.domain.team.service.TeamService
import com.b1nd.dodamdodam.inapp.infrastructure.redis.key.TeamInviteRedisKey
import com.b1nd.dodamdodam.inapp.infrastructure.user.client.UserQueryClient
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.util.UUID

@Component
@Transactional(rollbackFor = [Exception::class])
class TeamUseCase(
    private val teamService: TeamService,
    private val userQueryClient: UserQueryClient,
    private val redisService: RedisService,
) {
    fun createTeam(request: CreateTeamRequest): Response<CreateTeamResponse> {
        val userId = PassportHolder.current().requireUserId()
        val teamId = teamService.create(userId, request.toTeamEntity()).publicId
        return Response.created("팀이 생성되었어요.", CreateTeamResponse(teamId!!))
    }

    fun getMyTeam(): Response<List<MyTeamResponse>> {
        val userId = PassportHolder.current().requireUserId()
        val teamMemberList = teamService.getAllByUser(userId);
        return Response.ok("내 팀들을 조회했어요.", teamMemberList.toMyTeamResponses())
    }

    @Transactional(readOnly = true)
    fun getTeam(teamId: UUID): Response<TeamDetailResponse> {
        val team = teamService.getById(teamId)
        val isOwner = teamService.existsOwner(PassportHolder.current().requireUserId(), team)
        return Response.ok("팀을 조회했어요.", team.toTeamDetailResponse(isOwner))
    }

    fun addTeamMembers(request: AddTeamMemberRequest): Response<Any> {
        val team = teamService.getById(request.teamId)
        teamService.addMember(team, request.users)
        return Response.ok("팀 멤버가 추가되었어요.")
    }

    fun deleteTeamMembers(teamId: UUID, users: List<UUID>): Response<Any> {
        teamService.validateOwner(PassportHolder.current().requireUserId(), teamId)
        teamService.deleteMember(teamId, users)
        return Response.ok("팀 멤버가 삭제되었어요.")
    }

    fun deleteTeam(teamId: UUID): Response<Any> {
        teamService.validateOwner(PassportHolder.current().requireUserId(), teamId)
        teamService.delete(teamId)
        return Response.ok("팀이 삭제되었어요.")
    }

    fun editTeamInfo(request: EditTeamInfoRequest): Response<Any> {
        teamService.validateOwner(PassportHolder.current().requireUserId(), request.teamId)
        teamService.updateInfo(
            request.teamId,
            request.name,
            request.description,
            request.iconUrl,
            request.githubUrl
        )
        return Response.ok("팀 정보가 수정되었어요.")
    }

    fun transferOwnership(request: TransferOwnerRequest): Response<Any> {
        val userId = PassportHolder.current().requireUserId()
        teamService.validateOwner(userId, request.teamId)
        teamService.transferOwner(request.teamId, userId, request.userPublicId)
        return Response.ok("팀 오너가 변경되었어요.")
    }

    fun createInviteCode(request: CreateTeamInviteRequest): Response<TeamInviteResponse> {
        val userId = PassportHolder.current().requireUserId()
        val team = teamService.getById(request.teamPublicId)
        if (!teamService.isMember(userId, team)) throw TeamNotMemberException()

        val inviteCode = UUID.randomUUID().toString().replace("-", "")
        redisService.set(TeamInviteRedisKey.INVITE, inviteCode, team.publicId.toString(), Duration.ofDays(1))
        return Response.created("초대 코드가 생성됐어요.", TeamInviteResponse(inviteCode))
    }

    fun acceptInvite(request: AcceptTeamInviteRequest): Response<Any> {
        val userId = PassportHolder.current().requireUserId()
        val teamPublicId = redisService.get(TeamInviteRedisKey.INVITE, request.inviteCode)
            ?: throw TeamInviteCodeNotFoundException()

        val team = teamService.getById(UUID.fromString(teamPublicId))
        if (teamService.isMember(userId, team)) throw TeamAlreadyMemberException()

        teamService.addMember(team, listOf(userId))
        redisService.delete(TeamInviteRedisKey.INVITE, request.inviteCode)
        return Response.ok("팀에 가입했어요.")
    }

    @Transactional(readOnly = true)
    fun getTeamMembers(teamId: UUID): Response<List<TeamMemberResponse>> {
        val members = teamService.getAllByTeam(teamId)
        val userIds = members.map { it.user.toString() }
        val userMap = runBlocking { userQueryClient.getUsers(userIds) }
            .usersList
            .associateBy { UUID.fromString(it.publicId) }
        return Response.ok("팀 멤버를 조회했어요.", members.toTeamMemberResponses(userMap))
    }
}