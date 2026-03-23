package com.b1nd.dodamdodam.oauth.domain.token.repository

import com.b1nd.dodamdodam.oauth.domain.token.entity.OauthAuthorizationCode
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Mono

interface OauthAuthorizationCodeRepository : ReactiveCrudRepository<OauthAuthorizationCode, Long> {
    fun findByCode(code: String): Mono<OauthAuthorizationCode>
}
