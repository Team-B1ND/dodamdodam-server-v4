package com.b1nd.dodamdodam.nightstudy.application.openapi.data.response

import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.enumeration.NightStudyStatusType
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.enumeration.NightStudyType
import java.time.LocalDate

data class OpenApiNightStudyResponse(
    val id: Long,
    val name: String?,
    val description: String,
    val period: Int,
    val startAt: LocalDate,
    val endAt: LocalDate,
    val needPhone: Boolean,
    val needPhoneReason: String?,
    val status: NightStudyStatusType,
    val leader: OpenApiUserInfoResponse,
    val members: List<OpenApiUserInfoResponse>,
    val rejectionReason: String?,
    val type: NightStudyType,
)
