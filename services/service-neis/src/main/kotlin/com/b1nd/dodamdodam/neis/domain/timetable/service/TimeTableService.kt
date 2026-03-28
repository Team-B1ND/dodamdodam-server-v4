package com.b1nd.dodamdodam.neis.domain.timetable.service

import com.b1nd.dodamdodam.neis.domain.timetable.entity.TimeTableEntity
import com.b1nd.dodamdodam.neis.domain.timetable.repository.TimeTableRepository
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class TimeTableService(
    private val timeTableRepository: TimeTableRepository,
) {
    fun getTimeTablesByDateAndRoom(date: LocalDate, grade: Int, room: Int): List<TimeTableEntity> =
        timeTableRepository.findAllByDateAndGradeAndRoomOrderByPeriodAsc(date, grade, room)

    fun getTimeTablesByDate(date: LocalDate): List<TimeTableEntity> =
        timeTableRepository.findAllByDateOrderByGradeAscRoomAscPeriodAsc(date)

    fun getWeeklyTimeTablesByRoom(startDate: LocalDate, endDate: LocalDate, grade: Int, room: Int): List<TimeTableEntity> =
        timeTableRepository.findAllByDateBetweenAndGradeAndRoomOrderByDateAscPeriodAsc(startDate, endDate, grade, room)

    fun saveOrUpdate(date: LocalDate, grade: Int, room: Int, period: Int, subject: String, teacher: String) {
        val entity = timeTableRepository.findByDateAndGradeAndRoomAndPeriod(date, grade, room, period)
            ?.apply { updateTimeTable(subject, teacher) }
            ?: TimeTableEntity(date, grade, room, period, subject, teacher)

        timeTableRepository.save(entity)
    }

    fun deleteTimeTablesBetween(startDate: LocalDate, endDate: LocalDate) {
        timeTableRepository.deleteAllByDateBetween(startDate, endDate)
    }
}
