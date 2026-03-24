package com.b1nd.dodamdodam.oauth.infrastructure.security

import com.nimbusds.jose.jwk.RSAKey
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.security.KeyFactory
import java.security.KeyPairGenerator
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.util.Base64

@Configuration
class JwtConfig(private val properties: OauthProperties) {

    private val log = LoggerFactory.getLogger(javaClass)

    companion object {
        const val KID = "dodam-oauth-v1"
    }

    @Bean
    fun rsaKey(): RSAKey {
        val (publicKey, privateKey) = if (properties.rsaPrivateKey.isNotBlank() && properties.rsaPublicKey.isNotBlank()) {
            loadFromPem(properties.rsaPublicKey, properties.rsaPrivateKey)
        } else {
            log.warn("RSA keys not configured — generating ephemeral key pair (dev only)")
            generateKeyPair()
        }

        return RSAKey.Builder(publicKey)
            .privateKey(privateKey)
            .keyID(KID)
            .build()
    }

    private fun loadFromPem(publicPem: String, privatePem: String): Pair<RSAPublicKey, RSAPrivateKey> {
        val keyFactory = KeyFactory.getInstance("RSA")

        val publicBytes = Base64.getDecoder().decode(
            publicPem.replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replace("\\s".toRegex(), "")
        )
        val publicKey = keyFactory.generatePublic(X509EncodedKeySpec(publicBytes)) as RSAPublicKey

        val privateBytes = Base64.getDecoder().decode(
            privatePem.replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replace("\\s".toRegex(), "")
        )
        val privateKey = keyFactory.generatePrivate(PKCS8EncodedKeySpec(privateBytes)) as RSAPrivateKey

        return publicKey to privateKey
    }

    private fun generateKeyPair(): Pair<RSAPublicKey, RSAPrivateKey> {
        val keyPair = KeyPairGenerator.getInstance("RSA").apply { initialize(2048) }.generateKeyPair()
        return (keyPair.public as RSAPublicKey) to (keyPair.private as RSAPrivateKey)
    }
}
