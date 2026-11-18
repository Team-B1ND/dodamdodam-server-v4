package com.b1nd.dodamdodam.file.infrastructure.s3

import com.b1nd.dodamdodam.file.domain.exception.InvalidProfileImageException
import org.springframework.stereotype.Component
import java.net.URI

@Component
class S3FileUrl(private val properties: S3Properties) {
    fun build(key: String): String = "${baseUrl()}/$key"

    fun resolveKey(url: String): String {
        if (url.length > 2048) throw InvalidProfileImageException()
        val uri = try { URI(url) } catch (_: Exception) { throw InvalidProfileImageException() }
        val base = URI(baseUrl())
        if (uri.scheme != base.scheme || uri.host == null || !uri.host.equals(base.host, ignoreCase = true) ||
            uri.port != base.port || uri.rawUserInfo != null || uri.rawQuery != null || uri.rawFragment != null) {
            throw InvalidProfileImageException()
        }
        val basePath = "${base.rawPath.orEmpty()}/"
        val path = uri.rawPath ?: throw InvalidProfileImageException()
        if (!path.startsWith(basePath) || '%' in path || '\\' in path) throw InvalidProfileImageException()
        val key = path.removePrefix(basePath)
        if (key.split('/').any { it.isEmpty() || it == "." || it == ".." }) throw InvalidProfileImageException()
        val prefix = properties.keyPrefix.trim('/')
        if (prefix.isNotEmpty() && !key.startsWith("$prefix/")) throw InvalidProfileImageException()
        return key
    }

    private fun baseUrl(): String = if (properties.endpoint.isNotBlank()) {
        "${properties.endpoint.trimEnd('/')}/${properties.bucket}"
    } else {
        "https://${properties.bucket}.s3.${properties.region}.amazonaws.com"
    }
}
