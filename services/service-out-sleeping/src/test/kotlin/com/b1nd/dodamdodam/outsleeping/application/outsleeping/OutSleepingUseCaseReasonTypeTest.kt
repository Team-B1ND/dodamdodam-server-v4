package com.b1nd.dodamdodam.outsleeping.application.outsleeping

import com.b1nd.dodamdodam.core.security.passport.Passport
import com.b1nd.dodamdodam.core.security.passport.PassportUserDetails
import com.b1nd.dodamdodam.core.security.passport.enumerations.RoleType
import com.b1nd.dodamdodam.outsleeping.application.outsleeping.data.request.ApplyOutSleepingRequest
import com.b1nd.dodamdodam.outsleeping.application.outsleeping.data.request.ModifyOutSleepingRequest
import com.b1nd.dodamdodam.outsleeping.domain.deadline.service.OutSleepingDeadlineService
import com.b1nd.dodamdodam.outsleeping.domain.outsleeping.entity.OutSleepingEntity
import com.b1nd.dodamdodam.outsleeping.domain.outsleeping.enumeration.OutSleepingReasonType
import com.b1nd.dodamdodam.outsleeping.domain.outsleeping.enumeration.OutSleepingStatus
import com.b1nd.dodamdodam.outsleeping.domain.outsleeping.enumeration.OutSleepingStatusType
import com.b1nd.dodamdodam.outsleeping.domain.outsleeping.exception.OutSleepingAlreadyProcessedException
import com.b1nd.dodamdodam.outsleeping.domain.outsleeping.exception.OutSleepingInvalidDateException
import com.b1nd.dodamdodam.outsleeping.domain.outsleeping.service.OutSleepingService
import com.b1nd.dodamdodam.outsleeping.infrastructure.user.client.UserQueryClient
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.doThrow
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.mockito.Mockito.mockingDetails
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import java.time.LocalDate
import java.util.UUID

class OutSleepingUseCaseReasonTypeTest {
    private val outSleepingService = mock(OutSleepingService::class.java)
    private val userQueryClient = mock(UserQueryClient::class.java)
    private val deadlineService = mock(OutSleepingDeadlineService::class.java)
    private val useCase = OutSleepingUseCase(
        outSleepingService = outSleepingService,
        userQueryClient = userQueryClient,
        deadlineService = deadlineService,
    )

    private val userId = UUID.randomUUID()
    private val startAt = LocalDate.now().plusDays(1)
    private val endAt = LocalDate.now().plusDays(2)

    @BeforeEach
    fun login() {
        val passport = Passport(
            userId = userId,
            username = "student",
            role = listOf(RoleType.STUDENT),
            enabled = true,
            os = "test",
            version = "1.0.0",
            issuedAt = 0,
            expiredAt = 0,
        )
        val context = SecurityContextHolder.createEmptyContext()
        context.authentication = UsernamePasswordAuthenticationToken(
            PassportUserDetails(passport),
            "",
            emptyList(),
        )
        SecurityContextHolder.setContext(context)
        `when`(deadlineService.validateDeadline()).thenReturn(OutSleepingStatusType.NORMAL)
    }

    @AfterEach
    fun logout() {
        SecurityContextHolder.clearContext()
    }

    @Test
    fun `외박 신청 시 요청의 reasonType이 그대로 저장된다`() {
        useCase.apply(
            ApplyOutSleepingRequest(
                reasonType = OutSleepingReasonType.INTERNSHIP,
                reason = "현장 실습",
                startAt = startAt,
                endAt = endAt,
            )
        )

        val created = createdEntities(outSleepingService).single()
        assertEquals(OutSleepingReasonType.INTERNSHIP, created.reasonType)
        assertEquals("현장 실습", created.reason)
    }

    @Test
    fun `외박 신청 시 reason 없이 reasonType만으로 신청할 수 있다`() {
        useCase.apply(
            ApplyOutSleepingRequest(
                reasonType = OutSleepingReasonType.SILICON_VALLEY,
                reason = null,
                startAt = startAt,
                endAt = endAt,
            )
        )

        val created = createdEntities(outSleepingService).single()
        assertEquals(OutSleepingReasonType.SILICON_VALLEY, created.reasonType)
        assertNull(created.reason)
    }

    @Test
    fun `모든 reasonType으로 외박을 신청할 수 있다`() {
        OutSleepingReasonType.entries.forEach { reasonType ->
            val service = mock(OutSleepingService::class.java)
            val useCase = OutSleepingUseCase(service, userQueryClient, deadlineService)

            useCase.apply(
                ApplyOutSleepingRequest(
                    reasonType = reasonType,
                    reason = null,
                    startAt = startAt,
                    endAt = endAt,
                )
            )

            assertEquals(reasonType, createdEntities(service).single().reasonType)
        }
    }

    @Test
    fun `외박 신청 날짜가 유효하지 않으면 저장하지 않는다`() {
        doThrow(OutSleepingInvalidDateException())
            .`when`(outSleepingService).validateDate(startAt, endAt)

        assertThrows(OutSleepingInvalidDateException::class.java) {
            useCase.apply(
                ApplyOutSleepingRequest(
                    reasonType = OutSleepingReasonType.PERSONAL,
                    reason = null,
                    startAt = startAt,
                    endAt = endAt,
                )
            )
        }

        assertEquals(emptyList<OutSleepingEntity>(), createdEntities(outSleepingService))
    }

    @Test
    fun `외박 수정 시 reasonType이 변경된다`() {
        val publicId = UUID.randomUUID()
        val outSleeping = outSleeping(OutSleepingReasonType.PERSONAL, "개인 사정")
        `when`(outSleepingService.getByPublicId(publicId)).thenReturn(outSleeping)

        useCase.modify(
            publicId,
            ModifyOutSleepingRequest(
                reasonType = OutSleepingReasonType.SICK_LEAVE,
                reason = "병원 진료",
                startAt = startAt,
                endAt = endAt,
            )
        )

        assertEquals(OutSleepingReasonType.SICK_LEAVE, outSleeping.reasonType)
        assertEquals("병원 진료", outSleeping.reason)
    }

    @Test
    fun `외박 수정 시 reason을 null로 지울 수 있다`() {
        val publicId = UUID.randomUUID()
        val outSleeping = outSleeping(OutSleepingReasonType.ETC, "기타 사유")
        `when`(outSleepingService.getByPublicId(publicId)).thenReturn(outSleeping)

        useCase.modify(
            publicId,
            ModifyOutSleepingRequest(
                reasonType = OutSleepingReasonType.TRAINING,
                reason = null,
                startAt = startAt,
                endAt = endAt,
            )
        )

        assertEquals(OutSleepingReasonType.TRAINING, outSleeping.reasonType)
        assertNull(outSleeping.reason)
    }

    @Test
    fun `이미 처리된 외박은 reasonType을 수정할 수 없다`() {
        val publicId = UUID.randomUUID()
        val outSleeping = outSleeping(OutSleepingReasonType.PERSONAL, "개인 사정")
        outSleeping.allow()
        `when`(outSleepingService.getByPublicId(publicId)).thenReturn(outSleeping)

        assertThrows(OutSleepingAlreadyProcessedException::class.java) {
            useCase.modify(
                publicId,
                ModifyOutSleepingRequest(
                    reasonType = OutSleepingReasonType.SICK_LEAVE,
                    reason = "병원 진료",
                    startAt = startAt,
                    endAt = endAt,
                )
            )
        }

        assertEquals(OutSleepingReasonType.PERSONAL, outSleeping.reasonType)
        assertEquals("개인 사정", outSleeping.reason)
    }

    @Test
    fun `내 외박 목록 조회 시 reasonType이 응답에 포함된다`() {
        val outSleeping = outSleeping(OutSleepingReasonType.TRAINING, null)
        outSleeping.generatePublicId()
        `when`(outSleepingService.getByUserId(userId)).thenReturn(listOf(outSleeping))

        val response = useCase.getMy()

        val my = response.data!!.single()
        assertEquals(OutSleepingReasonType.TRAINING, my.reasonType)
        assertNull(my.reason)
        assertEquals(OutSleepingStatus.PENDING, my.status)
    }

    /**
     * Mockito matcher 는 null 을 반환해 코틀린 null 검사에 걸리므로,
     * 저장된 엔티티는 mock 에 기록된 호출에서 직접 꺼낸다.
     */
    private fun createdEntities(service: OutSleepingService): List<OutSleepingEntity> =
        mockingDetails(service).invocations
            .filter { it.method.name == "create" }
            .map { it.getArgument<OutSleepingEntity>(0) }

    private fun outSleeping(reasonType: OutSleepingReasonType, reason: String?) = OutSleepingEntity(
        userId = userId,
        reason = reason,
        startAt = startAt,
        endAt = endAt,
        statusType = OutSleepingStatusType.NORMAL,
        reasonType = reasonType,
    )
}
