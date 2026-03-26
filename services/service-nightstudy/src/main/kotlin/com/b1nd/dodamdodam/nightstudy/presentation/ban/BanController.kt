package com.b1nd.dodamdodam.nightstudy.presentation.ban

import com.b1nd.dodamdodam.core.common.data.Response
import com.b1nd.dodamdodam.core.security.annotation.authentication.UserAccess
import com.b1nd.dodamdodam.core.security.passport.enumerations.RoleType
import com.b1nd.dodamdodam.nightstudy.application.ban.BanUseCase
import com.b1nd.dodamdodam.nightstudy.application.ban.data.request.BanUserRequest
import com.b1nd.dodamdodam.nightstudy.application.ban.data.response.BannedUserResponse
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/bans")
class BanController(
    private val banUseCase: BanUseCase,
) {

    @UserAccess(roles = [RoleType.DORMITORY_MANAGER])
    @PostMapping
    fun ban(@RequestBody @Valid request: BanUserRequest): Response<Any> =
        banUseCase.ban(request)

    @UserAccess(roles = [RoleType.DORMITORY_MANAGER])
    @GetMapping
    fun getAll(): Response<List<BannedUserResponse>> =
        banUseCase.getAll()

    @UserAccess(roles = [RoleType.STUDENT])
    @GetMapping("/my")
    fun getMyBan(): Response<BannedUserResponse?> =
        banUseCase.getMyBan()

    @UserAccess(roles = [RoleType.DORMITORY_MANAGER])
    @DeleteMapping("/{userId}")
    fun unban(@PathVariable userId: UUID): Response<Any> =
        banUseCase.unban(userId)
}
