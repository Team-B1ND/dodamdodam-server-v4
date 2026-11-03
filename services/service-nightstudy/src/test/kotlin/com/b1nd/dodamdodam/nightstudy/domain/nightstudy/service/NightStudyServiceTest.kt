package com.b1nd.dodamdodam.nightstudy.domain.nightstudy.service

import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.entity.NightStudyEntity
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.enumeration.NightStudyStatusType
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.enumeration.NightStudyType
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.exception.PeriodOverlappedException
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.repository.nightStudy.NightStudyBannedRepository
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.repository.nightStudy.NightStudyQueryRepository
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.repository.nightStudy.NightStudyRepository
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.repository.nightStudyMember.NightStudyMemberQueryRepository
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.repository.nightStudyMember.NightStudyMemberRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import java.time.LocalDate
import java.util.UUID

class NightStudyServiceTest {
    private val nightStudyRepository = mock(NightStudyRepository::class.java)
    private val nightStudyQueryRepository = mock(NightStudyQueryRepository::class.java)
    private val nightStudyMemberRepository = mock(NightStudyMemberRepository::class.java)
    private val nightStudyMemberQueryRepository = mock(NightStudyMemberQueryRepository::class.java)
    private val bannedRepository = mock(NightStudyBannedRepository::class.java)
    private val service = NightStudyService(
        nightStudyRepository = nightStudyRepository,
        nightStudyQueryRepository = nightStudyQueryRepository,
        nightStudyMemberRepository = nightStudyMemberRepository,
        nightStudyMemberQueryRepository = nightStudyMemberQueryRepository,
        bannedRepository = bannedRepository,
    )

    @Test
    fun `프로젝트 승인 시 상태만 변경하고 다른 심자를 생성하지 않는다`() {
        val publicId = UUID.randomUUID()
        val memberId = UUID.randomUUID()
        val project = nightStudy(NightStudyType.PROJECT)
        `when`(nightStudyRepository.findByPublicIdForUpdate(publicId)).thenReturn(project)
        `when`(nightStudyMemberQueryRepository.findAllUserIdsByNightStudy(project)).thenReturn(listOf(memberId))

        service.allow(publicId)

        assertEquals(NightStudyStatusType.ALLOWED, project.status)
        verify(nightStudyRepository, never()).save(org.mockito.ArgumentMatchers.any())
    }

    @Test
    fun `개인 심자 승인 시 상태만 변경한다`() {
        val publicId = UUID.randomUUID()
        val userId = UUID.randomUUID()
        val personal = nightStudy(NightStudyType.PERSONAL)
        `when`(nightStudyRepository.findByPublicIdForUpdate(publicId)).thenReturn(personal)
        `when`(nightStudyMemberQueryRepository.findAllUserIdsByNightStudy(personal)).thenReturn(listOf(userId))

        service.allow(publicId)

        assertEquals(NightStudyStatusType.ALLOWED, personal.status)
        verify(nightStudyRepository, never()).save(org.mockito.ArgumentMatchers.any())
    }

    @Test
    fun `같은 교시에 승인된 심자가 겹치는 멤버가 있으면 승인할 수 없다`() {
        val publicId = UUID.randomUUID()
        val memberId = UUID.randomUUID()
        val project = nightStudy(NightStudyType.PROJECT)
        `when`(nightStudyRepository.findByPublicIdForUpdate(publicId)).thenReturn(project)
        `when`(nightStudyMemberQueryRepository.findAllUserIdsByNightStudy(project)).thenReturn(listOf(memberId))
        `when`(
            nightStudyQueryRepository.existsAllowedByUserIdAndPeriodOverlap(
                memberId,
                project.period,
                project.startAt,
                project.endAt,
                project.id!!,
            )
        ).thenReturn(true)

        assertThrows(PeriodOverlappedException::class.java) {
            service.allow(publicId)
        }

        assertEquals(NightStudyStatusType.PENDING, project.status)
    }

    @Test
    fun `이미 승인된 심자를 다시 승인해도 겹침을 검사하지 않는다`() {
        val publicId = UUID.randomUUID()
        val project = nightStudy(NightStudyType.PROJECT).apply { allow() }
        `when`(nightStudyRepository.findByPublicIdForUpdate(publicId)).thenReturn(project)

        service.allow(publicId)

        assertEquals(NightStudyStatusType.ALLOWED, project.status)
        verify(nightStudyMemberQueryRepository, never()).findAllUserIdsByNightStudy(project)
    }

    @Test
    fun `프로젝트 거절 시 상태와 거절 사유를 변경한다`() {
        val publicId = UUID.randomUUID()
        val project = nightStudy(NightStudyType.PROJECT).apply { allow() }
        `when`(nightStudyRepository.findByPublicIdForUpdate(publicId)).thenReturn(project)

        service.reject(publicId, "프로젝트 거절")

        assertEquals(NightStudyStatusType.REJECTED, project.status)
        assertEquals("프로젝트 거절", project.rejectionReason)
    }

    @Test
    fun `프로젝트 대기 전환 시 거절 사유를 지운다`() {
        val publicId = UUID.randomUUID()
        val project = nightStudy(NightStudyType.PROJECT).apply { reject("프로젝트 거절") }
        `when`(nightStudyRepository.findByPublicIdForUpdate(publicId)).thenReturn(project)

        service.pending(publicId)

        assertEquals(NightStudyStatusType.PENDING, project.status)
        assertEquals(null, project.rejectionReason)
    }

    @Test
    fun `프로젝트 삭제 시 멤버와 프로젝트를 삭제한다`() {
        val publicId = UUID.randomUUID()
        val userId = UUID.randomUUID()
        val project = nightStudy(NightStudyType.PROJECT)
        `when`(nightStudyRepository.findByPublicIdForUpdate(publicId)).thenReturn(project)
        `when`(nightStudyMemberRepository.existsByNightStudyAndUserId(project, userId)).thenReturn(true)
        `when`(nightStudyMemberQueryRepository.findLeaderUserIdByNightStudy(project)).thenReturn(userId)

        service.delete(userId, publicId)

        verify(nightStudyMemberRepository).deleteAllByNightStudy(project)
        verify(nightStudyRepository).delete(project)
    }

    private fun nightStudy(type: NightStudyType): NightStudyEntity {
        val nightStudy = NightStudyEntity(
            description = "심자",
            period = 1,
            startAt = LocalDate.of(2026, 8, 18),
            endAt = LocalDate.of(2026, 8, 21),
            needPhone = false,
            type = type,
        )
        NightStudyEntity::class.java.getDeclaredField("id").apply {
            isAccessible = true
            set(nightStudy, 1L)
        }
        return nightStudy
    }
}
