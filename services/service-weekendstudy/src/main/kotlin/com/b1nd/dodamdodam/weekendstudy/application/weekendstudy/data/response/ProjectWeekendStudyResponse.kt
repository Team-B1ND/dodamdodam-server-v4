package com.b1nd.dodamdodam.weekendstudy.application.weekendstudy.data.response

import java.time.LocalDate
import java.util.UUID

data class ProjectWeekendStudyResponse(
    val id: UUID,
    val name: String,
    val description: String,
    val date: LocalDate,
    val periods: List<Int>,
    val isLeader: Boolean,
    val room: RoomInfo?,
) {
    data class RoomInfo(
        val id: Long,
        val name: String,
    )
}
