package com.b1nd.dodamdodam.weekendstudy.domain.weekendstudy.service

import com.b1nd.dodamdodam.weekendstudy.domain.room.entity.ProjectRoomEntity
import com.b1nd.dodamdodam.weekendstudy.domain.weekendstudy.entity.WeekendStudyEntity
import com.b1nd.dodamdodam.weekendstudy.domain.weekendstudy.entity.WeekendStudyPeriodEntity
import com.b1nd.dodamdodam.weekendstudy.domain.weekendstudy.enumeration.WeekendStudyStatusType
import com.b1nd.dodamdodam.weekendstudy.domain.weekendstudy.enumeration.WeekendStudyType
import com.b1nd.dodamdodam.weekendstudy.domain.weekendstudy.exception.AlreadyAllowedException
import com.b1nd.dodamdodam.weekendstudy.domain.weekendstudy.exception.AttendanceAlreadyCheckedException
import com.b1nd.dodamdodam.weekendstudy.domain.weekendstudy.exception.NotLeaderException
import com.b1nd.dodamdodam.weekendstudy.domain.weekendstudy.exception.NotMyWeekendStudyException
import com.b1nd.dodamdodam.weekendstudy.domain.weekendstudy.exception.NotProjectWeekendStudyException
import com.b1nd.dodamdodam.weekendstudy.domain.weekendstudy.exception.PeriodOverlappedException
import com.b1nd.dodamdodam.weekendstudy.domain.weekendstudy.exception.RoomAlreadyAssignedException
import com.b1nd.dodamdodam.weekendstudy.domain.weekendstudy.exception.RoomAssignNotAllowedException
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
        val userIds = listOf(weekendStudy.userId) + memberIds
        userIds.forEach { validateNotOverlapped(it, weekendStudy.date, periods) }

        val saved = weekendStudyRepository.save(weekendStudy)
        try {
            weekendStudyPeriodRepository.saveAll(
                userIds.flatMap { userId -> periods.map { WeekendStudyPeriodEntity(saved, userId, saved.date, it) } }
            )
        } catch (e: DataIntegrityViolationException) {
            throw PeriodOverlappedException()
        }
    }

    fun allow(publicId: UUID) {
        val weekendStudy = weekendStudyRepository.findByPublicId(publicId)
            ?: throw WeekendStudyNotFoundException()
        weekendStudy.allow()
    }

    fun reject(publicId: UUID, rejectionReason: String) {
        val weekendStudy = weekendStudyRepository.findByPublicId(publicId)
            ?: throw WeekendStudyNotFoundException()
        weekendStudy.reject(rejectionReason)
    }

    fun cancel(userId: UUID, publicId: UUID) {
        val weekendStudy = getByPublicId(publicId)
        if (!weekendStudyPeriodRepository.existsByWeekendStudyAndUserId(weekendStudy, userId))
            throw NotMyWeekendStudyException()
        if (weekendStudy.type == WeekendStudyType.PROJECT && weekendStudy.userId != userId)
            throw NotLeaderException()
        if (weekendStudy.status == WeekendStudyStatusType.ALLOWED)
            throw AlreadyAllowedException()
        weekendStudyPeriodRepository.deleteAllByWeekendStudy(weekendStudy)
        weekendStudyRepository.delete(weekendStudy)
    }

    fun pending(publicId: UUID) {
        val weekendStudy = getByPublicId(publicId)
        if (weekendStudyPeriodQueryRepository.existsAttendanceByWeekendStudyId(weekendStudy.id!!))
            throw AttendanceAlreadyCheckedException()
        weekendStudy.pending()
    }

    fun getAllMyPeriods(userId: UUID, type: WeekendStudyType, date: LocalDate): List<WeekendStudyPeriodEntity> =
        weekendStudyPeriodQueryRepository.findAllByUserIdAndTypeAndDateGoe(userId, type, date)

    fun getAllPeriods(date: LocalDate, type: WeekendStudyType): List<WeekendStudyPeriodEntity> =
        weekendStudyPeriodQueryRepository.findAllByDateAndType(date, type)

    fun getPeriod(userId: UUID, date: LocalDate, period: Int): WeekendStudyPeriodEntity =
        weekendStudyPeriodRepository.findByUserIdAndDateAndPeriod(userId, date, period)
            ?: throw WeekendStudyPeriodNotFoundException()

    fun getByPublicId(publicId: UUID): WeekendStudyEntity =
        weekendStudyRepository.findByPublicId(publicId)
            ?: throw WeekendStudyNotFoundException()

    fun getPeriods(weekendStudy: WeekendStudyEntity): List<Int> =
        weekendStudyPeriodQueryRepository.findPeriodsByWeekendStudyId(weekendStudy.id!!)

    fun     assignRoom(weekendStudy: WeekendStudyEntity, room: ProjectRoomEntity, periods: List<Int>) {
        if (weekendStudy.type != WeekendStudyType.PROJECT)
            throw NotProjectWeekendStudyException()
        if (weekendStudy.status != WeekendStudyStatusType.ALLOWED)
            throw RoomAssignNotAllowedException()
        if (weekendStudyPeriodQueryRepository.existsAssignedRoomByDateAndPeriods(
                room.id!!, weekendStudy.date, periods, weekendStudy.id!!))
            throw RoomAlreadyAssignedException()
        weekendStudy.assignRoom(room)
    }

    fun unassignRoom(publicId: UUID) {
        getByPublicId(publicId).unassignRoom()
    }

    private fun validateNotOverlapped(userId: UUID, date: LocalDate, periods: List<Int>) {
        if (weekendStudyPeriodRepository.existsByUserIdAndWeekendStudyDateAndPeriodIn(userId, date, periods))
            throw PeriodOverlappedException()
    }
}
