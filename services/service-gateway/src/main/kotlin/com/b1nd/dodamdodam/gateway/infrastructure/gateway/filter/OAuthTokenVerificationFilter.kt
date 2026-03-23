package com.b1nd.dodamdodam.gateway.infrastructure.gateway.filter

import com.nimbusds.jose.crypto.RSASSAVerifier
import com.nimbusds.jose.jwk.JWKSet
import com.nimbusds.jose.jwk.RSAKey
import com.nimbusds.jwt.SignedJWT
import org.slf4j.LoggerFactory
import org.springframework.cloud.gateway.filter.GatewayFilterChain
import org.springframework.cloud.gateway.filter.GlobalFilter
import org.springframework.core.Ordered
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.server.reactive.ServerHttpRequestDecorator
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import java.util.Date
import java.util.concurrent.ConcurrentHashMap

@Component
class OAuthTokenVerificationFilter(
    webClientBuilder: WebClient.Builder,
) : GlobalFilter, Ordered {

    private val log = LoggerFactory.getLogger(javaClass)
    private val jwksCache = ConcurrentHashMap<String, RSAKey>()
    private val webClient = webClientBuilder.build()

    companion object {
        private const val OAUTH_KID_PREFIX = "dodam-oauth-"
        private const val JWKS_URL = "http://localhost:8087/.well-known/jwks.json"

        private val SCOPE_RULES: List<ScopeRule> = listOf(
            ScopeRule("/nightstudy/", read = "nightstudy:read"),
            ScopeRule("/outgoing/", read = "outgoing:read"),
            ScopeRule("/wakeup-song/", read = "wakeupsong:read", write = "wakeupsong:write"),
            ScopeRule("/user/", read = "profile:read"),
        )
    }

    override fun getOrder(): Int = -1

    override fun filter(exchange: ServerWebExchange, chain: GatewayFilterChain): Mono<Void> {
        val path = exchange.request.uri.path
        if (path.startsWith("/oauth/token") || path.startsWith("/oauth/authorize") || path.startsWith("/.well-known/")) {
            return chain.filter(exchange)
        }

        val authHeader = exchange.request.headers.getFirst(HttpHeaders.AUTHORIZATION) ?: return chain.filter(exchange)
        if (!authHeader.startsWith("Bearer ")) return chain.filter(exchange)

        val token = authHeader.removePrefix("Bearer ").trim()
        val jwt = try { SignedJWT.parse(token) } catch (_: Exception) { return chain.filter(exchange) }
        val kid = jwt.header.keyID ?: return chain.filter(exchange)

        if (!kid.startsWith(OAUTH_KID_PREFIX)) return chain.filter(exchange)

        return verifyOAuthToken(jwt)
            .flatMap { claims ->
                if (!claims.trusted) {
                    val requiredScope = resolveRequiredScope(path, exchange.request.method)
                    val grantedScopes = claims.scope.split(" ").toSet()

                    if (requiredScope != null && requiredScope !in grantedScopes) {
                        exchange.response.statusCode = HttpStatus.FORBIDDEN
                        return@flatMap exchange.response.setComplete()
                    }
                }

                val decoratedRequest = object : ServerHttpRequestDecorator(exchange.request) {
                    override fun getHeaders(): HttpHeaders {
                        val headers = HttpHeaders()
                        headers.putAll(super.getHeaders())
                        headers.set(HttpHeaders.AUTHORIZATION, "Bearer ${claims.authAccessToken}")
                        headers.set("X-OAuth-Scope", claims.scope)
                        headers.set("X-OAuth-Client-Id", claims.clientId)
                        return headers
                    }
                }
                chain.filter(exchange.mutate().request(decoratedRequest).build())
            }
            .onErrorResume {
                log.error("OAuth token verification failed", it)
                exchange.response.statusCode = HttpStatus.UNAUTHORIZED
                exchange.response.setComplete()
            }
    }

    private fun resolveRequiredScope(path: String, method: HttpMethod?): String? {
        val rule = SCOPE_RULES.find { path.startsWith(it.pathPrefix) } ?: return null
        val isWrite = method == HttpMethod.POST || method == HttpMethod.PUT || method == HttpMethod.DELETE || method == HttpMethod.PATCH
        return if (isWrite) (rule.write ?: rule.read) else rule.read
    }

    private fun verifyOAuthToken(jwt: SignedJWT): Mono<OAuthClaims> {
        val kid = jwt.header.keyID
        val cachedKey = jwksCache[kid]

        val keyMono = if (cachedKey != null) {
            Mono.just(cachedKey)
        } else {
            fetchJwks().map { jwkSet ->
                val key = jwkSet.getKeyByKeyId(kid) as? RSAKey
                    ?: throw IllegalStateException("Key $kid not found in JWKS")
                jwksCache[kid] = key
                key
            }
        }

        return keyMono.map { rsaKey ->
            val verifier = RSASSAVerifier(rsaKey.toRSAPublicKey())
            if (!jwt.verify(verifier)) throw IllegalStateException("JWT signature verification failed")

            val claims = jwt.jwtClaimsSet
            if (claims.expirationTime.before(Date())) throw IllegalStateException("JWT expired")

            val authAccessToken = claims.getStringClaim("aat")
                ?: throw IllegalStateException("auth access token (aat) not found in OAuth JWT")

            // inner auth token 만료 확인
            try {
                val innerJwt = SignedJWT.parse(authAccessToken)
                if (innerJwt.jwtClaimsSet.expirationTime.before(Date())) {
                    throw IllegalStateException("inner auth token expired")
                }
            } catch (e: IllegalStateException) { throw e }
            catch (_: Exception) { throw IllegalStateException("invalid inner auth token") }

            OAuthClaims(
                authAccessToken = authAccessToken,
                clientId = claims.audience?.firstOrNull() ?: "",
                scope = claims.getStringClaim("scope") ?: "",
                trusted = claims.getBooleanClaim("trusted") ?: false,
            )
        }
    }

    private fun fetchJwks(): Mono<JWKSet> {
        return webClient.get()
            .uri(JWKS_URL)
            .retrieve()
            .bodyToMono(String::class.java)
            .map { JWKSet.parse(it) }
    }

    private data class OAuthClaims(val authAccessToken: String, val clientId: String, val scope: String, val trusted: Boolean)
    private data class ScopeRule(val pathPrefix: String, val read: String? = null, val write: String? = null)
}
