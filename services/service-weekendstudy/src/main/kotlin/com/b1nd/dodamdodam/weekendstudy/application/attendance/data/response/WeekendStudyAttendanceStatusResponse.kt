package com.b1nd.dodamdodam.weekendstudy.application.attendance.data.response

import com.b1nd.dodamdodam.weekendstudy.application.weekendstudy.data.response.WeekendStudyApplicantResponse
import com.b1nd.dodamdodam.weekendstudy.domain.attendance.enumeration.WeekendStudyAttendanceStatus
import com.b1nd.dodamdodam.weekendstudy.domain.weekendstudy.enumeration.WeekendStudyType
import java.time.LocalDateTime

data class WeekendStudyAttendanceStatusResponse(
    val student: WeekendStudyApplicantResponse,
    val period: Int,
    val type: WeekendStudyType,
    val status: WeekendStudyAttendanceStatus?,
    val checkedAt: LocalDateTime?,
    val reason: String?,
)
