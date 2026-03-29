package com.b1nd.dodamdodam.oauth.application.data.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.util.UUID

data class TransferOwnershipRequest(
    @field:NotBlank val clientSecret: String,
    @field:NotNull val newOwnerPublicId: UUID,
)
