package com.b1nd.dodamdodam.nightstudy.application.room.data.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class SaveProjectRoomRequest(
    @NotBlank
    val name: String,
    @NotNull
    val floor: Int
)
