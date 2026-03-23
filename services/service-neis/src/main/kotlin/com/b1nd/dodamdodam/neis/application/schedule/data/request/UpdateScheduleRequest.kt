package com.b1nd.dodamdodam.neis.application.schedule.data.request

import com.b1nd.dodamdodam.neis.domain.schedule.enums.ScheduleTarget
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import java.time.LocalDate
import java.util.UUID

data class UpdateScheduleRequest(
    val publicId: UUID,

    @field:NotBlank
    val title: String,

    val startDate: LocalDate,

    val endDate: LocalDate,

    @field:NotEmpty
    val targets: List<ScheduleTarget>,
)
