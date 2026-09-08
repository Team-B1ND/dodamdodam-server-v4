package com.b1nd.dodamdodam.nightstudy.domain.nightstudy.command

import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.enumeration.NightStudyType
import java.util.UUID

data class NightStudyAssignmentCommand(
    val userId: UUID,
    val type: NightStudyType,
    val projectRoomName: String?,
    val projectRoomFloor: Int?,
)
