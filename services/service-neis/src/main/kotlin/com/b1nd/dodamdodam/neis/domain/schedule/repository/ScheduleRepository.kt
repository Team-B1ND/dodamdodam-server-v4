package com.b1nd.dodamdodam.neis.domain.schedule.repository

import com.b1nd.dodamdodam.neis.domain.schedule.entity.ScheduleEntity
import com.b1nd.dodamdodam.neis.domain.schedule.enums.ScheduleType
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.LocalDate
import java.util.UUID

interface ScheduleRepository : JpaRepository<ScheduleEntity, Long> {
    fun findByPublicId(publicId: UUID): ScheduleEntity?

    fun deleteAllByTypeAndStartDateBetween(type: ScheduleType, startDate: LocalDate, endDate: LocalDate)

    @Query("SELECT s FROM ScheduleEntity s WHERE s.startDate <= :endOfMonth AND s.endDate >= :startOfMonth ORDER BY s.startDate ASC")
    fun findSchedulesByMonth(startOfMonth: LocalDate, endOfMonth: LocalDate, pageable: Pageable): Page<ScheduleEntity>
}
