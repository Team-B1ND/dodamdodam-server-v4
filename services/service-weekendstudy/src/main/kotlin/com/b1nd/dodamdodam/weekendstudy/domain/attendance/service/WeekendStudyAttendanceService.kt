package com.b1nd.dodamdodam.weekendstudy.domain.attendance.service

import com.b1nd.dodamdodam.weekendstudy.domain.attendance.entity.WeekendStudyAttendanceEntity
import com.b1nd.dodamdodam.weekendstudy.domain.attendance.enumeration.WeekendStudyAttendanceStatus
import com.b1nd.dodamdodam.weekendstudy.domain.attendance.exception.InvalidDateRangeException
import com.b1nd.dodamdodam.weekendstudy.domain.attendance.exception.NotAllowedWeekendStudyException
import com.b1nd.dodamdodam.weekendstudy.domain.attendance.repository.WeekendStudyAttendanceQueryRepository
import com.b1nd.dodamdodam.weekendstudy.domain.attendance.repository.WeekendStudyAttendanceRepository
import com.b1nd.dodamdodam.weekendstudy.domain.weekendstudy.entity.WeekendStudyPeriodEntity
import com.b1nd.dodamdodam.weekendstudy.domain.weekendstudy.enumeration.WeekendStudyStatusType
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.util.UUID

@Service
class WeekendStudyAttendanceService(
    private val attendanceRepository: WeekendStudyAttendanceRepository,
    private val attendanceQueryRepository: WeekendStudyAttendanceQueryRepository,
) {
    fun check(period: WeekendStudyPeriodEntity, status: WeekendStudyAttendanceStatus, checkerId: UUID) {
        if (period.weekendStudy.status != WeekendStudyStatusType.ALLOWED)
            throw NotAllowedWeekendStudyException()

        val attendance = attendanceRepository.findByPeriod(period)
        if (attendance == null) {
            attendanceRepository.save(WeekendStudyAttendanceEntity(period, status, checkerId))
            return
        }
        attendance.update(status, checkerId)
    }

    fun countByUserAndStatus(startAt: LocalDate, endAt: LocalDate): Map<UUID, Map<WeekendStudyAttendanceStatus, Long>> {
        if (startAt.isAfter(endAt))
            throw InvalidDateRangeException()
        return attendanceQueryRepository.countByUserIdAndStatus(startAt, endAt)
    }
}
