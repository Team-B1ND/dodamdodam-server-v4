package com.b1nd.dodamdodam.nightstudy.domain.nightstudy.service

import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.command.NightStudyWithMembersCommand.Participation
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.command.NightStudyWithMembersCommand.Attendance
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.command.NightStudyWithMembersCommand.ProjectRoom
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.entity.NightStudyEntity
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.enumeration.NightStudyAttendanceStatus.ABSENT
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.enumeration.NightStudyAttendanceStatus.ATTENDANCE
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.enumeration.NightStudyAttendanceStatus.NOT_APPLIED
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.enumeration.NightStudyType
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.repository.nightStudy.NightStudyQueryRepository
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.repository.nightStudyMember.NightStudyMemberQueryRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import java.time.LocalDate
import java.util.UUID

class OpenApiNightStudyServiceTest {
    private val nightStudyQueryRepository = mock(NightStudyQueryRepository::class.java)
    private val nightStudyMemberQueryRepository = mock(NightStudyMemberQueryRepository::class.java)
    private val service = OpenApiNightStudyService(
        nightStudyQueryRepository = nightStudyQueryRepository,
        nightStudyMemberQueryRepository = nightStudyMemberQueryRepository,
    )

    private val date = LocalDate.of(2026, 9, 6)

    @Test
    fun `출석 기록이 있으면 해당 차수는 ATTENDANCE다`() {
        val period1User = UUID.randomUUID()
        val period2User = UUID.randomUUID()
        stub(
            nightStudies = listOf(nightStudy(id = 1L, period = 2)),
            leaders = mapOf(1L to period1User),
            members = mapOf(1L to listOf(period1User, period2User)),
            applied1 = listOf(period1User, period2User),
            applied2 = listOf(period1User, period2User),
            attendances = mapOf(1 to setOf(period1User), 2 to setOf(period2User)),
        )

        val participations = service.getActiveNightStudiesByType(NightStudyType.PROJECT, date).single().participations

        assertEquals(Attendance(ATTENDANCE, ABSENT), participations[period1User].attendance)
        assertEquals(Attendance(ABSENT, ATTENDANCE), participations[period2User].attendance)
    }

    @Test
    fun `신청했지만 출석 기록이 없으면 ABSENT다`() {
        val userId = UUID.randomUUID()
        stub(
            nightStudies = listOf(nightStudy(id = 1L, period = 2)),
            leaders = mapOf(1L to userId),
            members = mapOf(1L to listOf(userId)),
            applied1 = listOf(userId),
            applied2 = listOf(userId),
            attendances = emptyMap(),
        )

        val participations = service.getActiveNightStudiesByType(NightStudyType.PROJECT, date).single().participations

        assertEquals(Attendance(ABSENT, ABSENT), participations[userId].attendance)
    }

    @Test
    fun `1차만 신청한 학생의 2차는 NOT_APPLIED다`() {
        val userId = UUID.randomUUID()
        stub(
            nightStudies = listOf(nightStudy(id = 1L, period = 1)),
            leaders = mapOf(1L to userId),
            members = mapOf(1L to listOf(userId)),
            applied1 = listOf(userId),
            applied2 = emptyList(),
            attendances = mapOf(1 to setOf(userId)),
        )

        val participations = service.getActiveNightStudiesByType(NightStudyType.PROJECT, date).single().participations

        assertEquals(Attendance(ATTENDANCE, NOT_APPLIED), participations[userId].attendance)
    }

    @Test
    fun `신청하지 않은 학생은 빈 참여 정보를 돌려준다`() {
        val applicant = UUID.randomUUID()
        val outsider = UUID.randomUUID()
        stub(
            nightStudies = listOf(nightStudy(id = 1L, period = 1)),
            leaders = mapOf(1L to applicant),
            members = mapOf(1L to listOf(applicant)),
            applied1 = listOf(applicant),
            applied2 = emptyList(),
            attendances = emptyMap(),
        )

        val participations = service.getActiveNightStudiesByType(NightStudyType.PROJECT, date).single().participations

        assertEquals(Participation.NONE, participations[outsider])
    }

    @Test
    fun `신청 기록 없이 출석만 찍힌 학생도 ATTENDANCE로 담는다`() {
        val ghost = UUID.randomUUID()
        stub(
            nightStudies = listOf(nightStudy(id = 1L, period = 1)),
            leaders = emptyMap(),
            members = emptyMap(),
            applied1 = emptyList(),
            applied2 = emptyList(),
            attendances = mapOf(1 to setOf(ghost)),
        )

        val participations = service.getActiveNightStudiesByType(NightStudyType.PROJECT, date).single().participations

        assertEquals(Attendance(ATTENDANCE, NOT_APPLIED), participations[ghost].attendance)
    }

    @Test
    fun `1차 2차가 아닌 차수의 출석 기록은 버린다`() {
        val userId = UUID.randomUUID()
        stub(
            nightStudies = listOf(nightStudy(id = 1L, period = 1)),
            leaders = mapOf(1L to userId),
            members = mapOf(1L to listOf(userId)),
            applied1 = listOf(userId),
            applied2 = emptyList(),
            attendances = mapOf(3 to setOf(userId)),
        )

        val participations = service.getActiveNightStudiesByType(NightStudyType.PROJECT, date).single().participations

        assertEquals(Attendance(ABSENT, NOT_APPLIED), participations[userId].attendance)
    }

    @Test
    fun `배정된 프로젝트실을 차수별로 담는다`() {
        val userId = UUID.randomUUID()
        stub(
            nightStudies = listOf(nightStudy(id = 1L, period = 2)),
            leaders = mapOf(1L to userId),
            members = mapOf(1L to listOf(userId)),
            applied1 = listOf(userId),
            applied2 = listOf(userId),
            attendances = emptyMap(),
            projectRoom1 = mapOf(userId to "정보관 301"),
            projectRoom2 = mapOf(userId to "정보관 302"),
        )

        val participations = service.getActiveNightStudiesByType(NightStudyType.PROJECT, date).single().participations

        assertEquals(ProjectRoom("정보관 301", "정보관 302"), participations[userId].projectRoom)
    }

    @Test
    fun `프로젝트 심자를 1차만 하면 2차 방은 null이다`() {
        val userId = UUID.randomUUID()
        stub(
            nightStudies = listOf(nightStudy(id = 1L, period = 1)),
            leaders = mapOf(1L to userId),
            members = mapOf(1L to listOf(userId)),
            applied1 = listOf(userId),
            applied2 = emptyList(),
            attendances = emptyMap(),
            projectRoom1 = mapOf(userId to "정보관 301"),
            projectRoom2 = emptyMap(),
        )

        val participations = service.getActiveNightStudiesByType(NightStudyType.PROJECT, date).single().participations

        assertEquals(ProjectRoom("정보관 301", null), participations[userId].projectRoom)
    }

    @Test
    fun `personal 심자만 있으면 두 차수 모두 방이 null이다`() {
        val userId = UUID.randomUUID()
        stub(
            nightStudies = listOf(nightStudy(id = 1L, period = 2)),
            leaders = mapOf(1L to userId),
            members = mapOf(1L to listOf(userId)),
            applied1 = listOf(userId),
            applied2 = listOf(userId),
            attendances = emptyMap(),
            projectRoom1 = emptyMap(),
            projectRoom2 = emptyMap(),
        )

        val participations = service.getActiveNightStudiesByType(NightStudyType.PERSONAL, date).single().participations

        assertEquals(ProjectRoom(null, null), participations[userId].projectRoom)
    }

    @Test
    fun `여러 심자가 동일한 참여 정보를 공유한다`() {
        val leader1 = UUID.randomUUID()
        val leader2 = UUID.randomUUID()
        val first = nightStudy(id = 1L, period = 1)
        val second = nightStudy(id = 2L, period = 2)
        stub(
            nightStudies = listOf(first, second),
            leaders = mapOf(1L to leader1, 2L to leader2),
            members = mapOf(1L to listOf(leader1), 2L to listOf(leader2)),
            applied1 = listOf(leader1, leader2),
            applied2 = listOf(leader2),
            attendances = mapOf(1 to setOf(leader1), 2 to setOf(leader2)),
        )

        val commands = service.getActiveNightStudiesByType(NightStudyType.PROJECT, date)

        assertEquals(2, commands.size)
        assertEquals(commands[0].participations, commands[1].participations)
        assertEquals(Attendance(ATTENDANCE, NOT_APPLIED), commands[0].participations[leader1].attendance)
        assertEquals(Attendance(ABSENT, ATTENDANCE), commands[0].participations[leader2].attendance)
    }

    @Test
    fun `멤버가 없는 심자도 빈 참여 정보와 함께 반환한다`() {
        stub(
            nightStudies = listOf(nightStudy(id = 1L, period = 1)),
            leaders = emptyMap(),
            members = emptyMap(),
            applied1 = emptyList(),
            applied2 = emptyList(),
            attendances = emptyMap(),
        )

        val command = service.getActiveNightStudiesByType(NightStudyType.PROJECT, date).single()

        assertNull(command.leaderId)
        assertTrue(command.memberIds.isEmpty())
        assertTrue(command.participations.isEmpty())
    }

    private fun stub(
        nightStudies: List<NightStudyEntity>,
        leaders: Map<Long, UUID>,
        members: Map<Long, List<UUID>>,
        applied1: List<UUID>,
        applied2: List<UUID>,
        attendances: Map<Int, Set<UUID>>,
        projectRoom1: Map<UUID, String> = emptyMap(),
        projectRoom2: Map<UUID, String> = emptyMap(),
    ) {
        `when`(
            nightStudyQueryRepository
                .findAllByTypeAndStartAtLessThanEqualAndEndAtGreaterThanEqual(NightStudyType.PROJECT, date, date)
        ).thenReturn(nightStudies)
        `when`(
            nightStudyQueryRepository
                .findAllByTypeAndStartAtLessThanEqualAndEndAtGreaterThanEqual(NightStudyType.PERSONAL, date, date)
        ).thenReturn(nightStudies)
        `when`(nightStudyMemberQueryRepository.findLeaderUserIdsByNightStudies(nightStudies)).thenReturn(leaders)
        `when`(nightStudyMemberQueryRepository.findAllMemberUserIdsByNightStudies(nightStudies)).thenReturn(members)
        `when`(nightStudyQueryRepository.findAllowedUserIdsByDateAndPeriod(date, 1)).thenReturn(applied1)
        `when`(nightStudyQueryRepository.findAllowedUserIdsByDateAndPeriod(date, 2)).thenReturn(applied2)
        `when`(nightStudyQueryRepository.findAttendedUserIdsByDate(date)).thenReturn(attendances)
        `when`(nightStudyQueryRepository.findProjectRoomNamesByDateAndPeriod(date, 1)).thenReturn(projectRoom1)
        `when`(nightStudyQueryRepository.findProjectRoomNamesByDateAndPeriod(date, 2)).thenReturn(projectRoom2)
    }

    private fun nightStudy(id: Long, period: Int): NightStudyEntity =
        mock(NightStudyEntity::class.java).also { nightStudy ->
            `when`(nightStudy.id).thenReturn(id)
            `when`(nightStudy.period).thenReturn(period)
        }
}
