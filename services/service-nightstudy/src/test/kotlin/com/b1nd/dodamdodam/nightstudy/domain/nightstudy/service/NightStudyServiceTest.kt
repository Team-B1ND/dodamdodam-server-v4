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
import org.mockito.ArgumentCaptor
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
        val existingAuto = autoNightStudy(null, NightStudyStatusType.PENDING)
        `when`(nightStudyRepository.findByPublicIdForUpdate(publicId)).thenReturn(project)
        `when`(nightStudyMemberQueryRepository.findAllUserIdsByNightStudy(project))
            .thenReturn(listOf(memberId, memberId))
        `when`(
            nightStudyQueryRepository.findActiveByUserIdAndPeriodAndTypesOverlap(
                memberId,
                2,
                setOf(NightStudyType.PERSONAL, NightStudyType.AUTO),
                project.startAt,
                project.endAt,
                emptySet(),
            )
        ).thenReturn(listOf(existingAuto))

        service.allow(publicId)

        assertEquals(NightStudyStatusType.ALLOWED, project.status)
        verify(nightStudyQueryRepository, times(1)).findActiveByUserIdAndPeriodAndTypesOverlap(
            memberId,
            2,
            setOf(NightStudyType.PERSONAL, NightStudyType.AUTO),
            project.startAt,
            project.endAt,
            emptySet(),
        )
        verify(nightStudyRepository, never()).save(org.mockito.ArgumentMatchers.any())
    }

    @Test
    fun `프로젝트 2차 승인 시 자동 심자의 1차 개인 신청 여부를 검사한다`() {
        val publicId = UUID.randomUUID()
        val memberId = UUID.randomUUID()
        val project = projectNightStudy(period = 2)
        val personal = personalNightStudy(period = 1, startAt = project.startAt, endAt = project.endAt)
        `when`(nightStudyRepository.findByPublicIdForUpdate(publicId)).thenReturn(project)
        `when`(nightStudyMemberQueryRepository.findAllUserIdsByNightStudy(project)).thenReturn(listOf(memberId))
        `when`(
            nightStudyQueryRepository.findActiveByUserIdAndPeriodAndTypesOverlap(
                memberId,
                1,
                setOf(NightStudyType.PERSONAL, NightStudyType.AUTO),
                project.startAt,
                project.endAt,
                emptySet(),
            )
        ).thenReturn(listOf(personal))

        service.allow(publicId)

        assertEquals(NightStudyStatusType.ALLOWED, project.status)
        verify(nightStudyQueryRepository).findActiveByUserIdAndPeriodAndTypesOverlap(
            memberId,
            1,
            setOf(NightStudyType.PERSONAL, NightStudyType.AUTO),
            project.startAt,
            project.endAt,
            emptySet(),
        )
        verify(nightStudyRepository, never()).save(org.mockito.ArgumentMatchers.any())
    }

    @Test
    fun `프로젝트 재승인 시 연결된 자동 심자를 대기 상태로 재사용한다`() {
        val publicId = UUID.randomUUID()
        val memberId = UUID.randomUUID()
        val project = projectNightStudy(period = 1)
        val auto = autoNightStudy(project, NightStudyStatusType.REJECTED)
        `when`(nightStudyRepository.findByPublicIdForUpdate(publicId)).thenReturn(project)
        `when`(nightStudyMemberQueryRepository.findAllUserIdsByNightStudy(project)).thenReturn(listOf(memberId))
        `when`(nightStudyRepository.findAllBySourceProject(project)).thenReturn(listOf(auto))
        `when`(nightStudyMemberQueryRepository.findAllMemberUserIdsByNightStudies(listOf(auto)))
            .thenReturn(mapOf(auto.id!! to listOf(memberId)))
        `when`(
            nightStudyQueryRepository.findActiveByUserIdAndPeriodAndTypesOverlap(
                memberId,
                2,
                setOf(NightStudyType.PERSONAL, NightStudyType.AUTO),
                project.startAt,
                project.endAt,
                setOf(auto.id!!),
            )
        ).thenReturn(emptyList())

        service.allow(publicId)

        assertEquals(NightStudyStatusType.ALLOWED, project.status)
        assertEquals(NightStudyStatusType.PENDING, auto.status)
        verify(nightStudyRepository, never()).save(org.mockito.ArgumentMatchers.any())
    }

    @Test
    fun `개인 심자가 일부 날짜에만 있으면 비어 있는 기간별로 자동 심자를 생성한다`() {
        val publicId = UUID.randomUUID()
        val memberId = UUID.randomUUID()
        val project = projectNightStudy(period = 1)
        val personal = personalNightStudy(
            period = 2,
            startAt = LocalDate.of(2026, 8, 20),
            endAt = LocalDate.of(2026, 8, 20),
        )
        `when`(nightStudyRepository.findByPublicIdForUpdate(publicId)).thenReturn(project)
        `when`(nightStudyMemberQueryRepository.findAllUserIdsByNightStudy(project)).thenReturn(listOf(memberId))
        `when`(
            nightStudyQueryRepository.findActiveByUserIdAndPeriodAndTypesOverlap(
                memberId,
                2,
                setOf(NightStudyType.PERSONAL, NightStudyType.AUTO),
                project.startAt,
                project.endAt,
                emptySet(),
            )
        ).thenReturn(listOf(personal))
        `when`(nightStudyRepository.save(org.mockito.ArgumentMatchers.any(NightStudyEntity::class.java)))
            .thenAnswer { it.getArgument(0) }

        service.allow(publicId)

        val captor = ArgumentCaptor.forClass(NightStudyEntity::class.java)
        verify(nightStudyRepository, times(2)).save(captor.capture())
        assertEquals(
            listOf(
                LocalDate.of(2026, 8, 18) to LocalDate.of(2026, 8, 19),
                LocalDate.of(2026, 8, 21) to LocalDate.of(2026, 8, 21),
            ),
            captor.allValues.map { it.startAt to it.endAt },
        )
        captor.allValues.forEach {
            assertEquals(NightStudyType.AUTO, it.type)
            assertEquals(project, it.sourceProject)
        }
    }

    @Test
    fun `프로젝트 거절 시 연결된 자동 심자도 거절한다`() {
        val publicId = UUID.randomUUID()
        val project = projectNightStudy(period = 1).apply { allow() }
        val auto = autoNightStudy(project, NightStudyStatusType.ALLOWED)
        `when`(nightStudyRepository.findByPublicIdForUpdate(publicId)).thenReturn(project)
        `when`(nightStudyRepository.findAllBySourceProject(project)).thenReturn(listOf(auto))

        service.reject(publicId, "프로젝트 거절")

        assertEquals(NightStudyStatusType.REJECTED, project.status)
        assertEquals(NightStudyStatusType.REJECTED, auto.status)
        assertEquals("프로젝트 심자 상태 변경으로 인한 자동 거절", auto.rejectionReason)
    }

    @Test
    fun `프로젝트 대기 전환 시 연결된 자동 심자도 대기 상태로 변경한다`() {
        val publicId = UUID.randomUUID()
        val project = projectNightStudy(period = 1).apply { allow() }
        val auto = autoNightStudy(project, NightStudyStatusType.ALLOWED)
        `when`(nightStudyRepository.findByPublicIdForUpdate(publicId)).thenReturn(project)
        `when`(nightStudyRepository.findAllBySourceProject(project)).thenReturn(listOf(auto))

        service.pending(publicId)

        assertEquals(NightStudyStatusType.PENDING, project.status)
        assertEquals(NightStudyStatusType.PENDING, auto.status)
    }

    @Test
    fun `프로젝트 삭제 시 연결된 자동 심자를 먼저 삭제한다`() {
        val publicId = UUID.randomUUID()
        val userId = UUID.randomUUID()
        val project = projectNightStudy(period = 1)
        val auto = autoNightStudy(project, NightStudyStatusType.PENDING)
        `when`(nightStudyRepository.findByPublicIdForUpdate(publicId)).thenReturn(project)
        `when`(nightStudyMemberRepository.existsByNightStudyAndUserId(project, userId)).thenReturn(true)
        `when`(nightStudyMemberQueryRepository.findLeaderUserIdByNightStudy(project)).thenReturn(userId)
        `when`(nightStudyRepository.findAllBySourceProject(project)).thenReturn(listOf(auto))

        service.delete(userId, publicId)

        verify(nightStudyMemberRepository).deleteAllByNightStudy(auto)
        verify(nightStudyRepository).delete(auto)
        verify(nightStudyRepository).delete(project)
    }

    @Test
    fun `개인 심자 승인 시 완전히 겹치는 자동 심자를 제거한다`() {
        val publicId = UUID.randomUUID()
        val userId = UUID.randomUUID()
        val project = projectNightStudy(period = 1).apply { allow() }
        val auto = autoNightStudy(project, NightStudyStatusType.ALLOWED)
        val personal = personalNightStudy(
            period = 2,
            startAt = auto.startAt,
            endAt = auto.endAt,
        )
        `when`(nightStudyRepository.findByPublicIdForUpdate(publicId)).thenReturn(personal)
        `when`(nightStudyMemberQueryRepository.findAllUserIdsByNightStudy(personal)).thenReturn(listOf(userId))
        `when`(
            nightStudyQueryRepository.findActiveByUserIdAndPeriodAndTypesOverlap(
                userId,
                2,
                setOf(NightStudyType.AUTO),
                personal.startAt,
                personal.endAt,
                emptySet(),
            )
        ).thenReturn(listOf(auto))

        service.allow(publicId)

        assertEquals(NightStudyStatusType.ALLOWED, personal.status)
        verify(nightStudyMemberRepository).deleteAllByNightStudy(auto)
        verify(nightStudyRepository).delete(auto)
        verify(nightStudyRepository, never()).save(org.mockito.ArgumentMatchers.any())
    }

    @Test
    fun `개인 심자 승인 시 자동 심자의 겹치지 않는 기간은 보존한다`() {
        val publicId = UUID.randomUUID()
        val userId = UUID.randomUUID()
        val project = projectNightStudy(period = 1).apply { allow() }
        val auto = autoNightStudy(project, NightStudyStatusType.ALLOWED)
        val personal = personalNightStudy(
            period = 2,
            startAt = LocalDate.of(2026, 8, 20),
            endAt = LocalDate.of(2026, 8, 20),
        )
        `when`(nightStudyRepository.findByPublicIdForUpdate(publicId)).thenReturn(personal)
        `when`(nightStudyMemberQueryRepository.findAllUserIdsByNightStudy(personal)).thenReturn(listOf(userId))
        `when`(
            nightStudyQueryRepository.findActiveByUserIdAndPeriodAndTypesOverlap(
                userId,
                2,
                setOf(NightStudyType.AUTO),
                personal.startAt,
                personal.endAt,
                emptySet(),
            )
        ).thenReturn(listOf(auto))
        `when`(nightStudyRepository.save(org.mockito.ArgumentMatchers.any(NightStudyEntity::class.java)))
            .thenAnswer { it.getArgument(0) }

        service.allow(publicId)

        val captor = ArgumentCaptor.forClass(NightStudyEntity::class.java)
        verify(nightStudyRepository, times(2)).save(captor.capture())
        assertEquals(
            listOf(
                LocalDate.of(2026, 8, 18) to LocalDate.of(2026, 8, 19),
                LocalDate.of(2026, 8, 21) to LocalDate.of(2026, 8, 21),
            ),
            captor.allValues.map { it.startAt to it.endAt },
        )
        captor.allValues.forEach {
            assertEquals(NightStudyStatusType.ALLOWED, it.status)
            assertEquals(project, it.sourceProject)
        }
    }

    private fun projectNightStudy(period: Int): NightStudyEntity {
        val project = NightStudyEntity(
            name = "프로젝트",
            description = "프로젝트 심자",
            period = period,
            startAt = LocalDate.of(2026, 8, 18),
            endAt = LocalDate.of(2026, 8, 21),
            needPhone = false,
            type = NightStudyType.PROJECT,
        )
        NightStudyEntity::class.java.getDeclaredField("id").apply {
            isAccessible = true
            set(project, 1L)
        }
        return project
    }

    private fun autoNightStudy(
        sourceProject: NightStudyEntity?,
        status: NightStudyStatusType,
    ): NightStudyEntity {
        val auto = NightStudyEntity(
            description = "자동 심자",
            period = if (sourceProject?.period == 2) 1 else 2,
            startAt = sourceProject?.startAt ?: LocalDate.of(2026, 8, 18),
            endAt = sourceProject?.endAt ?: LocalDate.of(2026, 8, 21),
            needPhone = false,
            status = status,
            type = NightStudyType.AUTO,
            sourceProject = sourceProject,
        )
        NightStudyEntity::class.java.getDeclaredField("id").apply {
            isAccessible = true
            set(auto, 2L)
        }
        return auto
    }

    private fun personalNightStudy(
        period: Int,
        startAt: LocalDate,
        endAt: LocalDate,
    ): NightStudyEntity {
        val personal = NightStudyEntity(
            description = "개인 심자",
            period = period,
            startAt = startAt,
            endAt = endAt,
            needPhone = false,
            status = NightStudyStatusType.PENDING,
            type = NightStudyType.PERSONAL,
        )
        NightStudyEntity::class.java.getDeclaredField("id").apply {
            isAccessible = true
            set(personal, 3L)
        }
        return personal
    }
}
