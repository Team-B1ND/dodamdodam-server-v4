package com.b1nd.dodamdodam.neis.domain.schedule.repository

import com.b1nd.dodamdodam.neis.domain.schedule.entity.ScheduleEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.LocalDate
import java.util.UUID

interface ScheduleRepository : JpaRepository<ScheduleEntity, Long> {
    fun findByPublicId(publicId: UUID): ScheduleEntity?

    @Query("SELECT s FROM ScheduleEntity s WHERE s.startAt <= :endAt AND s.endAt >= :startAt ORDER BY s.startAt ASC")
    fun findAllByMonth(startAt: LocalDate, endAt: LocalDate): List<ScheduleEntity>

    fun deleteAllByStartAtBetween(startAt: LocalDate, endAt: LocalDate)
}
