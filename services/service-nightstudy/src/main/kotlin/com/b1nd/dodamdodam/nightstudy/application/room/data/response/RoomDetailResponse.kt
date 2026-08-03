package com.b1nd.dodamdodam.nightstudy.application.room.data.response

import java.util.UUID

data class RoomDetailResponse(
    val roomMember: List<RoomMemberResponse>,
)

data class RoomMemberResponse(
    val userId: UUID,
    val name: String,
    val profileImage: String?,
    val grade: Int,
    val room: Int,
    val number: Int,
    val attended: Boolean,
)
