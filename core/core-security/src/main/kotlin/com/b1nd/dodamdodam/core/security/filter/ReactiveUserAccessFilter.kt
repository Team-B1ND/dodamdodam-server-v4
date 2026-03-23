package com.b1nd.dodamdodam.core.security.filter

import com.b1nd.dodamdodam.core.security.annotation.authentication.UserAccess
import com.b1nd.dodamdodam.core.security.annotation.authentication.UserAccessValidator
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.web.method.HandlerMethod
import org.springframework.web.reactive.result.method.annotation.RequestMappingHandlerMapping
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono

class ReactiveUserAccessFilter(
    private val handlerMapping: RequestMappingHandlerMapping,
) : WebFilter {

    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        return handlerMapping.getHandler(exchange)
            .flatMap { handler ->
                val method = (handler as? HandlerMethod)
                    ?.method
                    ?.getAnnotation(UserAccess::class.java)

                if (method == null) return@flatMap chain.filter(exchange)

                ReactiveSecurityContextHolder.getContext()
                    .defaultIfEmpty(org.springframework.security.core.context.SecurityContextImpl())
                    .flatMap { context ->
                        UserAccessValidator.validate(context.authentication, method)
                        chain.filter(exchange)
                    }
            }
            .switchIfEmpty(chain.filter(exchange))
    }
}
