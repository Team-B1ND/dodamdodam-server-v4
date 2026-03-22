package com.b1nd.dodamdodam.outsleeping.domain.deadline.service

import com.b1nd.dodamdodam.outsleeping.domain.deadline.entity.OutSleepingDeadlineEntity
import com.b1nd.dodamdodam.outsleeping.domain.deadline.repository.OutSleepingDeadlineRepository
import com.b1nd.dodamdodam.outsleeping.domain.outsleeping.exception.OutSleepingDeadlineExceededException
import org.springframework.stereotype.Service
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.LocalTime

@Service
class OutSleepingDeadlineService(
    private val deadlineRepository: OutSleepingDeadlineRepository
) {

    fun get(): OutSleepingDeadlineEntity? =
        deadlineRepository.findAll().firstOrNull()

    fun update(
        startDayOfWeek: DayOfWeek,
        startTime: LocalTime,
        endDayOfWeek: DayOfWeek,
        endTime: LocalTime,
    ): OutSleepingDeadlineEntity {
        val deadline = deadlineRepository.findAll().firstOrNull()
        if (deadline != null) {
            deadline.update(startDayOfWeek, startTime, endDayOfWeek, endTime)
            return deadline
        }
        return deadlineRepository.save(
            OutSleepingDeadlineEntity(startDayOfWeek, startTime, endDayOfWeek, endTime)
        )
    }

    fun validateDeadline() {
        val deadline = deadlineRepository.findAll().firstOrNull() ?: return

        val now = LocalDateTime.now()
        if (!isWithinRange(now.dayOfWeek, now.toLocalTime(), deadline)) {
            throw OutSleepingDeadlineExceededException()
        }
    }

    private fun isWithinRange(
        currentDay: DayOfWeek,
        currentTime: LocalTime,
        deadline: OutSleepingDeadlineEntity,
    ): Boolean {
        val currentDayValue = currentDay.value
        val startDayValue = deadline.startDayOfWeek.value
        val endDayValue = deadline.endDayOfWeek.value

        val isWithinSameWeek = startDayValue <= endDayValue
        val isBeforeStart = currentDayValue < startDayValue ||
            (currentDayValue == startDayValue && currentTime.isBefore(deadline.startTime))
        val isAfterEnd = currentDayValue > endDayValue ||
            (currentDayValue == endDayValue && currentTime.isAfter(deadline.endTime))

        if (isWithinSameWeek) {
            return !isBeforeStart && !isAfterEnd
        }

        return !isAfterEnd || !isBeforeStart
    }
}
