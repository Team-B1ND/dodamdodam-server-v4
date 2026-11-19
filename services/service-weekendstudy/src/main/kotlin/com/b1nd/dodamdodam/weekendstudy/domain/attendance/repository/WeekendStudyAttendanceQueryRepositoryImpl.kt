package com.b1nd.dodamdodam.weekendstudy.domain.attendance.repository

import com.b1nd.dodamdodam.weekendstudy.domain.attendance.entity.QWeekendStudyAttendanceEntity.weekendStudyAttendanceEntity
import com.b1nd.dodamdodam.weekendstudy.domain.attendance.enumeration.WeekendStudyAttendanceStatus
import com.b1nd.dodamdodam.weekendstudy.domain.weekendstudy.entity.QWeekendStudyPeriodEntity.weekendStudyPeriodEntity
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.util.UUID

@Repository
class WeekendStudyAttendanceQueryRepositoryImpl(
    private val queryFactory: JPAQueryFactory
) : WeekendStudyAttendanceQueryRepository {

    override fun countByUserIdAndStatus(startAt: LocalDate, endAt: LocalDate): Map<UUID, Map<WeekendStudyAttendanceStatus, Long>> {
        val count = weekendStudyAttendanceEntity.count()
        return queryFactory
            .select(weekendStudyPeriodEntity.userId, weekendStudyAttendanceEntity.status, count)
            .from(weekendStudyAttendanceEntity)
            .join(weekendStudyAttendanceEntity.period, weekendStudyPeriodEntity)
            .where(weekendStudyPeriodEntity.date.between(startAt, endAt))
            .groupBy(weekendStudyPeriodEntity.userId, weekendStudyAttendanceEntity.status)
            .fetch()
            .groupBy(
                { it.get(weekendStudyPeriodEntity.userId)!! },
                { it.get(weekendStudyAttendanceEntity.status)!! to (it.get(count) ?: 0L) }
            )
            .mapValues { (_, counts) -> counts.toMap() }
    }
}
