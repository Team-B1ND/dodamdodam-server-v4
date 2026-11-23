package com.b1nd.dodamdodam.weekendstudy.application.weekendstudy.data.response

import java.time.LocalDate
import java.util.UUID

data class PersonalWeekendStudyResponse(
    val id: UUID,
    val date: LocalDate,
    val periods: List<Int>,
)
