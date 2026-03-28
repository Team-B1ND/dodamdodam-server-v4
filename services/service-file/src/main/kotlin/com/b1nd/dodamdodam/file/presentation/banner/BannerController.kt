package com.b1nd.dodamdodam.file.presentation.banner

import com.b1nd.dodamdodam.core.common.data.Response
import com.b1nd.dodamdodam.core.security.annotation.authentication.UserAccess
import com.b1nd.dodamdodam.core.security.passport.enumerations.RoleType
import com.b1nd.dodamdodam.file.application.banner.BannerUseCase
import com.b1nd.dodamdodam.file.application.banner.data.request.CreateBannerRequest
import com.b1nd.dodamdodam.file.application.banner.data.response.BannerResponse
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/banner")
class BannerController(
    private val bannerUseCase: BannerUseCase,
) {
    @UserAccess(roles = [RoleType.ADMIN])
    @PostMapping
    fun create(@RequestBody request: CreateBannerRequest): Response<Any> =
        bannerUseCase.create(request)

    @UserAccess
    @GetMapping
    fun getAll(): Response<List<BannerResponse>> =
        bannerUseCase.getAll()

    @UserAccess(roles = [RoleType.ADMIN])
    @PatchMapping("/{id}/active")
    fun updateActive(@PathVariable id: Long, @RequestParam active: Boolean): Response<Any> =
        bannerUseCase.updateActive(id, active)

    @UserAccess(roles = [RoleType.ADMIN])
    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long): Response<Any> =
        bannerUseCase.delete(id)
}
