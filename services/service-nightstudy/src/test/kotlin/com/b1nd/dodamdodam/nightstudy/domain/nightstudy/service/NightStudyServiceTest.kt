package com.b1nd.dodamdodam.nightstudy.domain.nightstudy.service

import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.entity.NightStudyEntity
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.enumeration.NightStudyStatusType
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.enumeration.NightStudyType
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.repository.nightStudy.NightStudyBannedRepository
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.repository.nightStudy.NightStudyQueryRepository
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.repository.nightStudy.NightStudyRepository
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.repository.nightStudyMember.NightStudyMemberQueryRepository
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.repository.nightStudyMember.NightStudyMemberRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.times
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
    fun `프로젝트 재승인 시 기존 자동 심자가 있으면 중복 생성하지 않는다`() {
        val publicId = UUID.randomUUID()
        val memberId = UUID.randomUUID()
        val project = projectNightStudy(period = 1)
        `when`(nightStudyQueryRepository.findByPublicId(publicId)).thenReturn(project)
        `when`(nightStudyMemberQueryRepository.findAllUserIdsByNightStudy(project))
            .thenReturn(listOf(memberId, memberId))
        `when`(
            nightStudyQueryRepository.existsByUserIdAndPeriodOverlap(
                memberId,
                2,
                NightStudyType.PERSONAL,
                project.startAt,
                project.endAt,
            )
        ).thenReturn(false)
        `when`(
            nightStudyQueryRepository.existsByUserIdAndPeriodOverlap(
                memberId,
                2,
                NightStudyType.AUTO,
                project.startAt,
                project.endAt,
            )
        ).thenReturn(true)

        service.allow(publicId)

        assertEquals(NightStudyStatusType.ALLOWED, project.status)
        verify(nightStudyQueryRepository, times(1)).existsByUserIdAndPeriodOverlap(
            memberId,
            2,
            NightStudyType.AUTO,
            project.startAt,
            project.endAt,
        )
        verify(nightStudyRepository, never()).save(org.mockito.ArgumentMatchers.any())
    }

    @Test
    fun `프로젝트 2차 승인 시 자동 심자의 1차 개인 신청 여부를 검사한다`() {
        val publicId = UUID.randomUUID()
        val memberId = UUID.randomUUID()
        val project = projectNightStudy(period = 2)
        `when`(nightStudyQueryRepository.findByPublicId(publicId)).thenReturn(project)
        `when`(nightStudyMemberQueryRepository.findAllUserIdsByNightStudy(project)).thenReturn(listOf(memberId))
        `when`(
            nightStudyQueryRepository.existsByUserIdAndPeriodOverlap(
                memberId,
                1,
                NightStudyType.PERSONAL,
                project.startAt,
                project.endAt,
            )
        ).thenReturn(true)

        service.allow(publicId)

        assertEquals(NightStudyStatusType.ALLOWED, project.status)
        verify(nightStudyQueryRepository).existsByUserIdAndPeriodOverlap(
            memberId,
            1,
            NightStudyType.PERSONAL,
            project.startAt,
            project.endAt,
        )
        verify(nightStudyRepository, never()).save(org.mockito.ArgumentMatchers.any())
    }

    private fun projectNightStudy(period: Int) = NightStudyEntity(
        name = "프로젝트",
        description = "프로젝트 심자",
        period = period,
        startAt = LocalDate.of(2026, 8, 18),
        endAt = LocalDate.of(2026, 8, 21),
        needPhone = false,
        type = NightStudyType.PROJECT,
    )
}
