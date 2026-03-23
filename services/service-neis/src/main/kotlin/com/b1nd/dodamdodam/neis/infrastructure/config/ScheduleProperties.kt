package com.b1nd.dodamdodam.neis.infrastructure.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "schedule")
data class ScheduleProperties(
    val neisApiKey: String,
    val eduOfficeCode: String,
    val schoolCode: String,
)
