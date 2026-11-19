package com.b1nd.dodamdodam.weekendstudy.domain.attendance.repository

import com.b1nd.dodamdodam.weekendstudy.domain.attendance.enumeration.WeekendStudyAttendanceStatus
import java.time.LocalDate
import java.util.UUID

interface WeekendStudyAttendanceQueryRepository {
    fun countByUserIdAndStatus(startAt: LocalDate, endAt: LocalDate): Map<UUID, Map<WeekendStudyAttendanceStatus, Long>>
}
