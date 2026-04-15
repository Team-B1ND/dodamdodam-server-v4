package com.b1nd.dodamdodam.nightstudy.application.nightstudy

import com.b1nd.dodamdodam.core.common.data.Response
import com.b1nd.dodamdodam.core.security.passport.holder.PassportHolder
import com.b1nd.dodamdodam.core.security.passport.requireUserId
import com.b1nd.dodamdodam.nightstudy.application.nightstudy.data.request.PersonalNightStudyApplyRequest
import com.b1nd.dodamdodam.nightstudy.application.nightstudy.data.request.ProjectNightStudyApplyRequest
import com.b1nd.dodamdodam.nightstudy.application.nightstudy.data.response.NightStudyApplicationResponse
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.command.NightStudyWithMembersCommand
import com.b1nd.dodamdodam.nightstudy.application.nightstudy.data.response.NightStudyApplicantResponse
import com.b1nd.dodamdodam.nightstudy.application.nightstudy.data.response.PersonalNightStudyResponse
import com.b1nd.dodamdodam.nightstudy.application.nightstudy.data.response.ProjectNightStudyResponse
import com.b1nd.dodamdodam.nightstudy.application.nightstudy.data.toEntity
import com.b1nd.dodamdodam.nightstudy.application.nightstudy.data.toNightStudyApplicationDetailResponse
import com.b1nd.dodamdodam.nightstudy.application.nightstudy.data.toNightStudyApplicationResponses
import com.b1nd.dodamdodam.nightstudy.application.nightstudy.data.toOpenApiUserInfoResponse
import com.b1nd.dodamdodam.nightstudy.application.nightstudy.data.toPersonalNightStudyListResponse
import com.b1nd.dodamdodam.nightstudy.application.nightstudy.data.toProjectNightStudyResponse
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.entity.NightStudyEntity
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.enumeration.NightStudyType
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.service.NightStudyService
import com.b1nd.dodamdodam.nightstudy.domain.room.service.ProjectRoomService
import com.b1nd.dodamdodam.nightstudy.infrastructure.user.client.UserQueryClient
import com.b1nd.dodamdodam.core.common.data.InfinityScrollPageResponse
import com.b1nd.dodamdodam.core.common.exception.BasicException
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.enumeration.NightStudyStatusType
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.exception.NightStudyExceptionCode
import kotlinx.coroutines.runBlocking
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

@Component
@Transactional(rollbackFor = [Exception::class])
class NightStudyUseCase (
    private val nightStudyService: NightStudyService,
    private val projectRoomService: ProjectRoomService,
    private val userQueryClient: UserQueryClient,
) {

    fun applyPersonalNightStudy(request: PersonalNightStudyApplyRequest): Response<Any> {
        val userId = PassportHolder.current().requireUserId()
        isActiveNow()
        nightStudyService.save(request.toEntity(), userId, null)
        return Response.created("개인 심자 신청이 완료됐어요.")
    }

    fun applyProjectNightStudy(request: ProjectNightStudyApplyRequest): Response<Any> {
        val userId = PassportHolder.current().requireUserId()
        isActiveNow()
        nightStudyService.save(request.toEntity(), userId, request.members)
        return Response.created("프로젝트 심자 신청이 완료됐어요.")
    }

    fun getMyPersonalNightStudy(): Response<List<PersonalNightStudyResponse>> {
        val userId = PassportHolder.current().requireUserId()
        val results = nightStudyService.getAllByUserIdAndType(userId, NightStudyType.PERSONAL)
        return Response.ok("개인 심자 신청 목록을 조회했어요.", results.toPersonalNightStudyListResponse())
    }

    fun getMyProjectNightStudy(): Response<List<ProjectNightStudyResponse>> {
        val userId = PassportHolder.current().requireUserId()
        val results = nightStudyService.getAllByUserIdAndType(userId, NightStudyType.PROJECT)
        val leaderMap = nightStudyService.getLeadersByNightStudies(results)
        val responses = results.map { nightStudy ->
            nightStudy.toProjectNightStudyResponse(isLeader = leaderMap[nightStudy.id] == userId)
        }
        return Response.ok("프로젝트 심자 신청 목록을 조회했어요.", responses)
    }

    fun cancelNightStudy(id: UUID): Response<Any> {
        val userId = PassportHolder.current().requireUserId()
        nightStudyService.delete(userId, id)
        return Response.ok("심자 신청을 취소했어요.")
    }

    fun searchAllByType(type: NightStudyType, keyword: String?, status: NightStudyStatusType?, pageable: Pageable): Response<InfinityScrollPageResponse<NightStudyApplicationResponse>> {
        val userIds = keyword?.takeIf { it.isNotBlank() }?.let {
            runBlocking { userQueryClient.getUsersByNameKeyword(it) }
                .usersList.map { user -> UUID.fromString(user.publicId) }
                .ifEmpty { return Response.ok("전체 심야자습 신청 목록을 조회했어요.", InfinityScrollPageResponse(emptyList(), false)) }
        }

        val nightStudiesPage = nightStudyService.searchByType(type, userIds, status, pageable)

        val nightStudiesWithMembers = getNightStudiesWithMembersAndLeaders(nightStudiesPage.content)
        val usersMap = fetchUsersMap(nightStudiesWithMembers)
        val responses = nightStudiesWithMembers.toNightStudyApplicationResponses(usersMap)

        return Response.ok(
            "전체 심야자습 신청 목록을 조회했어요.",
            InfinityScrollPageResponse(
                content = responses,
                hasNext = nightStudiesPage.hasNext()
            )
        )
    }

    fun findById(id: UUID): Response<NightStudyApplicationResponse> {
        val nightStudy = nightStudyService.getByPublicId(id)
        val memberIds = nightStudyService.getMembersByNightStudy(nightStudy)
        val leaderId = nightStudyService.getLeaderByNightStudy(nightStudy)

        val usersMap = fetchUsersMapForSingleNightStudy(leaderId, memberIds)
        val response = nightStudy.toNightStudyApplicationDetailResponse(leaderId, memberIds, usersMap)

        return Response.ok("심자 신청을 조회했어요.", response)
    }

    fun allow(id: UUID): Response<Any> {
        nightStudyService.allow(id)
        return Response.ok("심자 신청을 승인했어요.")
    }

    fun reject(id: UUID, rejectionReason: String): Response<Any> {
        nightStudyService.reject(id, rejectionReason)
        return Response.ok("심자 신청을 거절했어요.")
    }

    fun pending(id: UUID): Response<Any> {
        nightStudyService.pending(id)
        return Response.ok("심자 신청을 대기 상태로 변경했어요.")
    }

    fun assignRoom(id: UUID, roomId: Long): Response<Any> {
        val room = projectRoomService.getById(roomId)
        nightStudyService.assignRoom(id, room)
        return Response.ok("방 배정이 완료됐어요.")
    }

    fun unassignRoom(id: UUID): Response<Any> {
        nightStudyService.unassignRoom(id)
        return Response.ok("방 배정을 해제했어요.")
    }

    private fun getNightStudiesWithMembersAndLeaders(
        nightStudies: List<NightStudyEntity>
    ): List<NightStudyWithMembersCommand> {
        val leaderMap = nightStudyService.getLeadersByNightStudies(nightStudies)
        val membersMap = nightStudyService.getMembersByNightStudies(nightStudies)

        return nightStudies.map { nightStudy ->
            NightStudyWithMembersCommand(
                nightStudy = nightStudy,
                leaderId = leaderMap[nightStudy.id],
                memberIds = membersMap[nightStudy.id] ?: emptyList()
            )
        }
    }

    private fun fetchUsersMap(
        nightStudiesWithMembers: List<NightStudyWithMembersCommand>
    ): Map<String, NightStudyApplicantResponse> {
        val allUserIds = nightStudiesWithMembers
            .flatMap { dto -> listOfNotNull(dto.leaderId) + dto.memberIds }
            .map { it.toString() }
            .distinct()

        return if (allUserIds.isNotEmpty()) {
            runBlocking { userQueryClient.getUsers(allUserIds) }.usersList
                .associate { it.publicId to it.toOpenApiUserInfoResponse() }
        } else {
            emptyMap()
        }
    }

    private fun fetchUsersMapForSingleNightStudy(
        leaderId: UUID?,
        memberIds: List<UUID>
    ): Map<String, NightStudyApplicantResponse> {
        val userIds = (listOfNotNull(leaderId) + memberIds).map { it.toString() }
        return runBlocking { userQueryClient.getUsers(userIds) }.usersList
            .associate { it.publicId to it.toOpenApiUserInfoResponse() }
    }

    val appliedAt: LocalDateTime
        get() = LocalDate.now().atTime(20, 30, 0)

    private fun isActiveNow(): Unit {
        if (LocalDateTime.now().isAfter(appliedAt)) {
            throw BasicException(NightStudyExceptionCode.NOT_APPLICATION_TIME)
        }
    }
}

