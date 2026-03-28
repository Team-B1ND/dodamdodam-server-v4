package com.b1nd.dodamdodam.neis.infrastructure.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "neis")
data class NeisProperties(
    val eduOfficeCode: String,
    val schoolCode: String,
    val meal: ApiKeyConfig,
    val schedule: ApiKeyConfig,
) {
    data class ApiKeyConfig(val apiKey: String)
}
