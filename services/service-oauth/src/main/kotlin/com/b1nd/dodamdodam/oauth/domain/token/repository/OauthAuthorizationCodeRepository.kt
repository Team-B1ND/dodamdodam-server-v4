package com.b1nd.dodamdodam.oauth.domain.token.repository

import com.b1nd.dodamdodam.oauth.domain.token.entity.OauthAuthorizationCode
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface OauthAuthorizationCodeRepository : CoroutineCrudRepository<OauthAuthorizationCode, Long> {
    suspend fun findByCode(code: String): OauthAuthorizationCode?
}
