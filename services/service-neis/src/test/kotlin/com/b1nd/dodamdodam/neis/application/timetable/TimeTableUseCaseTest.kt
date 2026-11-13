package com.b1nd.dodamdodam.neis.application.timetable

import com.b1nd.dodamdodam.core.security.passport.Passport
import com.b1nd.dodamdodam.core.security.passport.PassportUserDetails
import com.b1nd.dodamdodam.grpc.user.StudentInfo
import com.b1nd.dodamdodam.grpc.user.UserResponse
import com.b1nd.dodamdodam.neis.domain.timetable.entity.TimeTableEntity
import com.b1nd.dodamdodam.neis.domain.timetable.service.TimeTableService
import com.b1nd.dodamdodam.neis.infrastructure.comcigan.ComciganClient
import com.b1nd.dodamdodam.neis.infrastructure.user.UserQueryClient
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID

class TimeTableUseCaseTest {
    private val service = mock(TimeTableService::class.java)
    private val client = mock(ComciganClient::class.java)
    private val users = mock(UserQueryClient::class.java)
    private val useCase = TimeTableUseCase(service, client, users)

    @AfterEach
    fun cleanup() = SecurityContextHolder.clearContext()

    @Test
    fun `로그인 학생의 학년과 반으로 한국 시간 기준 이번 주 시간표를 조회한다`() = runBlocking {
        val id = UUID.randomUUID()
        val passport = Passport(userId = id, enabled = true, os = "test", version = "test", issuedAt = 0, expiredAt = 0)
        SecurityContextHolder.getContext().authentication = UsernamePasswordAuthenticationToken(PassportUserDetails(passport), "", emptyList())
        `when`(users.getUser(id.toString())).thenReturn(UserResponse.newBuilder()
            .setStudent(StudentInfo.newBuilder().setGrade(2).setRoom(3).setNumber(17)).build())
        val monday = LocalDate.now(ZoneId.of("Asia/Seoul")).with(DayOfWeek.MONDAY)
        `when`(service.getWeeklyTimeTablesByRoom(monday, monday.plusDays(4), 2, 3))
            .thenReturn(listOf(TimeTableEntity(monday, 2, 3, 1, "웹프", "김경")))

        val response = useCase.getMyWeeklyTimeTables()

        assertEquals("웹프", response.data!!.single().subject)
        verify(service).getWeeklyTimeTablesByRoom(monday, monday.plusDays(4), 2, 3)
        verifyNoInteractions(client)
    }

    @Test
    fun `컴시간 실패 시 저장된 시간표를 수정하지 않는다`() {
        val monday = LocalDate.of(2026, 11, 9)
        `when`(client.fetchWeeklyTimeTables(monday)).thenThrow(IllegalStateException("unavailable"))
        assertThrows(IllegalStateException::class.java) { useCase.syncWeeklyTimeTables(monday) }
        verifyNoInteractions(service)
    }
}
