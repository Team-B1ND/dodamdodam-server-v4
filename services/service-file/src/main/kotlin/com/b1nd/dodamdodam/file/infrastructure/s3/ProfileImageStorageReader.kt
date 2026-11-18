package com.b1nd.dodamdodam.file.infrastructure.s3

import com.b1nd.dodamdodam.file.domain.exception.InvalidProfileImageException
import org.springframework.stereotype.Component
import software.amazon.awssdk.core.sync.ResponseTransformer
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import software.amazon.awssdk.services.s3.model.GetObjectResponse
import software.amazon.awssdk.services.s3.model.S3Exception
import java.time.Duration

@Component
class ProfileImageStorageReader(
    private val s3Client: S3Client,
    private val properties: S3Properties,
) {
    fun read(key: String): ByteArray {
        val request = GetObjectRequest.builder().bucket(properties.bucket).key(key)
            .overrideConfiguration { it.apiCallTimeout(Duration.ofSeconds(5)).apiCallAttemptTimeout(Duration.ofSeconds(3)) }
            .build()
        try {
            val bytes = s3Client.getObject(request, ResponseTransformer<GetObjectResponse, ByteArray> { response, stream ->
                if (response.contentLength() !in 1..MAX_BYTES.toLong()) {
                    stream.abort()
                    return@ResponseTransformer byteArrayOf()
                }
                val bytes = stream.readNBytes(MAX_BYTES + 1)
                if (bytes.size > MAX_BYTES || bytes.size.toLong() != response.contentLength()) {
                    stream.abort()
                    return@ResponseTransformer byteArrayOf()
                }
                bytes
            })
            if (bytes.isEmpty()) throw InvalidProfileImageException()
            return bytes
        } catch (e: S3Exception) {
            if (e.statusCode() == 404) throw InvalidProfileImageException()
            throw e
        }
    }

    companion object {
        const val MAX_BYTES = 10 * 1024 * 1024
    }
}
