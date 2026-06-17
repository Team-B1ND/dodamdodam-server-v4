package com.b1nd.dodamdodam.nightstudy.domain.nightstudy.repository.nightStudyAttendance

import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.entity.NightStudyAttendanceEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDate
import java.util.UUID

interface NightStudyAttendanceRepository : JpaRepository<NightStudyAttendanceEntity, Long> {
    fun findByUserIdAndDateAndPeriod(
        userId: UUID,
        date: LocalDate,
        period: Int
    ): NightStudyAttendanceEntity?

    fun findAllByDateAndPeriodAndAttendedTrueAndUserIdIn(
        date: LocalDate,
        period: Int,
        userIds: Collection<UUID>
    ): List<NightStudyAttendanceEntity>
}
