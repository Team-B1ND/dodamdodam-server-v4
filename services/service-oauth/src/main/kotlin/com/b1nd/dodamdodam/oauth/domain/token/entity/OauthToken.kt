package com.b1nd.dodamdodam.oauth.domain.token.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("oauth_tokens")
data class OauthToken(
    @Id val id: Long? = null,
    @Column("access_token_hash") val accessTokenHash: String,
    @Column("access_token") val accessToken: String,
    @Column("refresh_token") val refreshToken: String,
    @Column("client_id") val clientId: String,
    @Column("user_public_id") val userPublicId: String,
    val scopes: String,
    @Column("access_expires_at") val accessExpiresAt: LocalDateTime,
    @Column("refresh_expires_at") val refreshExpiresAt: LocalDateTime,
    val revoked: Boolean = false,
    @Column("created_at") val createdAt: LocalDateTime? = null,
)
