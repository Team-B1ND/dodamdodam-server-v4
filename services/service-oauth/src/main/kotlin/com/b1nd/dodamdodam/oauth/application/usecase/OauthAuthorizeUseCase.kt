package com.b1nd.dodamdodam.oauth.application.usecase

import com.b1nd.dodamdodam.oauth.application.data.request.ConsentRequest
import com.b1nd.dodamdodam.oauth.application.data.response.AuthorizeResponse
import com.b1nd.dodamdodam.oauth.application.data.response.ConsentRedirectResponse
import com.b1nd.dodamdodam.oauth.application.data.response.ScopeResponse
import com.b1nd.dodamdodam.oauth.domain.client.service.OauthClientService
import com.b1nd.dodamdodam.oauth.domain.consent.entity.OauthConsent
import com.b1nd.dodamdodam.oauth.domain.consent.repository.OauthConsentRepository
import com.b1nd.dodamdodam.oauth.domain.scope.service.OauthScopeService
import com.b1nd.dodamdodam.oauth.domain.token.entity.OauthAuthorizationCode
import com.b1nd.dodamdodam.oauth.domain.token.service.OauthTokenService
import com.b1nd.dodamdodam.oauth.infrastructure.exception.OauthException
import com.b1nd.dodamdodam.oauth.infrastructure.exception.OauthExceptionCode
import com.b1nd.dodamdodam.oauth.infrastructure.security.properties.OauthProperties
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.util.UriComponentsBuilder
import java.security.SecureRandom
import java.time.LocalDateTime
import java.util.Base64
import java.util.UUID

@Component
class OauthAuthorizeUseCase(
    private val clientService: OauthClientService,
    private val tokenService: OauthTokenService,
    private val scopeService: OauthScopeService,
    private val consentRepository: OauthConsentRepository,
    private val properties: OauthProperties,
) {

    private val secureRandom = SecureRandom()

    suspend fun authorize(
        responseType: String,
        clientId: String,
        redirectUri: String,
        scope: String,
        state: String?,
        codeChallenge: String?,
        codeChallengeMethod: String?,
        userPublicId: UUID?,
    ): AuthorizeResponse {
        if (responseType != "code") throw OauthException(OauthExceptionCode.INVALID_REQUEST)
        if (codeChallenge != null && codeChallengeMethod != "S256") throw OauthException(OauthExceptionCode.INVALID_REQUEST)

        val client = clientService.findActiveByClientId(clientId)
        if (redirectUri !in client.getRedirectUriList()) throw OauthException(OauthExceptionCode.INVALID_REDIRECT_URI)

        val requestedScopes = scope.split(" ").filter { it.isNotBlank() }
        val allowedScopes = client.getScopeList()
        if (!allowedScopes.containsAll(requestedScopes)) throw OauthException(OauthExceptionCode.INVALID_SCOPE)

        val scopeDetails = scopeService.findByKeys(requestedScopes)
        if (scopeDetails.size != requestedScopes.size) throw OauthException(OauthExceptionCode.INVALID_SCOPE)

        val consented = if (userPublicId != null) {
            val existing = consentRepository.findByUserPublicIdAndClientId(userPublicId, clientId)
            existing != null && requestedScopes.toSet() == existing.scopes.split(" ").toSet()
        } else false

        return AuthorizeResponse(
            clientName = client.clientName,
            clientId = client.clientId,
            scopes = scopeDetails.map { ScopeResponse.of(it) },
            redirectUri = redirectUri,
            state = state,
            codeChallenge = codeChallenge,
            codeChallengeMethod = codeChallengeMethod,
            trusted = client.trusted,
            consented = consented,
        )
    }

    @Transactional
    suspend fun consent(request: ConsentRequest, userPublicId: UUID): ConsentRedirectResponse {
        if (!request.approved) {
            val uri = UriComponentsBuilder.fromUriString(request.redirectUri)
                .queryParam("error", "access_denied")
                .apply { request.state?.let { queryParam("state", it) } }
                .build().toUriString()
            return ConsentRedirectResponse(uri)
        }

        val client = clientService.findActiveByClientId(request.clientId)
        if (request.redirectUri !in client.getRedirectUriList()) throw OauthException(OauthExceptionCode.INVALID_REDIRECT_URI)

        val existing = consentRepository.findByUserPublicIdAndClientId(userPublicId, request.clientId)
        if (existing != null) {
            consentRepository.save(existing.copy(scopes = request.scope))
        } else {
            consentRepository.save(OauthConsent(userPublicId = userPublicId, clientId = request.clientId, scopes = request.scope))
        }

        val code = generateCode()
        tokenService.saveCode(
            OauthAuthorizationCode(
                code = code,
                clientId = request.clientId,
                userPublicId = userPublicId,
                redirectUri = request.redirectUri,
                scopes = request.scope,
                codeChallenge = request.codeChallenge,
                codeChallengeMethod = request.codeChallengeMethod,
                expiresAt = LocalDateTime.now().plusMinutes(properties.authorizationCodeExpiryMinutes),
            )
        )

        val uri = UriComponentsBuilder.fromUriString(request.redirectUri)
            .queryParam("code", code)
            .apply { request.state?.let { queryParam("state", it) } }
            .build().toUriString()

        return ConsentRedirectResponse(uri)
    }

    private fun generateCode(): String {
        val bytes = ByteArray(96)
        secureRandom.nextBytes(bytes)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
    }
}
