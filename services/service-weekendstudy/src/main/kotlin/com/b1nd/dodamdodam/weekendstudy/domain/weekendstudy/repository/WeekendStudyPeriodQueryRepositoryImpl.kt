package com.b1nd.dodamdodam.weekendstudy.domain.weekendstudy.repository

import com.b1nd.dodamdodam.weekendstudy.domain.attendance.entity.QWeekendStudyAttendanceEntity.weekendStudyAttendanceEntity
import com.b1nd.dodamdodam.weekendstudy.domain.room.entity.QProjectRoomEntity.projectRoomEntity
import com.b1nd.dodamdodam.weekendstudy.domain.weekendstudy.entity.QWeekendStudyEntity.weekendStudyEntity
import com.b1nd.dodamdodam.weekendstudy.domain.weekendstudy.entity.QWeekendStudyPeriodEntity.weekendStudyPeriodEntity
import com.b1nd.dodamdodam.weekendstudy.domain.weekendstudy.entity.WeekendStudyPeriodEntity
import com.b1nd.dodamdodam.weekendstudy.domain.weekendstudy.enumeration.WeekendStudyType
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.util.UUID

@Repository
class WeekendStudyPeriodQueryRepositoryImpl(
    private val queryFactory: JPAQueryFactory
) : WeekendStudyPeriodQueryRepository {

    override fun findAllByUserIdAndTypeAndDateGoe(userId: UUID, type: WeekendStudyType, date: LocalDate): List<WeekendStudyPeriodEntity> {
        return queryFactory.selectFrom(weekendStudyPeriodEntity)
            .join(weekendStudyPeriodEntity.weekendStudy, weekendStudyEntity).fetchJoin()
            .leftJoin(weekendStudyEntity.room, projectRoomEntity).fetchJoin()
            .where(
                weekendStudyPeriodEntity.userId.eq(userId),
                weekendStudyEntity.type.eq(type),
                weekendStudyPeriodEntity.date.goe(date),
            )
            .orderBy(
                weekendStudyPeriodEntity.date.asc(),
                weekendStudyPeriodEntity.period.asc(),
            )
            .fetch()
    }

    override fun findAllByDateAndType(date: LocalDate, type: WeekendStudyType): List<WeekendStudyPeriodEntity> {
        return queryFactory.selectFrom(weekendStudyPeriodEntity)
            .join(weekendStudyPeriodEntity.weekendStudy, weekendStudyEntity).fetchJoin()
            .leftJoin(weekendStudyEntity.room, projectRoomEntity).fetchJoin()
            .where(
                weekendStudyPeriodEntity.date.eq(date),
                weekendStudyEntity.type.eq(type),
            )
            .orderBy(
                weekendStudyEntity.createdAt.asc(),
                weekendStudyEntity.id.asc(),
                weekendStudyPeriodEntity.period.asc(),
            )
            .fetch()
    }

    override fun existsReservedRoomByDateAndPeriods(
        roomId: Long,
        date: LocalDate,
        periods: Collection<Int>,
        excludeWeekendStudyId: Long?,
    ): Boolean {
        return queryFactory.selectOne()
            .from(weekendStudyPeriodEntity)
            .join(weekendStudyPeriodEntity.weekendStudy, weekendStudyEntity)
            .where(
                weekendStudyEntity.room.id.eq(roomId),
                weekendStudyEntity.date.eq(date),
                excludeWeekendStudyId?.let { weekendStudyEntity.id.ne(it) },
                weekendStudyPeriodEntity.period.`in`(periods),
            )
            .fetchFirst() != null
    }

    override fun existsAttendanceByWeekendStudyId(weekendStudyId: Long): Boolean {
        return queryFactory.selectOne()
            .from(weekendStudyAttendanceEntity)
            .join(weekendStudyAttendanceEntity.period, weekendStudyPeriodEntity)
            .where(weekendStudyPeriodEntity.weekendStudy.id.eq(weekendStudyId))
            .fetchFirst() != null
    }

    override fun findReservedRoomPeriodsByDate(date: LocalDate): List<Pair<Long, Int>> {
        return queryFactory.select(weekendStudyEntity.room.id, weekendStudyPeriodEntity.period)
            .distinct()
            .from(weekendStudyPeriodEntity)
            .join(weekendStudyPeriodEntity.weekendStudy, weekendStudyEntity)
            .where(
                weekendStudyEntity.date.eq(date),
                weekendStudyEntity.room.isNotNull,
            )
            .fetch()
            .map { it.get(weekendStudyEntity.room.id)!! to it.get(weekendStudyPeriodEntity.period)!! }
    }

    override fun existsReservedRoomBetween(
        roomId: Long,
        startAt: LocalDate,
        endAt: LocalDate,
        periods: Collection<Int>,
    ): Boolean {
        return queryFactory.selectOne()
            .from(weekendStudyPeriodEntity)
            .join(weekendStudyPeriodEntity.weekendStudy, weekendStudyEntity)
            .where(
                weekendStudyEntity.room.id.eq(roomId),
                weekendStudyEntity.date.between(startAt, endAt),
                weekendStudyPeriodEntity.period.`in`(periods),
            )
            .fetchFirst() != null
    }
}
