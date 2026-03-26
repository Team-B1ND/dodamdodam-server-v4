package com.b1nd.dodamdodam.oauth.domain.consent.repository

import com.b1nd.dodamdodam.oauth.domain.consent.entity.OauthConsent
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import java.util.UUID

interface OauthConsentRepository : CoroutineCrudRepository<OauthConsent, Long> {
    suspend fun findByUserPublicIdAndClientId(userPublicId: UUID, clientId: String): OauthConsent?
}
