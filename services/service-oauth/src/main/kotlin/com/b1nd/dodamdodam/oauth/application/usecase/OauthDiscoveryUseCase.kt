package com.b1nd.dodamdodam.oauth.application.usecase

import com.b1nd.dodamdodam.oauth.application.data.response.OpenIdConfigurationResponse
import com.b1nd.dodamdodam.oauth.domain.scope.service.OauthScopeService
import com.b1nd.dodamdodam.oauth.infrastructure.security.properties.OauthProperties
import com.nimbusds.jose.jwk.JWKSet
import com.nimbusds.jose.jwk.RSAKey
import org.springframework.stereotype.Component

@Component
class OauthDiscoveryUseCase(
    private val properties: OauthProperties,
    private val rsaKey: RSAKey,
    private val scopeService: OauthScopeService,
) {

    suspend fun getOpenIdConfiguration(): OpenIdConfigurationResponse {
        val base = properties.issuer
        return OpenIdConfigurationResponse(
            issuer = base,
            authorizationEndpoint = "$base/oauth/authorize",
            tokenEndpoint = "$base/oauth/token",
            revocationEndpoint = "$base/oauth/token/revoke",
            introspectionEndpoint = "$base/oauth/token/introspect",
            jwksUri = "$base/oauth/.well-known/jwks.json",
            responseTypesSupported = listOf("code"),
            grantTypesSupported = listOf("authorization_code", "refresh_token"),
            tokenEndpointAuthMethodsSupported = listOf("client_secret_post"),
            scopesSupported = scopeService.findAllActiveKeys(),
            codeChallengeMethodsSupported = listOf("S256"),
            subjectTypesSupported = listOf("public"),
            idTokenSigningAlgValuesSupported = listOf("RS256"),
        )
    }

    fun getJwks(): Map<String, Any> = JWKSet(rsaKey.toPublicJWK()).toJSONObject()
}
