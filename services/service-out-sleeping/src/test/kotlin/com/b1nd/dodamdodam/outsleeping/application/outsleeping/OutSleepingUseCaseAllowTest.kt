package com.b1nd.dodamdodam.outsleeping.application.outsleeping

import com.b1nd.dodamdodam.outsleeping.domain.deadline.service.OutSleepingDeadlineService
import com.b1nd.dodamdodam.outsleeping.domain.outsleeping.entity.OutSleepingEntity
import com.b1nd.dodamdodam.outsleeping.domain.outsleeping.enumeration.OutSleepingReasonType
import com.b1nd.dodamdodam.outsleeping.domain.outsleeping.enumeration.OutSleepingStatus
import com.b1nd.dodamdodam.outsleeping.domain.outsleeping.enumeration.OutSleepingStatusType
import com.b1nd.dodamdodam.outsleeping.domain.outsleeping.exception.OutSleepingAlreadyProcessedException
import com.b1nd.dodamdodam.outsleeping.domain.outsleeping.repository.OutSleepingRepository
import com.b1nd.dodamdodam.outsleeping.domain.outsleeping.service.OutSleepingService
import com.b1nd.dodamdodam.outsleeping.infrastructure.user.client.UserQueryClient
import com.b1nd.dodamdodam.outsleeping.infrastructure.user.client.UserSmsClient
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.mockito.Mockito.mockingDetails
import java.time.LocalDate
import java.util.UUID

class OutSleepingUseCaseAllowTest {
    private val outSleepingRepository = mock(OutSleepingRepository::class.java)
    private val userQueryClient = mock(UserQueryClient::class.java)
    private val deadlineService = mock(OutSleepingDeadlineService::class.java)
    private val smsScope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined + CoroutineExceptionHandler { _, _ -> })

    private val publicId = UUID.randomUUID()

    @Test
    fun `외박을 승인하면 ALLOWED로 바뀌고 보호자 문자를 요청한다`() {
        val userSmsClient = mock(UserSmsClient::class.java) { true }
        val outSleeping = outSleeping()
        `when`(outSleepingRepository.findByPublicId(publicId)).thenReturn(outSleeping)

        useCase(userSmsClient).allow(publicId)

        assertEquals(OutSleepingStatus.ALLOWED, outSleeping.status)
        assertEquals(1, mockingDetails(userSmsClient).invocations.size)
    }

    @Test
    fun `보호자 문자가 접수되지 않아도 외박은 승인된다`() {
        val rejectedClient = mock(UserSmsClient::class.java) { false }
        val outSleeping = outSleeping()
        `when`(outSleepingRepository.findByPublicId(publicId)).thenReturn(outSleeping)

        useCase(rejectedClient).allow(publicId)

        assertEquals(OutSleepingStatus.ALLOWED, outSleeping.status)
    }

    @Test
    fun `보호자 문자 발송 중 예외가 나도 외박은 승인된다`() {
        val failingClient = mock(UserSmsClient::class.java) { throw IllegalStateException("gRPC 실패") }
        val outSleeping = outSleeping()
        `when`(outSleepingRepository.findByPublicId(publicId)).thenReturn(outSleeping)

        useCase(failingClient).allow(publicId)

        assertEquals(OutSleepingStatus.ALLOWED, outSleeping.status)
    }

    @Test
    fun `이미 처리된 외박은 승인하지 않고 문자도 보내지 않는다`() {
        val userSmsClient = mock(UserSmsClient::class.java) { true }
        val outSleeping = outSleeping()
        outSleeping.allow()
        `when`(outSleepingRepository.findByPublicId(publicId)).thenReturn(outSleeping)

        assertThrows(OutSleepingAlreadyProcessedException::class.java) {
            useCase(userSmsClient).allow(publicId)
        }
        assertEquals(0, mockingDetails(userSmsClient).invocations.size)
    }

    private fun useCase(userSmsClient: UserSmsClient) = OutSleepingUseCase(
        outSleepingService = OutSleepingService(outSleepingRepository, userSmsClient, smsScope),
        userQueryClient = userQueryClient,
        deadlineService = deadlineService,
    )

    private fun outSleeping() = OutSleepingEntity(
        userId = UUID.randomUUID(),
        reason = "가족 행사",
        startAt = LocalDate.now().plusDays(1),
        endAt = LocalDate.now().plusDays(2),
        statusType = OutSleepingStatusType.NORMAL,
        reasonType = OutSleepingReasonType.values().first(),
    )
}
