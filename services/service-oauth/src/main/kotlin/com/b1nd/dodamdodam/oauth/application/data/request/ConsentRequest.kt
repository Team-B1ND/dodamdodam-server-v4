package com.b1nd.dodamdodam.oauth.application.data.request

import jakarta.validation.constraints.NotBlank

data class ConsentRequest(
    @field:NotBlank val clientId: String,
    @field:NotBlank val redirectUri: String,
    val scope: String,
    val state: String? = null,
    val codeChallenge: String? = null,
    val codeChallengeMethod: String? = null,
    val approved: Boolean,
)
