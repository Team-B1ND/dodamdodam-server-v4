package com.b1nd.dodamdodam.oauth.infrastructure.security

import com.b1nd.dodamdodam.oauth.domain.client.entity.OauthClient
import com.b1nd.dodamdodam.oauth.domain.client.repository.OauthClientRepository
import kotlinx.coroutines.reactor.awaitSingleOrNull
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component

@Component
class DauthClientInitializer(
    private val clientRepository: OauthClientRepository,
    private val passwordEncoder: PasswordEncoder,
    private val properties: OauthProperties,
) : ApplicationRunner {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun run(args: ApplicationArguments?) = runBlocking {
        val clientId = properties.dauthClientId ?: return@runBlocking
        val clientSecret = properties.dauthClientSecret ?: return@runBlocking

        val existing = clientRepository.findByClientId(clientId).awaitSingleOrNull()
        if (existing != null) return@runBlocking

        clientRepository.save(
            OauthClient(
                clientId = clientId,
                clientSecret = passwordEncoder.encode(clientSecret),
                ownerPublicId = "system",
                clientName = "DAuth",
                redirectUris = properties.dauthRedirectUri ?: "http://localhost:3000/callback",
                scopes = "profile:read",
                description = "DAuth 인증 서비스",
                isActive = true,
                trusted = true,
            )
        ).awaitSingle()

        log.info("DAuth self client registered: {}", clientId)
    }
}
