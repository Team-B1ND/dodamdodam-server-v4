package com.b1nd.dodamdodam.weekendstudy.application.weekendstudy.data.request

import com.fasterxml.jackson.annotation.JsonFormat
import jakarta.validation.constraints.NotBlank
import java.time.LocalDate
import java.util.UUID

data class ProjectWeekendStudyApplyRequest(
    @NotBlank
    val name: String,
    @NotBlank
    val description: String,
    @JsonFormat(pattern = "yyyy-MM-dd")
    val date: LocalDate,
    val periods: List<Int>,
    val members: List<UUID>,
    val roomId: Long,
)
