package com.b1nd.dodamdodam.oauth.infrastructure.security

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "oauth")
data class OauthProperties(
    val issuer: String,
    val accessTokenExpirySeconds: Long,
    val refreshTokenExpiryDays: Long,
    val authorizationCodeExpiryMinutes: Long,
    val rsaPrivateKey: String,
    val rsaPublicKey: String,
    val dauthClientId: String? = null,
    val dauthClientSecret: String? = null,
    val dauthRedirectUri: String? = null,
)
