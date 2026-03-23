package com.b1nd.dodamdodam.oauth.domain.scope.repository

import com.b1nd.dodamdodam.oauth.domain.scope.entity.OauthScope
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Flux

interface OauthScopeRepository : ReactiveCrudRepository<OauthScope, Long> {
    fun findAllByIsActiveTrue(): Flux<OauthScope>
    fun findByScopeKeyIn(scopeKeys: Collection<String>): Flux<OauthScope>
}
