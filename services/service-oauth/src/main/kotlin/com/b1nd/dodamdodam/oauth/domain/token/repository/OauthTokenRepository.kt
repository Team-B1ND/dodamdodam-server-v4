package com.b1nd.dodamdodam.oauth.domain.token.repository

import com.b1nd.dodamdodam.oauth.domain.token.entity.OauthToken
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface OauthTokenRepository : CoroutineCrudRepository<OauthToken, Long> {
    suspend fun findByAccessTokenHash(accessTokenHash: String): OauthToken?
    suspend fun findByRefreshToken(refreshToken: String): OauthToken?
}
