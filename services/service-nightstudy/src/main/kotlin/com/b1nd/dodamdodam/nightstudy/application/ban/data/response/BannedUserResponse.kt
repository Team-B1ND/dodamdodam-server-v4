package com.b1nd.dodamdodam.nightstudy.application.ban.data.response

import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

data class BannedUserResponse(
    val userId: UUID,
    val reason: String,
    val endAt: LocalDate,
    val createdAt: LocalDateTime,
)
