package com.b1nd.dodamdodam.neis.application.schedule.data

import com.b1nd.dodamdodam.neis.application.schedule.data.response.ScheduleResponse
import com.b1nd.dodamdodam.neis.domain.schedule.entity.ScheduleEntity

fun ScheduleEntity.toResponse() = ScheduleResponse(
    publicId = publicId!!,
    title = title,
    startDate = startDate,
    endDate = endDate,
    type = type,
    targets = targets.map { it.target },
)
