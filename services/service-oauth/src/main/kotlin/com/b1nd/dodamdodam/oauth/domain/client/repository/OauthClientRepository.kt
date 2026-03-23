package com.b1nd.dodamdodam.oauth.domain.client.repository

import com.b1nd.dodamdodam.oauth.domain.client.entity.OauthClient
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Mono

interface OauthClientRepository : ReactiveCrudRepository<OauthClient, Long> {
    fun findByClientId(clientId: String): Mono<OauthClient>
    fun findByClientIdAndIsActiveTrue(clientId: String): Mono<OauthClient>
}
