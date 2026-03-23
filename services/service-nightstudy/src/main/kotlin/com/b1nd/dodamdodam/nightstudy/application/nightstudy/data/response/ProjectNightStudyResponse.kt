package com.b1nd.dodamdodam.nightstudy.application.nightstudy.data.response

import java.time.LocalDate
import java.util.UUID

data class ProjectNightStudyResponse(
    val id: UUID,
    val name: String,
    val description: String,
    val period: Int,
    val startAt: LocalDate,
    val endAt: LocalDate,
    val rejectionReason: String?,
    val isLeader: Boolean,
)
