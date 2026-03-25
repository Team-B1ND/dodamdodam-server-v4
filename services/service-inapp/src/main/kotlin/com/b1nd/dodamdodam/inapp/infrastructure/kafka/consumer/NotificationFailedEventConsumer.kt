package com.b1nd.dodamdodam.inapp.infrastructure.kafka.consumer

import com.b1nd.dodamdodam.core.kafka.constants.KafkaTopics
import com.b1nd.dodamdodam.core.kafka.event.notification.NotificationFailedEvent
import com.b1nd.dodamdodam.inapp.domain.notification.entity.NotificationFailureLogEntity
import com.b1nd.dodamdodam.inapp.domain.notification.repository.NotificationFailureLogRepository
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class NotificationFailedEventConsumer(
    private val repository: NotificationFailureLogRepository,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @KafkaListener(topics = [KafkaTopics.NOTIFICATION_FAILED], groupId = "service-inapp-notification", containerFactory = "kafkaListenerContainerFactory")
    fun consume(event: NotificationFailedEvent) {
        log.error("Notification failed: appName={}, title={}, error={}", event.appName ?: event.appPublicId, event.title, event.errorMessage)

        repository.save(
            NotificationFailureLogEntity(
                appPublicId = event.appPublicId,
                appName = event.appName,
                title = event.title,
                body = event.body,
                targetUserIds = event.targetUserPublicIds.joinToString(","),
                errorMessage = event.errorMessage,
                retryCount = event.retryCount,
                occurredAt = event.occurredAt,
            )
        )
    }
}
