package com.b1nd.dodamdodam.oauth.presentation.client

import com.b1nd.dodamdodam.core.common.data.Response
import com.b1nd.dodamdodam.oauth.application.data.response.ClientResponse
import com.b1nd.dodamdodam.oauth.domain.client.service.OauthClientService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/admin")
class AdminController(private val clientService: OauthClientService) {

    @PatchMapping("/clients/{clientId}/trusted")
    suspend fun setTrusted(
        @PathVariable clientId: String,
        @RequestParam trusted: Boolean,
    ): Response<ClientResponse> {
        val client = clientService.findByClientId(clientId)
        val updated = clientService.save(client.copy(trusted = trusted))
        return Response.ok("Trusted updated", ClientResponse.of(updated))
    }
}
