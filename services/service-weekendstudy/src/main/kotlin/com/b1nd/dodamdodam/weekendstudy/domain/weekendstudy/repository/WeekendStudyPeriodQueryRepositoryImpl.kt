package com.b1nd.dodamdodam.weekendstudy.domain.weekendstudy.repository

import com.b1nd.dodamdodam.weekendstudy.domain.attendance.entity.QWeekendStudyAttendanceEntity.weekendStudyAttendanceEntity
import com.b1nd.dodamdodam.weekendstudy.domain.room.entity.QProjectRoomEntity
import com.b1nd.dodamdodam.weekendstudy.domain.room.entity.QProjectRoomEntity.projectRoomEntity
import com.b1nd.dodamdodam.weekendstudy.domain.weekendstudy.entity.QWeekendStudyEntity.weekendStudyEntity
import com.b1nd.dodamdodam.weekendstudy.domain.weekendstudy.entity.QWeekendStudyPeriodEntity.weekendStudyPeriodEntity
import com.b1nd.dodamdodam.weekendstudy.domain.weekendstudy.entity.WeekendStudyPeriodEntity
import com.b1nd.dodamdodam.weekendstudy.domain.weekendstudy.enumeration.WeekendStudyStatusType
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
        val assignedRoom = QProjectRoomEntity("assignedRoom")
        return queryFactory.selectFrom(weekendStudyPeriodEntity)
            .join(weekendStudyPeriodEntity.weekendStudy, weekendStudyEntity).fetchJoin()
            .leftJoin(weekendStudyEntity.wishRoom, projectRoomEntity).fetchJoin()
            .leftJoin(weekendStudyEntity.room, assignedRoom).fetchJoin()
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
        val assignedRoom = QProjectRoomEntity("assignedRoom")
        return queryFactory.selectFrom(weekendStudyPeriodEntity)
            .join(weekendStudyPeriodEntity.weekendStudy, weekendStudyEntity).fetchJoin()
            .leftJoin(weekendStudyEntity.wishRoom, projectRoomEntity).fetchJoin()
            .leftJoin(weekendStudyEntity.room, assignedRoom).fetchJoin()
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

    override fun findPeriodsByWeekendStudyId(weekendStudyId: Long): List<Int> {
        return queryFactory.select(weekendStudyPeriodEntity.period)
            .distinct()
            .from(weekendStudyPeriodEntity)
            .where(weekendStudyPeriodEntity.weekendStudy.id.eq(weekendStudyId))
            .orderBy(weekendStudyPeriodEntity.period.asc())
            .fetch()
    }

    override fun existsAssignedRoomByDateAndPeriods(
        roomId: Long,
        date: LocalDate,
        periods: Collection<Int>,
        excludeWeekendStudyId: Long,
    ): Boolean {
        return queryFactory.selectOne()
            .from(weekendStudyPeriodEntity)
            .join(weekendStudyPeriodEntity.weekendStudy, weekendStudyEntity)
            .where(
                weekendStudyEntity.room.id.eq(roomId),
                weekendStudyEntity.date.eq(date),
                weekendStudyEntity.status.eq(WeekendStudyStatusType.ALLOWED),
                weekendStudyEntity.id.ne(excludeWeekendStudyId),
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
}
