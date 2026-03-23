package com.b1nd.dodamdodam.gateway.infrastructure.gateway.filter

import com.b1nd.dodamdodam.core.common.data.webclient.WebClientApiResponse
import com.b1nd.dodamdodam.gateway.domain.passport.repository.PassportCacheRepository
import com.b1nd.dodamdodam.gateway.infrastructure.auth.client.data.ExchangePassportResponse
import com.b1nd.dodamdodam.gateway.infrastructure.auth.properties.AuthProperties
import org.springframework.cloud.gateway.filter.GatewayFilterChain
import org.springframework.cloud.gateway.filter.GlobalFilter
import org.springframework.core.Ordered
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import java.time.Duration

@Component
class PassportExchangeFilter(
    webClientBuilder: WebClient.Builder,
    private val properties: AuthProperties,
    private val cache: PassportCacheRepository,
) : GlobalFilter, Ordered {

    private val authClient = webClientBuilder.baseUrl(properties.url).build()

    override fun getOrder(): Int = 0

    override fun filter(exchange: ServerWebExchange, chain: GatewayFilterChain): Mono<Void> {
        val path = exchange.request.uri.path

        if (SKIP_PATHS.any { path.startsWith(it) }) return chain.filter(exchange)

        val jwt = resolveJwt(exchange, path)

        return resolvePassport(jwt)
            .onErrorResume { it.rethrowIfAuthError(); exchangeGuest() }
            .flatMap { passport -> forwardWithPassport(exchange, chain, passport) }
            .switchIfEmpty(chain.filter(exchange))
    }

    private fun resolveJwt(exchange: ServerWebExchange, path: String): String? {
        val headerJwt = exchange.extractBearerToken()
        val cookieJwt = exchange.request.cookies
            .getFirst(properties.accessTokenCookie)
            ?.value

        return headerJwt ?: if (path in IGNORE_COOKIE_PATHS) null else cookieJwt
    }

    private fun resolvePassport(jwt: String?): Mono<String> {
        if (jwt.isNullOrBlank()) return exchangeGuest()

        return cache.get(jwt)
            .switchIfEmpty(
                exchange(jwt).flatMap { passport ->
                    cache.set(jwt, passport, CACHE_TTL).thenReturn(passport)
                }
            )
    }

    private fun exchange(jwt: String): Mono<String> =
        authClient.post()
            .uri("/passport")
            .headers { it.setBearerAuth(jwt) }
            .exchangeToMono(::parseResponse)

    private fun exchangeGuest(): Mono<String> =
        authClient.post()
            .uri("/passport")
            .exchangeToMono(::parseResponse)

    private fun parseResponse(response: ClientResponse): Mono<String> =
        response.bodyToMono<WebClientApiResponse<ExchangePassportResponse>>()
            .defaultIfEmpty(WebClientApiResponse(status = response.statusCode().value(), message = "empty"))
            .map { it.toPassportOrThrow() }

    private fun forwardWithPassport(exchange: ServerWebExchange, chain: GatewayFilterChain, passport: String): Mono<Void> {
        val decorated = exchange.decorateHeaders("X-User-Passport" to passport)
        return chain.filter(exchange.mutate().request(decorated).build())
    }

    companion object {
        private val CACHE_TTL = Duration.ofMinutes(2)
        private val SKIP_PATHS = listOf("/swagger-ui", "/v3/api-docs")
        private val IGNORE_COOKIE_PATHS = setOf("/auth/login", "/auth/refresh")
    }
}
