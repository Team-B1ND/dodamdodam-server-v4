package com.b1nd.dodamdodam.oauth.application.usecase

import com.b1nd.dodamdodam.oauth.application.data.response.IntrospectResponse
import com.b1nd.dodamdodam.oauth.application.data.response.TokenResponse
import com.b1nd.dodamdodam.oauth.domain.client.service.OauthClientService
import com.b1nd.dodamdodam.oauth.domain.token.entity.OauthToken
import com.b1nd.dodamdodam.oauth.domain.token.service.OauthTokenService
import com.b1nd.dodamdodam.oauth.infrastructure.exception.OauthException
import com.b1nd.dodamdodam.oauth.infrastructure.exception.OauthExceptionCode
import com.b1nd.dodamdodam.oauth.infrastructure.security.properties.OauthProperties
import com.b1nd.dodamdodam.oauth.support.AuthTokenClient
import com.b1nd.dodamdodam.oauth.support.JwtProvider
import com.b1nd.dodamdodam.oauth.support.TokenHashUtil
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.time.LocalDateTime
import java.util.Base64
import java.util.UUID

@Component
class OauthTokenUseCase(
    private val tokenService: OauthTokenService,
    private val clientService: OauthClientService,
    private val jwtProvider: JwtProvider,
    private val authTokenClient: AuthTokenClient,
    private val properties: OauthProperties,
    private val passwordEncoder: PasswordEncoder,
) {

    @Transactional
    suspend fun exchangeToken(grantType: String, code: String?, redirectUri: String?, clientId: String, clientSecret: String, refreshToken: String?, codeVerifier: String?): TokenResponse {
        return when (grantType) {
            "authorization_code" -> exchangeAuthorizationCode(code!!, redirectUri!!, clientId, clientSecret, codeVerifier)
            "refresh_token" -> refreshAccessToken(refreshToken!!, clientId, clientSecret)
            else -> throw OauthException(OauthExceptionCode.UNSUPPORTED_GRANT_TYPE)
        }
    }

    private suspend fun exchangeAuthorizationCode(code: String, redirectUri: String, clientId: String, clientSecret: String, codeVerifier: String?): TokenResponse {
        val client = clientService.findActiveByClientId(clientId)
        clientService.verifyClientSecret(client, clientSecret, passwordEncoder)

        val authCode = tokenService.findByCode(code)

        if (authCode.used) throw OauthException(OauthExceptionCode.INVALID_GRANT)
        if (authCode.expiresAt.isBefore(LocalDateTime.now())) throw OauthException(OauthExceptionCode.INVALID_GRANT)
        if (authCode.clientId != clientId) throw OauthException(OauthExceptionCode.INVALID_GRANT)
        if (authCode.redirectUri != redirectUri) throw OauthException(OauthExceptionCode.INVALID_GRANT)

        if (authCode.codeChallenge != null) {
            if (codeVerifier == null) throw OauthException(OauthExceptionCode.INVALID_CODE_VERIFIER)
            verifyCodeChallenge(authCode.codeChallenge, codeVerifier)
        }

        tokenService.markCodeUsed(authCode)
        return issueTokenPair(authCode.userPublicId, clientId, authCode.scopes, client.trusted)
    }

    private suspend fun refreshAccessToken(refreshToken: String, clientId: String, clientSecret: String): TokenResponse {
        val client = clientService.findActiveByClientId(clientId)
        clientService.verifyClientSecret(client, clientSecret, passwordEncoder)

        val token = tokenService.findByRefreshToken(refreshToken)
            ?: throw OauthException(OauthExceptionCode.INVALID_GRANT)

        if (token.revoked) throw OauthException(OauthExceptionCode.INVALID_GRANT)
        if (token.refreshExpiresAt.isBefore(LocalDateTime.now())) throw OauthException(OauthExceptionCode.INVALID_GRANT)
        if (token.clientId != clientId) throw OauthException(OauthExceptionCode.INVALID_GRANT)

        tokenService.revokeToken(token)
        return issueTokenPair(token.userPublicId, clientId, token.scopes, client.trusted)
    }

    suspend fun revokeToken(token: String) {
        val oauthToken = tokenService.findByAccessToken(token)
            ?: tokenService.findByRefreshToken(token)
        if (oauthToken != null && !oauthToken.revoked) {
            tokenService.revokeToken(oauthToken)
        }
    }

    suspend fun introspect(token: String): IntrospectResponse {
        val claims = jwtProvider.verifyAccessToken(token)
            ?: return IntrospectResponse(active = false)

        val oauthToken = tokenService.findByAccessToken(token)
        if (oauthToken == null || oauthToken.revoked) return IntrospectResponse(active = false)

        return IntrospectResponse(
            active = true,
            scope = claims.getStringClaim("scope"),
            clientId = claims.audience?.firstOrNull(),
            sub = claims.subject,
            exp = claims.expirationTime?.time?.div(1000),
            iat = claims.issueTime?.time?.div(1000),
            tokenType = "Bearer",
        )
    }

    private suspend fun issueTokenPair(userPublicId: UUID, clientId: String, scopes: String, trusted: Boolean): TokenResponse {
        val authResult = authTokenClient.issueToken(userPublicId)
        val accessToken = jwtProvider.createAccessToken(userPublicId, clientId, scopes, authResult.roles.map { it.value }, authResult.accessToken, trusted)
        val refreshToken = jwtProvider.createRefreshToken()
        val now = LocalDateTime.now()

        tokenService.saveToken(
            OauthToken(
                accessTokenHash = TokenHashUtil.sha256(accessToken),
                accessToken = accessToken,
                refreshToken = refreshToken,
                clientId = clientId,
                userPublicId = userPublicId,
                scopes = scopes,
                accessExpiresAt = now.plusSeconds(properties.accessTokenExpirySeconds),
                refreshExpiresAt = now.plusDays(properties.refreshTokenExpiryDays),
            )
        )

        return TokenResponse(
            accessToken = accessToken,
            expiresIn = properties.accessTokenExpirySeconds,
            refreshToken = refreshToken,
            scope = scopes,
        )
    }

    private fun verifyCodeChallenge(codeChallenge: String, codeVerifier: String) {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(codeVerifier.toByteArray(StandardCharsets.US_ASCII))
        val computed = Base64.getUrlEncoder().withoutPadding().encodeToString(hash)
        if (computed != codeChallenge) throw OauthException(OauthExceptionCode.INVALID_CODE_VERIFIER)
    }
}
