package com.b1nd.dodamdodam.neis.domain.schedule.repository

import com.b1nd.dodamdodam.neis.domain.schedule.entity.ScheduleEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.time.LocalDate

interface ScheduleQueryRepository {
    fun findSchedulesByMonth(startOfMonth: LocalDate, endOfMonth: LocalDate, pageable: Pageable): Page<ScheduleEntity>
}
