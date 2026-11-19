package com.b1nd.dodamdodam.weekendstudy.domain.weekendstudy.repository

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
            .leftJoin(weekendStudyEntity.wishRoom, projectRoomEntity).fetchJoin()
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
            .leftJoin(weekendStudyEntity.wishRoom, projectRoomEntity).fetchJoin()
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
}
