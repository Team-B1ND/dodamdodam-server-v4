package com.b1nd.dodamdodam.neis.domain.schedule.repository

import com.b1nd.dodamdodam.neis.domain.schedule.entity.QScheduleEntity.scheduleEntity
import com.b1nd.dodamdodam.neis.domain.schedule.entity.ScheduleEntity
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
class ScheduleQueryRepositoryImpl(
    private val queryFactory: JPAQueryFactory
) : ScheduleQueryRepository {

    override fun findSchedulesByMonth(startOfMonth: LocalDate, endOfMonth: LocalDate, pageable: Pageable): Page<ScheduleEntity> {
        val content = queryFactory
            .selectFrom(scheduleEntity)
            .where(
                scheduleEntity.startDate.loe(endOfMonth),
                scheduleEntity.endDate.goe(startOfMonth),
            )
            .orderBy(scheduleEntity.startDate.asc())
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong())
            .fetch()

        val total = queryFactory
            .select(scheduleEntity.count())
            .from(scheduleEntity)
            .where(
                scheduleEntity.startDate.loe(endOfMonth),
                scheduleEntity.endDate.goe(startOfMonth),
            )
            .fetchOne() ?: 0L

        return PageImpl(content, pageable, total)
    }
}
