package com.b1nd.dodamdodam.outsleeping.application.openapi

import com.b1nd.dodamdodam.core.common.data.Response
import com.b1nd.dodamdodam.outsleeping.application.outsleeping.data.response.OutSleepingResponse
import com.b1nd.dodamdodam.outsleeping.application.outsleeping.data.toResponse
import com.b1nd.dodamdodam.outsleeping.domain.outsleeping.service.OpenApiOutSleepingService
import com.b1nd.dodamdodam.outsleeping.infrastructure.user.client.UserQueryClient
import jakarta.transaction.Transactional
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.util.*

@Component
@Transactional(rollbackOn = [Exception::class])
class OpenApiOutSleepingUseCase(
    private val openApiOutSleepingService: OpenApiOutSleepingService,
    private val userQueryClient: UserQueryClient,
) {

    fun getValid(): Response<List<OutSleepingResponse>> {
        val outSleepings = openApiOutSleepingService.getByDate(LocalDate.now())
        val userInfoMap = getUserInfoMap(outSleepings.map { it.userId })

        return Response.ok(
            "유효한 외박 목록을 조회했어요.",
            outSleepings.map { it.toResponse(userInfoMap[it.userId]) }
        )
    }

    private fun getUserInfoMap(userIds: List<UUID>) = runBlocking {
        userQueryClient.getUsers(userIds)
    }.associateBy { UUID.fromString(it.publicId) }
}