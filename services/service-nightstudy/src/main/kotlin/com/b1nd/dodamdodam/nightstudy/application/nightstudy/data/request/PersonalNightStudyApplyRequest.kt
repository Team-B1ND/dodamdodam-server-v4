package com.b1nd.dodamdodam.nightstudy.application.nightstudy.data.request

import com.fasterxml.jackson.annotation.JsonFormat
import jakarta.validation.constraints.NotBlank
import java.time.LocalDate

data class PersonalNightStudyApplyRequest(
    @NotBlank
    val description: String,
    val period: Int,
    @JsonFormat(pattern = "yyyy-MM-dd")
    val startAt: LocalDate,
    @JsonFormat(pattern = "yyyy-MM-dd")
    val endAt: LocalDate,
    val needPhone: Boolean,
    val needPhoneReason: String?,
)
