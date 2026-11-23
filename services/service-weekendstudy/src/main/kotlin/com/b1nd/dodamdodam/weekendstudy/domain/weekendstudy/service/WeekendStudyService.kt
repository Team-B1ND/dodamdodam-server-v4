package com.b1nd.dodamdodam.weekendstudy.domain.weekendstudy.service

import com.b1nd.dodamdodam.weekendstudy.domain.room.entity.ProjectRoomEntity
import com.b1nd.dodamdodam.weekendstudy.domain.weekendstudy.entity.WeekendStudyEntity
import com.b1nd.dodamdodam.weekendstudy.domain.weekendstudy.entity.WeekendStudyPeriodEntity
import com.b1nd.dodamdodam.weekendstudy.domain.weekendstudy.enumeration.WeekendStudyType
import com.b1nd.dodamdodam.weekendstudy.domain.weekendstudy.exception.AttendanceAlreadyCheckedException
import com.b1nd.dodamdodam.weekendstudy.domain.weekendstudy.exception.InvalidWeekendStudyTypeException
import com.b1nd.dodamdodam.weekendstudy.domain.weekendstudy.exception.NotLeaderException
import com.b1nd.dodamdodam.weekendstudy.domain.weekendstudy.exception.NotMyWeekendStudyException
import com.b1nd.dodamdodam.weekendstudy.domain.weekendstudy.exception.PeriodOverlappedException
import com.b1nd.dodamdodam.weekendstudy.domain.weekendstudy.exception.RoomAlreadyReservedException
import com.b1nd.dodamdodam.weekendstudy.domain.weekendstudy.exception.WeekendStudyNotFoundException
import com.b1nd.dodamdodam.weekendstudy.domain.weekendstudy.exception.WeekendStudyPeriodNotFoundException
import com.b1nd.dodamdodam.weekendstudy.domain.weekendstudy.repository.WeekendStudyPeriodQueryRepository
import com.b1nd.dodamdodam.weekendstudy.domain.weekendstudy.repository.WeekendStudyPeriodRepository
import com.b1nd.dodamdodam.weekendstudy.domain.weekendstudy.repository.WeekendStudyRepository
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.util.*

@Service
class WeekendStudyService(
    private val weekendStudyRepository: WeekendStudyRepository,
    private val weekendStudyPeriodRepository: WeekendStudyPeriodRepository,
    private val weekendStudyPeriodQueryRepository: WeekendStudyPeriodQueryRepository,
) {
    fun save(weekendStudy: WeekendStudyEntity, periods: List<Int>, memberIds: List<UUID> = emptyList()) {
        weekendStudy.room?.let { validateRoomNotReserved(it, weekendStudy.date, periods, null) }
        val userIds = listOf(weekendStudy.userId) + memberIds
        userIds.forEach { validateNotOverlapped(it, weekendStudy.date, periods) }

        val saved = weekendStudyRepository.save(weekendStudy)
        savePeriods(saved, userIds, periods)
    }

    fun getMine(userId: UUID, publicId: UUID, type: WeekendStudyType? = null): WeekendStudyEntity {
        val weekendStudy = getByPublicId(publicId)
        if (weekendStudy.userId != userId) {
            if (weekendStudyPeriodRepository.existsByWeekendStudyAndUserId(weekendStudy, userId))
                throw NotLeaderException()
            throw NotMyWeekendStudyException()
        }
        if (type != null && weekendStudy.type != type)
            throw InvalidWeekendStudyTypeException()
        return weekendStudy
    }

    fun updatePersonal(weekendStudy: WeekendStudyEntity, periods: List<Int>) {
        replacePeriods(weekendStudy, listOf(weekendStudy.userId), periods)
    }

    fun updateProject(
        weekendStudy: WeekendStudyEntity,
        name: String,
        description: String,
        room: ProjectRoomEntity,
        periods: List<Int>,
        memberIds: List<UUID>,
    ) {
        validateRoomNotReserved(room, weekendStudy.date, periods, weekendStudy.id)
        weekendStudy.update(name, description, room)
        replacePeriods(weekendStudy, listOf(weekendStudy.userId) + memberIds, periods)
    }

    fun cancel(weekendStudy: WeekendStudyEntity) {
        validateAttendanceNotChecked(weekendStudy)
        weekendStudyPeriodRepository.deleteAllByWeekendStudy(weekendStudy)
        weekendStudyRepository.delete(weekendStudy)
    }

    fun getAllMyPeriods(userId: UUID, type: WeekendStudyType, date: LocalDate): List<WeekendStudyPeriodEntity> =
        weekendStudyPeriodQueryRepository.findAllByUserIdAndTypeAndDateGoe(userId, type, date)

    fun getAllPeriods(date: LocalDate, type: WeekendStudyType): List<WeekendStudyPeriodEntity> =
        weekendStudyPeriodQueryRepository.findAllByDateAndType(date, type)

    fun getPeriod(userId: UUID, date: LocalDate, period: Int): WeekendStudyPeriodEntity =
        weekendStudyPeriodRepository.findByUserIdAndDateAndPeriod(userId, date, period)
            ?: throw WeekendStudyPeriodNotFoundException()

    fun getReservedRoomPeriods(date: LocalDate): Map<Long, Set<Int>> =
        weekendStudyPeriodQueryRepository.findReservedRoomPeriodsByDate(date)
            .groupBy({ it.first }, { it.second })
            .mapValues { it.value.toSet() }

    fun getByPublicId(publicId: UUID): WeekendStudyEntity =
        weekendStudyRepository.findByPublicId(publicId)
            ?: throw WeekendStudyNotFoundException()

    private fun replacePeriods(weekendStudy: WeekendStudyEntity, userIds: List<UUID>, periods: List<Int>) {
        validateAttendanceNotChecked(weekendStudy)
        weekendStudyPeriodRepository.deleteAllByWeekendStudy(weekendStudy)
        weekendStudyPeriodRepository.flush()
        userIds.forEach { validateNotOverlapped(it, weekendStudy.date, periods) }
        savePeriods(weekendStudy, userIds, periods)
    }

    private fun savePeriods(weekendStudy: WeekendStudyEntity, userIds: List<UUID>, periods: List<Int>) {
        try {
            weekendStudyPeriodRepository.saveAll(
                userIds.flatMap { userId -> periods.map { WeekendStudyPeriodEntity(weekendStudy, userId, weekendStudy.date, it) } }
            )
        } catch (e: DataIntegrityViolationException) {
            throw PeriodOverlappedException()
        }
    }

    private fun validateAttendanceNotChecked(weekendStudy: WeekendStudyEntity) {
        if (weekendStudyPeriodQueryRepository.existsAttendanceByWeekendStudyId(weekendStudy.id!!))
            throw AttendanceAlreadyCheckedException()
    }

    private fun validateRoomNotReserved(room: ProjectRoomEntity, date: LocalDate, periods: List<Int>, excludeWeekendStudyId: Long?) {
        if (weekendStudyPeriodQueryRepository.existsReservedRoomByDateAndPeriods(room.id!!, date, periods, excludeWeekendStudyId))
            throw RoomAlreadyReservedException()
    }

    private fun validateNotOverlapped(userId: UUID, date: LocalDate, periods: List<Int>) {
        if (weekendStudyPeriodRepository.existsByUserIdAndWeekendStudyDateAndPeriodIn(userId, date, periods))
            throw PeriodOverlappedException()
    }
}
