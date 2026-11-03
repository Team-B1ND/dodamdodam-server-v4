package com.b1nd.dodamdodam.outsleeping.application.outsleeping

import com.b1nd.dodamdodam.grpc.user.StudentInfo
import com.b1nd.dodamdodam.grpc.user.UserResponse
import com.b1nd.dodamdodam.outsleeping.domain.deadline.service.OutSleepingDeadlineService
import com.b1nd.dodamdodam.outsleeping.domain.outsleeping.entity.OutSleepingEntity
import com.b1nd.dodamdodam.outsleeping.domain.outsleeping.enumeration.OutSleepingReasonType
import com.b1nd.dodamdodam.outsleeping.domain.outsleeping.enumeration.OutSleepingStatusType
import com.b1nd.dodamdodam.outsleeping.domain.outsleeping.repository.OutSleepingRepository
import com.b1nd.dodamdodam.outsleeping.domain.outsleeping.service.OutSleepingService
import com.b1nd.dodamdodam.outsleeping.infrastructure.user.client.UserQueryClient
import com.b1nd.dodamdodam.outsleeping.infrastructure.user.client.UserSmsClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.springframework.data.domain.PageRequest
import java.time.LocalDate
import java.util.UUID

class OutSleepingUseCaseGetByDateTest {
    private val outSleepingRepository = mock(OutSleepingRepository::class.java)
    private val userSmsClient = mock(UserSmsClient::class.java)
    private val deadlineService = mock(OutSleepingDeadlineService::class.java)
    private val smsScope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)

    private val date = LocalDate.now()

    @Test
    fun `학년 반 번호 순으로 정렬된다`() {
        val a = outSleeping()
        val b = outSleeping()
        val c = outSleeping()
        val d = outSleeping()
        val users = listOf(
            user(a.userId, "a", grade = 2, room = 1, number = 1),
            user(b.userId, "b", grade = 1, room = 2, number = 3),
            user(c.userId, "c", grade = 1, room = 2, number = 1),
            user(d.userId, "d", grade = 1, room = 1, number = 9),
        )
        stub(listOf(a, b, c, d))

        val result = useCase(users).getByDate(date, PageRequest.of(0, 10)).data!!

        assertEquals(listOf("d", "c", "b", "a"), result.content.map { it.student?.name })
        assertFalse(result.hasNext)
    }

    @Test
    fun `학생 정보가 없는 외박은 맨 뒤로 간다`() {
        val withoutInfo = outSleeping()
        val first = outSleeping()
        val second = outSleeping()
        val users = listOf(
            user(second.userId, "second", grade = 3, room = 4, number = 5),
            user(first.userId, "first", grade = 1, room = 1, number = 1),
        )
        stub(listOf(withoutInfo, second, first))

        val result = useCase(users).getByDate(date, PageRequest.of(0, 10)).data!!

        assertEquals(listOf("first", "second", null), result.content.map { it.student?.name })
    }

    @Test
    fun `페이지를 나눠 조회해도 전체 정렬 순서가 이어진다`() {
        val entities = (1..5).map { outSleeping() }
        val users = entities.mapIndexed { i, e ->
            user(e.userId, "n${i + 1}", grade = 1, room = 1, number = i + 1)
        }
        stub(entities.reversed())

        val firstPage = useCase(users).getByDate(date, PageRequest.of(0, 2)).data!!
        val secondPage = useCase(users).getByDate(date, PageRequest.of(1, 2)).data!!
        val lastPage = useCase(users).getByDate(date, PageRequest.of(2, 2)).data!!

        assertEquals(listOf("n1", "n2"), firstPage.content.map { it.student?.name })
        assertTrue(firstPage.hasNext)
        assertEquals(listOf("n3", "n4"), secondPage.content.map { it.student?.name })
        assertTrue(secondPage.hasNext)
        assertEquals(listOf("n5"), lastPage.content.map { it.student?.name })
        assertFalse(lastPage.hasNext)
    }

    @Test
    fun `해당 날짜의 외박이 없으면 빈 목록과 hasNext false를 돌려준다`() {
        stub(emptyList())

        val result = useCase(emptyList()).getByDate(date, PageRequest.of(0, 10)).data!!

        assertTrue(result.content.isEmpty())
        assertFalse(result.hasNext)
    }

    private fun stub(entities: List<OutSleepingEntity>) {
        `when`(outSleepingRepository.findAllByStartAtLessThanEqualAndEndAtGreaterThanEqual(date, date))
            .thenReturn(entities)
    }

    private fun useCase(users: List<UserResponse>) = OutSleepingUseCase(
        outSleepingService = OutSleepingService(outSleepingRepository, userSmsClient, smsScope),
        userQueryClient = mock(UserQueryClient::class.java) { users },
        deadlineService = deadlineService,
    )

    private fun user(userId: UUID, name: String, grade: Int, room: Int, number: Int): UserResponse =
        UserResponse.newBuilder()
            .setPublicId(userId.toString())
            .setName(name)
            .setStudent(StudentInfo.newBuilder().setGrade(grade).setRoom(room).setNumber(number))
            .build()

    private fun outSleeping() = OutSleepingEntity(
        userId = UUID.randomUUID(),
        reason = "가족 행사",
        startAt = date,
        endAt = date,
        statusType = OutSleepingStatusType.NORMAL,
        reasonType = OutSleepingReasonType.values().first(),
    ).also { it.generatePublicId() }
}
