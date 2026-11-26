package com.b1nd.dodamdodam.weekendstudy.application.deadline.data.request

import java.time.DayOfWeek
import java.time.LocalTime

data class UpdateDeadlineRequest(
    val startDayOfWeek: DayOfWeek,
    val startTime: LocalTime,
    val endDayOfWeek: DayOfWeek,
    val endTime: LocalTime,
)
