package com.b1nd.dodamdodam.oauth.infrastructure.exception

import com.b1nd.dodamdodam.core.common.data.Response
import com.b1nd.dodamdodam.core.common.exception.BasicException
import com.b1nd.dodamdodam.oauth.application.data.response.OauthErrorResponse
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.bind.support.WebExchangeBindException
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

@Order(-2)
@Component
class OauthExceptionHandler(
    private val objectMapper: ObjectMapper,
) : ErrorWebExceptionHandler {

    override fun handle(exchange: ServerWebExchange, ex: Throwable): Mono<Void> {
        val response = exchange.response
        if (response.isCommitted) return Mono.empty()

        val result: Pair<HttpStatus, Any> = when (ex) {
            is OauthException -> ex.oauthCode.status to OauthErrorResponse(error = ex.oauthCode.code.lowercase(), errorDescription = ex.oauthCode.message)
            is BasicException -> HttpStatus.valueOf(ex.exceptionCode.status.value()) to Response.error(ex)
            is WebExchangeBindException -> HttpStatus.BAD_REQUEST to Response.of<Unit>(HttpStatus.BAD_REQUEST, "요청 형식이 올바르지 않아요.")
            else -> HttpStatus.BAD_REQUEST to Response.of<Unit>(HttpStatus.BAD_REQUEST, "요청을 처리하지 못했어요.")
        }

        response.statusCode = result.first
        response.headers.contentType = MediaType.APPLICATION_JSON

        val buffer = response.bufferFactory().wrap(objectMapper.writeValueAsBytes(result.second))
        return response.writeWith(Mono.just(buffer))
    }
}
