package com.b1nd.dodamdodam.neis.domain.schedule.service

import com.b1nd.dodamdodam.neis.domain.schedule.entity.ScheduleEntity
import com.b1nd.dodamdodam.neis.domain.schedule.entity.ScheduleTargetEntity
import com.b1nd.dodamdodam.neis.domain.schedule.enums.ScheduleTarget
import com.b1nd.dodamdodam.neis.domain.schedule.enums.ScheduleType
import com.b1nd.dodamdodam.neis.domain.schedule.exception.ScheduleNotFoundException
import com.b1nd.dodamdodam.neis.domain.schedule.repository.ScheduleQueryRepository
import com.b1nd.dodamdodam.neis.domain.schedule.repository.ScheduleRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.util.UUID

@Service
class ScheduleService(
    private val scheduleRepository: ScheduleRepository,
    private val scheduleQueryRepository: ScheduleQueryRepository,
) {
    fun create(title: String, startDate: LocalDate, endDate: LocalDate, type: ScheduleType, targets: List<ScheduleTarget>): ScheduleEntity {
        val schedule = ScheduleEntity(title, startDate, endDate, type)
        targets.forEach { target ->
            schedule.targets.add(ScheduleTargetEntity(schedule, target))
        }
        return scheduleRepository.save(schedule)
    }

    fun delete(publicId: UUID) {
        val schedule = scheduleRepository.findByPublicId(publicId)
            ?: throw ScheduleNotFoundException()
        scheduleRepository.delete(schedule)
    }

    fun deleteAllNeisSchedulesByMonth(startOfMonth: LocalDate, endOfMonth: LocalDate) {
        scheduleRepository.deleteAllByTypeAndStartDateBetween(ScheduleType.NEIS, startOfMonth, endOfMonth)
    }

    fun getSchedulesByMonth(startOfMonth: LocalDate, endOfMonth: LocalDate, pageable: Pageable): Page<ScheduleEntity> =
        scheduleQueryRepository.findSchedulesByMonth(startOfMonth, endOfMonth, pageable)
}
