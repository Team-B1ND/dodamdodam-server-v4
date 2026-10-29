package com.b1nd.dodamdodam.outsleeping.domain.outsleeping.service

import com.b1nd.dodamdodam.outsleeping.domain.outsleeping.entity.OutSleepingEntity
import com.b1nd.dodamdodam.outsleeping.domain.outsleeping.enumeration.OutSleepingReasonType
import com.b1nd.dodamdodam.outsleeping.domain.outsleeping.enumeration.OutSleepingStatusType
import com.b1nd.dodamdodam.outsleeping.domain.outsleeping.repository.OutSleepingRepository
import com.b1nd.dodamdodam.outsleeping.infrastructure.user.client.UserSmsClient
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.mockingDetails
import java.time.LocalDate
import java.util.UUID

class OutSleepingServiceSmsTest {
    private val outSleepingRepository = mock(OutSleepingRepository::class.java)
    private val smsScope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined + CoroutineExceptionHandler { _, _ -> })

    @Test
    fun `학생 userId로 보호자 문자 발송을 요청한다`() {
        val userSmsClient = mock(UserSmsClient::class.java) { true }
        val service = OutSleepingService(outSleepingRepository, userSmsClient, smsScope)
        val outSleeping = outSleeping()

        service.sendParentNotification(outSleeping)

        val invocations = mockingDetails(userSmsClient).invocations
        assertEquals(1, invocations.size)
        assertEquals("sendParentSms", invocations.single().method.name)
        assertEquals(outSleeping.userId, invocations.single().arguments[0])
    }

    @Test
    fun `보호자 문자 문구는 들여쓰기 없이 발송한다`() {
        val userSmsClient = mock(UserSmsClient::class.java) { true }
        val service = OutSleepingService(outSleepingRepository, userSmsClient, smsScope)

        service.sendParentNotification(outSleeping())

        val message = mockingDetails(userSmsClient).invocations.single().arguments[1] as String
        assertTrue(message.lines().none { it.startsWith(" ") || it.isEmpty() })
    }

    @Test
    fun `문자가 접수되지 않아도 예외 없이 끝난다`() {
        val rejectedClient = mock(UserSmsClient::class.java) { false }
        val service = OutSleepingService(outSleepingRepository, rejectedClient, smsScope)

        assertDoesNotThrow { service.sendParentNotification(outSleeping()) }
    }

    @Test
    fun `문자 발송 중 예외가 나도 호출한 쪽으로 전파하지 않는다`() {
        val failingClient = mock(UserSmsClient::class.java) { throw IllegalStateException("gRPC 실패") }
        val service = OutSleepingService(outSleepingRepository, failingClient, smsScope)

        assertDoesNotThrow { service.sendParentNotification(outSleeping()) }
    }

    private fun outSleeping() = OutSleepingEntity(
        userId = UUID.randomUUID(),
        reason = "가족 행사",
        startAt = LocalDate.now().plusDays(1),
        endAt = LocalDate.now().plusDays(2),
        statusType = OutSleepingStatusType.NORMAL,
        reasonType = OutSleepingReasonType.values().first(),
    )
}
