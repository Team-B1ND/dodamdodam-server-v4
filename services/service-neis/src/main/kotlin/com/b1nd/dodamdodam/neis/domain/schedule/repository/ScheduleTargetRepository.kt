package com.b1nd.dodamdodam.neis.domain.schedule.repository

import com.b1nd.dodamdodam.neis.domain.schedule.entity.ScheduleEntity
import com.b1nd.dodamdodam.neis.domain.schedule.entity.ScheduleTargetEntity
import org.springframework.data.jpa.repository.JpaRepository

interface ScheduleTargetRepository : JpaRepository<ScheduleTargetEntity, Long> {
    fun findAllByScheduleIn(schedules: List<ScheduleEntity>): List<ScheduleTargetEntity>

    fun deleteAllBySchedule(schedule: ScheduleEntity)
}
