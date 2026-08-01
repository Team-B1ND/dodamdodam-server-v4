package com.b1nd.dodamdodam.nightstudy.application.room.data.response

data class RoomSummaryResponse(
    val roomId: String,
    val roomName: String,
    val memberCount: Int,
    val unchecked: Int,
)
