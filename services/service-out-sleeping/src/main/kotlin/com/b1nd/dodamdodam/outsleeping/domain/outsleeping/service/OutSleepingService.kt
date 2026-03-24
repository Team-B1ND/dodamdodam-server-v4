package com.b1nd.dodamdodam.outsleeping.domain.outsleeping.service

import com.b1nd.dodamdodam.outsleeping.domain.outsleeping.entity.OutSleepingEntity
import com.b1nd.dodamdodam.outsleeping.domain.outsleeping.enumeration.OutSleepingStatus
import com.b1nd.dodamdodam.outsleeping.domain.outsleeping.exception.OutSleepingAlreadyProcessedException
import com.b1nd.dodamdodam.outsleeping.domain.outsleeping.exception.OutSleepingNotFoundException
import com.b1nd.dodamdodam.outsleeping.domain.outsleeping.exception.OutSleepingNotOwnerException
import com.b1nd.dodamdodam.outsleeping.domain.outsleeping.repository.OutSleepingRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.util.UUID

@Service
class OutSleepingService(
    private val outSleepingRepository: OutSleepingRepository
) {

    fun create(outSleeping: OutSleepingEntity): OutSleepingEntity =
        outSleepingRepository.save(outSleeping)

    fun getByPublicId(publicId: UUID): OutSleepingEntity =
        outSleepingRepository.findByPublicId(publicId) ?: throw OutSleepingNotFoundException()

    fun getByUserId(userId: UUID): List<OutSleepingEntity> =
        outSleepingRepository.findAllByUserId(userId)

    fun getByDate(date: LocalDate, pageable: Pageable): Page<OutSleepingEntity> =
        outSleepingRepository.findAllByStartAtLessThanEqualAndEndAtGreaterThanEqual(date, date, pageable)

    fun getAllowedByDate(date: LocalDate, pageable: Pageable): Page<OutSleepingEntity> =
        outSleepingRepository.findAllByStatusAndStartAtLessThanEqualAndEndAtGreaterThanEqual(
            OutSleepingStatus.ALLOWED, date, date, pageable
        )

    fun validateOwner(outSleeping: OutSleepingEntity, userId: UUID) {
        if (outSleeping.userId != userId) {
            throw OutSleepingNotOwnerException()
        }
    }

    fun validatePending(outSleeping: OutSleepingEntity) {
        if (outSleeping.status != OutSleepingStatus.PENDING) {
            throw OutSleepingAlreadyProcessedException()
        }
    }

    fun delete(outSleeping: OutSleepingEntity) =
        outSleepingRepository.delete(outSleeping)
}
