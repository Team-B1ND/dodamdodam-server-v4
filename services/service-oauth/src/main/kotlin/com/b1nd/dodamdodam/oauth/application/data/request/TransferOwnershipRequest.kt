package com.b1nd.dodamdodam.oauth.application.data.request

import jakarta.validation.constraints.NotBlank

data class TransferOwnershipRequest(
    @field:NotBlank val clientSecret: String,
    @field:NotBlank val newOwnerPublicId: String,
)
