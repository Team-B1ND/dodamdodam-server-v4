package com.b1nd.dodamdodam.oauth.domain.scope.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("oauth_scopes")
data class OauthScope(
    @Id val id: Long? = null,
    @Column("scope_key") val scopeKey: String,
    val description: String,
    @Column("is_active") val isActive: Boolean = true,
)
