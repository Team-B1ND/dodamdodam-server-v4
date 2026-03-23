package com.b1nd.dodamdodam.oauth.presentation.client

import com.b1nd.dodamdodam.core.common.data.Response
import com.b1nd.dodamdodam.core.security.annotation.authentication.UserAccess
import com.b1nd.dodamdodam.core.security.passport.PassportResolver
import com.b1nd.dodamdodam.core.security.passport.enumerations.RoleType
import com.b1nd.dodamdodam.oauth.application.data.request.RegisterClientRequest
import com.b1nd.dodamdodam.oauth.application.data.request.TransferOwnershipRequest
import com.b1nd.dodamdodam.oauth.application.data.request.UpdateClientRequest
import com.b1nd.dodamdodam.oauth.application.data.response.ClientResponse
import com.b1nd.dodamdodam.oauth.application.data.response.ScopeResponse
import com.b1nd.dodamdodam.oauth.application.usecase.OauthClientUseCase
import com.b1nd.dodamdodam.oauth.domain.client.service.OauthClientService
import com.b1nd.dodamdodam.oauth.domain.scope.repository.OauthScopeRepository
import com.b1nd.dodamdodam.oauth.infrastructure.exception.OauthException
import com.b1nd.dodamdodam.oauth.infrastructure.exception.OauthExceptionCode
import jakarta.validation.Valid
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/clients")
class ClientController(
    private val clientUseCase: OauthClientUseCase,
    private val clientService: OauthClientService,
    private val scopeRepository: OauthScopeRepository,
) {

    @PostMapping
    suspend fun register(
        @RequestHeader("X-User-Passport") passport: String,
        @Valid @RequestBody request: RegisterClientRequest,
    ): Response<ClientResponse> {
        val ownerPublicId = PassportResolver.extractUserId(passport)
        return Response.created("Client registered", clientUseCase.register(request, ownerPublicId))
    }

    @GetMapping("/{clientId}")
    suspend fun getClient(@PathVariable clientId: String): Response<ClientResponse> {
        return Response.ok("Client found", clientUseCase.getClient(clientId))
    }

    @PutMapping("/{clientId}")
    suspend fun updateClient(
        @PathVariable clientId: String,
        @Valid @RequestBody request: UpdateClientRequest,
    ): Response<ClientResponse> {
        return Response.ok("Client updated", clientUseCase.updateClient(clientId, request.clientSecret, request))
    }

    @DeleteMapping("/{clientId}")
    suspend fun deactivateClient(
        @PathVariable clientId: String,
        @RequestBody body: Map<String, String>,
    ): Response<Unit> {
        val secret = body["clientSecret"] ?: throw OauthException(OauthExceptionCode.INVALID_REQUEST)
        clientUseCase.deactivateClient(clientId, secret)
        return Response.ok("Client deactivated")
    }

    @PostMapping("/{clientId}/secret/reset")
    suspend fun resetSecret(
        @PathVariable clientId: String,
        @RequestBody body: Map<String, String>,
    ): Response<ClientResponse> {
        val secret = body["clientSecret"] ?: throw OauthException(OauthExceptionCode.INVALID_REQUEST)
        return Response.ok("Secret reset", clientUseCase.resetSecret(clientId, secret))
    }

    @PostMapping("/{clientId}/secret/owner-reset")
    suspend fun ownerResetSecret(
        @PathVariable clientId: String,
        @RequestHeader("X-User-Passport") passport: String,
    ): Response<ClientResponse> {
        val ownerPublicId = PassportResolver.extractUserId(passport)
        return Response.ok("Secret reset", clientUseCase.ownerResetSecret(clientId, ownerPublicId))
    }

    @PostMapping("/{clientId}/transfer")
    suspend fun transferOwnership(
        @PathVariable clientId: String,
        @Valid @RequestBody request: TransferOwnershipRequest,
    ): Response<ClientResponse> {
        return Response.ok("Ownership transferred", clientUseCase.transferOwnership(clientId, request.clientSecret, request.newOwnerPublicId))
    }

    @GetMapping("/scopes")
    suspend fun getScopes(): Response<List<ScopeResponse>> {
        val scopes = scopeRepository.findAllByIsActiveTrue()
            .map { ScopeResponse.of(it) }
            .toList()
        return Response.ok("Scopes found", scopes)
    }

    @UserAccess(roles = [RoleType.ADMIN])
    @PatchMapping("/{clientId}/trusted")
    suspend fun setTrusted(
        @PathVariable clientId: String,
        @RequestParam trusted: Boolean,
    ): Response<ClientResponse> {
        val client = clientService.findByClientId(clientId)
        val updated = clientService.save(client.copy(trusted = trusted))
        return Response.ok("Trusted updated", ClientResponse.of(updated))
    }
}
