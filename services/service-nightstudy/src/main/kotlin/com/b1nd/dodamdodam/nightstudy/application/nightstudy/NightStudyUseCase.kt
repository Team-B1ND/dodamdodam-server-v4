package com.b1nd.dodamdodam.nightstudy.application.nightstudy

import com.b1nd.dodamdodam.core.common.data.Response
import com.b1nd.dodamdodam.core.security.passport.holder.PassportHolder
import com.b1nd.dodamdodam.core.security.passport.requireUserId
import com.b1nd.dodamdodam.nightstudy.application.nightstudy.data.request.ApplyPersonalNightStudyRequest
import com.b1nd.dodamdodam.nightstudy.application.nightstudy.data.request.ApplyProjectNightStudyRequest
import com.b1nd.dodamdodam.nightstudy.application.nightstudy.data.response.OpenApiNightStudyResponse
import com.b1nd.dodamdodam.nightstudy.application.nightstudy.data.response.PersonalNightStudyResponse
import com.b1nd.dodamdodam.nightstudy.application.nightstudy.data.response.ProjectNightStudyResponse
import com.b1nd.dodamdodam.nightstudy.application.nightstudy.data.toEntity
import com.b1nd.dodamdodam.nightstudy.application.nightstudy.data.toOpenApiNightStudyResponse
import com.b1nd.dodamdodam.nightstudy.application.nightstudy.data.toOpenApiUserInfoResponse
import com.b1nd.dodamdodam.nightstudy.application.nightstudy.data.toPersonalNightStudyListResponse
import com.b1nd.dodamdodam.nightstudy.application.nightstudy.data.toProjectNightStudyListResponse
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.enumeration.NightStudyStatusType
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.enumeration.NightStudyType
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.service.NightStudyService
import com.b1nd.dodamdodam.nightstudy.infrastructure.user.client.UserQueryClient
import jakarta.transaction.Transactional
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Component

@Component
@Transactional(rollbackOn = [Exception::class])
class NightStudyUseCase (
    private val nightStudyService: NightStudyService,
    private val userQueryClient: UserQueryClient,
) {
    fun applyPersonalNightStudy(request: ApplyPersonalNightStudyRequest): Response<Any> {
        val userId = PassportHolder.current().requireUserId()
        nightStudyService.save(request.toEntity(userId), null)
        return Response.created("개인 심자 신청이 완료됐어요.")
    }

    fun applyProjectNightStudy(request: ApplyProjectNightStudyRequest): Response<Any> {
        val userId = PassportHolder.current().requireUserId()
        nightStudyService.save(request.toEntity(userId), request.members)
        return Response.created("프로젝트 심자 신청이 완료됐어요.")
    }

    fun getMyPersonalNightStudy(status: NightStudyStatusType): Response<List<PersonalNightStudyResponse>> {
        val userId = PassportHolder.current().requireUserId()
        val result = nightStudyService.findAllByUserIdAndStatusAndType(userId, status, NightStudyType.PERSONAL)
        return Response.ok("개인 심자 신청 목록을 조회했어요.", result.toPersonalNightStudyListResponse())
    }

    fun getMyProjectNightStudy(status: NightStudyStatusType): Response<List<ProjectNightStudyResponse>> {
        val userId = PassportHolder.current().requireUserId()
        val result = nightStudyService.findAllByUserIdAndStatusAndType(userId, status, NightStudyType.PROJECT)
        return Response.ok("프로젝트 심자 신청 목록을 조회했어요.", result.toProjectNightStudyListResponse())
    }

    fun cancelNightStudy(id: Long): Response<Any> {
        val userId = PassportHolder.current().requireUserId()
        nightStudyService.delete(userId, id)
        return Response.ok("심자 신청을 취소했어요.")
    }

    fun findAllByType(type: NightStudyType): Response<List<OpenApiNightStudyResponse>> {
        val nightStudies = nightStudyService.findAllByType(type)
        val nightStudyWithMembers = nightStudies.map { it to nightStudyService.findMembersByNightStudyId(it.id!!) }

        val allUserIds = nightStudyWithMembers.flatMap { (ns, members) -> listOf(ns.leaderId) + members }
            .map { it.toString() }
            .distinct()

        val usersMap = if (allUserIds.isNotEmpty()) {
            runBlocking { userQueryClient.getUsers(allUserIds) }.usersList
                .associate { it.publicId to it.toOpenApiUserInfoResponse() }
        } else emptyMap()

        val responses = nightStudyWithMembers.map { (nightStudy, memberIds) ->
            nightStudy.toOpenApiNightStudyResponse(
                leader = usersMap[nightStudy.leaderId.toString()]!!,
                members = memberIds.map { usersMap[it.toString()]!! }
            )
        }
        return Response.ok("전체 심자 신청 목록을 조회했어요.", responses)
    }

    fun findById(id: Long): Response<OpenApiNightStudyResponse> {
        val nightStudy = nightStudyService.findById(id)!!
        val memberIds = nightStudyService.findMembersByNightStudyId(nightStudy.id!!)
        val userIds = (listOf(nightStudy.leaderId) + memberIds).map { it.toString() }

        val usersMap = runBlocking { userQueryClient.getUsers(userIds) }.usersList
            .associate { it.publicId to it.toOpenApiUserInfoResponse() }

        val response = nightStudy.toOpenApiNightStudyResponse(
            leader = usersMap[nightStudy.leaderId.toString()]!!,
            members = memberIds.map { usersMap[it.toString()]!! }
        )
        return Response.ok("심자 신청을 조회했어요.", response)
    }

    fun allow(id: Long): Response<Any> {
        nightStudyService.allow(id)
        return Response.ok("심자 신청을 승인했어요.")
    }

    fun reject(id: Long, rejectionReason: String): Response<Any> {
        nightStudyService.reject(id, rejectionReason)
        return Response.ok("심자 신청을 거절했어요.")
    }

    fun pending(id: Long): Response<Any> {
        nightStudyService.pending(id)
        return Response.ok("심자 신청을 대기 상태로 변경했어요.")
    }
}

