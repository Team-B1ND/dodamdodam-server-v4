package com.b1nd.dodamdodam.oauth.presentation.authorize

import com.b1nd.dodamdodam.core.common.data.Response
import com.b1nd.dodamdodam.oauth.application.data.request.ConsentRequest
import com.b1nd.dodamdodam.oauth.application.data.response.AuthorizeResponse
import com.b1nd.dodamdodam.oauth.application.data.response.ConsentRedirectResponse
import com.b1nd.dodamdodam.oauth.application.usecase.OauthAuthorizeUseCase
import com.b1nd.dodamdodam.oauth.infrastructure.exception.OauthException
import com.b1nd.dodamdodam.oauth.infrastructure.exception.OauthExceptionCode
import com.b1nd.dodamdodam.oauth.support.PassportParser
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/authorize")
class AuthorizeController(private val authorizeUseCase: OauthAuthorizeUseCase) {

    @GetMapping
    suspend fun authorize(
        @RequestParam("response_type") responseType: String,
        @RequestParam("client_id") clientId: String,
        @RequestParam("redirect_uri") redirectUri: String,
        @RequestParam scope: String,
        @RequestParam(required = false) state: String?,
        @RequestParam("code_challenge", required = false) codeChallenge: String?,
        @RequestParam("code_challenge_method", required = false) codeChallengeMethod: String?,
        @RequestHeader("X-User-Passport", required = false) passport: String?,
    ): Response<AuthorizeResponse> {
        val userPublicId = passport?.let {
            try { PassportParser.extractUserPublicId(it) } catch (_: Exception) { null }
        }
        val result = authorizeUseCase.authorize(responseType, clientId, redirectUri, scope, state, codeChallenge, codeChallengeMethod, userPublicId)
        return Response.ok("Authorization request validated", result)
    }

    @PostMapping("/consent")
    suspend fun consent(
        @RequestHeader("X-User-Passport") passport: String,
        @Valid @RequestBody request: ConsentRequest,
    ): Response<ConsentRedirectResponse> {
        val userPublicId = try {
            PassportParser.extractUserPublicId(passport)
        } catch (_: Exception) {
            throw OauthException(OauthExceptionCode.INVALID_REQUEST)
        }
        val result = authorizeUseCase.consent(request, userPublicId)
        return Response.ok("Consent processed", result)
    }
}
