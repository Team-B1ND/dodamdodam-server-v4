package com.b1nd.dodamdodam.gateway.infrastructure.gateway.filter

import org.springframework.http.HttpHeaders
import org.springframework.http.server.reactive.ServerHttpRequestDecorator
import org.springframework.web.server.ServerWebExchange

fun ServerWebExchange.extractBearerToken(): String? =
    request.headers.getFirst(HttpHeaders.AUTHORIZATION)
        ?.takeIf { it.startsWith("Bearer ") }
        ?.removePrefix("Bearer ")
        ?.trim()
        ?.takeIf { it.isNotBlank() }

fun ServerWebExchange.decorateHeaders(vararg headers: Pair<String, String>): ServerHttpRequestDecorator =
    object : ServerHttpRequestDecorator(request) {
        override fun getHeaders() = HttpHeaders().apply {
            putAll(super.getHeaders())
            headers.forEach { (key, value) -> set(key, value) }
        }
    }
