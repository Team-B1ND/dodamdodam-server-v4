package com.b1nd.dodamdodam.weekendstudy.domain.weekendstudy.repository

import com.b1nd.dodamdodam.weekendstudy.domain.weekendstudy.entity.WeekendStudyPeriodEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDate
import java.util.UUID

interface WeekendStudyPeriodRepository : JpaRepository<WeekendStudyPeriodEntity, Long> {
    fun existsByUserIdAndWeekendStudyDateAndPeriodIn(userId: UUID, date: LocalDate, periods: Collection<Int>): Boolean
    fun findByUserIdAndDateAndPeriod(userId: UUID, date: LocalDate, period: Int): WeekendStudyPeriodEntity?
}
