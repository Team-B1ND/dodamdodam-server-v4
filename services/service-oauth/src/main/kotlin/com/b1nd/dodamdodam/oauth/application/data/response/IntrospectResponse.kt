package com.b1nd.dodamdodam.oauth.application.data.response

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

@JsonInclude(JsonInclude.Include.NON_NULL)
data class IntrospectResponse(
    val active: Boolean,
    val scope: String? = null,
    @JsonProperty("client_id") val clientId: String? = null,
    val sub: String? = null,
    val exp: Long? = null,
    val iat: Long? = null,
    @JsonProperty("token_type") val tokenType: String? = null,
)
