package com.b1nd.dodamdodam.weekendstudy.application.attendance.data.request

import com.b1nd.dodamdodam.weekendstudy.domain.attendance.enumeration.WeekendStudyAttendanceStatus
import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDate

data class CheckWeekendStudyAttendanceRequest(
    @JsonFormat(pattern = "yyyy-MM-dd")
    val date: LocalDate,
    val period: Int,
    val status: WeekendStudyAttendanceStatus,
    val reason: String? = null,
)
