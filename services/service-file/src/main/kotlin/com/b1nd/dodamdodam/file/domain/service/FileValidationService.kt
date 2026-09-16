package com.b1nd.dodamdodam.file.domain.service

import com.b1nd.dodamdodam.file.domain.enumeration.FileType
import com.b1nd.dodamdodam.file.domain.exception.FileDimensionNotAllowedException
import com.b1nd.dodamdodam.file.domain.exception.FileDimensionReadFailedException
import com.b1nd.dodamdodam.file.domain.exception.FileEmptyException
import com.b1nd.dodamdodam.file.domain.exception.FileTypeNotAllowedException
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import javax.imageio.ImageIO

data class ValidatedFileMetadata(
    val extension: String,
    val contentType: String,
)

@Service
class FileValidationService {

    fun validate(file: MultipartFile, allowType: FileType?, width: Int?, height: Int?): ValidatedFileMetadata {
        if (file.isEmpty) throw FileEmptyException()

        file.rejectBlockedSignature()

        val extension = file.extractExtension()
        val detectedType = FileType.fromExtension(extension)
            ?: throw FileTypeNotAllowedException()

        if (allowType != null && detectedType != allowType) {
            throw FileTypeNotAllowedException()
        }

        if (width != null && height != null && detectedType.supportsDimensionCheck) {
            validateDimension(file, width, height)
        }

        return ValidatedFileMetadata(
            extension = extension,
            contentType = FileType.contentTypeFromExtension(extension)
                ?: throw FileTypeNotAllowedException(),
        )
    }

    private fun MultipartFile.extractExtension(): String =
        originalFilename
            ?.substringAfterLast('.', "")
            ?.lowercase()
            ?.takeIf { it.isNotBlank() }
            ?: throw FileTypeNotAllowedException()

    private fun MultipartFile.rejectBlockedSignature() {
        val header = ByteArray(SIGNATURE_SIZE)
        val read = inputStream.use { it.readNBytes(header, 0, SIGNATURE_SIZE) }

        val blocked = BLOCKED_SIGNATURES.any { signature ->
            read >= signature.size && signature.indices.all { header[it] == signature[it] }
        }

        if (blocked) throw FileTypeNotAllowedException()
    }

    private fun validateDimension(file: MultipartFile, requiredWidth: Int, requiredHeight: Int) {
        val iis = ImageIO.createImageInputStream(file.inputStream)
            ?: throw FileDimensionReadFailedException()

        val reader = ImageIO.getImageReaders(iis)
            .takeIf { it.hasNext() }
            ?.next()
            ?: run {
                iis.close()
                throw FileDimensionReadFailedException()
            }

        try {
            reader.input = iis
            if (reader.getWidth(0) != requiredWidth || reader.getHeight(0) != requiredHeight) {
                throw FileDimensionNotAllowedException()
            }
        } finally {
            reader.dispose()
            iis.close()
        }
    }

    companion object {
        private val BLOCKED_SIGNATURES = listOf("GIF87a", "GIF89a")
            .map { it.toByteArray(Charsets.US_ASCII) }

        private val SIGNATURE_SIZE = BLOCKED_SIGNATURES.maxOf { it.size }
    }
}
