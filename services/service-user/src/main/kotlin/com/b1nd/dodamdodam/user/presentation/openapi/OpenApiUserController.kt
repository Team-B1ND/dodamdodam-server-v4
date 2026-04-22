package com.b1nd.dodamdodam.user.presentation.openapi

import com.b1nd.dodamdodam.core.common.data.Response
import com.b1nd.dodamdodam.user.application.openapi.OpenApiUserUseCase
import com.b1nd.dodamdodam.user.application.openapi.data.toUserInfoResponse
import com.b1nd.dodamdodam.user.application.user.data.response.UserInfoResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/openapi")
class OpenApiUserController(
    private val openApiUserUseCase: OpenApiUserUseCase,
) {

    @GetMapping("/search")
    fun searchUsers(
        @RequestParam(required = false) keyword: String?,
    ): Response<List<UserInfoResponse>> {
        if (keyword.isNullOrBlank()) {
            return Response.ok("유저 목록을 조회했어요.", emptyList())
        }
        val users = openApiUserUseCase.getUsersByNameKeyword(keyword)
            .map { it.toUserInfoResponse() }
        return Response.ok("유저 목록을 조회했어요.", users)
    }
}
