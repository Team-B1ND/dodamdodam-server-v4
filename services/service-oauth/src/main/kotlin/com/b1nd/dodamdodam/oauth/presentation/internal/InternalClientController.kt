package com.b1nd.dodamdodam.oauth.presentation.internal

import com.b1nd.dodamdodam.oauth.domain.client.service.OauthClientService
import com.b1nd.dodamdodam.oauth.infrastructure.exception.OauthException
import com.b1nd.dodamdodam.oauth.infrastructure.exception.OauthExceptionCode
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import java.util.Base64

@RestController
@RequestMapping("/internal/clients")
class InternalClientController(
    private val clientService: OauthClientService,
    private val passwordEncoder: PasswordEncoder,
) {

    @PostMapping("/verify")
    suspend fun verify(
        @RequestHeader(HttpHeaders.AUTHORIZATION) authorization: String,
    ): ClientVerifyResponse {
        val credentials = parseBasicAuth(authorization)
            ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid Basic Auth")

        val client = clientService.findActiveByClientId(credentials.first)
        clientService.verifyClientSecret(client, credentials.second, passwordEncoder)

        return ClientVerifyResponse(
            clientId = client.clientId,
            scopes = client.getScopeList(),
        )
    }

    private fun parseBasicAuth(authorization: String): Pair<String, String>? {
        if (!authorization.startsWith("Basic ")) return null
        return try {
            val decoded = String(Base64.getDecoder().decode(authorization.removePrefix("Basic ").trim()))
            val colonIdx = decoded.indexOf(':')
            if (colonIdx < 0) return null
            decoded.substring(0, colonIdx) to decoded.substring(colonIdx + 1)
        } catch (e: Exception) {
            null
        }
    }

    data class ClientVerifyResponse(val clientId: String, val scopes: List<String>)
}
