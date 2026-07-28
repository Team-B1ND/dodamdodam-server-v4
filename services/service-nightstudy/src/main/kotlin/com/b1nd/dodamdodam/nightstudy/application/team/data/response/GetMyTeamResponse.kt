package com.b1nd.dodamdodam.nightstudy.application.team.data.response

import com.b1nd.dodamdodam.nightstudy.domain.team.NightStudyTeamEntity
import java.util.UUID

data class GetMyTeamResponse(
    val publicId: UUID?,
    val name: String,
    val imageUrl: String?
) {
    companion object {
        fun of(nightStudyTeamEntity: NightStudyTeamEntity): GetMyTeamResponse =
            GetMyTeamResponse(
                publicId = nightStudyTeamEntity.publicId,
                name = nightStudyTeamEntity.name,
                imageUrl = nightStudyTeamEntity.imageUrl
            )

        fun fromList(nightStudyTeamEntityList: List<NightStudyTeamEntity>): List<GetMyTeamResponse> =
            nightStudyTeamEntityList.map(GetMyTeamResponse::of)
    }
}
