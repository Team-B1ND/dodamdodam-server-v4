package com.b1nd.dodamdodam.file.application.banner

import com.b1nd.dodamdodam.core.common.data.Response
import com.b1nd.dodamdodam.file.application.banner.data.request.CreateBannerRequest
import com.b1nd.dodamdodam.file.application.banner.data.response.BannerResponse
import com.b1nd.dodamdodam.file.application.banner.data.toResponse
import com.b1nd.dodamdodam.file.domain.banner.entity.BannerEntity
import com.b1nd.dodamdodam.file.domain.banner.service.BannerService
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional(rollbackFor = [Exception::class])
class BannerUseCase(
    private val bannerService: BannerService,
) {
    fun create(request: CreateBannerRequest): Response<Any> {
        bannerService.save(BannerEntity(request.imageUrl, request.linkUrl))
        return Response.created("배너가 등록됐어요.")
    }

    @Transactional(readOnly = true)
    fun getAll(): Response<List<BannerResponse>> {
        val banners = bannerService.getAll().map { it.toResponse() }
        return Response.ok("배너를 조회했어요.", banners)
    }

    fun updateActive(id: Long, active: Boolean): Response<Any> {
        bannerService.getById(id).updateActive(active)
        return Response.ok("배너 활성화 상태를 변경했어요.")
    }

    fun delete(id: Long): Response<Any> {
        bannerService.delete(id)
        return Response.ok("배너를 삭제했어요.")
    }
}
