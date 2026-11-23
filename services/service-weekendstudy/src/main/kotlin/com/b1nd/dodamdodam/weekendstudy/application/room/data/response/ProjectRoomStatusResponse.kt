package com.b1nd.dodamdodam.weekendstudy.application.room.data.response

import com.b1nd.dodamdodam.weekendstudy.domain.room.enumeration.ProjectRoomPeriodStatus

data class ProjectRoomStatusResponse(
    val id: Long,
    val name: String,
    val floor: Int,
    val periods: List<PeriodStatus>,
) {
    data class PeriodStatus(
        val period: Int,
        val status: ProjectRoomPeriodStatus,
        val reason: String?,
    )
}
