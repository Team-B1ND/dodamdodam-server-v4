package com.b1nd.dodamdodam.nightstudy.application.ban.data.request

import com.fasterxml.jackson.annotation.JsonFormat
import jakarta.validation.constraints.Future
import jakarta.validation.constraints.NotBlank
import java.time.LocalDate
import java.util.UUID

data class BanUserRequest(
    val userId: UUID,
    @NotBlank
    val reason: String,
    @Future
    @JsonFormat(pattern = "yyyy-MM-dd")
    val endAt: LocalDate,
)
