package com.b1nd.dodamdodam.weekendstudy.application.weekendstudy.data.response

import com.b1nd.dodamdodam.weekendstudy.domain.weekendstudy.enumeration.WeekendStudyStatusType
import java.time.LocalDate
import java.util.UUID

data class PersonalWeekendStudyResponse(
    val id: UUID,
    val date: LocalDate,
    val periods: List<Int>,
    val status: WeekendStudyStatusType,
    val rejectionReason: String?,
)
