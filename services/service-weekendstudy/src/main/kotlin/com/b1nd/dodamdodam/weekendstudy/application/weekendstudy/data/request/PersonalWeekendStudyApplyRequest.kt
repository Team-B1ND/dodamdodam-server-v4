package com.b1nd.dodamdodam.weekendstudy.application.weekendstudy.data.request

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDate

data class PersonalWeekendStudyApplyRequest(
    @JsonFormat(pattern = "yyyy-MM-dd")
    val date: LocalDate,
    val periods: List<Int>,
)
