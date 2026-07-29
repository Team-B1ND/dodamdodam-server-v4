package com.b1nd.dodamdodam.nightstudy.application.team.data.response

import com.b1nd.dodamdodam.nightstudy.domain.team.NightStudyTeamEntity
import java.util.UUID

data class GetTeamResponse(
    val publicId: UUID?,
    val name: String,
    val description: String?,
    val imageUrl: String?
) {
    companion object {
        fun of(nightStudyTeamEntity: NightStudyTeamEntity): GetTeamResponse =
            GetTeamResponse(
                publicId = nightStudyTeamEntity.publicId,
                name = nightStudyTeamEntity.name,
                description = nightStudyTeamEntity.description,
                imageUrl = nightStudyTeamEntity.imageUrl
            )

        fun fromList(nightStudyTeamEntityList: List<NightStudyTeamEntity>): List<GetTeamResponse> =
            nightStudyTeamEntityList.map(GetTeamResponse::of)
    }
}
