package com.b1nd.dodamdodam.auth.infrastructure.cookie

import com.b1nd.dodamdodam.auth.infrastructure.security.jwt.properties.JwtProperties
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseCookie
import org.springframework.stereotype.Component
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import java.time.Duration

@Component
class TokenCookieProvider(
    private val cookieProperties: CookieProperties,
    private val jwtProperties: JwtProperties
) {
    fun addTokenCookies(accessToken: String, refreshToken: String) {
        val response = currentResponse()
        response.addHeader(HttpHeaders.SET_COOKIE, createAccessTokenCookie(accessToken).toString())
        response.addHeader(HttpHeaders.SET_COOKIE, createRefreshTokenCookie(refreshToken).toString())
    }

    fun addExpiredCookies() {
        val response = currentResponse()
        response.addHeader(HttpHeaders.SET_COOKIE, createExpiredCookie(cookieProperties.accessTokenName, "/").toString())
        response.addHeader(HttpHeaders.SET_COOKIE, createExpiredCookie(cookieProperties.refreshTokenName, cookieProperties.refreshTokenPath).toString())
    }

    fun clearStaleTokenCookies() {
        val response = currentResponse()
        val names = listOf(cookieProperties.accessTokenName, cookieProperties.refreshTokenName)
        val domains = listOf("", cookieProperties.domain).distinct()
        val paths = listOf("/", cookieProperties.refreshTokenPath).distinct()
        names.forEach { name ->
            domains.forEach { domain ->
                paths.forEach { path ->
                    response.addHeader(HttpHeaders.SET_COOKIE, createExpiredCookie(name, path, domain).toString())
                }
            }
        }
    }

    private fun currentResponse() =
        (RequestContextHolder.getRequestAttributes() as ServletRequestAttributes).response!!

    private fun cookieMaxAge(): Duration =
        Duration.ofSeconds(jwtProperties.refreshExpireSeconds * 2)

    private fun createAccessTokenCookie(token: String): ResponseCookie =
        buildCookie(cookieProperties.accessTokenName, token)
            .path("/")
            .maxAge(cookieMaxAge())
            .build()

    private fun createRefreshTokenCookie(token: String): ResponseCookie =
        buildCookie(cookieProperties.refreshTokenName, token)
            .path(cookieProperties.refreshTokenPath)
            .maxAge(cookieMaxAge())
            .build()

    private fun createExpiredCookie(name: String, path: String, cookieDomain: String = cookieProperties.domain): ResponseCookie =
        buildCookie(name, "", cookieDomain)
            .path(path)
            .maxAge(0)
            .build()

    private fun buildCookie(
        name: String,
        value: String,
        cookieDomain: String = cookieProperties.domain
    ): ResponseCookie.ResponseCookieBuilder =
        ResponseCookie.from(name, value)
            .httpOnly(true)
            .secure(cookieProperties.secure)
            .sameSite(cookieProperties.sameSite)
            .apply {
                cookieDomain.takeIf { it.isNotBlank() }?.let { domain(it) }
            }
}
