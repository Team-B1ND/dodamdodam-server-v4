package com.b1nd.dodamdodam.oauth.presentation.token

import com.b1nd.dodamdodam.oauth.application.data.response.IntrospectResponse
import com.b1nd.dodamdodam.oauth.application.data.response.TokenResponse
import com.b1nd.dodamdodam.oauth.application.usecase.OauthTokenUseCase
import com.b1nd.dodamdodam.oauth.infrastructure.exception.OauthException
import com.b1nd.dodamdodam.oauth.infrastructure.exception.OauthExceptionCode
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ServerWebExchange
import kotlinx.coroutines.reactor.awaitSingle

@RestController
@RequestMapping("/token")
class TokenController(private val tokenUseCase: OauthTokenUseCase) {

    @PostMapping
    suspend fun token(exchange: ServerWebExchange): TokenResponse {
        val formData = exchange.formData.awaitSingle()
        val grantType = formData.getFirst("grant_type") ?: throw OauthException(OauthExceptionCode.INVALID_REQUEST)
        val clientId = formData.getFirst("client_id") ?: throw OauthException(OauthExceptionCode.INVALID_REQUEST)
        val clientSecret = formData.getFirst("client_secret") ?: ""
        val code = formData.getFirst("code")
        val redirectUri = formData.getFirst("redirect_uri")
        val refreshToken = formData.getFirst("refresh_token")
        val codeVerifier = formData.getFirst("code_verifier")

        return tokenUseCase.exchangeToken(grantType, code, redirectUri, clientId, clientSecret, refreshToken, codeVerifier)
    }

    @PostMapping("/revoke")
    suspend fun revoke(exchange: ServerWebExchange): ResponseEntity<Void> {
        val formData = exchange.formData.awaitSingle()
        val token = formData.getFirst("token") ?: return ResponseEntity.ok().build()
        tokenUseCase.revokeToken(token)
        return ResponseEntity.ok().build()
    }

    @PostMapping("/introspect")
    suspend fun introspect(
        @RequestHeader("Authorization") authorization: String,
        exchange: ServerWebExchange,
    ): ResponseEntity<IntrospectResponse> {
        if (!authorization.startsWith("Basic ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        }
        val formData = exchange.formData.awaitSingle()
        val token = formData.getFirst("token") ?: return ResponseEntity.badRequest().build()
        val response = tokenUseCase.introspect(token)
        return ResponseEntity.ok(response)
    }
}
