package com.b1nd.dodamdodam.weekendstudy.domain.attendance.repository

import com.b1nd.dodamdodam.weekendstudy.domain.attendance.entity.WeekendStudyAttendanceEntity
import com.b1nd.dodamdodam.weekendstudy.domain.attendance.enumeration.WeekendStudyAttendanceStatus
import com.b1nd.dodamdodam.weekendstudy.domain.weekendstudy.entity.WeekendStudyPeriodEntity
import java.time.LocalDate
import java.util.UUID

data class WeekendStudyPeriodWithAttendance(
    val period: WeekendStudyPeriodEntity,
    val attendance: WeekendStudyAttendanceEntity?,
)

interface WeekendStudyAttendanceQueryRepository {
    fun countByUserIdAndStatus(startAt: LocalDate, endAt: LocalDate): Map<UUID, Map<WeekendStudyAttendanceStatus, Long>>
    fun findAllPeriodsWithAttendance(date: LocalDate, period: Int?): List<WeekendStudyPeriodWithAttendance>
}
