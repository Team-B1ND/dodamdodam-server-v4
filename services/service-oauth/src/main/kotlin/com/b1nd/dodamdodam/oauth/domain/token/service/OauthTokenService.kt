package com.b1nd.dodamdodam.oauth.domain.token.service

import com.b1nd.dodamdodam.oauth.domain.token.entity.OauthAuthorizationCode
import com.b1nd.dodamdodam.oauth.domain.token.entity.OauthToken
import com.b1nd.dodamdodam.oauth.domain.token.repository.OauthAuthorizationCodeRepository
import com.b1nd.dodamdodam.oauth.domain.token.repository.OauthTokenRepository
import com.b1nd.dodamdodam.oauth.infrastructure.exception.OauthException
import com.b1nd.dodamdodam.oauth.infrastructure.exception.OauthExceptionCode
import com.b1nd.dodamdodam.oauth.support.TokenHashUtil
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.stereotype.Service

@Service
class OauthTokenService(
    private val tokenRepository: OauthTokenRepository,
    private val codeRepository: OauthAuthorizationCodeRepository,
) {

    suspend fun saveCode(code: OauthAuthorizationCode): OauthAuthorizationCode = codeRepository.save(code).awaitSingle()

    suspend fun findByCode(code: String): OauthAuthorizationCode {
        return codeRepository.findByCode(code).awaitSingleOrNull()
            ?: throw OauthException(OauthExceptionCode.INVALID_GRANT)
    }

    suspend fun markCodeUsed(code: OauthAuthorizationCode) {
        codeRepository.save(code.copy(used = true)).awaitSingle()
    }

    suspend fun saveToken(token: OauthToken): OauthToken = tokenRepository.save(token).awaitSingle()

    suspend fun findByAccessToken(accessToken: String): OauthToken? {
        val hash = TokenHashUtil.sha256(accessToken)
        return tokenRepository.findByAccessTokenHash(hash).awaitSingleOrNull()
    }

    suspend fun findByRefreshToken(refreshToken: String): OauthToken? = tokenRepository.findByRefreshToken(refreshToken).awaitSingleOrNull()

    suspend fun revokeToken(token: OauthToken) {
        tokenRepository.save(token.copy(revoked = true)).awaitSingle()
    }
}
