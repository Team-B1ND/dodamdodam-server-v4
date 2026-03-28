package com.b1nd.dodamdodam.neis.application.schedule.data

import com.b1nd.dodamdodam.neis.application.schedule.data.response.ScheduleResponse
import com.b1nd.dodamdodam.neis.domain.schedule.entity.ScheduleEntity
import com.b1nd.dodamdodam.neis.domain.schedule.enums.ScheduleTarget

fun ScheduleEntity.toResponse(targets: List<ScheduleTarget>) = ScheduleResponse(
    publicId = publicId!!,
    title = title,
    startAt = startAt,
    endAt = endAt,
    targets = targets,
)
