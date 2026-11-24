package com.b1nd.dodamdodam.weekendstudy.application.room.data

import com.b1nd.dodamdodam.weekendstudy.application.room.data.request.BlockProjectRoomRequest
import com.b1nd.dodamdodam.weekendstudy.application.room.data.request.SaveProjectRoomRequest
import com.b1nd.dodamdodam.weekendstudy.application.room.data.response.ProjectRoomBlockResponse
import com.b1nd.dodamdodam.weekendstudy.application.room.data.response.ProjectRoomResponse
import com.b1nd.dodamdodam.weekendstudy.application.room.data.response.ProjectRoomStatusResponse
import com.b1nd.dodamdodam.weekendstudy.domain.room.entity.ProjectRoomBlockEntity
import com.b1nd.dodamdodam.weekendstudy.domain.room.entity.ProjectRoomEntity
import com.b1nd.dodamdodam.weekendstudy.domain.room.enumeration.ProjectRoomPeriodStatus

fun SaveProjectRoomRequest.toEntity() = ProjectRoomEntity(name = name, floor = floor)

fun BlockProjectRoomRequest.toEntities(room: ProjectRoomEntity) = periods.map {
    ProjectRoomBlockEntity(room = room, period = it, description = description, startAt = startAt, endAt = endAt)
}

fun ProjectRoomEntity.toResponse() = ProjectRoomResponse(
    id = id!!,
    name = name,
    floor = floor,
    enabled = enabled,
)

fun ProjectRoomBlockEntity.toResponse() = ProjectRoomBlockResponse(
    id = id!!,
    period = period,
    description = description,
    startAt = startAt,
    endAt = endAt,
)

fun ProjectRoomEntity.toStatusResponse(
    periods: List<Int>,
    blocks: List<ProjectRoomBlockEntity>,
    reservedPeriods: Set<Int>,
) = ProjectRoomStatusResponse(
    id = id!!,
    name = name,
    floor = floor,
    periods = periods.map { period ->
        val block = blocks.firstOrNull { it.period == period }
        ProjectRoomStatusResponse.PeriodStatus(
            period = period,
            status = when {
                block != null -> ProjectRoomPeriodStatus.BLOCKED
                period in reservedPeriods -> ProjectRoomPeriodStatus.RESERVED
                else -> ProjectRoomPeriodStatus.AVAILABLE
            },
            reason = block?.description,
        )
    },
)
