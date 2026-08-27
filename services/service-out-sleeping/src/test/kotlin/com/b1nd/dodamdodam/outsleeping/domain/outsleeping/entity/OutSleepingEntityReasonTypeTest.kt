package com.b1nd.dodamdodam.outsleeping.domain.outsleeping.entity

import com.b1nd.dodamdodam.outsleeping.domain.outsleeping.enumeration.OutSleepingReasonType
import com.b1nd.dodamdodam.outsleeping.domain.outsleeping.enumeration.OutSleepingStatusType
import com.b1nd.dodamdodam.outsleeping.domain.outsleeping.exception.OutSleepingAlreadyProcessedException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.util.UUID

class OutSleepingEntityReasonTypeTest {

    private val startAt = LocalDate.of(2026, 9, 1)
    private val endAt = LocalDate.of(2026, 9, 2)

    @Test
    fun `update는 reasonType과 reason을 함께 변경한다`() {
        val outSleeping = outSleeping(OutSleepingReasonType.PERSONAL, "개인 사정")

        outSleeping.update(
            reason = "병원 진료",
            startAt = startAt.plusDays(1),
            endAt = endAt.plusDays(1),
            type = OutSleepingStatusType.LATE,
            reasonType = OutSleepingReasonType.SICK_LEAVE,
        )

        assertEquals(OutSleepingReasonType.SICK_LEAVE, outSleeping.reasonType)
        assertEquals("병원 진료", outSleeping.reason)
        assertEquals(OutSleepingStatusType.LATE, outSleeping.statusType)
        assertEquals(startAt.plusDays(1), outSleeping.startAt)
        assertEquals(endAt.plusDays(1), outSleeping.endAt)
    }

    @Test
    fun `update로 reason을 null로 만들어도 reasonType은 남는다`() {
        val outSleeping = outSleeping(OutSleepingReasonType.ETC, "기타 사유")

        outSleeping.update(
            reason = null,
            startAt = startAt,
            endAt = endAt,
            type = OutSleepingStatusType.NORMAL,
            reasonType = OutSleepingReasonType.TRAINING,
        )

        assertNull(outSleeping.reason)
        assertEquals(OutSleepingReasonType.TRAINING, outSleeping.reasonType)
    }

    @Test
    fun `승인된 외박은 reasonType을 변경할 수 없다`() {
        val outSleeping = outSleeping(OutSleepingReasonType.PERSONAL, "개인 사정")
        outSleeping.allow()

        assertThrows(OutSleepingAlreadyProcessedException::class.java) {
            outSleeping.update(
                reason = "병원 진료",
                startAt = startAt,
                endAt = endAt,
                type = OutSleepingStatusType.NORMAL,
                reasonType = OutSleepingReasonType.SICK_LEAVE,
            )
        }

        assertEquals(OutSleepingReasonType.PERSONAL, outSleeping.reasonType)
    }

    @Test
    fun `거절된 외박은 reasonType을 변경할 수 없다`() {
        val outSleeping = outSleeping(OutSleepingReasonType.PERSONAL, "개인 사정")
        outSleeping.deny("사유 부족")

        assertThrows(OutSleepingAlreadyProcessedException::class.java) {
            outSleeping.update(
                reason = null,
                startAt = startAt,
                endAt = endAt,
                type = OutSleepingStatusType.NORMAL,
                reasonType = OutSleepingReasonType.INTERNSHIP,
            )
        }

        assertEquals(OutSleepingReasonType.PERSONAL, outSleeping.reasonType)
    }

    private fun outSleeping(reasonType: OutSleepingReasonType, reason: String?) = OutSleepingEntity(
        userId = UUID.randomUUID(),
        reason = reason,
        startAt = startAt,
        endAt = endAt,
        statusType = OutSleepingStatusType.NORMAL,
        reasonType = reasonType,
    )
}
