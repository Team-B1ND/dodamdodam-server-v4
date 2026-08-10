package com.b1nd.dodamdodam.outsleeping.domain.outsleeping.service

import com.b1nd.dodamdodam.outsleeping.domain.outsleeping.exception.OutSleepingStartDateInPastException
import com.b1nd.dodamdodam.outsleeping.domain.outsleeping.repository.OutSleepingRepository
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import java.time.LocalDate

class OutSleepingServiceTest {
    private val outSleepingRepository = mock(OutSleepingRepository::class.java)
    private val outSleepingService = OutSleepingService(outSleepingRepository)

    @Test
    fun `외박 시작일이 오늘 이전이면 예외가 발생한다`() {
        val yesterday = LocalDate.now().minusDays(1)

        assertThrows(OutSleepingStartDateInPastException::class.java) {
            outSleepingService.validateStartAt(yesterday)
        }
    }

    @Test
    fun `외박 시작일이 오늘이면 신청할 수 있다`() {
        val today = LocalDate.now()

        assertDoesNotThrow {
            outSleepingService.validateStartAt(today)
        }
    }
}
