package com.b1nd.dodamdodam.outsleeping.domain.outsleeping.service

import com.b1nd.dodamdodam.outsleeping.domain.outsleeping.repository.OutSleepingRepository
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class OpenApiOutSleepingService(
    private val outSleepingRepository: OutSleepingRepository
) {
    fun getByDate(date: LocalDate) =
        outSleepingRepository.findAllByStartAtLessThanEqualAndEndAtGreaterThanEqual(date, date)
}