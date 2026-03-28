package com.b1nd.dodamdodam.neis.infrastructure.neis.data

import com.b1nd.dodamdodam.neis.domain.schedule.enums.ScheduleTarget
import java.time.LocalDate

data class ParsedSchedule(
    val date: LocalDate,
    val title: String,
    val targets: List<ScheduleTarget>,
)
