package com.b1nd.dodamdodam.oauth.domain.scope.service

import com.b1nd.dodamdodam.oauth.domain.scope.entity.OauthScope
import com.b1nd.dodamdodam.oauth.domain.scope.repository.OauthScopeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import org.springframework.stereotype.Service

@Service
class OauthScopeService(private val repository: OauthScopeRepository) {

    fun findAllActive(): Flow<OauthScope> = repository.findAllByIsActiveTrue()

    suspend fun findAllActiveKeys(): List<String> =
        repository.findAllByIsActiveTrue().map { it.scopeKey }.toList()

    suspend fun findByKeys(keys: Collection<String>): List<OauthScope> =
        repository.findByScopeKeyIn(keys).toList()
}
