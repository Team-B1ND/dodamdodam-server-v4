package com.b1nd.dodamdodam.nightstudy.domain.nightstudy.policy

import com.b1nd.dodamdodam.core.common.exception.BasicException
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.exception.InvalidNightStudyTypeException
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.exception.NightStudyExceptionCode
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

object NightStudyApplicationPolicy {
    private val zone = ZoneId.of("Asia/Seoul")

    fun validate(
        startAt: LocalDate,
        period: Int,
        applicationDeadlineEnabled: Boolean,
        now: LocalDateTime = LocalDateTime.now(zone),
    ) {
        val deadline = now.toLocalDate().atTime(20, 30)

        if (applicationDeadlineEnabled && now.isAfter(deadline))
            throw BasicException(NightStudyExceptionCode.NOT_APPLICATION_TIME)
        if (startAt.isBefore(now.toLocalDate()))
            throw BasicException(NightStudyExceptionCode.INVALID_START_AT)
        if (period !in 1..2)
            throw InvalidNightStudyTypeException()
    }
}
