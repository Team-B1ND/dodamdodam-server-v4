package com.b1nd.dodamdodam.user.domain.user.service

import com.b1nd.dodamdodam.user.domain.user.entity.UserEntity
import com.b1nd.dodamdodam.user.domain.user.enumeration.Gender
import com.b1nd.dodamdodam.user.domain.user.exception.InvalidProfileImageException
import com.b1nd.dodamdodam.user.domain.user.exception.ProfileImageValidationUnavailableException
import com.b1nd.dodamdodam.user.domain.user.repository.UserRepository
import com.b1nd.dodamdodam.user.domain.user.repository.UserRoleRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.springframework.security.crypto.password.PasswordEncoder
import java.util.UUID

class UserProfileImageValidationTest {
    private val repository = mock(UserRepository::class.java)
    private val validator = mock(ProfileImageValidator::class.java)
    private val service = UserService(repository, mock(UserRoleRepository::class.java), validator, mock(PasswordEncoder::class.java))
    private val id = UUID.randomUUID()
    private val user = UserEntity(username = "test", name = "기존 이름", password = "unused", gender = Gender.MALE, profileImage = "old.png")

    @Test
    fun `정상 이미지는 검증 후 저장한다`() {
        `when`(repository.findByPublicId(id)).thenReturn(user)
        `when`(repository.save(user)).thenReturn(user)
        service.update(id, null, null, "https://storage.example/a.png", null)
        val order = inOrder(validator, repository)
        order.verify(validator).validate("https://storage.example/a.png")
        order.verify(repository).save(user)
        assertEquals("https://storage.example/a.png", user.profileImage)
    }

    @Test
    fun `검증 거부와 장애 시 다른 사용자 정보까지 변경하지 않는다`() {
        listOf(InvalidProfileImageException(), ProfileImageValidationUnavailableException()).forEach { failure ->
            `when`(repository.findByPublicId(id)).thenReturn(user)
            doThrow(failure).`when`(validator).validate("bad-url")
            assertThrows(failure.javaClass) { service.update(id, "변경 이름", null, "bad-url", Gender.FEMALE) }
            assertEquals("기존 이름", user.name)
            assertEquals("old.png", user.profileImage)
            assertEquals(Gender.MALE, user.gender)
            verify(repository, never()).save(user)
        }
    }

    @Test
    fun `프로필 미변경과 빈 문자열 삭제에는 파일 서비스를 호출하지 않는다`() {
        `when`(repository.findByPublicId(id)).thenReturn(user)
        `when`(repository.save(user)).thenReturn(user)
        service.update(id, "새 이름", null, null, null)
        assertEquals("old.png", user.profileImage)
        service.update(id, null, null, "", null)
        assertEquals("", user.profileImage)
        verifyNoInteractions(validator)
    }
}
