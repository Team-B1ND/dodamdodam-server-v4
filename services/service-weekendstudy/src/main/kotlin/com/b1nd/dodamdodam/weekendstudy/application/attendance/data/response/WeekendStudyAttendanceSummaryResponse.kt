package com.b1nd.dodamdodam.weekendstudy.application.attendance.data.response

import com.b1nd.dodamdodam.weekendstudy.application.weekendstudy.data.response.WeekendStudyApplicantResponse

data class WeekendStudyAttendanceSummaryResponse(
    val student: WeekendStudyApplicantResponse,
    val attendance: Long,
    val late: Long,
)
