package com.b1nd.dodamdodam.weekendstudy.domain.weekendstudy.service

import com.b1nd.dodamdodam.weekendstudy.domain.weekendstudy.entity.WeekendStudyEntity
import com.b1nd.dodamdodam.weekendstudy.domain.weekendstudy.entity.WeekendStudyPeriodEntity
import com.b1nd.dodamdodam.weekendstudy.domain.weekendstudy.enumeration.WeekendStudyType
import com.b1nd.dodamdodam.weekendstudy.domain.weekendstudy.exception.PeriodOverlappedException
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

    fun getAllMyPeriods(userId: UUID, type: WeekendStudyType, date: LocalDate): List<WeekendStudyPeriodEntity> =
        weekendStudyPeriodQueryRepository.findAllByUserIdAndTypeAndDateGoe(userId, type, date)

    fun getAllPeriods(date: LocalDate, type: WeekendStudyType): List<WeekendStudyPeriodEntity> =
        weekendStudyPeriodQueryRepository.findAllByDateAndType(date, type)

    fun getPeriod(userId: UUID, date: LocalDate, period: Int): WeekendStudyPeriodEntity =
        weekendStudyPeriodRepository.findByUserIdAndDateAndPeriod(userId, date, period)
            ?: throw WeekendStudyPeriodNotFoundException()

    private fun validateNotOverlapped(userId: UUID, date: LocalDate, periods: List<Int>) {
        if (weekendStudyPeriodRepository.existsByUserIdAndWeekendStudyDateAndPeriodIn(userId, date, periods))
            throw PeriodOverlappedException()
    }
}
