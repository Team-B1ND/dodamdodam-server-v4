package com.b1nd.dodamdodam.file.domain.service

import com.b1nd.dodamdodam.file.domain.exception.InvalidProfileImageException
import org.springframework.stereotype.Component
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.imageio.ImageIO
import javax.imageio.ImageReader
import javax.imageio.stream.MemoryCacheImageInputStream

@Component
class StaticProfileImageValidator {
    fun validate(bytes: ByteArray, extension: String) {
        try {
            val expectedFormat = when (extension.lowercase()) {
                "jpg", "jpeg" -> "jpeg"
                "png", "bmp", "webp", "tiff" -> extension.lowercase()
                else -> throw InvalidProfileImageException()
            }

            when (expectedFormat) {
                "png" -> rejectAnimatedPng(bytes)
                "webp" -> rejectAnimatedWebp(bytes)
            }

            MemoryCacheImageInputStream(bytes.inputStream()).use { input ->
                val readers = ImageIO.getImageReaders(input)
                if (!readers.hasNext()) {
                    throw InvalidProfileImageException()
                }

                val reader = readers.next()
                try {
                    reader.input = input
                    validateImage(reader, expectedFormat)
                } finally {
                    reader.dispose()
                }
            }
        } catch (e: InvalidProfileImageException) {
            throw e
        } catch (_: Exception) {
            throw InvalidProfileImageException()
        }
    }

    private fun validateImage(reader: ImageReader, expectedFormat: String) {
        val actualFormat = when (val format = reader.formatName.lowercase()) {
            "tif" -> "tiff"
            else -> format
        }

        if (actualFormat != expectedFormat || reader.getNumImages(true) != 1) {
            throw InvalidProfileImageException()
        }

        val width = reader.getWidth(0)
        val height = reader.getHeight(0)
        val pixelCount = width.toLong() * height

        if (width !in 1..MAX_DIMENSION || height !in 1..MAX_DIMENSION || pixelCount > MAX_PIXELS) {
            throw InvalidProfileImageException()
        }

        // 헤더만 정상인 손상 파일도 거부하도록 실제 이미지 데이터를 디코딩한다.
        if (reader.read(0) == null) {
            throw InvalidProfileImageException()
        }
    }

    // ImageIO는 APNG도 단일 PNG로 읽으므로 애니메이션 청크를 별도로 검사한다.
    private fun rejectAnimatedPng(bytes: ByteArray) {
        val signature = bytes.take(PNG_SIGNATURE.size).toByteArray()
        if (!signature.contentEquals(PNG_SIGNATURE)) {
            throw InvalidProfileImageException()
        }

        var offset = PNG_SIGNATURE.size
        while (offset + 12 <= bytes.size) {
            val length = readUnsignedInt(bytes, offset, ByteOrder.BIG_ENDIAN)
            val end = offset.toLong() + length + 12
            if (end > bytes.size) {
                throw InvalidProfileImageException()
            }

            when (readChunkType(bytes, offset + 4)) {
                "acTL", "fcTL", "fdAT" -> throw InvalidProfileImageException()
                "IEND" -> {
                    if (length != 0L || end != bytes.size.toLong()) {
                        throw InvalidProfileImageException()
                    }
                    return
                }
            }

            offset = end.toInt()
        }

        throw InvalidProfileImageException()
    }

    private fun rejectAnimatedWebp(bytes: ByteArray) {
        if (bytes.size < 12) {
            throw InvalidProfileImageException()
        }

        val containerType = readChunkType(bytes, 0)
        val imageType = readChunkType(bytes, 8)
        val declaredSize = readUnsignedInt(bytes, 4, ByteOrder.LITTLE_ENDIAN) + 8

        if (containerType != "RIFF" || imageType != "WEBP" || declaredSize != bytes.size.toLong()) {
            throw InvalidProfileImageException()
        }

        var offset = 12
        while (offset + 8 <= bytes.size) {
            val length = readUnsignedInt(bytes, offset + 4, ByteOrder.LITTLE_ENDIAN)
            val padding = length % 2
            val end = offset.toLong() + 8 + length + padding
            if (end > bytes.size) {
                throw InvalidProfileImageException()
            }

            when (readChunkType(bytes, offset)) {
                "ANIM", "ANMF" -> throw InvalidProfileImageException()
                "VP8X" -> {
                    if (length != 10L) {
                        throw InvalidProfileImageException()
                    }

                    val hasAnimation = (bytes[offset + 8].toInt() and 0x02) != 0
                    if (hasAnimation) {
                        throw InvalidProfileImageException()
                    }
                }
            }

            offset = end.toInt()
        }

        if (offset != bytes.size) {
            throw InvalidProfileImageException()
        }
    }

    private fun readChunkType(bytes: ByteArray, offset: Int): String =
        String(bytes, offset, 4, Charsets.US_ASCII)

    private fun readUnsignedInt(bytes: ByteArray, offset: Int, order: ByteOrder): Long =
        ByteBuffer.wrap(bytes, offset, 4).order(order).int.toLong() and 0xffffffffL

    companion object {
        private const val MAX_DIMENSION = 8192
        private const val MAX_PIXELS = 16_000_000
        private val PNG_SIGNATURE = byteArrayOf(0x89.toByte(), 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a)
    }
}
