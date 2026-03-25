package com.b1nd.dodamdodam.core.kafka.event.notification

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.time.LocalDateTime

@JsonIgnoreProperties(ignoreUnknown = true)
data class NotificationSendEvent(
    val appPublicId: String? = null,
    val appName: String? = null,
    val title: String,
    val body: String,
    val targetUserPublicIds: List<String> = emptyList(),
    val data: Map<String, String> = emptyMap(),
    val occurredAt: LocalDateTime = LocalDateTime.now(),
)
