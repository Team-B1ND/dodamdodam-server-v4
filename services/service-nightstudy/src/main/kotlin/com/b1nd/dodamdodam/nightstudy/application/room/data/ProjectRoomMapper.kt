package com.b1nd.dodamdodam.nightstudy.application.room.data

import com.b1nd.dodamdodam.nightstudy.application.room.data.request.SaveProjectRoomRequest
import com.b1nd.dodamdodam.nightstudy.application.room.data.response.ProjectRoomResponse
import com.b1nd.dodamdodam.nightstudy.domain.room.entity.ProjectRoomEntity

fun SaveProjectRoomRequest.toEntity() = ProjectRoomEntity(name = name)

fun ProjectRoomEntity.toResponse() = ProjectRoomResponse(
    id = id!!,
    name = name,
)

fun List<ProjectRoomEntity>.toResponseList() = map { it.toResponse() }
