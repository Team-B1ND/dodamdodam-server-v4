package com.b1nd.dodamdodam.nightstudy.application.nightstudy.data.response

import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.enumeration.NightStudyStatusType
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.enumeration.NightStudyType
import java.time.LocalDate
import java.util.UUID

data class NightStudyApplicationResponse(
    val id: UUID,
    val name: String?,
    val description: String,
    val period: Int,
    val startAt: LocalDate,
    val endAt: LocalDate,
    val needPhone: Boolean,
    val needPhoneReason: String?,
    val status: NightStudyStatusType,
    val leader: NightStudyApplicantResponse,
    val members: List<NightStudyApplicantResponse>,
    val rejectionReason: String?,
    val type: NightStudyType,
    val room: RoomInfo?,
) {
    data class RoomInfo(
        val id: UUID,
        val name: String,
    )
}
