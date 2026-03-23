package com.b1nd.dodamdodam.oauth.domain.consent.repository

import com.b1nd.dodamdodam.oauth.domain.consent.entity.OauthConsent
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Mono

interface OauthConsentRepository : ReactiveCrudRepository<OauthConsent, Long> {
    fun findByUserPublicIdAndClientId(userPublicId: String, clientId: String): Mono<OauthConsent>
}
