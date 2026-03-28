package com.b1nd.dodamdodam.neis.application.timetable.data

import com.b1nd.dodamdodam.neis.application.timetable.data.response.TimeTableResponse
import com.b1nd.dodamdodam.neis.domain.timetable.entity.TimeTableEntity

fun TimeTableEntity.toResponse() = TimeTableResponse(
    date = date,
    grade = grade,
    room = room,
    period = period,
    subject = subject,
    teacher = teacher,
)
