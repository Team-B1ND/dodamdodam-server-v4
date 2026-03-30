package com.b1nd.dodamdodam.auth.presentation.auth

import com.b1nd.dodamdodam.auth.application.auth.AuthUseCase
import com.b1nd.dodamdodam.auth.application.auth.data.request.LoginRequest
import com.b1nd.dodamdodam.auth.application.auth.data.request.RefreshRequest
import com.b1nd.dodamdodam.auth.application.auth.data.response.LoginResponse
import com.b1nd.dodamdodam.auth.infrastructure.cookie.CookieProperties
import com.b1nd.dodamdodam.core.common.data.Response
import com.b1nd.dodamdodam.core.security.annotation.authentication.UserAccess
import jakarta.servlet.http.HttpServletRequest
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class AuthController(
    private val useCase: AuthUseCase,
    private val cookieProperties: CookieProperties,
) {
    @PostMapping("/login")
    fun login(@RequestBody request: LoginRequest): Response<LoginResponse> =
        useCase.login(request)

    @PostMapping("/refresh")
    fun refresh(
        @RequestBody(required = false) request: RefreshRequest?,
        httpRequest: HttpServletRequest,
    ): Response<LoginResponse> {
        val cookieRefreshToken = httpRequest.cookies?.firstOrNull { it.name == cookieProperties.refreshTokenName }?.value
        return useCase.refresh(request?.refreshToken, cookieRefreshToken)
    }

    @PostMapping("/logout")
    fun logout(httpRequest: HttpServletRequest): Response<Unit> {
        val cookieRefreshToken = httpRequest.cookies?.firstOrNull { it.name == cookieProperties.refreshTokenName }?.value
        useCase.logout(cookieRefreshToken)
        return Response.ok("로그아웃에 성공했어요.")
    }

    @GetMapping("/health")
    fun health(): String = "OK"

    @UserAccess(enabledOnly = true)
    @GetMapping("/test")
    fun test(): String = "TEST"
}
