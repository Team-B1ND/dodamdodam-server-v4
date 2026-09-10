package com.b1nd.dodamdodam.nightstudy.domain.nightstudy.policy

import com.b1nd.dodamdodam.core.common.exception.BasicException
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.exception.InvalidNightStudyTypeException
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.exception.NightStudyExceptionCode
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class NightStudyApplicationPolicyTest {
    private val afterDeadline = LocalDateTime.of(2026, 9, 10, 20, 31)

    @Test
    fun `신청 마감 제한이 활성화되면 20시 30분 이후 신청할 수 없다`() {
        val exception = assertThrows(BasicException::class.java) {
            NightStudyApplicationPolicy.validate(
                startAt = afterDeadline.toLocalDate(),
                period = 1,
                applicationDeadlineEnabled = true,
                now = afterDeadline,
            )
        }

        assertEquals(NightStudyExceptionCode.NOT_APPLICATION_TIME, exception.exceptionCode)
    }

    @Test
    fun `신청 마감 제한이 비활성화되면 20시 30분 이후에도 신청할 수 있다`() {
        assertDoesNotThrow {
            NightStudyApplicationPolicy.validate(
                startAt = afterDeadline.toLocalDate(),
                period = 1,
                applicationDeadlineEnabled = false,
                now = afterDeadline,
            )
        }
    }

    @Test
    fun `신청 마감 제한이 비활성화되어도 과거 날짜는 신청할 수 없다`() {
        val exception = assertThrows(BasicException::class.java) {
            NightStudyApplicationPolicy.validate(
                startAt = afterDeadline.toLocalDate().minusDays(1),
                period = 1,
                applicationDeadlineEnabled = false,
                now = afterDeadline,
            )
        }

        assertEquals(NightStudyExceptionCode.INVALID_START_AT, exception.exceptionCode)
    }

    @Test
    fun `신청 마감 제한이 비활성화되어도 잘못된 교시는 신청할 수 없다`() {
        assertThrows(InvalidNightStudyTypeException::class.java) {
            NightStudyApplicationPolicy.validate(
                startAt = afterDeadline.toLocalDate(),
                period = 3,
                applicationDeadlineEnabled = false,
                now = afterDeadline,
            )
        }
    }
}
