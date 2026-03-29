package com.b1nd.dodamdodam.core.kafka.event.notification

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.time.LocalDateTime

@JsonIgnoreProperties(ignoreUnknown = true)
data class NotificationFailedEvent(
    val appPublicId: String? = null,
    val appName: String? = null,
    val title: String,
    val body: String,
    val targetUserPublicIds: List<String> = emptyList(),
    val errorMessage: String,
    val retryCount: Int,
    val occurredAt: LocalDateTime = LocalDateTime.now(),
)
