package com.b1nd.dodamdodam.oauth.domain.token.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("oauth_authorization_codes")
data class OauthAuthorizationCode(
    @Id val id: Long? = null,
    val code: String,
    @Column("client_id") val clientId: String,
    @Column("user_public_id") val userPublicId: String,
    @Column("redirect_uri") val redirectUri: String,
    val scopes: String,
    @Column("code_challenge") val codeChallenge: String? = null,
    @Column("code_challenge_method") val codeChallengeMethod: String? = null,
    @Column("expires_at") val expiresAt: LocalDateTime,
    val used: Boolean = false,
    @Column("created_at") val createdAt: LocalDateTime? = null,
)
