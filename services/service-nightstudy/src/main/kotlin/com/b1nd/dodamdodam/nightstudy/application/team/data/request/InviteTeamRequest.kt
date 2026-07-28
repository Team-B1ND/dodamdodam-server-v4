package com.b1nd.dodamdodam.nightstudy.application.team.data.request

import jakarta.validation.constraints.NotEmpty
import java.util.UUID

data class InviteTeamRequest(
    val publicId: UUID,

    @field:NotEmpty
    val members: List<UUID>
)