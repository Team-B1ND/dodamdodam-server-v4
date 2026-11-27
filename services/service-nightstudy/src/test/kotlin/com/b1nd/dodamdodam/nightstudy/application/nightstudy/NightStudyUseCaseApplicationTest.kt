package com.b1nd.dodamdodam.nightstudy.application.nightstudy

import com.b1nd.dodamdodam.core.common.exception.BasicException
import com.b1nd.dodamdodam.core.security.passport.Passport
import com.b1nd.dodamdodam.core.security.passport.PassportUserDetails
import com.b1nd.dodamdodam.nightstudy.application.nightstudy.data.request.PersonalNightStudyApplyRequest
import com.b1nd.dodamdodam.nightstudy.application.nightstudy.data.request.ProjectNightStudyApplyRequest
import com.b1nd.dodamdodam.nightstudy.domain.deadline.entity.NightStudyDeadlineEntity
import com.b1nd.dodamdodam.nightstudy.domain.deadline.repository.NightStudyDeadlineRepository
import com.b1nd.dodamdodam.nightstudy.domain.deadline.service.NightStudyDeadlineService
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.entity.NightStudyEntity
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.enumeration.NightStudyType
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.exception.InvalidNightStudyTypeException
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.exception.NightStudyExceptionCode
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.service.NightStudyAttendanceService
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.service.NightStudyService
import com.b1nd.dodamdodam.nightstudy.domain.room.service.ProjectRoomService
import com.b1nd.dodamdodam.nightstudy.infrastructure.outSleeping.client.OutSleepingClient
import com.b1nd.dodamdodam.nightstudy.infrastructure.user.client.UserQueryClient
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.mockito.Mockito.CALLS_REAL_METHODS
import org.mockito.Mockito.mock
import org.mockito.Mockito.mockStatic
import org.mockito.Mockito.mockingDetails
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.Mockito.`when`
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.UUID

class NightStudyUseCaseApplicationTest {
    private val nightStudyService = mock(NightStudyService::class.java)
    private val projectRoomService = mock(ProjectRoomService::class.java)
    private val userId = UUID.randomUUID()
    private val afterDeadline = LocalDateTime.of(2026, 9, 10, 20, 31)

    @BeforeEach
    fun login() {
        val passport = Passport(
            userId = userId,
            enabled = true,
            os = "test",
            version = "1.0.0",
            issuedAt = 0,
            expiredAt = 0,
        )
        val context = SecurityContextHolder.createEmptyContext()
        context.authentication = UsernamePasswordAuthenticationToken(
            PassportUserDetails(passport), "", emptyList(),
        )
        SecurityContextHolder.setContext(context)
    }

    @AfterEach
    fun logout() {
        SecurityContextHolder.clearContext()
    }

    @ParameterizedTest
    @ValueSource(booleans = [false, true])
    fun `기본 설정에서는 마감 이후 신청을 저장하지 않는다`(project: Boolean) = at(afterDeadline) {
        val exception = assertThrows(BasicException::class.java) {
            apply(useCase(), project)
        }

        assertEquals(NightStudyExceptionCode.NOT_APPLICATION_TIME, exception.exceptionCode)
        verifyNoInteractions(nightStudyService, projectRoomService)
    }

    @ParameterizedTest
    @ValueSource(booleans = [false, true])
    fun `마감 시간을 늦추면 20시 30분 이후에도 신청을 저장한다`(project: Boolean) = at(afterDeadline) {
        apply(useCase(deadline = NightStudyDeadlineEntity(LocalTime.MIDNIGHT, LocalTime.of(21, 0))), project)

        assertSaved(project)
    }

    @ParameterizedTest
    @ValueSource(booleans = [false, true])
    fun `마감 설정이 없으면 시간 제한 없이 신청을 저장한다`(project: Boolean) = at(afterDeadline) {
        apply(useCase(deadline = null), project)

        assertSaved(project)
    }

    @ParameterizedTest
    @ValueSource(booleans = [false, true])
    fun `시작 시간 이전에는 신청을 저장하지 않는다`(project: Boolean) = at(afterDeadline.withHour(7)) {
        val exception = assertThrows(BasicException::class.java) {
            apply(useCase(deadline = NightStudyDeadlineEntity(LocalTime.of(8, 0), LocalTime.of(20, 30))), project)
        }

        assertEquals(NightStudyExceptionCode.NOT_APPLICATION_TIME, exception.exceptionCode)
        verifyNoInteractions(nightStudyService, projectRoomService)
    }

    @ParameterizedTest
    @ValueSource(booleans = [false, true])
    fun `기본 마감 설정에서도 정확히 20시 30분에는 신청할 수 있다`(project: Boolean) =
        at(afterDeadline.withMinute(30)) {
            apply(useCase(), project, period = 2)

            assertSaved(project, period = 2)
        }

    @ParameterizedTest
    @ValueSource(booleans = [false, true])
    fun `마감 시간 안이어도 과거 날짜는 신청할 수 없다`(project: Boolean) = at(afterDeadline) {
        val exception = assertThrows(BasicException::class.java) {
            apply(useCase(deadline = null), project, startAt = afterDeadline.toLocalDate().minusDays(1))
        }

        assertEquals(NightStudyExceptionCode.INVALID_START_AT, exception.exceptionCode)
        verifyNoInteractions(nightStudyService, projectRoomService)
    }

    @ParameterizedTest
    @ValueSource(booleans = [false, true])
    fun `마감 시간 안이어도 잘못된 교시는 신청할 수 없다`(project: Boolean) = at(afterDeadline) {
        for (period in listOf(0, 3)) {
            assertThrows(InvalidNightStudyTypeException::class.java) {
                apply(useCase(deadline = null), project, period = period)
            }
        }

        verifyNoInteractions(nightStudyService, projectRoomService)
    }

    private fun useCase(
        deadline: NightStudyDeadlineEntity? = NightStudyDeadlineEntity(LocalTime.MIDNIGHT, LocalTime.of(20, 30)),
    ): NightStudyUseCase {
        val deadlineRepository = mock(NightStudyDeadlineRepository::class.java)
        `when`(deadlineRepository.findAll()).thenReturn(listOfNotNull(deadline))
        return NightStudyUseCase(
            nightStudyService = nightStudyService,
            nightStudyAttendanceService = mock(NightStudyAttendanceService::class.java),
            projectRoomService = projectRoomService,
            userQueryClient = mock(UserQueryClient::class.java),
            outSleepingClient = mock(OutSleepingClient::class.java),
            deadlineService = NightStudyDeadlineService(deadlineRepository),
        )
    }

    private fun apply(
        useCase: NightStudyUseCase,
        project: Boolean,
        startAt: LocalDate = afterDeadline.toLocalDate(),
        period: Int = 1,
    ) {
        if (project) {
            useCase.applyProjectNightStudy(
                ProjectNightStudyApplyRequest(
                    name = "프로젝트", description = "개발", period = period,
                    startAt = startAt, endAt = startAt, members = emptyList(),
                )
            )
        } else {
            useCase.applyPersonalNightStudy(
                PersonalNightStudyApplyRequest(
                    description = "자습", period = period, startAt = startAt, endAt = startAt,
                    needPhone = false, needPhoneReason = null,
                )
            )
        }
    }

    private fun assertSaved(project: Boolean, period: Int = 1) {
        val invocation = mockingDetails(nightStudyService).invocations.single { it.method.name == "save" }
        val entity = invocation.arguments[0] as NightStudyEntity
        assertEquals(if (project) NightStudyType.PROJECT else NightStudyType.PERSONAL, entity.type)
        assertEquals(period, entity.period)
        assertEquals(afterDeadline.toLocalDate(), entity.startAt)
        assertEquals(userId, invocation.arguments[1])
        assertEquals(if (project) emptyList<UUID>() else null, invocation.arguments[2])
    }

    private fun at(now: LocalDateTime, block: () -> Unit) {
        mockStatic(LocalDateTime::class.java, CALLS_REAL_METHODS).use { mocked ->
            mocked.`when`<LocalDateTime> { LocalDateTime.now() }.thenReturn(now)
            block()
        }
    }
}
