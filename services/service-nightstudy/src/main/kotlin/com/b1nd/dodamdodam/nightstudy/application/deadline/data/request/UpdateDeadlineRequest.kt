package com.b1nd.dodamdodam.nightstudy.application.deadline.data.request

import java.time.LocalTime

data class UpdateDeadlineRequest(
    val startTime: LocalTime,
    val endTime: LocalTime,
)
