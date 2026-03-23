package com.b1nd.dodamdodam.oauth.domain.consent.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("oauth_consents")
data class OauthConsent(
    @Id val id: Long? = null,
    @Column("user_public_id") val userPublicId: String,
    @Column("client_id") val clientId: String,
    val scopes: String,
    @Column("created_at") val createdAt: LocalDateTime? = null,
)
