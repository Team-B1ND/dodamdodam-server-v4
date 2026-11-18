package com.b1nd.dodamdodam.file.infrastructure.s3

import com.b1nd.dodamdodam.file.domain.exception.InvalidProfileImageException
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class S3FileUrlTest {
    private val resolver = S3FileUrl(S3Properties(region = "ap-northeast-2", bucket = "dodam", keyPrefix = "prod/static"))
    private val base = "https://dodam.s3.ap-northeast-2.amazonaws.com"

    @Test
    fun `업로드에서 발급한 URL을 같은 객체 키로 해석한다`() {
        val key = "prod/static/example.png"
        assertEquals(key, resolver.resolveKey(resolver.build(key)))
    }

    @ParameterizedTest
    @ValueSource(strings = [
        "https://external.example/a.png", "https://dodam.s3.ap-northeast-2.amazonaws.com.evil.example/prod/static/a.png",
        "https://user@dodam.s3.ap-northeast-2.amazonaws.com/prod/static/a.png", "http://dodam.s3.ap-northeast-2.amazonaws.com/prod/static/a.png",
        "https://dodam.s3.ap-northeast-2.amazonaws.com:444/prod/static/a.png", "data:image/png;base64,a", "not a url", ""
    ])
    fun `외부 저장소와 위장 출처는 거부한다`(url: String) {
        assertThrows(InvalidProfileImageException::class.java) { resolver.resolveKey(url) }
    }

    @ParameterizedTest
    @ValueSource(strings = ["/dev/static/a.png", "/prod/static-other/a.png", "/prod/static/../a.png", "/prod/static/%2e%2e/a.png", "/prod/static/%252e/a.png", "/prod/static//a.png", "/prod/static/", "/prod/static/a.png?redirect=x", "/prod/static/a.png#x"])
    fun `다른 환경의 경로와 우회 경로는 거부한다`(path: String) {
        assertThrows(InvalidProfileImageException::class.java) { resolver.resolveKey(base + path) }
    }

    @Test
    fun `S3 호환 endpoint는 버킷 경계까지 확인한다`() {
        val custom = S3FileUrl(S3Properties(endpoint = "https://storage.example/s3", region = "local", bucket = "dodam", keyPrefix = "static"))
        assertEquals("static/a.png", custom.resolveKey("https://storage.example/s3/dodam/static/a.png"))
        assertThrows(InvalidProfileImageException::class.java) { custom.resolveKey("https://storage.example/s3/other/static/a.png") }
    }
}
