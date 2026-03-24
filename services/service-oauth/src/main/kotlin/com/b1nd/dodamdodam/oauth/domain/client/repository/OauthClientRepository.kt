package com.b1nd.dodamdodam.oauth.domain.client.repository

import com.b1nd.dodamdodam.oauth.domain.client.entity.OauthClient
import kotlinx.coroutines.flow.Flow
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import java.util.UUID

interface OauthClientRepository : CoroutineCrudRepository<OauthClient, Long> {
    suspend fun findByClientId(clientId: String): OauthClient?
    suspend fun findByClientIdAndIsActiveTrue(clientId: String): OauthClient?
    fun findAllByOwnerPublicIdAndIsActiveTrue(ownerPublicId: UUID): Flow<OauthClient>
}
