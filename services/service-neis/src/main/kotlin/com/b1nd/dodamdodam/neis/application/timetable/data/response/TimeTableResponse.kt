package com.b1nd.dodamdodam.neis.application.timetable.data.response

import java.time.LocalDate

data class TimeTableResponse(
    val date: LocalDate,
    val grade: Int,
    val room: Int,
    val period: Int,
    val subject: String,
    val teacher: String,
)
