package com.b1nd.dodamdodam.gateway.infrastructure.gateway.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.openapi")
data class OpenApiProperties(
    val oauthServiceUrl: String = "http://localhost:8088",
)
