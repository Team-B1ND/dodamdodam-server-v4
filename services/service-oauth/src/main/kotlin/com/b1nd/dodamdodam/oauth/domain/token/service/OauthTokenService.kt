package com.b1nd.dodamdodam.oauth.domain.token.service

import com.b1nd.dodamdodam.oauth.domain.token.entity.OauthAuthorizationCode
import com.b1nd.dodamdodam.oauth.domain.token.entity.OauthToken
import com.b1nd.dodamdodam.oauth.domain.token.repository.OauthAuthorizationCodeRepository
import com.b1nd.dodamdodam.oauth.domain.token.repository.OauthTokenRepository
import com.b1nd.dodamdodam.oauth.infrastructure.exception.OauthException
import com.b1nd.dodamdodam.oauth.infrastructure.exception.OauthExceptionCode
import com.b1nd.dodamdodam.oauth.support.TokenHashUtil
import org.springframework.stereotype.Service

@Service
class OauthTokenService(
    private val tokenRepository: OauthTokenRepository,
    private val codeRepository: OauthAuthorizationCodeRepository,
) {

    suspend fun saveCode(code: OauthAuthorizationCode): OauthAuthorizationCode = codeRepository.save(code)

    suspend fun findByCode(code: String): OauthAuthorizationCode {
        return codeRepository.findByCode(code)
            ?: throw OauthException(OauthExceptionCode.INVALID_GRANT)
    }

    suspend fun markCodeUsed(code: OauthAuthorizationCode) {
        codeRepository.save(code.copy(used = true))
    }

    suspend fun saveToken(token: OauthToken): OauthToken = tokenRepository.save(token)

    suspend fun findByAccessToken(accessToken: String): OauthToken? {
        val hash = TokenHashUtil.sha256(accessToken)
        return tokenRepository.findByAccessTokenHash(hash)
    }

    suspend fun findByRefreshToken(refreshToken: String): OauthToken? = tokenRepository.findByRefreshToken(refreshToken)

    suspend fun revokeToken(token: OauthToken) {
        tokenRepository.save(token.copy(revoked = true))
    }
}
