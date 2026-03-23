package com.b1nd.dodamdodam.oauth.domain.token.repository

import com.b1nd.dodamdodam.oauth.domain.token.entity.OauthToken
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Mono

interface OauthTokenRepository : ReactiveCrudRepository<OauthToken, Long> {
    fun findByAccessTokenHash(accessTokenHash: String): Mono<OauthToken>
    fun findByRefreshToken(refreshToken: String): Mono<OauthToken>
}
