package com.b1nd.dodamdodam.nightstudy.application.deadline.data.response

import java.time.LocalTime

data class DeadlineResponse(
    val startTime: LocalTime,
    val endTime: LocalTime,
)
