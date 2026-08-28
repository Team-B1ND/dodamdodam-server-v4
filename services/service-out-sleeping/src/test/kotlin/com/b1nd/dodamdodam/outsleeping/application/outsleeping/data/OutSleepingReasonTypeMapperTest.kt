package com.b1nd.dodamdodam.outsleeping.application.outsleeping.data

import com.b1nd.dodamdodam.grpc.user.StudentInfo
import com.b1nd.dodamdodam.grpc.user.UserResponse
import com.b1nd.dodamdodam.outsleeping.application.outsleeping.data.request.ApplyOutSleepingRequest
import com.b1nd.dodamdodam.outsleeping.domain.outsleeping.entity.OutSleepingEntity
import com.b1nd.dodamdodam.outsleeping.domain.outsleeping.enumeration.OutSleepingReasonType
import com.b1nd.dodamdodam.outsleeping.domain.outsleeping.enumeration.OutSleepingStatusType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.util.UUID

class OutSleepingReasonTypeMapperTest {

    private val startAt = LocalDate.of(2026, 9, 1)
    private val endAt = LocalDate.of(2026, 9, 2)

    @Test
    fun `toEntity는 요청의 reasonType을 엔티티로 옮긴다`() {
        val userId = UUID.randomUUID()
        val request = ApplyOutSleepingRequest(
            reasonType = OutSleepingReasonType.INTERNSHIP,
            reason = "현장 실습",
            startAt = startAt,
            endAt = endAt,
        )

        val entity = request.toEntity(userId, OutSleepingStatusType.LATE)

        assertEquals(OutSleepingReasonType.INTERNSHIP, entity.reasonType)
        assertEquals("현장 실습", entity.reason)
        assertEquals(OutSleepingStatusType.LATE, entity.statusType)
        assertEquals(userId, entity.userId)
    }

    @Test
    fun `toEntity는 reason이 null이어도 reasonType을 유지한다`() {
        val request = ApplyOutSleepingRequest(
            reasonType = OutSleepingReasonType.SILICON_VALLEY,
            reason = null,
            startAt = startAt,
            endAt = endAt,
        )

        val entity = request.toEntity(UUID.randomUUID(), OutSleepingStatusType.NORMAL)

        assertEquals(OutSleepingReasonType.SILICON_VALLEY, entity.reasonType)
        assertNull(entity.reason)
    }

    @Test
    fun `toResponse는 reasonType을 응답에 담는다`() {
        val entity = outSleeping(OutSleepingReasonType.SICK_LEAVE, "병원 진료")
        val user = UserResponse.newBuilder()
            .setPublicId(UUID.randomUUID().toString())
            .setName("김도담")
            .setStudent(StudentInfo.newBuilder().setGrade(2).setRoom(3).setNumber(4))
            .build()

        val response = entity.toResponse(user)

        assertEquals(OutSleepingReasonType.SICK_LEAVE, response.reasonType)
        assertEquals("병원 진료", response.reason)
        assertEquals("김도담", response.student?.name)
    }

    @Test
    fun `toMyResponse는 reason이 없어도 reasonType을 담는다`() {
        val entity = outSleeping(OutSleepingReasonType.TRAINING, null)

        val response = entity.toMyResponse()

        assertEquals(OutSleepingReasonType.TRAINING, response.reasonType)
        assertNull(response.reason)
    }

    private fun outSleeping(reasonType: OutSleepingReasonType, reason: String?) =
        OutSleepingEntity(
            userId = UUID.randomUUID(),
            reason = reason,
            startAt = startAt,
            endAt = endAt,
            statusType = OutSleepingStatusType.NORMAL,
            reasonType = reasonType,
        ).apply { generatePublicId() }
}
