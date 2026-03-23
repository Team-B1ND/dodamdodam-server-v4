package com.b1nd.dodamdodam.oauth.domain.client.repository

import com.b1nd.dodamdodam.oauth.domain.client.entity.OauthClient
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface OauthClientRepository : CoroutineCrudRepository<OauthClient, Long> {
    suspend fun findByClientId(clientId: String): OauthClient?
    suspend fun findByClientIdAndIsActiveTrue(clientId: String): OauthClient?
}
