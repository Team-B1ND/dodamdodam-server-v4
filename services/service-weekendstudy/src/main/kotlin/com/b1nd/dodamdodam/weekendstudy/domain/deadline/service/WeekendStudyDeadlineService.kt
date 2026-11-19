package com.b1nd.dodamdodam.weekendstudy.domain.deadline.service

import com.b1nd.dodamdodam.weekendstudy.domain.deadline.entity.WeekendStudyDeadlineEntity
import com.b1nd.dodamdodam.weekendstudy.domain.deadline.repository.WeekendStudyDeadlineRepository
import com.b1nd.dodamdodam.weekendstudy.domain.weekendstudy.exception.NotApplicationTimeException
import org.springframework.stereotype.Service
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.LocalTime

@Service
class WeekendStudyDeadlineService(
    private val deadlineRepository: WeekendStudyDeadlineRepository
) {

    fun get(): WeekendStudyDeadlineEntity? =
        deadlineRepository.findAll().firstOrNull()

    fun update(
        startDayOfWeek: DayOfWeek,
        startTime: LocalTime,
        endDayOfWeek: DayOfWeek,
        endTime: LocalTime,
    ): WeekendStudyDeadlineEntity {
        val deadline = deadlineRepository.findAll().firstOrNull()
        if (deadline != null) {
            deadline.update(startDayOfWeek, startTime, endDayOfWeek, endTime)
            return deadline
        }
        return deadlineRepository.save(
            WeekendStudyDeadlineEntity(startDayOfWeek, startTime, endDayOfWeek, endTime)
        )
    }

    fun validateDeadline() {
        val deadline = deadlineRepository.findAll().firstOrNull() ?: return

        val now = LocalDateTime.now()
        if (!isWithinRange(now.dayOfWeek, now.toLocalTime(), deadline)) {
            throw NotApplicationTimeException()
        }
    }

    private fun isWithinRange(
        currentDay: DayOfWeek,
        currentTime: LocalTime,
        deadline: WeekendStudyDeadlineEntity,
    ): Boolean {
        val currentDayValue = currentDay.value
        val startDayValue = deadline.startDayOfWeek.value
        val endDayValue = deadline.endDayOfWeek.value

        val isBeforeStart = currentDayValue < startDayValue ||
            (currentDayValue == startDayValue && currentTime.isBefore(deadline.startTime))
        val isAfterEnd = currentDayValue > endDayValue ||
            (currentDayValue == endDayValue && !currentTime.isBefore(deadline.endTime))

        if (startDayValue <= endDayValue) {
            if (isBeforeStart) return false
            if (isAfterEnd) return false
            return true
        }

        if (isAfterEnd && isBeforeStart) return false
        return true
    }
}
