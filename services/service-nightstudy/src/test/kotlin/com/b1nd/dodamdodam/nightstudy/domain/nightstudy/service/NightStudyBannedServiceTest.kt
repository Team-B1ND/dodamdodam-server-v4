package com.b1nd.dodamdodam.nightstudy.domain.nightstudy.service

import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.entity.NightStudyBannedEntity
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.exception.AlreadyBannedException
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.exception.BanNotFoundException
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.repository.nightStudy.NightStudyBannedRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.inOrder
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import java.time.LocalDate
import java.util.UUID

class NightStudyBannedServiceTest {
    private val bannedRepository = mock(NightStudyBannedRepository::class.java)
    private val service = NightStudyBannedService(
        bannedRepository = bannedRepository,
    )

    @Test
    fun `오늘 기준으로 정지 중이면 다시 정지할 수 없다`() {
        val userId = UUID.randomUUID()
        val banned = banned(userId, LocalDate.now().plusDays(7))
        `when`(bannedRepository.existsByUserIdAndEndAtGreaterThanEqual(userId, LocalDate.now())).thenReturn(true)

        assertThrows(AlreadyBannedException::class.java) {
            service.ban(banned)
        }

        verify(bannedRepository, never()).deleteByUserId(userId)
        verify(bannedRepository, never()).save(banned)
    }

    @Test
    fun `유효한 정지가 없으면 남은 정지를 지우고 저장한다`() {
        val userId = UUID.randomUUID()
        val banned = banned(userId, LocalDate.now().plusDays(7))
        `when`(bannedRepository.existsByUserIdAndEndAtGreaterThanEqual(userId, LocalDate.now())).thenReturn(false)

        service.ban(banned)

        val order = inOrder(bannedRepository)
        order.verify(bannedRepository).deleteByUserId(userId)
        order.verify(bannedRepository).save(banned)
    }

    @Test
    fun `정지 중인지는 새 정지의 종료일이 아니라 오늘 기준으로 판단한다`() {
        val userId = UUID.randomUUID()
        val banned = banned(userId, LocalDate.now().plusDays(7))
        `when`(bannedRepository.existsByUserIdAndEndAtGreaterThanEqual(userId, banned.endAt)).thenReturn(true)
        `when`(bannedRepository.existsByUserIdAndEndAtGreaterThanEqual(userId, LocalDate.now())).thenReturn(false)

        service.ban(banned)

        verify(bannedRepository).save(banned)
    }

    @Test
    fun `정지 중이면 해제한다`() {
        val userId = UUID.randomUUID()
        `when`(bannedRepository.existsByUserIdAndEndAtGreaterThanEqual(userId, LocalDate.now())).thenReturn(true)

        service.unban(userId)

        verify(bannedRepository).deleteByUserId(userId)
    }

    @Test
    fun `유효한 정지가 없으면 해제할 수 없다`() {
        val userId = UUID.randomUUID()
        `when`(bannedRepository.existsByUserIdAndEndAtGreaterThanEqual(userId, LocalDate.now())).thenReturn(false)

        assertThrows(BanNotFoundException::class.java) {
            service.unban(userId)
        }

        verify(bannedRepository, never()).deleteByUserId(userId)
    }

    @Test
    fun `오늘 기준으로 유효한 정지 목록을 돌려준다`() {
        val bans = listOf(
            banned(UUID.randomUUID(), LocalDate.now()),
            banned(UUID.randomUUID(), LocalDate.now().plusDays(7)),
        )
        `when`(bannedRepository.findAllByEndAtGreaterThanEqual(LocalDate.now())).thenReturn(bans)

        val result = service.getAll()

        assertEquals(bans, result)
    }

    @Test
    fun `정지 목록에 날짜 조건 없는 전체 조회를 쓰지 않는다`() {
        `when`(bannedRepository.findAllByEndAtGreaterThanEqual(LocalDate.now())).thenReturn(emptyList())

        service.getAll()

        verify(bannedRepository, never()).findAll()
    }

    @Test
    fun `오늘 기준으로 유효한 정지가 있으면 돌려준다`() {
        val userId = UUID.randomUUID()
        val banned = banned(userId, LocalDate.now().plusDays(7))
        `when`(bannedRepository.findByUserIdAndEndAtGreaterThanEqual(userId, LocalDate.now())).thenReturn(banned)

        val result = service.findByUserId(userId)

        assertSame(banned, result)
    }

    @Test
    fun `오늘 기준으로 유효한 정지가 없으면 null을 돌려준다`() {
        val userId = UUID.randomUUID()
        val expired = banned(userId, LocalDate.now().minusDays(1))
        `when`(bannedRepository.findByUserIdAndEndAtGreaterThanEqual(userId, LocalDate.now().minusDays(1))).thenReturn(expired)
        `when`(bannedRepository.findByUserIdAndEndAtGreaterThanEqual(userId, LocalDate.now())).thenReturn(null)

        val result = service.findByUserId(userId)

        assertNull(result)
    }

    private fun banned(userId: UUID, endAt: LocalDate): NightStudyBannedEntity {
        return NightStudyBannedEntity(
            userId = userId,
            reason = "정지",
            endAt = endAt,
        )
    }
}
