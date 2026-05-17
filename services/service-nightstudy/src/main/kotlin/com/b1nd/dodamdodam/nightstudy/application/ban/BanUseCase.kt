package com.b1nd.dodamdodam.nightstudy.application.ban

import com.b1nd.dodamdodam.core.common.data.Response
import com.b1nd.dodamdodam.core.security.passport.holder.PassportHolder
import com.b1nd.dodamdodam.core.security.passport.requireUserId
import com.b1nd.dodamdodam.grpc.user.UserResponse
import com.b1nd.dodamdodam.nightstudy.application.ban.data.request.BanUserRequest
import com.b1nd.dodamdodam.nightstudy.application.ban.data.response.BannedUserResponse
import com.b1nd.dodamdodam.nightstudy.application.ban.data.response.DormitoryManagerBannedUserResponse
import com.b1nd.dodamdodam.nightstudy.application.ban.data.toDormitoryManagerResponseList
import com.b1nd.dodamdodam.nightstudy.application.ban.data.toEntity
import com.b1nd.dodamdodam.nightstudy.application.ban.data.toResponse
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.service.NightStudyBannedService
import com.b1nd.dodamdodam.nightstudy.infrastructure.user.client.UserQueryClient
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Component
@Transactional(rollbackFor = [Exception::class])
class BanUseCase(
    private val nightStudyBannedService: NightStudyBannedService,
    private val userQueryClient: UserQueryClient,
) {

    fun ban(request: BanUserRequest): Response<Any> {
        nightStudyBannedService.ban(request.toEntity())
        return Response.created("심야 자습 정지 처리가 완료됐어요.")
    }

    @Transactional(readOnly = true)
    fun getAll(): Response<List<DormitoryManagerBannedUserResponse>> {
        val bans = nightStudyBannedService.getAll()
        val userIds = bans.map { it.userId.toString() }.distinct()
        val usersMap = fetchUsersMap(userIds)

        return Response.ok("심야 자습 정지 목록을 조회했어요.", bans.toDormitoryManagerResponseList(usersMap))
    }

    @Transactional(readOnly = true)
    fun getMyBan(): Response<BannedUserResponse> {
        val userId = PassportHolder.current().requireUserId()
        val ban = nightStudyBannedService.findByUserId(userId)?.toResponse()
        return Response.ok("내 심야 자습 정지 정보를 조회했어요.", ban)
    }

    fun unban(userId: UUID): Response<Any> {
        nightStudyBannedService.unban(userId)
        return Response.ok("심야 자습 정지를 해제했어요.")
    }

    private fun fetchUsersMap(userIds: List<String>): Map<String, UserResponse> {
        return if (userIds.isNotEmpty()) {
            runBlocking { userQueryClient.getUsers(userIds) }
                .usersList.associateBy { it.publicId }
        } else emptyMap()
    }
}
