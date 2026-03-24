package com.b1nd.dodamdodam.oauth.presentation.client

import com.b1nd.dodamdodam.core.common.data.Response
import com.b1nd.dodamdodam.core.security.annotation.authentication.UserAccess
import com.b1nd.dodamdodam.core.security.passport.PassportResolver
import com.b1nd.dodamdodam.oauth.application.data.request.RegisterClientRequest
import com.b1nd.dodamdodam.oauth.application.data.request.TransferOwnershipRequest
import com.b1nd.dodamdodam.oauth.application.data.request.UpdateClientRequest
import com.b1nd.dodamdodam.oauth.application.data.response.ClientResponse
import com.b1nd.dodamdodam.oauth.application.data.response.ScopeResponse
import com.b1nd.dodamdodam.oauth.application.usecase.OauthClientUseCase
import com.b1nd.dodamdodam.oauth.domain.client.service.OauthClientService
import com.b1nd.dodamdodam.oauth.domain.scope.service.OauthScopeService
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
    private val scopeService: OauthScopeService,
) {

    @PostMapping
    suspend fun register(
        @RequestHeader("X-User-Passport") passport: String,
        @Valid @RequestBody request: RegisterClientRequest,
    ): Response<ClientResponse> {
        val ownerPublicId = PassportResolver.extractUserId(passport)
        return Response.created("클라이언트가 등록되었어요.", clientUseCase.register(request, ownerPublicId))
    }

    @UserAccess(hasAnyRoleOnly = true)
    @GetMapping("/me")
    suspend fun getMyClients(
        @RequestHeader("X-User-Passport") passport: String,
    ): Response<List<ClientResponse>> {
        val ownerPublicId = PassportResolver.extractUserId(passport)
        val clients = clientService.findAllByOwner(ownerPublicId)
            .map { ClientResponse.of(it) }
            .toList()
        return Response.ok("내 클라이언트 목록을 조회했어요.", clients)
    }

    @GetMapping("/{clientId}")
    suspend fun getClient(@PathVariable clientId: String): Response<ClientResponse> {
        return Response.ok("클라이언트를 조회했어요.", clientUseCase.getClient(clientId))
    }

    @PutMapping("/{clientId}")
    suspend fun updateClient(
        @PathVariable clientId: String,
        @Valid @RequestBody request: UpdateClientRequest,
    ): Response<ClientResponse> {
        return Response.ok("클라이언트가 수정되었어요.", clientUseCase.updateClient(clientId, request.clientSecret, request))
    }

    @DeleteMapping("/{clientId}")
    suspend fun deactivateClient(
        @PathVariable clientId: String,
        @RequestBody body: Map<String, String>,
    ): Response<Unit> {
        val secret = body["clientSecret"] ?: throw OauthException(OauthExceptionCode.INVALID_REQUEST)
        clientUseCase.deactivateClient(clientId, secret)
        return Response.ok("클라이언트가 비활성화되었어요.")
    }

    @PostMapping("/{clientId}/secret/reset")
    suspend fun resetSecret(
        @PathVariable clientId: String,
        @RequestBody body: Map<String, String>,
    ): Response<ClientResponse> {
        val secret = body["clientSecret"] ?: throw OauthException(OauthExceptionCode.INVALID_REQUEST)
        return Response.ok("시크릿이 재발급되었어요.", clientUseCase.resetSecret(clientId, secret))
    }

    @PostMapping("/{clientId}/secret/owner-reset")
    suspend fun ownerResetSecret(
        @PathVariable clientId: String,
        @RequestHeader("X-User-Passport") passport: String,
    ): Response<ClientResponse> {
        val ownerPublicId = PassportResolver.extractUserId(passport)
        return Response.ok("시크릿이 재발급되었어요.", clientUseCase.ownerResetSecret(clientId, ownerPublicId))
    }

    @PostMapping("/{clientId}/transfer")
    suspend fun transferOwnership(
        @PathVariable clientId: String,
        @Valid @RequestBody request: TransferOwnershipRequest,
    ): Response<ClientResponse> {
        return Response.ok("소유권이 이전되었어요.", clientUseCase.transferOwnership(clientId, request.clientSecret, request.newOwnerPublicId))
    }

    @GetMapping("/scopes")
    suspend fun getScopes(): Response<List<ScopeResponse>> {
        val scopes = scopeService.findAllActive()
            .map { ScopeResponse.of(it) }
            .toList()
        return Response.ok("스코프 목록을 조회했어요.", scopes)
    }
}
