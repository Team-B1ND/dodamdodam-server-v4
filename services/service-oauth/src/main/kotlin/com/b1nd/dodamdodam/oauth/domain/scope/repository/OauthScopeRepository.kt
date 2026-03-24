package com.b1nd.dodamdodam.oauth.domain.scope.repository

import com.b1nd.dodamdodam.oauth.domain.scope.entity.OauthScope
import kotlinx.coroutines.flow.Flow
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface OauthScopeRepository : CoroutineCrudRepository<OauthScope, Long> {
    fun findAllByIsActiveTrue(): Flow<OauthScope>
    fun findByScopeKeyIn(scopeKeys: Collection<String>): Flow<OauthScope>
}
