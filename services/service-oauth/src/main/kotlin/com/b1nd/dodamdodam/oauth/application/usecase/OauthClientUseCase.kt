package com.b1nd.dodamdodam.oauth.application.usecase

import com.b1nd.dodamdodam.oauth.application.data.request.RegisterClientRequest
import com.b1nd.dodamdodam.oauth.application.data.request.UpdateClientRequest
import com.b1nd.dodamdodam.oauth.application.data.response.ClientResponse
import com.b1nd.dodamdodam.oauth.domain.client.entity.OauthClient
import com.b1nd.dodamdodam.oauth.domain.client.service.OauthClientService
import com.b1nd.dodamdodam.oauth.domain.scope.repository.OauthScopeRepository
import com.b1nd.dodamdodam.oauth.infrastructure.exception.OauthException
import com.b1nd.dodamdodam.oauth.infrastructure.exception.OauthExceptionCode
import kotlinx.coroutines.flow.toList
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Component
class OauthClientUseCase(
    private val clientService: OauthClientService,
    private val scopeRepository: OauthScopeRepository,
    private val passwordEncoder: PasswordEncoder,
) {

    @Transactional
    suspend fun register(request: RegisterClientRequest, ownerPublicId: UUID): ClientResponse {
        validateRedirectUris(request.redirectUris)
        validateScopes(request.scopes)

        val clientId = "dodam_${UUID.randomUUID().toString().replace("-", "").take(12)}"
        val rawSecret = "dcs_${UUID.randomUUID().toString().replace("-", "")}"
        val encodedSecret = passwordEncoder.encode(rawSecret)

        val client = clientService.save(
            OauthClient(
                clientId = clientId,
                clientSecret = encodedSecret,
                ownerPublicId = ownerPublicId,
                clientName = request.clientName,
                redirectUris = request.redirectUris.joinToString(","),
                scopes = request.scopes.joinToString(" "),
                websiteUrl = request.websiteUrl,
                description = request.description,
                logoUrl = request.logoUrl,
            )
        )

        return ClientResponse.of(client, rawSecret)
    }

    suspend fun getClient(clientId: String): ClientResponse {
        val client = clientService.findActiveByClientId(clientId)
        return ClientResponse.of(client)
    }

    @Transactional
    suspend fun updateClient(clientId: String, clientSecret: String, request: UpdateClientRequest): ClientResponse {
        val client = clientService.findByClientId(clientId)
        clientService.verifyClientSecret(client, clientSecret, passwordEncoder)
        validateRedirectUris(request.redirectUris)
        validateScopes(request.scopes)

        val updated = clientService.save(
            client.copy(
                clientName = request.clientName,
                redirectUris = request.redirectUris.joinToString(","),
                scopes = request.scopes.joinToString(" "),
                websiteUrl = request.websiteUrl,
                description = request.description,
                logoUrl = request.logoUrl,
            )
        )

        return ClientResponse.of(updated)
    }

    @Transactional
    suspend fun deactivateClient(clientId: String, clientSecret: String) {
        val client = clientService.findByClientId(clientId)
        clientService.verifyClientSecret(client, clientSecret, passwordEncoder)
        clientService.save(client.copy(isActive = false))
    }

    @Transactional
    suspend fun resetSecret(clientId: String, clientSecret: String): ClientResponse {
        val client = clientService.findByClientId(clientId)
        clientService.verifyClientSecret(client, clientSecret, passwordEncoder)

        val newRawSecret = "dcs_${UUID.randomUUID().toString().replace("-", "")}"
        val updated = clientService.save(client.copy(clientSecret = passwordEncoder.encode(newRawSecret)))

        return ClientResponse.of(updated, newRawSecret)
    }

    @Transactional
    suspend fun ownerResetSecret(clientId: String, ownerPublicId: UUID): ClientResponse {
        val client = clientService.findByClientId(clientId)
        if (client.ownerPublicId != ownerPublicId) throw OauthException(OauthExceptionCode.ACCESS_DENIED)

        val newRawSecret = "dcs_${UUID.randomUUID().toString().replace("-", "")}"
        val updated = clientService.save(client.copy(clientSecret = passwordEncoder.encode(newRawSecret)))

        return ClientResponse.of(updated, newRawSecret)
    }

    @Transactional
    suspend fun transferOwnership(clientId: String, clientSecret: String, newOwnerPublicId: UUID): ClientResponse {
        val client = clientService.findByClientId(clientId)
        clientService.verifyClientSecret(client, clientSecret, passwordEncoder)
        val updated = clientService.save(client.copy(ownerPublicId = newOwnerPublicId))
        return ClientResponse.of(updated)
    }

    private fun validateRedirectUris(uris: List<String>) {
        uris.forEach { uri ->
            val isLocalhost = uri.startsWith("http://localhost") || uri.startsWith("http://127.0.0.1")
            if (!isLocalhost && !uri.startsWith("https://")) {
                throw OauthException(OauthExceptionCode.INVALID_REDIRECT_URI)
            }
        }
    }

    private suspend fun validateScopes(scopes: List<String>) {
        val validScopes = scopeRepository.findByScopeKeyIn(scopes)
            .toList()
            .map { it.scopeKey }
            .toSet()

        if (!validScopes.containsAll(scopes)) {
            throw OauthException(OauthExceptionCode.INVALID_SCOPE)
        }
    }
}
