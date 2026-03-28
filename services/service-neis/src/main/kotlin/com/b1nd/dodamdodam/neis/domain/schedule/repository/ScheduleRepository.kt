package com.b1nd.dodamdodam.neis.domain.schedule.repository

import com.b1nd.dodamdodam.neis.domain.schedule.entity.ScheduleEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDate
import java.util.UUID

interface ScheduleRepository : JpaRepository<ScheduleEntity, Long> {
    fun findByPublicId(publicId: UUID): ScheduleEntity?

    fun findAllByStartAtLessThanEqualAndEndAtGreaterThanEqualOrderByStartAtAsc(endAt: LocalDate, startAt: LocalDate): List<ScheduleEntity>

    fun deleteAllByStartAtBetween(startAt: LocalDate, endAt: LocalDate)
}
