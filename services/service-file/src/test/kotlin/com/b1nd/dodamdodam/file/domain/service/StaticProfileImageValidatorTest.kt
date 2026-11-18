package com.b1nd.dodamdodam.file.domain.service

import com.b1nd.dodamdodam.file.domain.exception.InvalidProfileImageException
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.nio.ByteBuffer
import java.util.zip.CRC32

class StaticProfileImageValidatorTest {
    private val validator = StaticProfileImageValidator()

    @ParameterizedTest
    @ValueSource(strings = ["png", "jpg", "bmp", "tiff", "webp"])
    fun `정상 정적 이미지는 허용한다`(extension: String) {
        assertDoesNotThrow { validator.validate(fixture("static.$extension"), extension) }
    }

    @ParameterizedTest
    @ValueSource(strings = ["animated.png", "animated.webp", "animated.gif", "multipage.tiff"])
    fun `움직이는 이미지와 다중 페이지는 거부한다`(filename: String) {
        assertThrows(InvalidProfileImageException::class.java) {
            validator.validate(fixture(filename), filename.substringAfterLast('.'))
        }
    }

    @Test
    fun `png 확장자로 위장한 GIF와 영상은 거부한다`() {
        val video = byteArrayOf(0, 0, 0, 24) + "ftypmp42".toByteArray() + ByteArray(32)
        listOf(fixture("animated.gif"), video, "<html>not an image</html>".toByteArray()).forEach {
            assertThrows(InvalidProfileImageException::class.java) { validator.validate(it, "png") }
        }
    }

    @Test
    fun `실제 이미지와 다른 확장자는 거부한다`() {
        assertThrows(InvalidProfileImageException::class.java) { validator.validate(fixture("static.png"), "jpg") }
    }

    @Test
    fun `데이터가 잘리거나 청크 길이가 위조된 이미지는 거부한다`() {
        val png = fixture("static.png")
        val forged = png.clone().also { ByteBuffer.wrap(it, 8, 4).putInt(Int.MAX_VALUE) }
        listOf(png.copyOf(33), forged, ByteArray(0)).forEach {
            assertThrows(InvalidProfileImageException::class.java) { validator.validate(it, "png") }
        }
    }

    @Test
    fun `과도한 이미지 크기는 디코딩 전에 거부한다`() {
        listOf(9000 to 1, 4096 to 4096).forEach { (width, height) ->
            val png = fixture("static.png")
            ByteBuffer.wrap(png, 16, 8).putInt(width).putInt(height)
            val crc = CRC32().apply { update(png, 12, 17) }
            ByteBuffer.wrap(png, 29, 4).putInt(crc.value.toInt())
            assertThrows(InvalidProfileImageException::class.java) { validator.validate(png, "png") }
        }
    }

    private fun fixture(name: String) = javaClass.getResourceAsStream("/profile/$name")!!.use { it.readBytes() }
}
