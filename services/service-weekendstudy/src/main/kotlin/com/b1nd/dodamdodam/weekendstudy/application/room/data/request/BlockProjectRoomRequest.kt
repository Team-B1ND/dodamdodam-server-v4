package com.b1nd.dodamdodam.weekendstudy.application.room.data.request

import com.fasterxml.jackson.annotation.JsonFormat
import jakarta.validation.constraints.NotBlank
import java.time.LocalDate

data class BlockProjectRoomRequest(
    @JsonFormat(pattern = "yyyy-MM-dd")
    val startAt: LocalDate,
    @JsonFormat(pattern = "yyyy-MM-dd")
    val endAt: LocalDate,
    val periods: List<Int>,
    @NotBlank
    val description: String,
)
