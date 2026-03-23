package com.b1nd.dodamdodam.oauth.application.data.response

data class AuthorizeResponse(
    val clientName: String,
    val clientId: String,
    val scopes: List<ScopeResponse>,
    val redirectUri: String,
    val state: String?,
    val codeChallenge: String?,
    val codeChallengeMethod: String?,
    val trusted: Boolean,
    val consented: Boolean,
)
