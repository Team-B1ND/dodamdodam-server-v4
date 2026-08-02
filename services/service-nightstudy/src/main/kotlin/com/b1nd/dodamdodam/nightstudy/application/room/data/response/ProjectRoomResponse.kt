package com.b1nd.dodamdodam.nightstudy.application.room.data.response

data class ProjectRoomResponse(
    val id: Long,
    val name: String,
    val inUse: InUse,
) {
    data class InUse(
        val period1: Boolean,
        val period2: Boolean,
    )
}