package com.b1nd.dodamdodam.nightstudy.application.nightstudy

import com.b1nd.dodamdodam.core.common.data.Response
import com.b1nd.dodamdodam.core.security.passport.holder.PassportHolder
import com.b1nd.dodamdodam.core.security.passport.requireUserId
import com.b1nd.dodamdodam.nightstudy.application.nightstudy.data.request.ApplyPersonalNightStudyRequest
import com.b1nd.dodamdodam.nightstudy.application.nightstudy.data.request.ApplyProjectNightStudyRequest
import com.b1nd.dodamdodam.nightstudy.application.nightstudy.data.response.ApplicationResponse
import com.b1nd.dodamdodam.nightstudy.application.nightstudy.data.response.ApplicantResponse
import com.b1nd.dodamdodam.nightstudy.application.nightstudy.data.response.PersonalNightStudyResponse
import com.b1nd.dodamdodam.nightstudy.application.nightstudy.data.response.ProjectNightStudyResponse
import com.b1nd.dodamdodam.nightstudy.application.nightstudy.data.toEntity
import com.b1nd.dodamdodam.nightstudy.application.nightstudy.data.toOpenApiNightStudyResponse
import com.b1nd.dodamdodam.nightstudy.application.nightstudy.data.toOpenApiUserInfoResponse
import com.b1nd.dodamdodam.nightstudy.application.nightstudy.data.toPersonalNightStudyListResponse
import com.b1nd.dodamdodam.nightstudy.application.nightstudy.data.toProjectNightStudyResponse
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.entity.NightStudyEntity
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.enumeration.NightStudyStatusType
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.enumeration.NightStudyType
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.service.NightStudyService
import com.b1nd.dodamdodam.nightstudy.infrastructure.user.client.UserQueryClient
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Component
@Transactional(rollbackFor = [Exception::class])
class NightStudyUseCase (
    private val nightStudyService: NightStudyService,
    private val userQueryClient: UserQueryClient,
) {
    fun applyPersonalNightStudy(request: ApplyPersonalNightStudyRequest): Response<Any> {
        val userId = PassportHolder.current().requireUserId()
        nightStudyService.save(request.toEntity(), userId, null)
        return Response.created("개인 심자 신청이 완료됐어요.")
    }

    fun applyProjectNightStudy(request: ApplyProjectNightStudyRequest): Response<Any> {
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

    fun findAllByType(type: NightStudyType): Response<List<ApplicationResponse>> {
        val nightStudies = nightStudyService.getAllByType(type)
        val nightStudiesWithMembers = getNightStudiesWithMembersAndLeaders(nightStudies)
        val usersMap = fetchUsersMap(nightStudiesWithMembers)
        val responses = buildOpenApiResponses(nightStudiesWithMembers, usersMap)

        return Response.ok("전체 심자 신청 목록을 조회했어요.", responses)
    }

    fun findById(id: UUID): Response<ApplicationResponse> {
        val nightStudy = nightStudyService.getByPublicId(id)
        val memberIds = nightStudyService.getMembersByNightStudy(nightStudy)
        val leaderId = nightStudyService.getLeaderByNightStudy(nightStudy)

        val usersMap = fetchUsersMapForSingleNightStudy(leaderId, memberIds)
        val response = buildSingleOpenApiResponse(nightStudy, leaderId, memberIds, usersMap)

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
    ): List<Triple<NightStudyEntity, UUID?, List<UUID>>> {
        return nightStudies.map { nightStudy ->
            val members = nightStudyService.getMembersByNightStudy(nightStudy)
            val leader = nightStudyService.getLeaderByNightStudy(nightStudy)
            Triple(nightStudy, leader, members)
        }
    }

    private fun fetchUsersMap(
        nightStudiesWithMembers: List<Triple<NightStudyEntity, UUID?, List<UUID>>>
    ): Map<String, ApplicantResponse> {
        val allUserIds = nightStudiesWithMembers
            .flatMap { (_, leader, members) -> listOfNotNull(leader) + members }
            .map { it.toString() }
            .distinct()

        return if (allUserIds.isNotEmpty()) {
            runBlocking { userQueryClient.getUsers(allUserIds) }.usersList
                .associate { it.publicId to it.toOpenApiUserInfoResponse() }
        } else {
            emptyMap()
        }
    }

    private fun buildOpenApiResponses(
        nightStudiesWithMembers: List<Triple<NightStudyEntity, UUID?, List<UUID>>>,
        usersMap: Map<String, ApplicantResponse>
    ): List<ApplicationResponse> {
        return nightStudiesWithMembers.mapNotNull { (nightStudy, leaderId, memberIds) ->
            leaderId?.let {
                usersMap[it.toString()]?.let { leader ->
                    nightStudy.toOpenApiNightStudyResponse(
                        leader = leader,
                        members = memberIds.mapNotNull { memberId -> usersMap[memberId.toString()] }
                    )
                }
            }
        }
    }

    private fun fetchUsersMapForSingleNightStudy(
        leaderId: UUID?,
        memberIds: List<UUID>
    ): Map<String, ApplicantResponse> {
        val userIds = (listOfNotNull(leaderId) + memberIds).map { it.toString() }
        return runBlocking { userQueryClient.getUsers(userIds) }.usersList
            .associate { it.publicId to it.toOpenApiUserInfoResponse() }
    }

    private fun buildSingleOpenApiResponse(
        nightStudy: NightStudyEntity,
        leaderId: UUID?,
        memberIds: List<UUID>,
        usersMap: Map<String, ApplicantResponse>
    ): ApplicationResponse {
        return nightStudy.toOpenApiNightStudyResponse(
            leader = usersMap[leaderId.toString()]!!,
            members = memberIds.mapNotNull { usersMap[it.toString()] }
        )
    }
}

