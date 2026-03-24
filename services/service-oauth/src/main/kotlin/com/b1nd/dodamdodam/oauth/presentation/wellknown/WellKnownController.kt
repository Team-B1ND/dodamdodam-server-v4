package com.b1nd.dodamdodam.oauth.presentation.wellknown

import com.b1nd.dodamdodam.oauth.application.data.response.OpenIdConfigurationResponse
import com.b1nd.dodamdodam.oauth.application.usecase.OauthDiscoveryUseCase
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class WellKnownController(
    private val discoveryUseCase: OauthDiscoveryUseCase
) {

    @GetMapping("/.well-known/openid-configuration")
    suspend fun openIdConfiguration(): OpenIdConfigurationResponse =
        discoveryUseCase.getOpenIdConfiguration()

    @GetMapping("/.well-known/jwks.json")
    fun jwks(): Map<String, Any> = discoveryUseCase.getJwks()
}
