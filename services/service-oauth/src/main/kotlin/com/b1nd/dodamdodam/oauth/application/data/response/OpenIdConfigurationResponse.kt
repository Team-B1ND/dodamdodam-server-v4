package com.b1nd.dodamdodam.oauth.application.data.response

import com.fasterxml.jackson.annotation.JsonProperty

data class OpenIdConfigurationResponse(
    val issuer: String,
    @JsonProperty("authorization_endpoint") val authorizationEndpoint: String,
    @JsonProperty("token_endpoint") val tokenEndpoint: String,
    @JsonProperty("revocation_endpoint") val revocationEndpoint: String,
    @JsonProperty("introspection_endpoint") val introspectionEndpoint: String,
    @JsonProperty("jwks_uri") val jwksUri: String,
    @JsonProperty("response_types_supported") val responseTypesSupported: List<String>,
    @JsonProperty("grant_types_supported") val grantTypesSupported: List<String>,
    @JsonProperty("token_endpoint_auth_methods_supported") val tokenEndpointAuthMethodsSupported: List<String>,
    @JsonProperty("scopes_supported") val scopesSupported: List<String>,
    @JsonProperty("code_challenge_methods_supported") val codeChallengeMethodsSupported: List<String>,
    @JsonProperty("subject_types_supported") val subjectTypesSupported: List<String>,
    @JsonProperty("id_token_signing_alg_values_supported") val idTokenSigningAlgValuesSupported: List<String>,
)
