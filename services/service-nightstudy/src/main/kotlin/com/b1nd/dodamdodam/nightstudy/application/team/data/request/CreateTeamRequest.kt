package com.b1nd.dodamdodam.nightstudy.application.team.data.request

import com.b1nd.dodamdodam.nightstudy.domain.team.NightStudyTeamEntity
import jakarta.validation.constraints.NotBlank

data class CreateTeamRequest(
    @field:NotBlank
    val name: String,
    val description: String?,
    val imageUrl: String?
) {
    fun toEntity(): NightStudyTeamEntity =
         NightStudyTeamEntity(
            name = this.name,
            description = this.description,
            imageUrl = this.imageUrl
         )
}
