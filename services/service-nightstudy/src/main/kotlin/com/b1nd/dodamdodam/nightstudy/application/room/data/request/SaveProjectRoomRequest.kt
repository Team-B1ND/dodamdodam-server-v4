package com.b1nd.dodamdodam.nightstudy.application.room.data.request

import jakarta.validation.constraints.NotBlank

data class SaveProjectRoomRequest(
    @NotBlank
    val name: String,
)
