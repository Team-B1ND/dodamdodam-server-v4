package com.b1nd.dodamdodam.inapp.domain.notification.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "notification_failure_logs")
class NotificationFailureLogEntity(
    @Column(name = "app_public_id", length = 64)
    val appPublicId: String? = null,

    @Column(name = "app_name", length = 100)
    val appName: String? = null,

    @Column(nullable = false, length = 256)
    val title: String,

    @Column(nullable = false, columnDefinition = "TEXT")
    val body: String,

    @Column(name = "target_user_ids", columnDefinition = "TEXT")
    val targetUserIds: String? = null,

    @Column(name = "error_message", nullable = false, columnDefinition = "TEXT")
    val errorMessage: String,

    @Column(name = "retry_count", nullable = false)
    val retryCount: Int,

    @Column(name = "occurred_at", nullable = false)
    val occurredAt: LocalDateTime,
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
}
