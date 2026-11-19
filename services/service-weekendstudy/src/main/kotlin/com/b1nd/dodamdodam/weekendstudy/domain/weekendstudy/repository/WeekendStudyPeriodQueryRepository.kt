package com.b1nd.dodamdodam.weekendstudy.domain.weekendstudy.repository

import com.b1nd.dodamdodam.weekendstudy.domain.weekendstudy.entity.WeekendStudyPeriodEntity
import com.b1nd.dodamdodam.weekendstudy.domain.weekendstudy.enumeration.WeekendStudyType
import java.time.LocalDate
import java.util.UUID

interface WeekendStudyPeriodQueryRepository {
    fun findAllByUserIdAndTypeAndDateGoe(userId: UUID, type: WeekendStudyType, date: LocalDate): List<WeekendStudyPeriodEntity>
    fun findAllByDateAndType(date: LocalDate, type: WeekendStudyType): List<WeekendStudyPeriodEntity>
    fun findPeriodsByWeekendStudyId(weekendStudyId: Long): List<Int>
    fun existsAssignedRoomByDateAndPeriods(roomId: Long, date: LocalDate, periods: Collection<Int>, excludeWeekendStudyId: Long): Boolean
    fun existsAttendanceByWeekendStudyId(weekendStudyId: Long): Boolean
}
