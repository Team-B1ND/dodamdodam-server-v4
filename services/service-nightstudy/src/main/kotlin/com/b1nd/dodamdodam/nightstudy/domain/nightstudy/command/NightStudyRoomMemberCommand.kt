package com.b1nd.dodamdodam.nightstudy.domain.nightstudy.command

import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.enumeration.NightStudyType
import java.util.UUID

data class NightStudyRoomMemberCommand(
    val userId: UUID,
    val type: NightStudyType,
    val projectRoomId: Long?,
)
