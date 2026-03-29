package com.b1nd.dodamdodam.neis.domain.timetable.repository

import com.b1nd.dodamdodam.neis.domain.timetable.entity.TimeTableEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDate

interface TimeTableRepository : JpaRepository<TimeTableEntity, Long> {
    fun findAllByDateAndGradeAndRoomOrderByPeriodAsc(date: LocalDate, grade: Int, room: Int): List<TimeTableEntity>

    fun findAllByDateOrderByGradeAscRoomAscPeriodAsc(date: LocalDate): List<TimeTableEntity>

    fun findByDateAndGradeAndRoomAndPeriod(date: LocalDate, grade: Int, room: Int, period: Int): TimeTableEntity?

    fun findAllByDateBetweenAndGradeAndRoomOrderByDateAscPeriodAsc(
        startDate: LocalDate, endDate: LocalDate, grade: Int, room: Int,
    ): List<TimeTableEntity>

    fun deleteAllByDateBetween(startDate: LocalDate, endDate: LocalDate)
}
