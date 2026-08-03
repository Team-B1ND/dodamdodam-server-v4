package com.b1nd.dodamdodam.nightstudy.domain.nightstudy.service

import com.b1nd.dodamdodam.core.common.exception.BasicException
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.entity.NightStudyAttendanceEntity
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.exception.NightStudyExceptionCode
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.repository.nightStudy.NightStudyQueryRepository
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.repository.nightStudy.NightStudyAttendanceRepository
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.util.UUID

@Service
class NightStudyAttendanceService(
    private val attendanceRepository: NightStudyAttendanceRepository,
    private val nightStudyQueryRepository: NightStudyQueryRepository,
) {
    fun update(userId: UUID, date: LocalDate, period: Int, attended: Boolean): NightStudyAttendanceEntity {
        validateAllowedMember(userId, date, period)

        val attendance = attendanceRepository.findByUserIdAndDateAndPeriod(userId, date, period)
            ?: return attendanceRepository.save(
                NightStudyAttendanceEntity(
                    userId = userId,
                    date = date,
                    period = period,
                    attended = attended
                )
            )

        attendance.update(attended)
        return attendance
    }

    fun get(userId: UUID, date: LocalDate, period: Int): NightStudyAttendanceEntity {
        validateAllowedMember(userId, date, period)

        return attendanceRepository.findByUserIdAndDateAndPeriod(userId, date, period)
            ?: NightStudyAttendanceEntity(
                userId = userId,
                date = date,
                period = period,
                attended = false
            )
    }

    fun count(date: LocalDate, period: Int): Pair<Int, Int> {
        val allowedUserIds = nightStudyQueryRepository.findAllowedUserIdsByDateAndPeriod(date, period)
        val attendedCount = nightStudyQueryRepository.countAttendedUserIdsByDateAndPeriod(date, period, allowedUserIds)
            .toInt()

        return attendedCount to (allowedUserIds.size - attendedCount)
    }

    fun getAttendedUserIds(userIds: Collection<UUID>, date: LocalDate, period: Int): Set<UUID> {
        if (userIds.isEmpty()) return emptySet()

        return attendanceRepository
            .findAllByUserIdInAndDateAndPeriodAndAttendedIsTrue(userIds, date, period)
            .map { it.userId }
            .toSet()
    }

    private fun validateAllowedMember(userId: UUID, date: LocalDate, period: Int) {
        if (!nightStudyQueryRepository.existsAllowedMemberByUserIdAndDateAndPeriod(userId, date, period)) {
            throw BasicException(NightStudyExceptionCode.NOT_NIGHT_STUDY_MEMBER)
        }
    }
}
