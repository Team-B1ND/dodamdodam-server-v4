package com.b1nd.dodamdodam.outsleeping.application.outsleeping.data.request

import com.b1nd.dodamdodam.outsleeping.domain.outsleeping.enumeration.OutSleepingReasonType
import java.time.LocalDate

data class ModifyOutSleepingRequest(
    val reasonType: OutSleepingReasonType,
    val reason: String?,
    val startAt: LocalDate,
    val endAt: LocalDate,
)
