package com.b1nd.dodamdodam.oauth.application.data.response

import com.b1nd.dodamdodam.oauth.domain.client.entity.OauthClient
import com.fasterxml.jackson.annotation.JsonInclude
import java.time.LocalDateTime

data class ClientResponse(
    val clientId: String,
    @JsonInclude(JsonInclude.Include.NON_NULL) val clientSecret: String? = null,
    val clientName: String,
    val redirectUris: List<String>,
    val scopes: List<String>,
    val websiteUrl: String?,
    val description: String?,
    val logoUrl: String?,
    val createdAt: LocalDateTime?,
) {
    companion object {
        fun of(client: OauthClient, rawSecret: String? = null) = ClientResponse(
            clientId = client.clientId,
            clientSecret = rawSecret,
            clientName = client.clientName,
            redirectUris = client.getRedirectUriList(),
            scopes = client.getScopeList(),
            websiteUrl = client.websiteUrl,
            description = client.description,
            logoUrl = client.logoUrl,
            createdAt = client.createdAt,
        )
    }
}
