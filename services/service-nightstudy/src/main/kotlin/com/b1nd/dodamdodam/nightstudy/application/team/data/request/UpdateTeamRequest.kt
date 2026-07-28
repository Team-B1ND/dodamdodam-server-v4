package com.b1nd.dodamdodam.nightstudy.application.team.data.request

import com.b1nd.dodamdodam.nightstudy.domain.team.NightStudyTeamEntity
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.util.UUID

data class UpdateTeamRequest(
    @field:NotNull
    val publicId: UUID?,
    @field:NotBlank
    val name: String,
    val description: String?,
    val imageUrl: String?,
) {
    fun toEntity(): NightStudyTeamEntity =
        NightStudyTeamEntity(
            name = name,
            description = description,
            imageUrl = imageUrl
        )
}
