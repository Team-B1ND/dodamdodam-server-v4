package com.b1nd.dodamdodam.weekendstudy.application.attendance.data.request

import org.springframework.format.annotation.DateTimeFormat
import java.time.LocalDate

data class GetWeekendStudyAttendanceSummaryRequest(
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    val startAt: LocalDate,
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    val endAt: LocalDate,
)
