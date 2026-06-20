package com.b1nd.dodamdodam.nightstudy.domain.nightstudy.service

import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.command.NightStudyWithMembersCommand
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.enumeration.NightStudyType
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.repository.nightStudy.NightStudyAttendanceRepository
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.repository.nightStudy.NightStudyQueryRepository
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.repository.nightStudyMember.NightStudyMemberQueryRepository
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class OpenApiNightStudyService(
    private val nightStudyQueryRepository: NightStudyQueryRepository,
    private val nightStudyMemberQueryRepository: NightStudyMemberQueryRepository,
    private val nightStudyAttendanceRepository: NightStudyAttendanceRepository,
) {
    fun getActiveNightStudiesByType(type: NightStudyType, date: LocalDate): List<NightStudyWithMembersCommand> {
        val nightStudies = nightStudyQueryRepository
            .findAllByTypeAndStartAtLessThanEqualAndEndAtGreaterThanEqual(type, date, date)

        val leaderMap = nightStudyMemberQueryRepository.findLeaderUserIdsByNightStudies(nightStudies)
        val membersMap = nightStudyMemberQueryRepository.findAllMemberUserIdsByNightStudies(nightStudies)

        val attendedUserIdsByPeriod = nightStudyAttendanceRepository
            .findAllByDateAndAttendedIsTrue(date)
            .groupBy({ it.period }, { it.userId })
            .mapValues { (_, userIds) -> userIds.toSet() }

        return nightStudies.map { nightStudy ->
            NightStudyWithMembersCommand(
                nightStudy = nightStudy,
                leaderId = leaderMap[nightStudy.id],
                memberIds = membersMap[nightStudy.id] ?: emptyList(),
                attendedUserIds = attendedUserIdsByPeriod[nightStudy.period] ?: emptySet(),
            )
        }
    }
}