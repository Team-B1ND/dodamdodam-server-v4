package com.b1nd.dodamdodam.nightstudy.infrastructure.scheduler

import com.b1nd.dodamdodam.core.kafka.constants.KafkaTopics
import com.b1nd.dodamdodam.core.kafka.event.notification.NotificationSendEvent
import com.b1nd.dodamdodam.core.kafka.producer.KafkaMessageProducer
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class NightStudyScheduler(
    private val kafkaMessageProducer: KafkaMessageProducer
) {

    @Scheduled(cron = "0 20 20 * * MON-THU")
    fun notifyApplicationDeadlineApproaching() {
        kafkaMessageProducer.send(
            KafkaTopics.NOTIFICATION_SEND,
            NotificationSendEvent(
                appName = "nightstudy",
                title = "심야자습 신청 마감 시간 임박!!!",
                body = "심야자습 신청 마감 시간이 10분 남았습니다."
            )
        )
    }
}