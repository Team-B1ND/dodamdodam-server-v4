package com.b1nd.dodamdodam.gateway.infrastructure.gateway.filter

import com.b1nd.dodamdodam.gateway.infrastructure.gateway.properties.OpenApiProperties
import org.springframework.cloud.gateway.filter.GatewayFilterChain
import org.springframework.cloud.gateway.filter.GlobalFilter
import org.springframework.core.Ordered
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import java.util.concurrent.ConcurrentHashMap

@Component
class OpenApiClientAuthFilter(
    webClientBuilder: WebClient.Builder,
    properties: OpenApiProperties,
) : GlobalFilter, Ordered {

    private val oauthClient = webClientBuilder.baseUrl(properties.oauthServiceUrl).build()
    private val cache = ConcurrentHashMap<String, CachedVerification>()

    override fun getOrder(): Int = -2

    override fun filter(exchange: ServerWebExchange, chain: GatewayFilterChain): Mono<Void> {
        val path = exchange.request.uri.path
        if (!path.startsWith(OPENAPI_PATH_PREFIX)) return chain.filter(exchange)

        val basicCredentials = exchange.extractBasicCredentials()
            ?: return exchange.unauthorized()

        return verifyClient(basicCredentials)
            .flatMap { result ->
                val decorated = exchange.decorateHeaders(
                    "X-OAuth-Client-Id" to result.clientId,
                    "X-OAuth-Scopes" to result.scopes.joinToString(" "),
                )
                chain.filter(exchange.mutate().request(decorated).build())
            }
            .onErrorResume { exchange.unauthorized() }
    }

    private fun verifyClient(credentials: String): Mono<ClientVerifyResponse> {
        val cached = cache[credentials]
        if (cached != null && cached.expiry > System.currentTimeMillis()) {
            return Mono.just(cached.result)
        }

        return oauthClient.post()
            .uri("/internal/clients/verify")
            .header(HttpHeaders.AUTHORIZATION, "Basic $credentials")
            .retrieve()
            .bodyToMono<ClientVerifyResponse>()
            .doOnNext { result ->
                cache[credentials] = CachedVerification(result, System.currentTimeMillis() + CACHE_TTL_MS)
            }
    }

    private fun ServerWebExchange.extractBasicCredentials(): String? =
        request.headers.getFirst(HttpHeaders.AUTHORIZATION)
            ?.takeIf { it.startsWith("Basic ") }
            ?.removePrefix("Basic ")
            ?.trim()
            ?.takeIf { it.isNotBlank() }

    private fun ServerWebExchange.unauthorized(): Mono<Void> {
        response.statusCode = HttpStatus.UNAUTHORIZED
        return response.setComplete()
    }

    data class ClientVerifyResponse(val clientId: String, val scopes: List<String>)
    data class CachedVerification(val result: ClientVerifyResponse, val expiry: Long)

    companion object {
        private const val OPENAPI_PATH_PREFIX = "/user/openapi/"
        private const val CACHE_TTL_MS = 5 * 60 * 1000L
    }
}
