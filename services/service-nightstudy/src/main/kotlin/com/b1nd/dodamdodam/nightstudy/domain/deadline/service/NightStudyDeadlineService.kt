package com.b1nd.dodamdodam.nightstudy.domain.deadline.service

import com.b1nd.dodamdodam.core.common.exception.BasicException
import com.b1nd.dodamdodam.nightstudy.domain.deadline.entity.NightStudyDeadlineEntity
import com.b1nd.dodamdodam.nightstudy.domain.deadline.repository.NightStudyDeadlineRepository
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.exception.NightStudyExceptionCode
import org.springframework.stereotype.Service
import java.time.LocalTime

@Service
class NightStudyDeadlineService(
    private val deadlineRepository: NightStudyDeadlineRepository
) {

    fun get(): NightStudyDeadlineEntity? =
        deadlineRepository.findAll().firstOrNull()

    fun update(startTime: LocalTime, endTime: LocalTime): NightStudyDeadlineEntity {
        val deadline = deadlineRepository.findAll().firstOrNull()
        if (deadline != null) {
            deadline.update(startTime, endTime)
            return deadline
        }
        return deadlineRepository.save(NightStudyDeadlineEntity(startTime, endTime))
    }

    fun validateDeadline(currentTime: LocalTime) {
        val deadline = deadlineRepository.findAll().firstOrNull() ?: return

        if (!isWithinRange(currentTime, deadline))
            throw BasicException(NightStudyExceptionCode.NOT_APPLICATION_TIME)
    }

    private fun isWithinRange(currentTime: LocalTime, deadline: NightStudyDeadlineEntity): Boolean {
        val isBeforeStart = currentTime.isBefore(deadline.startTime)
        val isAfterEnd = currentTime.isAfter(deadline.endTime)

        if (!deadline.startTime.isAfter(deadline.endTime))
            return !isBeforeStart && !isAfterEnd

        return !isBeforeStart || !isAfterEnd
    }
}
