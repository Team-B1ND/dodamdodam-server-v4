package com.b1nd.dodamdodam.weekendstudy.application.room.data

import com.b1nd.dodamdodam.weekendstudy.application.room.data.request.BlockProjectRoomRequest
import com.b1nd.dodamdodam.weekendstudy.application.room.data.request.SaveProjectRoomRequest
import com.b1nd.dodamdodam.weekendstudy.application.room.data.response.ProjectRoomResponse
import com.b1nd.dodamdodam.weekendstudy.domain.room.entity.ProjectRoomBlockEntity
import com.b1nd.dodamdodam.weekendstudy.domain.room.entity.ProjectRoomEntity

fun SaveProjectRoomRequest.toEntity() = ProjectRoomEntity(name = name, floor = floor)

fun BlockProjectRoomRequest.toEntities(room: ProjectRoomEntity) = periods.map {
    ProjectRoomBlockEntity(room = room, period = it, description = description, startAt = startAt, endAt = endAt)
}

fun ProjectRoomEntity.toResponse() = ProjectRoomResponse(
    id = id!!,
    name = name,
    floor = floor,
)
