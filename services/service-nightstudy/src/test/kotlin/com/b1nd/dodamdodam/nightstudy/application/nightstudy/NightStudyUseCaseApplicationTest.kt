package com.b1nd.dodamdodam.nightstudy.application.nightstudy

import com.b1nd.dodamdodam.core.common.exception.BasicException
import com.b1nd.dodamdodam.core.security.passport.Passport
import com.b1nd.dodamdodam.core.security.passport.PassportUserDetails
import com.b1nd.dodamdodam.nightstudy.application.nightstudy.data.request.PersonalNightStudyApplyRequest
import com.b1nd.dodamdodam.nightstudy.application.nightstudy.data.request.ProjectNightStudyApplyRequest
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
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
    fun `마감 제한을 비활성화하면 마감 이후에도 신청을 저장한다`(project: Boolean) = at(afterDeadline) {
        apply(useCase(applicationDeadlineEnabled = false), project)

        assertSaved(project)
    }

    @ParameterizedTest
    @ValueSource(booleans = [false, true])
    fun `마감 제한이 활성화되어도 정확히 20시 30분에는 신청할 수 있다`(project: Boolean) =
        at(afterDeadline.withMinute(30)) {
            apply(useCase(), project, period = 2)

            assertSaved(project, period = 2)
        }

    @ParameterizedTest
    @ValueSource(booleans = [false, true])
    fun `마감 제한이 비활성화되어도 과거 날짜는 신청할 수 없다`(project: Boolean) = at(afterDeadline) {
        val exception = assertThrows(BasicException::class.java) {
            apply(useCase(applicationDeadlineEnabled = false), project, startAt = afterDeadline.toLocalDate().minusDays(1))
        }

        assertEquals(NightStudyExceptionCode.INVALID_START_AT, exception.exceptionCode)
        verifyNoInteractions(nightStudyService, projectRoomService)
    }

    @ParameterizedTest
    @ValueSource(booleans = [false, true])
    fun `마감 제한이 비활성화되어도 잘못된 교시는 신청할 수 없다`(project: Boolean) = at(afterDeadline) {
        for (period in listOf(0, 3)) {
            assertThrows(InvalidNightStudyTypeException::class.java) {
                apply(useCase(applicationDeadlineEnabled = false), project, period = period)
            }
        }

        verifyNoInteractions(nightStudyService, projectRoomService)
    }

    private fun useCase(applicationDeadlineEnabled: Boolean = true) = NightStudyUseCase(
        nightStudyService = nightStudyService,
        nightStudyAttendanceService = mock(NightStudyAttendanceService::class.java),
        projectRoomService = projectRoomService,
        userQueryClient = mock(UserQueryClient::class.java),
        outSleepingClient = mock(OutSleepingClient::class.java),
        applicationDeadlineEnabled = applicationDeadlineEnabled,
    )

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
            mocked.`when`<LocalDateTime> { LocalDateTime.now(ZoneId.of("Asia/Seoul")) }.thenReturn(now)
            block()
        }
    }
}
