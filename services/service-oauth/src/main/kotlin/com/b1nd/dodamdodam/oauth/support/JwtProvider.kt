package com.b1nd.dodamdodam.oauth.support

import com.b1nd.dodamdodam.oauth.infrastructure.security.JwtConfig
import com.b1nd.dodamdodam.oauth.infrastructure.security.OauthProperties
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.crypto.RSASSASigner
import com.nimbusds.jose.crypto.RSASSAVerifier
import com.nimbusds.jose.jwk.RSAKey
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.Date
import java.util.UUID

@Component
class JwtProvider(private val rsaKey: RSAKey, private val properties: OauthProperties) {

    private val signer = RSASSASigner(rsaKey)
    private val verifier = RSASSAVerifier(rsaKey.toRSAPublicKey())

    fun createAccessToken(userPublicId: String, clientId: String, scopes: String, roles: List<String>, authAccessToken: String, trusted: Boolean = false): String {
        val now = Instant.now()
        val builder = JWTClaimsSet.Builder()
            .issuer(properties.issuer)
            .subject(userPublicId)
            .audience(clientId)
            .expirationTime(Date.from(now.plusSeconds(properties.accessTokenExpirySeconds)))
            .issueTime(Date.from(now))
            .jwtID(UUID.randomUUID().toString())
            .claim("scope", scopes)
            .claim("roles", roles)
            .claim("aat", authAccessToken)
        if (trusted) builder.claim("trusted", true)
        val claims = builder.build()

        val header = JWSHeader.Builder(JWSAlgorithm.RS256)
            .keyID(JwtConfig.KID)
            .type(com.nimbusds.jose.JOSEObjectType.JWT)
            .build()

        return SignedJWT(header, claims).apply { sign(signer) }.serialize()
    }

    fun createRefreshToken(): String = "drt_${UUID.randomUUID().toString().replace("-", "")}"

    fun verifyAccessToken(token: String): JWTClaimsSet? {
        return try {
            val jwt = SignedJWT.parse(token)
            if (!jwt.verify(verifier)) return null
            val claims = jwt.jwtClaimsSet
            if (claims.issuer != properties.issuer) return null
            if (claims.expirationTime.before(Date())) return null
            claims
        } catch (_: Exception) {
            null
        }
    }
}
