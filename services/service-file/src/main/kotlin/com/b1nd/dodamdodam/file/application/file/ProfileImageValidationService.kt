package com.b1nd.dodamdodam.file.application.file

import com.b1nd.dodamdodam.file.domain.enumeration.FileType
import com.b1nd.dodamdodam.file.domain.exception.InvalidProfileImageException
import com.b1nd.dodamdodam.file.domain.service.StaticProfileImageValidator
import com.b1nd.dodamdodam.file.infrastructure.s3.ProfileImageStorageReader
import com.b1nd.dodamdodam.file.infrastructure.s3.S3FileUrl
import org.springframework.stereotype.Service

@Service
class ProfileImageValidationService(
    private val fileUrl: S3FileUrl,
    private val storageReader: ProfileImageStorageReader,
    private val validator: StaticProfileImageValidator,
) {
    fun validate(url: String) {
        val key = fileUrl.resolveKey(url)
        val extension = key.substringAfterLast('.', "").lowercase()
        if (FileType.fromExtension(extension) != FileType.IMAGE) {
            throw InvalidProfileImageException()
        }

        val bytes = storageReader.read(key)
        validator.validate(bytes, extension)
    }
}
