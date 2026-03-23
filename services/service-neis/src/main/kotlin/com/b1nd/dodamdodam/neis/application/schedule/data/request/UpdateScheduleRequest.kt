package com.b1nd.dodamdodam.neis.application.schedule.data.request

import com.b1nd.dodamdodam.neis.domain.schedule.enums.ScheduleTarget
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import java.time.LocalDate
import java.util.UUID

data class UpdateScheduleRequest(
    @NotNull
    val publicId: UUID,

    @NotBlank
    val title: String,

    @NotNull
    val startAt: LocalDate,

    @NotNull
    val endAt: LocalDate,

    @NotEmpty
    val targets: List<ScheduleTarget>,
)
