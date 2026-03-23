package com.b1nd.dodamdodam.gateway.infrastructure.gateway.filter

import com.b1nd.dodamdodam.gateway.infrastructure.gateway.filter.data.OAuthClaims
import com.b1nd.dodamdodam.gateway.infrastructure.gateway.properties.OAuthProperties
import com.nimbusds.jwt.SignedJWT
import org.slf4j.LoggerFactory
import org.springframework.cloud.gateway.filter.GatewayFilterChain
import org.springframework.cloud.gateway.filter.GlobalFilter
import org.springframework.core.Ordered
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import java.util.Date

@Component
class OAuthTokenVerificationFilter(
    private val properties: OAuthProperties,
) : GlobalFilter, Ordered {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun getOrder(): Int = -1

    override fun filter(exchange: ServerWebExchange, chain: GatewayFilterChain): Mono<Void> {
        val path = exchange.request.uri.path

        if (properties.skipPaths.any { path.startsWith(it) }) return chain.filter(exchange)

        val jwt = exchange.extractBearerToken()
            ?.let(::parseJwt)
            ?.takeIf { it.header.keyID.orEmpty().startsWith(properties.kidPrefix) }
            ?: return chain.filter(exchange)

        val claims = try {
            extractClaims(jwt)
        } catch (e: Exception) {
            log.error("OAuth token verification failed: {}", e.message)
            return exchange.respond(HttpStatus.UNAUTHORIZED)
        }

        if (!claims.trusted) {
            val required = resolveRequiredScope(path, exchange.request.method)
            if (required != null && required !in claims.scopes) {
                return exchange.respond(HttpStatus.FORBIDDEN)
            }
        }

        val decorated = exchange.decorateHeaders(
            HttpHeaders.AUTHORIZATION to "Bearer ${claims.innerToken}",
            "X-OAuth-Scope" to claims.scopes.joinToString(" "),
            "X-OAuth-Client-Id" to claims.clientId,
        )

        return chain.filter(exchange.mutate().request(decorated).build())
    }

    private fun extractClaims(jwt: SignedJWT): OAuthClaims {
        val claims = jwt.jwtClaimsSet
        check(!claims.expirationTime.before(Date())) { "OAuth token expired" }

        val innerToken = claims.getStringClaim("aat") ?: error("missing aat claim")
        val innerJwt = SignedJWT.parse(innerToken)
        check(!innerJwt.jwtClaimsSet.expirationTime.before(Date())) { "inner token expired" }

        return OAuthClaims(
            innerToken = innerToken,
            clientId = claims.audience?.firstOrNull() ?: "",
            scopes = claims.getStringClaim("scope")?.split(" ")?.toSet() ?: emptySet(),
            trusted = claims.getBooleanClaim("trusted") ?: false,
        )
    }

    private fun resolveRequiredScope(path: String, method: HttpMethod?): String? {
        val rule = properties.scopeRules.find { path.startsWith(it.path) } ?: return null
        return if (method in WRITE_METHODS) (rule.write ?: rule.read) else rule.read
    }

    private fun ServerWebExchange.respond(status: HttpStatus): Mono<Void> {
        response.statusCode = status
        return response.setComplete()
    }

    private fun parseJwt(token: String): SignedJWT? =
        try { SignedJWT.parse(token) } catch (_: Exception) { null }

    companion object {
        private val WRITE_METHODS = setOf(HttpMethod.POST, HttpMethod.PUT, HttpMethod.DELETE, HttpMethod.PATCH)
    }
}
