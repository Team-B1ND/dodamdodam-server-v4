package com.b1nd.dodamdodam.file.infrastructure.s3

import com.b1nd.dodamdodam.file.domain.exception.InvalidProfileImageException
import com.sun.net.httpserver.HttpServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.S3Exception
import java.net.InetSocketAddress
import java.net.URI

class ProfileImageStorageReaderTest {
    private var responseStatus = 200
    private var responseBytes = "image data".toByteArray()
    private var declaredSize: Long? = null
    private var requestedPath: String? = null
    private val server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0).apply {
        createContext("/") { exchange ->
            requestedPath = exchange.requestURI.path
            exchange.sendResponseHeaders(responseStatus, declaredSize ?: responseBytes.size.toLong())
            runCatching {
                exchange.responseBody.use { it.write(responseBytes) }
            }
            exchange.close()
        }
        start()
    }
    private val client = S3Client.builder()
        .endpointOverride(URI("http://127.0.0.1:${server.address.port}"))
        .region(Region.US_EAST_1)
        .forcePathStyle(true)
        .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create("test", "test")))
        .build()
    private val reader = ProfileImageStorageReader(
        client,
        S3Properties(region = "us-east-1", bucket = "dodam"),
    )

    @AfterEach
    fun close() {
        client.close()
        server.stop(0)
    }

    @Test
    fun `지정 버킷의 객체만 읽는다`() {
        assertArrayEquals(responseBytes, reader.read("static/a.png"))
        assertEquals("/dodam/static/a.png", requestedPath)
    }

    @Test
    fun `용량 초과 파일은 본문을 다운로드하지 않고 거부한다`() {
        declaredSize = ProfileImageStorageReader.MAX_BYTES + 1L
        responseBytes = ByteArray(0)

        assertThrows(InvalidProfileImageException::class.java) {
            reader.read("large.png")
        }
    }

    @Test
    fun `없는 파일은 잘못된 프로필로 처리한다`() {
        responseStatus = 404
        responseBytes = "<Error><Code>NoSuchKey</Code></Error>".toByteArray()

        assertThrows(InvalidProfileImageException::class.java) {
            reader.read("missing.png")
        }
    }

    @Test
    fun `저장소 권한 장애는 사용자 입력 오류로 숨기지 않는다`() {
        responseStatus = 403
        responseBytes = "<Error><Code>AccessDenied</Code></Error>".toByteArray()

        assertThrows(S3Exception::class.java) {
            reader.read("image.png")
        }
    }
}
