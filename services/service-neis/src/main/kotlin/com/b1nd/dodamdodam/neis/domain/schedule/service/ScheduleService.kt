package com.b1nd.dodamdodam.neis.domain.schedule.service

import com.b1nd.dodamdodam.neis.domain.schedule.entity.ScheduleEntity
import com.b1nd.dodamdodam.neis.domain.schedule.entity.ScheduleTargetEntity
import com.b1nd.dodamdodam.neis.domain.schedule.enums.ScheduleTarget
import com.b1nd.dodamdodam.neis.domain.schedule.exception.ScheduleNotFoundException
import com.b1nd.dodamdodam.neis.domain.schedule.repository.ScheduleRepository
import com.b1nd.dodamdodam.neis.domain.schedule.repository.ScheduleTargetRepository
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.util.UUID

@Service
class ScheduleService(
    private val scheduleRepository: ScheduleRepository,
    private val scheduleTargetRepository: ScheduleTargetRepository,
) {
    fun create(title: String, startAt: LocalDate, endAt: LocalDate, targets: List<ScheduleTarget>): ScheduleEntity {
        val schedule = scheduleRepository.save(ScheduleEntity(title, startAt, endAt))
        scheduleTargetRepository.saveAll(targets.map { ScheduleTargetEntity(schedule, it) })
        return schedule
    }

    fun update(publicId: UUID, title: String, startAt: LocalDate, endAt: LocalDate, targets: List<ScheduleTarget>): ScheduleEntity {
        val schedule = scheduleRepository.findByPublicId(publicId)
            ?: throw ScheduleNotFoundException()
        schedule.update(title, startAt, endAt)
        scheduleTargetRepository.deleteAllBySchedule(schedule)
        scheduleTargetRepository.saveAll(targets.map { ScheduleTargetEntity(schedule, it) })
        return scheduleRepository.save(schedule)
    }

    fun delete(publicId: UUID) {
        val schedule = scheduleRepository.findByPublicId(publicId)
            ?: throw ScheduleNotFoundException()
        scheduleRepository.delete(schedule)
    }

    fun getSchedulesByMonth(startAt: LocalDate, endAt: LocalDate): List<ScheduleEntity> =
        scheduleRepository.findAllByStartAtLessThanEqualAndEndAtGreaterThanEqualOrderByStartAtAsc(endAt, startAt)

    fun getTargetsBySchedules(schedules: List<ScheduleEntity>): Map<Long?, List<ScheduleTarget>> {
        if (schedules.isEmpty()) return emptyMap()
        return scheduleTargetRepository.findAllByScheduleIn(schedules)
            .groupBy({ it.schedule.id }, { it.target })
    }

    fun deleteAllByStartAtBetween(startAt: LocalDate, endAt: LocalDate) {
        scheduleRepository.deleteAllByStartAtBetween(startAt, endAt)
    }
}
