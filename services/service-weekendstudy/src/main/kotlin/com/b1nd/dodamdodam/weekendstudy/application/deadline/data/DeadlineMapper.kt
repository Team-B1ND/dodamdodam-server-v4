package com.b1nd.dodamdodam.weekendstudy.application.deadline.data

import com.b1nd.dodamdodam.weekendstudy.application.deadline.data.response.DeadlineResponse
import com.b1nd.dodamdodam.weekendstudy.domain.deadline.entity.WeekendStudyDeadlineEntity

fun WeekendStudyDeadlineEntity.toResponse() = DeadlineResponse(
    startDayOfWeek = startDayOfWeek,
    startTime = startTime,
    endDayOfWeek = endDayOfWeek,
    endTime = endTime,
)
