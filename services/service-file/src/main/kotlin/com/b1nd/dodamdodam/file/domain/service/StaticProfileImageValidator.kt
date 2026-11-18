package com.b1nd.dodamdodam.file.domain.service

import com.b1nd.dodamdodam.file.domain.exception.InvalidProfileImageException
import org.springframework.stereotype.Component
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.imageio.ImageIO
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
            if (expectedFormat == "png") rejectAnimatedPng(bytes)
            if (expectedFormat == "webp") rejectAnimatedWebp(bytes)
            MemoryCacheImageInputStream(bytes.inputStream()).use { input ->
                val readers = ImageIO.getImageReaders(input)
                if (!readers.hasNext()) throw InvalidProfileImageException()
                val reader = readers.next()
                try {
                    reader.input = input
                    val actualFormat = reader.formatName.lowercase().let { if (it == "tif") "tiff" else it }
                    if (actualFormat != expectedFormat || reader.getNumImages(true) != 1) {
                        throw InvalidProfileImageException()
                    }
                    val width = reader.getWidth(0)
                    val height = reader.getHeight(0)
                    if (width !in 1..8192 || height !in 1..8192 || width.toLong() * height > 16_000_000) {
                        throw InvalidProfileImageException()
                    }
                    // 헤더뿐 아니라 실제 이미지 데이터도 디코딩한다.
                    if (reader.read(0) == null) throw InvalidProfileImageException()
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

    // ImageIO는 APNG도 단일 PNG로 읽으므로 애니메이션 청크를 별도로 검사한다.
    private fun rejectAnimatedPng(bytes: ByteArray) {
        val signature = byteArrayOf(0x89.toByte(), 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a)
        if (!bytes.take(8).toByteArray().contentEquals(signature)) throw InvalidProfileImageException()
        var offset = 8
        while (offset + 12 <= bytes.size) {
            val length = uint32(bytes, offset, ByteOrder.BIG_ENDIAN)
            val end = offset.toLong() + length + 12
            if (end > bytes.size) throw InvalidProfileImageException()
            when (fourCC(bytes, offset + 4)) {
                "acTL", "fcTL", "fdAT" -> throw InvalidProfileImageException()
                "IEND" -> {
                    if (length != 0L || end != bytes.size.toLong()) throw InvalidProfileImageException()
                    return
                }
            }
            offset = end.toInt()
        }
        throw InvalidProfileImageException()
    }

    private fun rejectAnimatedWebp(bytes: ByteArray) {
        if (bytes.size < 12 || fourCC(bytes, 0) != "RIFF" || fourCC(bytes, 8) != "WEBP" ||
            uint32(bytes, 4, ByteOrder.LITTLE_ENDIAN) + 8 != bytes.size.toLong()) throw InvalidProfileImageException()
        var offset = 12
        while (offset + 8 <= bytes.size) {
            val length = uint32(bytes, offset + 4, ByteOrder.LITTLE_ENDIAN)
            val end = offset.toLong() + 8 + length + length % 2
            if (end > bytes.size) throw InvalidProfileImageException()
            when (fourCC(bytes, offset)) {
                "ANIM", "ANMF" -> throw InvalidProfileImageException()
                "VP8X" -> if (length != 10L || bytes[offset + 8].toInt() and 0x02 != 0) throw InvalidProfileImageException()
            }
            offset = end.toInt()
        }
        if (offset != bytes.size) throw InvalidProfileImageException()
    }

    private fun fourCC(bytes: ByteArray, offset: Int) = String(bytes, offset, 4, Charsets.US_ASCII)
    private fun uint32(bytes: ByteArray, offset: Int, order: ByteOrder) =
        ByteBuffer.wrap(bytes, offset, 4).order(order).int.toLong() and 0xffffffffL
}
