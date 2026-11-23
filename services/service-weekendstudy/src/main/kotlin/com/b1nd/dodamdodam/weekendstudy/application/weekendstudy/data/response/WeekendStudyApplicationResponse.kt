package com.b1nd.dodamdodam.weekendstudy.application.weekendstudy.data.response

import com.b1nd.dodamdodam.weekendstudy.domain.weekendstudy.enumeration.WeekendStudyType
import java.time.LocalDate
import java.util.UUID

data class WeekendStudyApplicationResponse(
    val id: UUID,
    val type: WeekendStudyType,
    val date: LocalDate,
    val periods: List<Int>,
    val name: String?,
    val description: String?,
    val room: RoomInfo?,
    val leader: WeekendStudyApplicantResponse,
    val members: List<WeekendStudyApplicantResponse>,
) {
    data class RoomInfo(
        val id: Long,
        val name: String,
    )
}
