package com.b1nd.dodamdodam.gateway.infrastructure.gateway.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import java.net.URI

@ConfigurationProperties("app.cors")
data class CorsProperties(
    val allowedOrigins: List<String> = emptyList(),
) {
    init {
        require(allowedOrigins.all(::isExactOrigin)) {
            "app.cors.allowed-origins에는 경로·와일드카드가 없는 http(s) 출처만 설정할 수 있습니다."
        }
    }

    private fun isExactOrigin(origin: String): Boolean = runCatching {
        val uri = URI(origin)
        origin.isNotBlank() && '*' !in origin &&
            uri.scheme in setOf("http", "https") && !uri.host.isNullOrBlank() &&
            uri.rawUserInfo == null && uri.rawPath.isNullOrEmpty() &&
            uri.rawQuery == null && uri.rawFragment == null &&
            (uri.port == -1 || uri.port in 1..65535)
    }.getOrDefault(false)
}
