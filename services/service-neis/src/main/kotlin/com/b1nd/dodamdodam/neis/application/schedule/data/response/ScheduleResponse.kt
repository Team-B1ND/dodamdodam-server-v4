package com.b1nd.dodamdodam.neis.application.schedule.data.response

import com.b1nd.dodamdodam.neis.domain.schedule.enums.ScheduleTarget
import java.time.LocalDate
import java.util.UUID

data class ScheduleResponse(
    val publicId: UUID,
    val title: String,
    val startAt: LocalDate,
    val endAt: LocalDate,
    val targets: List<ScheduleTarget>,
)
