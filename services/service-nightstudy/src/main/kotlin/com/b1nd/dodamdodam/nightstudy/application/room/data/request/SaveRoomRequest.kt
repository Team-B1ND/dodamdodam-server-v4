package com.b1nd.dodamdodam.nightstudy.application.room.data.request

import jakarta.validation.constraints.NotBlank

data class SaveRoomRequest(
    @NotBlank
    val name: String,
)
