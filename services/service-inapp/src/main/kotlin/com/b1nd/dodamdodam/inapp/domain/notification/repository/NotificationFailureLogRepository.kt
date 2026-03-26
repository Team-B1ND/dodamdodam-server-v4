package com.b1nd.dodamdodam.inapp.domain.notification.repository

import com.b1nd.dodamdodam.inapp.domain.notification.entity.NotificationFailureLogEntity
import org.springframework.data.jpa.repository.JpaRepository

interface NotificationFailureLogRepository : JpaRepository<NotificationFailureLogEntity, Long>
