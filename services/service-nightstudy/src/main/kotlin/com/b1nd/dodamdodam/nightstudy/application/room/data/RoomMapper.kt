package com.b1nd.dodamdodam.nightstudy.application.room.data

import com.b1nd.dodamdodam.nightstudy.application.room.data.request.SaveRoomRequest
import com.b1nd.dodamdodam.nightstudy.application.room.data.response.RoomResponse
import com.b1nd.dodamdodam.nightstudy.domain.room.entity.RoomEntity

fun SaveRoomRequest.toEntity() = RoomEntity(name = name)

fun RoomEntity.toResponse() = RoomResponse(
    id = publicId!!,
    name = name,
)

fun List<RoomEntity>.toResponseList() = map { it.toResponse() }
