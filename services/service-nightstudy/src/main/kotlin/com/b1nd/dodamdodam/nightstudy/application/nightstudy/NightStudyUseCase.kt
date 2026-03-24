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
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.enumeration.NightStudyStatusType
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.enumeration.NightStudyType
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.service.NightStudyService
import com.b1nd.dodamdodam.nightstudy.infrastructure.user.client.UserQueryClient
import com.b1nd.dodamdodam.core.common.data.InfinityScrollPageResponse
import kotlinx.coroutines.runBlocking
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Component
@Transactional(rollbackFor = [Exception::class])
class NightStudyUseCase (
    private val nightStudyService: NightStudyService,
    private val userQueryClient: UserQueryClient,
) {
    fun applyPersonalNightStudy(request: PersonalNightStudyApplyRequest): Response<Any> {
        val userId = PassportHolder.current().requireUserId()
        nightStudyService.save(request.toEntity(), userId, null)
        return Response.created("개인 심자 신청이 완료됐어요.")
    }

    fun applyProjectNightStudy(request: ProjectNightStudyApplyRequest): Response<Any> {
        val userId = PassportHolder.current().requireUserId()
        nightStudyService.save(request.toEntity(), userId, request.members)
        return Response.created("프로젝트 심자 신청이 완료됐어요.")
    }

    fun getMyPersonalNightStudy(status: NightStudyStatusType): Response<List<PersonalNightStudyResponse>> {
        val userId = PassportHolder.current().requireUserId()
        val result = nightStudyService.getAllByUserIdAndStatusAndType(userId, status, NightStudyType.PERSONAL)
        return Response.ok("개인 심자 신청 목록을 조회했어요.", result.toPersonalNightStudyListResponse())
    }

    fun getMyProjectNightStudy(status: NightStudyStatusType): Response<List<ProjectNightStudyResponse>> {
        val userId = PassportHolder.current().requireUserId()
        val result = nightStudyService.getAllByUserIdAndStatusAndType(userId, status, NightStudyType.PROJECT)
        val responses = result.map { nightStudy ->
            val leaderId = nightStudyService.getLeaderByNightStudy(nightStudy)
            nightStudy.toProjectNightStudyResponse(isLeader = leaderId == userId)
        }
        return Response.ok("프로젝트 심자 신청 목록을 조회했어요.", responses)
    }

    fun cancelNightStudy(id: UUID): Response<Any> {
        val userId = PassportHolder.current().requireUserId()
        nightStudyService.delete(userId, id)
        return Response.ok("심자 신청을 취소했어요.")
    }

    fun findAllByType(type: NightStudyType, pageable: Pageable): Response<InfinityScrollPageResponse<NightStudyApplicationResponse>> {
        val nightStudiesPage = nightStudyService.getAllByType(type, pageable)
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

    private fun getNightStudiesWithMembersAndLeaders(
        nightStudies: List<NightStudyEntity>
    ): List<NightStudyWithMembersCommand> {
        return nightStudies.map { nightStudy ->
            nightStudyService.getNightStudyWithMembers(nightStudy)
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
}

