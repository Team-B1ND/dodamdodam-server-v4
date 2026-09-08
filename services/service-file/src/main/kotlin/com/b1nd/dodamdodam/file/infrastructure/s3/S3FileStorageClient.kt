package com.b1nd.dodamdodam.file.infrastructure.s3

import com.b1nd.dodamdodam.file.domain.exception.FileUploadFailedException
import com.b1nd.dodamdodam.file.domain.service.ValidatedFileMetadata
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.ObjectCannedACL
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import java.util.UUID

@Component
class S3FileStorageClient(
    private val s3Client: S3Client,
    private val properties: S3Properties,
) {
    fun upload(file: MultipartFile, metadata: ValidatedFileMetadata): String {
        val key = generateKey(metadata.extension)

        val request = PutObjectRequest.builder()
            .bucket(properties.bucket)
            .key(key)
            .contentType(metadata.contentType)
            .contentLength(file.size)
            .apply {
                if (properties.publicRead) {
                    acl(ObjectCannedACL.PUBLIC_READ)
                }
            }
            .build()

        runCatching {
            file.inputStream.use { stream ->
                s3Client.putObject(request, RequestBody.fromInputStream(stream, file.size))
            }
        }.onFailure { throw FileUploadFailedException() }

        return buildFileUrl(key)
    }

    private fun generateKey(extension: String): String {
        val uuid = UUID.randomUUID().toString()
        val filename = if (extension.isNotBlank()) "$uuid.$extension" else uuid
        val prefix = properties.keyPrefix.trim('/')
        return if (prefix.isNotBlank()) "$prefix/$filename" else filename
    }

    private fun buildFileUrl(key: String): String =
        if (properties.endpoint.isNotBlank()) {
            "${properties.endpoint.trimEnd('/')}/${properties.bucket}/$key"
        } else {
            "https://${properties.bucket}.s3.${properties.region}.amazonaws.com/$key"
        }
}
