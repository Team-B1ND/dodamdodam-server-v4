package com.b1nd.dodamdodam.core.security.filter

import com.b1nd.dodamdodam.core.security.passport.PassportResolver
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono

class ReactivePassportFilter : WebFilter {

    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        val raw = exchange.request.headers.getFirst(PassportResolver.headerName())
        val authentication = PassportResolver.resolve(raw) ?: return chain.filter(exchange)

        return chain.filter(exchange)
            .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication))
    }
}
