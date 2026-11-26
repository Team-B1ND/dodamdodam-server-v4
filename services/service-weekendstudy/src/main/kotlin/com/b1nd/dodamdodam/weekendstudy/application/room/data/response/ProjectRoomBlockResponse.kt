package com.b1nd.dodamdodam.weekendstudy.application.room.data.response

import java.time.LocalDate

data class ProjectRoomBlockResponse(
    val id: Long,
    val period: Int,
    val description: String,
    val startAt: LocalDate,
    val endAt: LocalDate,
)
