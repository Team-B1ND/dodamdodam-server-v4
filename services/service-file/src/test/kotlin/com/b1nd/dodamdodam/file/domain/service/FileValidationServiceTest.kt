package com.b1nd.dodamdodam.file.domain.service

import com.b1nd.dodamdodam.file.domain.enumeration.FileType
import com.b1nd.dodamdodam.file.domain.exception.FileTypeNotAllowedException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.mock.web.MockMultipartFile

class FileValidationServiceTest {

    private val fileValidationService = FileValidationService()

    @Test
    fun `허용 목록에 없는 확장자는 allowType이 없어도 거부한다`() {
        val file = MockMultipartFile(
            "file",
            "page.html",
            "text/html",
            "<html></html>".toByteArray(),
        )

        assertThrows<FileTypeNotAllowedException> {
            fileValidationService.validate(file, null, null, null)
        }
    }

    @Test
    fun `브라우저에서 실행 가능한 SVG 파일은 거부한다`() {
        val file = MockMultipartFile(
            "file",
            "image.svg",
            "image/svg+xml",
            "<svg onload=\"alert(1)\"></svg>".toByteArray(),
        )

        assertThrows<FileTypeNotAllowedException> {
            fileValidationService.validate(file, null, null, null)
        }
    }

    @Test
    fun `클라이언트 Content-Type 대신 확장자의 안전한 Content-Type을 사용한다`() {
        val file = MockMultipartFile(
            "file",
            "image.png",
            "text/html",
            "<html></html>".toByteArray(),
        )

        val metadata = fileValidationService.validate(file, FileType.IMAGE, null, null)

        assertEquals("png", metadata.extension)
        assertEquals("image/png", metadata.contentType)
    }

    @Test
    fun `요청한 파일 타입과 확장자 타입이 다르면 거부한다`() {
        val file = MockMultipartFile(
            "file",
            "video.mov",
            "video/quicktime",
            byteArrayOf(1),
        )

        assertThrows<FileTypeNotAllowedException> {
            fileValidationService.validate(file, FileType.IMAGE, null, null)
        }
    }

    @Test
    fun `허용된 모든 확장자는 서버 Content-Type이 정의되어 있다`() {
        FileType.entries
            .flatMap { it.extensions }
            .forEach { extension ->
                assertNotNull(FileType.contentTypeFromExtension(extension))
            }
    }

    @Test
    fun `gif는 화이트리스트에 존재하지 않는다`() {
        assertNull(FileType.fromExtension("gif"))
        assertNull(FileType.contentTypeFromExtension("gif"))
    }

    @Test
    fun `gif 확장자는 거부한다`() {
        val file = MockMultipartFile(
            "file",
            "animation.gif",
            "image/gif",
            gifBytes("GIF89a"),
        )

        assertThrows<FileTypeNotAllowedException> {
            fileValidationService.validate(file, null, null, null)
        }
    }

    @Test
    fun `확장자를 png로 위장한 GIF 파일은 거부한다`() {
        val file = MockMultipartFile(
            "file",
            "image.png",
            "image/png",
            gifBytes("GIF89a"),
        )

        assertThrows<FileTypeNotAllowedException> {
            fileValidationService.validate(file, FileType.IMAGE, null, null)
        }
    }

    @Test
    fun `GIF이 아닌 파일은 시그니처 검사를 통과한다`() {
        val file = MockMultipartFile(
            "file",
            "document.pdf",
            "application/pdf",
            "%PDF-1.7".toByteArray(),
        )

        val metadata = fileValidationService.validate(file, FileType.DOCUMENT, null, null)

        assertEquals("pdf", metadata.extension)
        assertEquals("application/pdf", metadata.contentType)
    }

    @Test
    fun `시그니처보다 짧은 파일도 검사에서 예외 없이 처리한다`() {
        val file = MockMultipartFile(
            "file",
            "tiny.txt",
            "text/plain",
            "ab".toByteArray(),
        )

        val metadata = fileValidationService.validate(file, null, null, null)

        assertEquals("txt", metadata.extension)
        assertEquals("text/plain", metadata.contentType)
    }

    private fun gifBytes(version: String): ByteArray =
        version.toByteArray(Charsets.US_ASCII) +
            byteArrayOf(0x01, 0x00, 0x01, 0x00, 0x00, 0x00, 0x00)
}
