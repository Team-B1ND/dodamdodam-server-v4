package com.b1nd.dodamdodam.nightstudy.application.room.data

import com.b1nd.dodamdodam.nightstudy.application.room.data.request.SaveProjectRoomRequest
import com.b1nd.dodamdodam.nightstudy.application.room.data.response.ProjectRoomResponse
import com.b1nd.dodamdodam.nightstudy.domain.room.command.RoomPeriodCommand
import com.b1nd.dodamdodam.nightstudy.domain.room.entity.ProjectRoomEntity

fun SaveProjectRoomRequest.toEntity() = ProjectRoomEntity(name = name)

fun ProjectRoomEntity.toResponse(inUsePeriods: Set<Pair<Long, Int>>): ProjectRoomResponse {
    val roomId = id!!
    return ProjectRoomResponse(
        id = roomId,
        name = name,
        inUse = ProjectRoomResponse.InUse(
            period1 = (roomId to 1) in inUsePeriods,
            period2 = (roomId to 2) in inUsePeriods,
        ),
    )
}

fun List<ProjectRoomEntity>.toResponseList(inUsePeriods: List<RoomPeriodCommand>): List<ProjectRoomResponse> {
    val index = inUsePeriods.mapTo(mutableSetOf()) { it.roomId to it.period }
    return map { it.toResponse(index) }
}