package com.b1nd.dodamdodam.nightstudy.application.openapi

import com.b1nd.dodamdodam.core.common.data.Response
import com.b1nd.dodamdodam.nightstudy.application.nightstudy.data.toOpenApiUserInfoResponse
import com.b1nd.dodamdodam.nightstudy.application.openapi.data.response.OpenApiNightStudyResponse
import com.b1nd.dodamdodam.nightstudy.application.openapi.data.toOpenApiNightStudyResponse
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.enumeration.NightStudyType
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.service.OpenApiNightStudyService
import com.b1nd.dodamdodam.nightstudy.infrastructure.user.client.UserQueryClient
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
class OpenApiNightStudyUseCase(
    private val openApiNightStudyService: OpenApiNightStudyService,
    private val userQueryClient: UserQueryClient
) {

    fun getAttendances(type: NightStudyType, date: LocalDate): Response<List<OpenApiNightStudyResponse>> {
        val students = openApiNightStudyService.getActiveNightStudiesByType(type, date)

        val allUserIds = students
            .flatMap { dto -> listOfNotNull(dto.leaderId) + dto.memberIds }
            .map { it.toString() }
            .distinct()

        val userMap = runBlocking { userQueryClient.getUsers(allUserIds) }.usersList
            .associate { it.publicId to it }

        val response = students.toOpenApiNightStudyResponse(userMap)

        return Response.ok("전체 심야자습 인원을 조회했습니다.", response)
    }
}