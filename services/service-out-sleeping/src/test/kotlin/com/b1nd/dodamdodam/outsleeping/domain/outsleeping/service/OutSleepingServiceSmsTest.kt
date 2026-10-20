package com.b1nd.dodamdodam.outsleeping.domain.outsleeping.service

import com.b1nd.dodamdodam.outsleeping.domain.outsleeping.entity.OutSleepingEntity
import com.b1nd.dodamdodam.outsleeping.domain.outsleeping.enumeration.OutSleepingReasonType
import com.b1nd.dodamdodam.outsleeping.domain.outsleeping.enumeration.OutSleepingStatusType
import com.b1nd.dodamdodam.outsleeping.domain.outsleeping.repository.OutSleepingRepository
import com.b1nd.dodamdodam.outsleeping.infrastructure.user.client.UserSmsClient
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.mockingDetails
import java.time.LocalDate
import java.util.UUID

class OutSleepingServiceSmsTest {
    private val outSleepingRepository = mock(OutSleepingRepository::class.java)

    @Test
    fun `학생 userId로 보호자 문자 발송을 요청한다`() {
        val userSmsClient = mock(UserSmsClient::class.java) { true }
        val service = OutSleepingService(outSleepingRepository, userSmsClient)
        val outSleeping = outSleeping()

        runBlocking { service.sendParentNotification(outSleeping) }

        val invocations = mockingDetails(userSmsClient).invocations
        assertEquals(1, invocations.size)
        assertEquals("sendParentSms", invocations.single().method.name)
        assertEquals(outSleeping.userId, invocations.single().arguments[0])
    }

    @Test
    fun `가비아 접수 결과를 그대로 반환한다`() {
        val acceptedClient = mock(UserSmsClient::class.java) { true }
        val rejectedClient = mock(UserSmsClient::class.java) { false }

        val accepted = runBlocking {
            OutSleepingService(outSleepingRepository, acceptedClient).sendParentNotification(outSleeping())
        }
        val rejected = runBlocking {
            OutSleepingService(outSleepingRepository, rejectedClient).sendParentNotification(outSleeping())
        }

        assertEquals(true, accepted)
        assertEquals(false, rejected)
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
