package com.b1nd.dodamdodam.weekendstudy.application.weekendstudy

import com.b1nd.dodamdodam.core.security.passport.Passport
import com.b1nd.dodamdodam.core.security.passport.PassportUserDetails
import com.b1nd.dodamdodam.weekendstudy.application.weekendstudy.data.request.PersonalWeekendStudyApplyRequest
import com.b1nd.dodamdodam.weekendstudy.domain.deadline.entity.WeekendStudyDeadlineEntity
import com.b1nd.dodamdodam.weekendstudy.domain.deadline.repository.WeekendStudyDeadlineRepository
import com.b1nd.dodamdodam.weekendstudy.domain.deadline.service.WeekendStudyDeadlineService
import com.b1nd.dodamdodam.weekendstudy.domain.room.repository.ProjectRoomBlockRepository
import com.b1nd.dodamdodam.weekendstudy.domain.room.repository.ProjectRoomRepository
import com.b1nd.dodamdodam.weekendstudy.domain.room.service.ProjectRoomBlockService
import com.b1nd.dodamdodam.weekendstudy.domain.room.service.ProjectRoomService
import com.b1nd.dodamdodam.weekendstudy.domain.weekendstudy.entity.WeekendStudyEntity
import com.b1nd.dodamdodam.weekendstudy.domain.weekendstudy.entity.WeekendStudyPeriodEntity
import com.b1nd.dodamdodam.weekendstudy.domain.weekendstudy.enumeration.WeekendStudyType
import com.b1nd.dodamdodam.weekendstudy.domain.weekendstudy.exception.EmptyPeriodException
import com.b1nd.dodamdodam.weekendstudy.domain.weekendstudy.exception.InvalidPeriodException
import com.b1nd.dodamdodam.weekendstudy.domain.weekendstudy.exception.NotApplicationTimeException
import com.b1nd.dodamdodam.weekendstudy.domain.weekendstudy.exception.NotOperatingDayException
import com.b1nd.dodamdodam.weekendstudy.domain.weekendstudy.exception.PeriodOverlappedException
import com.b1nd.dodamdodam.weekendstudy.domain.weekendstudy.repository.WeekendStudyPeriodQueryRepository
import com.b1nd.dodamdodam.weekendstudy.domain.weekendstudy.repository.WeekendStudyPeriodRepository
import com.b1nd.dodamdodam.weekendstudy.domain.weekendstudy.repository.WeekendStudyRepository
import com.b1nd.dodamdodam.weekendstudy.domain.weekendstudy.service.WeekendStudyService
import com.b1nd.dodamdodam.weekendstudy.infrastructure.user.client.UserQueryClient
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.CALLS_REAL_METHODS
import org.mockito.Mockito.`when`
import org.mockito.Mockito.doAnswer
import org.mockito.Mockito.mock
import org.mockito.Mockito.mockStatic
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.UUID

class WeekendStudyUseCaseTest {
    private val sunday = LocalDate.of(2026, 11, 22)
    private val beforeDeadline = sunday.atTime(16, 59, 59)
    private val userId = UUID.randomUUID()

    private val weekendStudyRepository = mock(WeekendStudyRepository::class.java)
    private val weekendStudyPeriodRepository = mock(WeekendStudyPeriodRepository::class.java)
    private val deadlineRepository = mock(WeekendStudyDeadlineRepository::class.java)
    private val useCase = WeekendStudyUseCase(
        WeekendStudyService(weekendStudyRepository, weekendStudyPeriodRepository, mock(WeekendStudyPeriodQueryRepository::class.java)),
        WeekendStudyDeadlineService(deadlineRepository),
        ProjectRoomService(mock(ProjectRoomRepository::class.java)),
        ProjectRoomBlockService(mock(ProjectRoomBlockRepository::class.java)),
        UserQueryClient(),
    )

    private val deadline = WeekendStudyDeadlineEntity(DayOfWeek.MONDAY, LocalTime.of(0, 0), DayOfWeek.SUNDAY, LocalTime.of(17, 0))

    @BeforeEach
    fun setUp() {
        val passport = Passport(userId = userId, enabled = true, os = "test", version = "test", issuedAt = 0, expiredAt = 0)
        SecurityContextHolder.getContext().authentication =
            UsernamePasswordAuthenticationToken(PassportUserDetails(passport), null, emptyList())
        doAnswer { it.getArgument<WeekendStudyEntity>(0) }
            .`when`(weekendStudyRepository).save(any(WeekendStudyEntity::class.java))
        `when`(deadlineRepository.findAll()).thenReturn(listOf(deadline))
    }

    @AfterEach
    fun tearDown() {
        SecurityContextHolder.clearContext()
    }

    @Test
    fun `마감 전 이번 주 일요일에 신청 1건과 고른 교시들을 대기 상태로 저장한다`() = at(beforeDeadline) {
        val response = useCase.applyPersonal(PersonalWeekendStudyApplyRequest(sunday, listOf(17, 19, 20)))

        assertEquals(201, response.status)
        val study = ArgumentCaptor.forClass(WeekendStudyEntity::class.java)
        verify(weekendStudyRepository, times(1)).save(study.capture())
        assertEquals(userId, study.value.userId)
        assertEquals(sunday, study.value.date)
        assertEquals(WeekendStudyType.PERSONAL, study.value.type)

        @Suppress("UNCHECKED_CAST")
        val periods = ArgumentCaptor.forClass(List::class.java) as ArgumentCaptor<List<WeekendStudyPeriodEntity>>
        verify(weekendStudyPeriodRepository).saveAll(periods.capture())
        assertEquals(listOf(17, 19, 20), periods.value.map { it.period })
        assertTrue(periods.value.all { it.weekendStudy === study.value && it.userId == userId })
    }

    @Test
    fun `월요일에도 그 주 일요일을 신청할 수 있다`() = at(sunday.minusDays(6).atStartOfDay()) {
        assertEquals(201, useCase.applyPersonal(PersonalWeekendStudyApplyRequest(sunday, listOf(18))).status)
    }

    @Test
    fun `마감 시각 정각부터는 신청을 막는다`() = at(sunday.atTime(17, 0)) {
        assertThrows(NotApplicationTimeException::class.java) {
            useCase.applyPersonal(PersonalWeekendStudyApplyRequest(sunday, listOf(18)))
        }
        verifyNoInteractions(weekendStudyRepository)
    }

    @Test
    fun `마감 시간을 바꾸면 바뀐 시각부터 막는다`() = at(sunday.atTime(15, 0)) {
        deadline.update(DayOfWeek.MONDAY, LocalTime.of(0, 0), DayOfWeek.SUNDAY, LocalTime.of(15, 0))

        assertThrows(NotApplicationTimeException::class.java) {
            useCase.applyPersonal(PersonalWeekendStudyApplyRequest(sunday, listOf(18)))
        }
    }

    @Test
    fun `지난 일요일은 신청할 수 없다`() = at(beforeDeadline) {
        assertThrows(NotApplicationTimeException::class.java) {
            useCase.applyPersonal(PersonalWeekendStudyApplyRequest(sunday.minusWeeks(1), listOf(18)))
        }
    }

    @Test
    fun `다음 주 일요일은 미리 신청할 수 없다`() = at(beforeDeadline) {
        assertThrows(NotApplicationTimeException::class.java) {
            useCase.applyPersonal(PersonalWeekendStudyApplyRequest(sunday.plusWeeks(1), listOf(18)))
        }
    }

    @Test
    fun `일요일이 아니면 신청할 수 없다`() = at(beforeDeadline) {
        assertThrows(NotOperatingDayException::class.java) {
            useCase.applyPersonal(PersonalWeekendStudyApplyRequest(sunday.minusDays(1), listOf(18)))
        }
    }

    @Test
    fun `교시를 하나도 고르지 않으면 막는다`() = at(beforeDeadline) {
        assertThrows(EmptyPeriodException::class.java) {
            useCase.applyPersonal(PersonalWeekendStudyApplyRequest(sunday, emptyList()))
        }
    }

    @Test
    fun `17 18 19 20 이외의 교시는 막는다`() = at(beforeDeadline) {
        assertThrows(InvalidPeriodException::class.java) {
            useCase.applyPersonal(PersonalWeekendStudyApplyRequest(sunday, listOf(16)))
        }
    }

    @Test
    fun `같은 교시를 두 번 고르면 막는다`() = at(beforeDeadline) {
        assertThrows(InvalidPeriodException::class.java) {
            useCase.applyPersonal(PersonalWeekendStudyApplyRequest(sunday, listOf(18, 18)))
        }
    }

    @Test
    fun `이미 신청한 교시가 섞여 있으면 막는다`() = at(beforeDeadline) {
        `when`(weekendStudyPeriodRepository.existsByUserIdAndWeekendStudyDateAndPeriodIn(userId, sunday, listOf(18, 19)))
            .thenReturn(true)

        assertThrows(PeriodOverlappedException::class.java) {
            useCase.applyPersonal(PersonalWeekendStudyApplyRequest(sunday, listOf(18, 19)))
        }
        verifyNoInteractions(weekendStudyRepository)
    }

    private fun at(now: LocalDateTime, block: () -> Unit) {
        mockStatic(LocalDateTime::class.java, CALLS_REAL_METHODS).use { mockedDateTime ->
            mockStatic(LocalDate::class.java, CALLS_REAL_METHODS).use { mockedDate ->
                mockedDateTime.`when`<LocalDateTime> { LocalDateTime.now() }.thenReturn(now)
                mockedDate.`when`<LocalDate> { LocalDate.now() }.thenReturn(now.toLocalDate())
                block()
            }
        }
    }
}
