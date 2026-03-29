package com.b1nd.dodamdodam.nightstudy.application.ban

import com.b1nd.dodamdodam.core.common.data.Response
import com.b1nd.dodamdodam.core.security.passport.holder.PassportHolder
import com.b1nd.dodamdodam.core.security.passport.requireUserId
import com.b1nd.dodamdodam.nightstudy.application.ban.data.request.BanUserRequest
import com.b1nd.dodamdodam.nightstudy.application.ban.data.response.BannedUserResponse
import com.b1nd.dodamdodam.nightstudy.application.ban.data.toEntity
import com.b1nd.dodamdodam.nightstudy.application.ban.data.toResponse
import com.b1nd.dodamdodam.nightstudy.application.ban.data.toResponseList
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.service.NightStudyBannedService
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Component
@Transactional(rollbackFor = [Exception::class])
class BanUseCase(
    private val nightStudyBannedService: NightStudyBannedService,
) {

    fun ban(request: BanUserRequest): Response<Any> {
        nightStudyBannedService.ban(request.toEntity())
        return Response.created("심야 자습 정지 처리가 완료됐어요.")
    }

    fun getAll(): Response<List<BannedUserResponse>> {
        val bans = nightStudyBannedService.getAll().toResponseList()
        return Response.ok("심야 자습 정지 목록을 조회했어요.", bans)
    }

    @Transactional(readOnly = true)
    fun getMyBan(): Response<BannedUserResponse?> {
        val userId = PassportHolder.current().requireUserId()
        val ban = nightStudyBannedService.findByUserId(userId)?.toResponse()
        return Response.ok("내 심야 자습 정지 정보를 조회했어요.", ban)
    }

    fun unban(userId: UUID): Response<Any> {
        nightStudyBannedService.unban(userId)
        return Response.ok("심야 자습 정지를 해제했어요.")
    }
}
