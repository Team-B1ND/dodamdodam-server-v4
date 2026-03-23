package com.b1nd.dodamdodam.oauth.domain.client.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime
import java.util.UUID

@Table("oauth_clients")
data class OauthClient(
    @Id val id: Long? = null,
    @Column("client_id") val clientId: String,
    @Column("client_secret") val clientSecret: String,
    @Column("owner_public_id") var ownerPublicId: UUID,
    @Column("client_name") val clientName: String,
    @Column("redirect_uris") val redirectUris: String,
    val scopes: String,
    @Column("website_url") val websiteUrl: String? = null,
    val description: String? = null,
    @Column("logo_url") val logoUrl: String? = null,
    @Column("is_active") val isActive: Boolean = true,
    val trusted: Boolean = false,
    @Column("created_at") val createdAt: LocalDateTime? = null,
    @Column("updated_at") val updatedAt: LocalDateTime? = null,
) {
    fun getRedirectUriList(): List<String> = redirectUris.split(",").map { it.trim() }
    fun getScopeList(): List<String> = scopes.split(" ").filter { it.isNotBlank() }
}
