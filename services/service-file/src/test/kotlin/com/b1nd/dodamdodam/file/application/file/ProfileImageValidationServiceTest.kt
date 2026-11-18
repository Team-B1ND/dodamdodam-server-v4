package com.b1nd.dodamdodam.file.application.file

import com.b1nd.dodamdodam.file.domain.exception.InvalidProfileImageException
import com.b1nd.dodamdodam.file.domain.service.StaticProfileImageValidator
import com.b1nd.dodamdodam.file.infrastructure.s3.ProfileImageStorageReader
import com.b1nd.dodamdodam.file.infrastructure.s3.S3FileUrl
import com.b1nd.dodamdodam.file.infrastructure.s3.S3Properties
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.mockito.Mockito.verifyNoInteractions

class ProfileImageValidationServiceTest {
    private val storage = mock(ProfileImageStorageReader::class.java)
    private val urls = S3FileUrl(S3Properties(region = "ap-northeast-2", bucket = "dodam"))
    private val service = ProfileImageValidationService(urls, storage, StaticProfileImageValidator())

    @Test
    fun `외부 URL과 영상 URL은 저장소를 읽기 전에 거부한다`() {
        val invalidUrls = listOf("https://external.example/a.png", urls.build("a.mp4"), urls.build("a.gif"))

        invalidUrls.forEach { url ->
            assertThrows(InvalidProfileImageException::class.java) {
                service.validate(url)
            }
        }

        verifyNoInteractions(storage)
    }

    @Test
    fun `정상 URL이어도 실제 파일을 검사한다`() {
        `when`(storage.read("a.png")).thenReturn("GIF89a".toByteArray())

        assertThrows(InvalidProfileImageException::class.java) {
            service.validate(urls.build("a.png"))
        }
    }

    @Test
    fun `정상 URL의 정적 이미지는 허용한다`() {
        val bytes = javaClass.getResourceAsStream("/profile/static.png")!!.use { it.readBytes() }
        `when`(storage.read("a.png")).thenReturn(bytes)

        assertDoesNotThrow {
            service.validate(urls.build("a.png"))
        }
    }
}
