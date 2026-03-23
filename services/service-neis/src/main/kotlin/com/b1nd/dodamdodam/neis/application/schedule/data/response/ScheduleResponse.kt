package com.b1nd.dodamdodam.neis.application.schedule.data.response

import com.b1nd.dodamdodam.neis.domain.schedule.enums.ScheduleTarget
import com.b1nd.dodamdodam.neis.domain.schedule.enums.ScheduleType
import java.time.LocalDate
import java.util.UUID

data class ScheduleResponse(
    val publicId: UUID,
    val title: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val type: ScheduleType,
    val targets: List<ScheduleTarget>,
)
