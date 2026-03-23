package com.b1nd.dodamdodam.oauth.presentation.wellknown

import com.b1nd.dodamdodam.oauth.application.data.response.OpenIdConfigurationResponse
import com.b1nd.dodamdodam.oauth.domain.scope.repository.OauthScopeRepository
import com.b1nd.dodamdodam.oauth.infrastructure.security.OauthProperties
import com.nimbusds.jose.jwk.JWKSet
import com.nimbusds.jose.jwk.RSAKey
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class WellKnownController(
    private val properties: OauthProperties,
    private val rsaKey: RSAKey,
    private val scopeRepository: OauthScopeRepository,
) {

    @GetMapping("/.well-known/openid-configuration")
    suspend fun openIdConfiguration(): OpenIdConfigurationResponse {
        val scopes = scopeRepository.findAllByIsActiveTrue()
            .map { it.scopeKey }
            .toList()

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
            scopesSupported = scopes,
            codeChallengeMethodsSupported = listOf("S256"),
            subjectTypesSupported = listOf("public"),
            idTokenSigningAlgValuesSupported = listOf("RS256"),
        )
    }

    @GetMapping("/.well-known/jwks.json")
    fun jwks(): Map<String, Any> {
        val publicKey = rsaKey.toPublicJWK()
        return JWKSet(publicKey).toJSONObject()
    }
}
