package com.b1nd.dodamdodam.nightstudy.domain.nightstudy.command

import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.entity.NightStudyEntity
import java.util.UUID

data class NightStudyWithMembersCommand(
    val nightStudy: NightStudyEntity,
    val leaderId: UUID?,
    val memberIds: List<UUID>
)
