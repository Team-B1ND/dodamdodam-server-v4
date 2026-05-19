package com.b1nd.dodamdodam.nightstudy.application.nightstudy.data.response

import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.entity.NightStudyEntity
import java.time.LocalDate

data class NightStudyRejectionHistoryResponse(
    val description: String,
    val rejectionReason: String,
    val stratAt: LocalDate,
    val endAt: LocalDate
) {
    companion object {
        fun of(entity: NightStudyEntity): NightStudyRejectionHistoryResponse =
            NightStudyRejectionHistoryResponse(
                description = entity.description,
                rejectionReason = entity.rejectionReason ?: "",
                stratAt = entity.startAt,
                endAt = entity.endAt,
            )

        fun fromList(entities: List<NightStudyEntity>): List<NightStudyRejectionHistoryResponse> =
            entities.map(::of)
    }
}