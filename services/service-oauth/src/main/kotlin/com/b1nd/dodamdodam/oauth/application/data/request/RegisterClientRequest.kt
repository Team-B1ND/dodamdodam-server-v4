package com.b1nd.dodamdodam.oauth.application.data.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Size

data class RegisterClientRequest(
    @field:NotBlank @field:Size(min = 2, max = 100) val clientName: String,
    @field:NotEmpty val redirectUris: List<String>,
    @field:NotEmpty val scopes: List<String>,
    val websiteUrl: String? = null,
    val description: String? = null,
    val logoUrl: String? = null,
)
