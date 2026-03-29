package com.b1nd.dodamdodam.gateway.infrastructure.gateway.filter.data

data class OAuthClaims(
    val innerToken: String,
    val clientId: String,
    val scopes: Set<String>,
)
