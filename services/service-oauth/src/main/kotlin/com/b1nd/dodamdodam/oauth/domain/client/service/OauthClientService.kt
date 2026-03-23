package com.b1nd.dodamdodam.oauth.domain.client.service

import com.b1nd.dodamdodam.oauth.domain.client.entity.OauthClient
import com.b1nd.dodamdodam.oauth.domain.client.repository.OauthClientRepository
import com.b1nd.dodamdodam.oauth.infrastructure.exception.OauthException
import com.b1nd.dodamdodam.oauth.infrastructure.exception.OauthExceptionCode
import org.springframework.stereotype.Service

@Service
class OauthClientService(private val repository: OauthClientRepository) {

    suspend fun save(client: OauthClient): OauthClient = repository.save(client)

    suspend fun findByClientId(clientId: String): OauthClient {
        return repository.findByClientId(clientId)
            ?: throw OauthException(OauthExceptionCode.CLIENT_NOT_FOUND)
    }

    suspend fun findActiveByClientId(clientId: String): OauthClient {
        return repository.findByClientIdAndIsActiveTrue(clientId)
            ?: throw OauthException(OauthExceptionCode.INVALID_CLIENT)
    }

    suspend fun verifyClientSecret(client: OauthClient, rawSecret: String, passwordEncoder: org.springframework.security.crypto.password.PasswordEncoder) {
        if (!passwordEncoder.matches(rawSecret, client.clientSecret)) {
            throw OauthException(OauthExceptionCode.INVALID_CLIENT)
        }
    }
}
