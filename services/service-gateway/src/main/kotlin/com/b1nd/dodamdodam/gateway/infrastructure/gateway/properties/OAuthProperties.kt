package com.b1nd.dodamdodam.gateway.infrastructure.gateway.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.oauth")
data class OAuthProperties(
    val kidPrefix: String = "dodam-oauth-",
    val skipPaths: List<String> = listOf("/oauth/token", "/oauth/authorize", "/.well-known/"),
    val scopeRules: List<ScopeRule> = emptyList(),
) {
    data class ScopeRule(
        val path: String,
        val read: String? = null,
        val write: String? = null,
    )
}
