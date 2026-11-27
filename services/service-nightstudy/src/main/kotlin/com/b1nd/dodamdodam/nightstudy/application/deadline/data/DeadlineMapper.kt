package com.b1nd.dodamdodam.nightstudy.application.deadline.data

import com.b1nd.dodamdodam.nightstudy.application.deadline.data.response.DeadlineResponse
import com.b1nd.dodamdodam.nightstudy.domain.deadline.entity.NightStudyDeadlineEntity

fun NightStudyDeadlineEntity.toResponse() = DeadlineResponse(
    startTime = startTime,
    endTime = endTime,
)
