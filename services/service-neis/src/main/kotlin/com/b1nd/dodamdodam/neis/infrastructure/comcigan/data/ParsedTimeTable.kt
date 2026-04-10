package com.b1nd.dodamdodam.neis.infrastructure.comcigan.data

import java.time.LocalDate

data class ParsedTimeTable(
    val date: LocalDate,
    val grade: Int,
    val room: Int,
    val period: Int,
    val subject: String,
    val teacher: String,
    val isReplaced: Boolean = false,
)
