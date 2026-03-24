package com.b1nd.dodamdodam.nightstudy.application.nightstudy.data.request

import com.fasterxml.jackson.annotation.JsonFormat
import jakarta.validation.constraints.NotBlank
import java.time.LocalDate
import java.util.UUID

data class ProjectNightStudyApplyRequest(
    @field:NotBlank
    val name: String,
    @field:NotBlank
    val description: String,
    val period: Int,
    @JsonFormat(pattern = "yyyy-MM-dd")
    val startAt: LocalDate,
    @JsonFormat(pattern = "yyyy-MM-dd")
    val endAt: LocalDate,
    val members: List<UUID>,
)
