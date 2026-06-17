package com.b1nd.dodamdodam.nightstudy.application.nightstudy.data.response

import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.entity.NightStudyAttendanceEntity
import java.time.LocalDate
import java.util.UUID

data class NightStudyAttendanceResponse(
    val userId: UUID,
    val date: LocalDate,
    val period: Int,
    val attended: Boolean,
) {
    companion object {
        fun from(entity: NightStudyAttendanceEntity) = NightStudyAttendanceResponse(
            userId = entity.userId,
            date = entity.date,
            period = entity.period,
            attended = entity.attended,
        )
    }
}
