package com.b1nd.dodamdodam.neis.infrastructure.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "neis")
data class NeisProperties(
    val apiKey: String,
    val eduOfficeCode: String,
    val schoolCode: String,
)
