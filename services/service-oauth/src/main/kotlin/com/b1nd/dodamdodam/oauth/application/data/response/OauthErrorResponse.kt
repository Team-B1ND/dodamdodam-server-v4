package com.b1nd.dodamdodam.oauth.application.data.response

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

@JsonInclude(JsonInclude.Include.NON_NULL)
data class OauthErrorResponse(
    val error: String,
    @JsonProperty("error_description") val errorDescription: String? = null,
)
