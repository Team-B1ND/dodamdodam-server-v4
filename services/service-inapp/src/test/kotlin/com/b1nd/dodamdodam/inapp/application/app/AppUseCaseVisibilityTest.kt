package com.b1nd.dodamdodam.inapp.application.app

import com.b1nd.dodamdodam.core.github.client.GitHubClient
import com.b1nd.dodamdodam.core.security.exception.PassportInvalidException
import com.b1nd.dodamdodam.core.security.passport.Passport
import com.b1nd.dodamdodam.core.security.passport.PassportUserDetails
import com.b1nd.dodamdodam.core.security.passport.enumerations.RoleType
import com.b1nd.dodamdodam.inapp.domain.app.service.AppService
import com.b1nd.dodamdodam.inapp.infrastructure.config.InAppProperties
import com.b1nd.dodamdodam.inapp.infrastructure.user.client.UserQueryClient
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import java.util.UUID

class AppUseCaseVisibilityTest {
    private val appService = mock(AppService::class.java)
    private val userQueryClient = mock(UserQueryClient::class.java)
    private val gitHubClient = mock(GitHubClient::class.java)
    private val useCase = AppUseCase(
        appService = appService,
        userQueryClient = userQueryClient,
        inAppProperties = InAppProperties(),
        gitHubClient = gitHubClient,
    )

    @AfterEach
    fun clearSecurityContext() {
        SecurityContextHolder.clearContext()
    }

    @Test
    fun `어드민이 앱을 공개하면 소유자 검증 없이 공개된다`() {
        val appId = UUID.randomUUID()
        authenticate(userId = UUID.randomUUID(), roles = listOf(RoleType.ADMIN))

        val response = useCase.showApp(appId)

        verify(appService).updateAdminVisibilty(appId, true)
        verify(appService, never()).updateVisibility(any(), any(), anyBoolean())
        assertEquals(200, response.status)
        assertEquals("앱이 공개되었어요.", response.message)
    }

    @Test
    fun `어드민이 앱을 비공개하면 소유자 검증 없이 비공개된다`() {
        val appId = UUID.randomUUID()
        authenticate(userId = UUID.randomUUID(), roles = listOf(RoleType.ADMIN))

        val response = useCase.hideApp(appId)

        verify(appService).updateAdminVisibilty(appId, false)
        verify(appService, never()).updateVisibility(any(), any(), anyBoolean())
        assertEquals(200, response.status)
        assertEquals("앱이 비공개되었어요.", response.message)
    }

    @Test
    fun `어드민 권한이 여러 역할 중 하나여도 어드민 경로를 탄다`() {
        val appId = UUID.randomUUID()
        authenticate(userId = UUID.randomUUID(), roles = listOf(RoleType.TEACHER, RoleType.ADMIN))

        useCase.showApp(appId)

        verify(appService).updateAdminVisibilty(appId, true)
        verify(appService, never()).updateVisibility(any(), any(), anyBoolean())
    }

    @Test
    fun `어드민은 패스포트에 userId가 없어도 공개할 수 있다`() {
        val appId = UUID.randomUUID()
        authenticate(userId = null, roles = listOf(RoleType.ADMIN))

        val response = useCase.showApp(appId)

        verify(appService).updateAdminVisibilty(appId, true)
        assertEquals("앱이 공개되었어요.", response.message)
    }

    @Test
    fun `일반 사용자가 앱을 공개하면 소유자 검증을 거쳐 공개된다`() {
        val appId = UUID.randomUUID()
        val userId = UUID.randomUUID()
        authenticate(userId = userId, roles = listOf(RoleType.STUDENT))

        val response = useCase.showApp(appId)

        verify(appService).updateVisibility(userId, appId, true)
        verify(appService, never()).updateAdminVisibilty(any(), anyBoolean())
        assertEquals(200, response.status)
        assertEquals("앱이 공개되었어요.", response.message)
    }

    @Test
    fun `일반 사용자가 앱을 비공개하면 소유자 검증을 거쳐 비공개된다`() {
        val appId = UUID.randomUUID()
        val userId = UUID.randomUUID()
        authenticate(userId = userId, roles = listOf(RoleType.TEACHER))

        val response = useCase.hideApp(appId)

        verify(appService).updateVisibility(userId, appId, false)
        verify(appService, never()).updateAdminVisibilty(any(), anyBoolean())
        assertEquals(200, response.status)
        assertEquals("앱이 비공개되었어요.", response.message)
    }

    @Test
    fun `역할이 없는 패스포트는 일반 사용자로 처리한다`() {
        val appId = UUID.randomUUID()
        val userId = UUID.randomUUID()
        authenticate(userId = userId, roles = null)

        useCase.showApp(appId)

        verify(appService).updateVisibility(userId, appId, true)
        verify(appService, never()).updateAdminVisibilty(any(), anyBoolean())
    }

    @Test
    fun `어드민이 아닌 사용자가 userId 없는 패스포트로 요청하면 예외가 발생한다`() {
        val appId = UUID.randomUUID()
        authenticate(userId = null, roles = listOf(RoleType.STUDENT))

        assertThrows<PassportInvalidException> { useCase.showApp(appId) }
        assertThrows<PassportInvalidException> { useCase.hideApp(appId) }

        verify(appService, never()).updateVisibility(any(), any(), anyBoolean())
        verify(appService, never()).updateAdminVisibilty(any(), anyBoolean())
    }

    private fun authenticate(userId: UUID?, roles: List<RoleType>?) {
        val passport = Passport(
            userId = userId,
            username = "tester",
            role = roles,
            enabled = true,
            os = "iOS",
            version = "1.0.0",
            issuedAt = 0L,
            expiredAt = Long.MAX_VALUE,
        )
        val details = PassportUserDetails(passport)
        SecurityContextHolder.getContext().authentication =
            UsernamePasswordAuthenticationToken(details, null, details.authorities)
    }

    private fun <T> any(): T = org.mockito.ArgumentMatchers.any()
    private fun anyBoolean(): Boolean = org.mockito.ArgumentMatchers.anyBoolean()
}
