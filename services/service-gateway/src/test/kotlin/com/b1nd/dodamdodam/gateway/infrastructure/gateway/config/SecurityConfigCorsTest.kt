package com.b1nd.dodamdodam.gateway.infrastructure.gateway.config

import com.b1nd.dodamdodam.gateway.infrastructure.gateway.properties.CorsProperties
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.mock.http.server.reactive.MockServerHttpRequest
import org.springframework.mock.web.server.MockServerWebExchange
import org.springframework.web.cors.reactive.CorsWebFilter
import reactor.core.publisher.Mono

class SecurityConfigCorsTest {
    private val allowedOrigin = "https://dodam.b1nd.com"
    private val filter = CorsWebFilter(SecurityConfig(CorsProperties(listOf(allowedOrigin))).corsConfigurationSource())

    @Test
    fun `허용 출처의 인증정보 포함 요청은 통과한다`() {
        val exchange = MockServerWebExchange.from(
            MockServerHttpRequest.get("https://api.example.com/user/me")
                .header(HttpHeaders.ORIGIN, allowedOrigin)
                .header(HttpHeaders.COOKIE, "access_token=test")
        )
        var called = false
        filter.filter(exchange) { called = true; Mono.empty() }.block()
        assertTrue(called)
        assertEquals(allowedOrigin, exchange.response.headers.accessControlAllowOrigin)
        assertTrue(exchange.response.headers.accessControlAllowCredentials)
    }

    @ParameterizedTest
    @ValueSource(strings = ["https://external.example", "https://dodam.b1nd.com.evil.example", "https://evil.dodam.b1nd.com", "http://dodam.b1nd.com", "https://dodam.b1nd.com:444", "http://localhost:3000", "null"])
    fun `허용하지 않은 출처는 실제 요청과 preflight 모두 차단한다`(origin: String) {
        listOf(false, true).forEach { preflight ->
            val request = if (preflight) MockServerHttpRequest.options("https://api.example.com/user")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "PATCH")
            else MockServerHttpRequest.post("https://api.example.com/user")
            val exchange = MockServerWebExchange.from(request.header(HttpHeaders.ORIGIN, origin))
            var called = false
            filter.filter(exchange) { called = true; Mono.empty() }.block()
            assertFalse(called)
            assertEquals(HttpStatus.FORBIDDEN, exchange.response.statusCode)
            assertNull(exchange.response.headers.accessControlAllowOrigin)
            assertFalse(exchange.response.headers.accessControlAllowCredentials)
        }
    }

    @Test
    fun `허용 출처의 PATCH preflight에 인증 헤더를 허용한다`() {
        val exchange = MockServerWebExchange.from(
            MockServerHttpRequest.options("https://api.example.com/user")
                .header(HttpHeaders.ORIGIN, allowedOrigin)
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "PATCH")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "authorization,content-type")
        )
        filter.filter(exchange) { Mono.error(AssertionError("preflight는 서비스에 전달하면 안 됩니다.")) }.block()
        assertEquals(allowedOrigin, exchange.response.headers.accessControlAllowOrigin)
        assertTrue(exchange.response.headers.accessControlAllowCredentials)
        assertTrue(exchange.response.headers.accessControlAllowMethods.any { it.name() == "PATCH" })
        assertTrue(exchange.response.headers.accessControlAllowHeaders.map { it.lowercase() }.containsAll(listOf("authorization", "content-type")))
    }

    @Test
    fun `Origin 없는 앱 요청은 기존 인증 처리로 전달한다`() {
        val exchange = MockServerWebExchange.from(MockServerHttpRequest.get("https://api.example.com/user/me"))
        var called = false
        filter.filter(exchange) { called = true; Mono.empty() }.block()
        assertTrue(called)
        assertNull(exchange.response.headers.accessControlAllowOrigin)
    }

    @Test
    fun `빈 허용 목록에서는 교차 출처 요청을 허용하지 않는다`() {
        val emptyFilter = CorsWebFilter(SecurityConfig(CorsProperties()).corsConfigurationSource())
        val exchange = MockServerWebExchange.from(MockServerHttpRequest.get("https://api.example.com/user/me").header(HttpHeaders.ORIGIN, allowedOrigin))
        emptyFilter.filter(exchange) { Mono.error(AssertionError("빈 목록에서는 외부 출처를 허용하면 안 됩니다.")) }.block()
        assertEquals(HttpStatus.FORBIDDEN, exchange.response.statusCode)
    }

    @ParameterizedTest
    @ValueSource(strings = ["*", "https://*.b1nd.com", "null", "", "https://dodam.b1nd.com/", "https://dodam.b1nd.com/path", "https://user@dodam.b1nd.com", "https://dodam.b1nd.com?x=1", "https://dodam.b1nd.com#x", "ftp://dodam.b1nd.com", "https://dodam.b1nd.com:99999"])
    fun `와일드카드나 잘못된 출처 설정은 거부한다`(origin: String) {
        assertThrows(IllegalArgumentException::class.java) { CorsProperties(listOf(origin)) }
    }
}
