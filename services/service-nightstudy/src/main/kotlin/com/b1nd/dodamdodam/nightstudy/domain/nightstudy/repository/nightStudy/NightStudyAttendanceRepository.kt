package com.b1nd.dodamdodam.nightstudy.domain.nightstudy.repository.nightStudy

import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.entity.NightStudyAttendanceEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDate
import java.util.UUID

interface NightStudyAttendanceRepository: JpaRepository<NightStudyAttendanceEntity, Long> {
    fun findByUserIdAndDateAndPeriod(userId: UUID, date: LocalDate, period: Int): NightStudyAttendanceEntity?
    fun findAllByDateAndAttendedIsTrue(date: LocalDate): List<NightStudyAttendanceEntity>
    fun findAllByUserIdInAndDateAndPeriodAndAttendedIsTrue(
        userIds: Collection<UUID>,
        date: LocalDate,
        period: Int,
    ): List<NightStudyAttendanceEntity>
}
