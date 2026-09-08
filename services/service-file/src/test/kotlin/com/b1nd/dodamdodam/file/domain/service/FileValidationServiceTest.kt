package com.b1nd.dodamdodam.file.domain.service

import com.b1nd.dodamdodam.file.domain.enumeration.FileType
import com.b1nd.dodamdodam.file.domain.exception.FileTypeNotAllowedException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
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
}
