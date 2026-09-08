package com.b1nd.dodamdodam.nightstudy.domain.nightstudy.service

import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.command.NightStudyAssignmentCommand
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.command.NightStudyWithMembersCommand
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.command.NightStudyWithMembersCommand.Assignment
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.command.NightStudyWithMembersCommand.Attendance
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.command.NightStudyWithMembersCommand.Participation
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.command.NightStudyWithMembersCommand.Participations
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.command.NightStudyWithMembersCommand.PeriodAssignment
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.enumeration.NightStudyAttendanceStatus
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.enumeration.NightStudyType
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.repository.nightStudy.NightStudyQueryRepository
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.repository.nightStudyMember.NightStudyMemberQueryRepository
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.util.UUID

@Service
class OpenApiNightStudyService(
    private val nightStudyQueryRepository: NightStudyQueryRepository,
    private val nightStudyMemberQueryRepository: NightStudyMemberQueryRepository,
) {
    fun getActiveNightStudiesByType(type: NightStudyType, date: LocalDate): List<NightStudyWithMembersCommand> {
        val nightStudies = nightStudyQueryRepository
            .findAllByTypeAndStartAtLessThanEqualAndEndAtGreaterThanEqual(type, date, date)

        val leaderMap = nightStudyMemberQueryRepository.findLeaderUserIdsByNightStudies(nightStudies)
        val membersMap = nightStudyMemberQueryRepository.findAllMemberUserIdsByNightStudies(nightStudies)

        val participations = resolveParticipations(date)

        return nightStudies.map { nightStudy ->
            NightStudyWithMembersCommand(
                nightStudy = nightStudy,
                leaderId = leaderMap[nightStudy.id],
                memberIds = membersMap[nightStudy.id] ?: emptyList(),
                participations = participations,
            )
        }
    }

    private fun resolveParticipations(date: LocalDate): Participations {
        val attendedByPeriod = nightStudyQueryRepository.findAttendedUserIdsByDate(date)

        val attended1 = attendedByPeriod[1].orEmpty()
        val attended2 = attendedByPeriod[2].orEmpty()

        val assigned1 = resolveAssignments(date, 1)
        val assigned2 = resolveAssignments(date, 2)

        val values = (assigned1.keys + assigned2.keys + attended1 + attended2).associateWith { userId ->
            Participation(
                attendance = Attendance(
                    period1 = statusOf(userId, attended1, assigned1.keys),
                    period2 = statusOf(userId, attended2, assigned2.keys),
                ),
                assignment = Assignment(
                    period1 = assigned1[userId],
                    period2 = assigned2[userId],
                ),
            )
        }

        return Participations(values)
    }

    private fun resolveAssignments(date: LocalDate, period: Int): Map<UUID, PeriodAssignment> =
        nightStudyQueryRepository.findAllowedAssignmentsByDateAndPeriod(date, period)
            .groupBy { it.userId }
            .mapValues { (_, commands) -> select(commands).toPeriodAssignment() }

    private fun select(commands: List<NightStudyAssignmentCommand>): NightStudyAssignmentCommand =
        commands.firstOrNull { it.type == NightStudyType.PROJECT && it.projectRoomName != null }
            ?: commands.firstOrNull { it.type == NightStudyType.PERSONAL }
            ?: commands.first()

    private fun NightStudyAssignmentCommand.toPeriodAssignment() = PeriodAssignment(
        type = type,
        projectRoomName = projectRoomName,
        projectRoomFloor = projectRoomFloor,
    )

    private fun statusOf(userId: UUID, attended: Set<UUID>, applied: Set<UUID>) = when {
        userId in attended -> NightStudyAttendanceStatus.ATTENDANCE
        userId in applied -> NightStudyAttendanceStatus.ABSENT
        else -> NightStudyAttendanceStatus.NOT_APPLIED
    }
}
