package com.b1nd.dodamdodam.oauth.application.data.response

import com.b1nd.dodamdodam.oauth.domain.scope.entity.OauthScope

data class ScopeResponse(val scopeKey: String, val description: String) {
    companion object {
        fun of(scope: OauthScope) = ScopeResponse(scopeKey = scope.scopeKey, description = scope.description)
    }
}
