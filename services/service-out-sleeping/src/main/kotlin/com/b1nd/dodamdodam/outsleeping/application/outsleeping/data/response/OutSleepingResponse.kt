package com.b1nd.dodamdodam.outsleeping.application.outsleeping.data.response

import com.b1nd.dodamdodam.outsleeping.domain.outsleeping.enumeration.OutSleepingStatus
import java.time.LocalDate
import java.util.UUID

data class OutSleepingResponse(
    val publicId: UUID,
    val reason: String,
    val status: OutSleepingStatus,
    val student: StudentResponse?,
    val startAt: LocalDate,
    val endAt: LocalDate,
)

data class DeniedOutSleepingResponse(
    val publicId: UUID,
    val reason: String,
    val status: OutSleepingStatus,
    val student: StudentResponse?,
    val denyReason: String?,
    val startAt: LocalDate,
    val endAt: LocalDate,
)
