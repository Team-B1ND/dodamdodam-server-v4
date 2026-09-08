package com.b1nd.dodamdodam.nightstudy.domain.nightstudy.service

import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.command.NightStudyAssignmentCommand
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.command.NightStudyWithMembersCommand.Attendance
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.command.NightStudyWithMembersCommand.Participation
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.command.NightStudyWithMembersCommand.PeriodAssignment
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
        val user = UUID.randomUUID()
        stub(
            assigned1 = listOf(personal(user)),
            assigned2 = listOf(personal(user)),
            attendances = mapOf(1 to setOf(user)),
        )

        val participations = participations()

        assertEquals(Attendance(ATTENDANCE, ABSENT), participations[user].attendance)
    }

    @Test
    fun `신청했지만 출석 기록이 없으면 ABSENT다`() {
        val user = UUID.randomUUID()
        stub(assigned1 = listOf(personal(user)), assigned2 = listOf(personal(user)))

        assertEquals(Attendance(ABSENT, ABSENT), participations()[user].attendance)
    }

    @Test
    fun `1차만 신청한 학생의 2차는 NOT_APPLIED다`() {
        val user = UUID.randomUUID()
        stub(assigned1 = listOf(personal(user)), attendances = mapOf(1 to setOf(user)))

        assertEquals(Attendance(ATTENDANCE, NOT_APPLIED), participations()[user].attendance)
    }

    @Test
    fun `신청하지 않은 학생은 빈 참여 정보를 돌려준다`() {
        val applicant = UUID.randomUUID()
        val outsider = UUID.randomUUID()
        stub(assigned1 = listOf(personal(applicant)))

        assertEquals(Participation.NONE, participations()[outsider])
    }

    @Test
    fun `신청 기록 없이 출석만 찍힌 학생도 ATTENDANCE로 담는다`() {
        val ghost = UUID.randomUUID()
        stub(attendances = mapOf(1 to setOf(ghost)))

        val participation = participations()[ghost]

        assertEquals(Attendance(ATTENDANCE, NOT_APPLIED), participation.attendance)
        assertNull(participation.assignment.period1)
    }

    @Test
    fun `1차 2차가 아닌 차수의 출석 기록은 버린다`() {
        val user = UUID.randomUUID()
        stub(assigned1 = listOf(personal(user)), attendances = mapOf(3 to setOf(user)))

        assertEquals(Attendance(ABSENT, NOT_APPLIED), participations()[user].attendance)
    }

    @Test
    fun `프로젝트실 이름과 층을 차수별로 담는다`() {
        val user = UUID.randomUUID()
        stub(
            assigned1 = listOf(project(user, "정보관 301", 3)),
            assigned2 = listOf(project(user, "정보관 302", 2)),
        )

        val assignment = participations()[user].assignment

        assertEquals(PeriodAssignment(NightStudyType.PROJECT, "정보관 301", 3), assignment.period1)
        assertEquals(PeriodAssignment(NightStudyType.PROJECT, "정보관 302", 2), assignment.period2)
    }

    @Test
    fun `1차만 신청하면 2차 배정은 null이다`() {
        val user = UUID.randomUUID()
        stub(assigned1 = listOf(project(user, "정보관 301", 3)))

        assertNull(participations()[user].assignment.period2)
    }

    @Test
    fun `개인 심자는 방 정보 없이 타입만 담는다`() {
        val user = UUID.randomUUID()
        stub(assigned1 = listOf(personal(user)))

        assertEquals(PeriodAssignment(NightStudyType.PERSONAL, null, null), participations()[user].assignment.period1)
    }

    @Test
    fun `같은 차수에 프로젝트와 개인 심자가 겹치면 프로젝트실을 택한다`() {
        val user = UUID.randomUUID()
        stub(assigned1 = listOf(personal(user), project(user, "정보관 301", 3)))

        assertEquals(PeriodAssignment(NightStudyType.PROJECT, "정보관 301", 3), participations()[user].assignment.period1)
    }

    @Test
    fun `방이 배정되지 않은 프로젝트 심자보다 개인 심자를 먼저 택한다`() {
        val user = UUID.randomUUID()
        stub(assigned1 = listOf(project(user, null, null), personal(user)))

        assertEquals(PeriodAssignment(NightStudyType.PERSONAL, null, null), participations()[user].assignment.period1)
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
            assigned1 = listOf(personal(leader1), personal(leader2)),
            assigned2 = listOf(personal(leader2)),
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
        stub(leaders = emptyMap(), members = emptyMap())

        val command = service.getActiveNightStudiesByType(NightStudyType.PROJECT, date).single()

        assertNull(command.leaderId)
        assertTrue(command.memberIds.isEmpty())
        assertTrue(command.participations.isEmpty())
    }

    private fun participations() =
        service.getActiveNightStudiesByType(NightStudyType.PROJECT, date).single().participations

    private fun personal(userId: UUID) =
        NightStudyAssignmentCommand(userId, NightStudyType.PERSONAL, null, null)

    private fun project(userId: UUID, name: String?, floor: Int?) =
        NightStudyAssignmentCommand(userId, NightStudyType.PROJECT, name, floor)

    private fun stub(
        nightStudies: List<NightStudyEntity> = listOf(nightStudy(id = 1L, period = 1)),
        leaders: Map<Long, UUID> = emptyMap(),
        members: Map<Long, List<UUID>> = emptyMap(),
        assigned1: List<NightStudyAssignmentCommand> = emptyList(),
        assigned2: List<NightStudyAssignmentCommand> = emptyList(),
        attendances: Map<Int, Set<UUID>> = emptyMap(),
    ) {
        `when`(
            nightStudyQueryRepository
                .findAllByTypeAndStartAtLessThanEqualAndEndAtGreaterThanEqual(NightStudyType.PROJECT, date, date)
        ).thenReturn(nightStudies)
        `when`(nightStudyMemberQueryRepository.findLeaderUserIdsByNightStudies(nightStudies)).thenReturn(leaders)
        `when`(nightStudyMemberQueryRepository.findAllMemberUserIdsByNightStudies(nightStudies)).thenReturn(members)
        `when`(nightStudyQueryRepository.findAllowedAssignmentsByDateAndPeriod(date, 1)).thenReturn(assigned1)
        `when`(nightStudyQueryRepository.findAllowedAssignmentsByDateAndPeriod(date, 2)).thenReturn(assigned2)
        `when`(nightStudyQueryRepository.findAttendedUserIdsByDate(date)).thenReturn(attendances)
    }

    private fun nightStudy(id: Long, period: Int): NightStudyEntity =
        mock(NightStudyEntity::class.java).also { nightStudy ->
            `when`(nightStudy.id).thenReturn(id)
            `when`(nightStudy.period).thenReturn(period)
        }
}
