package com.b1nd.dodamdodam.nightstudy.domain.nightstudy.service

import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.command.NightStudyWithMembersCommand
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.command.NightStudyWithMembersCommand.Attendance
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.command.NightStudyWithMembersCommand.Participation
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.command.NightStudyWithMembersCommand.Participations
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.command.NightStudyWithMembersCommand.ProjectRoom
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

        val applied1 = nightStudyQueryRepository.findAllowedUserIdsByDateAndPeriod(date, 1).toSet()
        val applied2 = nightStudyQueryRepository.findAllowedUserIdsByDateAndPeriod(date, 2).toSet()

        val projectRoom1 = nightStudyQueryRepository.findProjectRoomNamesByDateAndPeriod(date, 1)
        val projectRoom2 = nightStudyQueryRepository.findProjectRoomNamesByDateAndPeriod(date, 2)

        val values = (applied1 + applied2 + attended1 + attended2).associateWith { userId ->
            Participation(
                attendance = Attendance(
                    period1 = statusOf(userId, attended1, applied1),
                    period2 = statusOf(userId, attended2, applied2),
                ),
                projectRoom = ProjectRoom(
                    period1 = projectRoom1[userId],
                    period2 = projectRoom2[userId],
                ),
            )
        }

        return Participations(values)
    }

    private fun statusOf(userId: UUID, attended: Set<UUID>, applied: Set<UUID>) = when {
        userId in attended -> NightStudyAttendanceStatus.ATTENDANCE
        userId in applied -> NightStudyAttendanceStatus.ABSENT
        else -> NightStudyAttendanceStatus.NOT_APPLIED
    }
}
