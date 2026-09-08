package com.b1nd.dodamdodam.user.domain.user.service

import com.b1nd.dodamdodam.core.security.passport.enumerations.RoleType
import com.b1nd.dodamdodam.user.domain.user.entity.UserEntity
import com.b1nd.dodamdodam.user.domain.user.entity.UserRoleEntity
import com.b1nd.dodamdodam.user.domain.user.enumeration.Gender
import com.b1nd.dodamdodam.user.domain.user.enumeration.StatusType
import com.b1nd.dodamdodam.user.domain.user.exception.PhoneAlreadyExistsException
import com.b1nd.dodamdodam.user.domain.user.exception.UserAlreadyExistsException
import com.b1nd.dodamdodam.user.domain.user.exception.UserNotFoundException
import com.b1nd.dodamdodam.user.domain.user.repository.UserRepository
import com.b1nd.dodamdodam.user.domain.user.repository.UserRoleRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.mockito.Mockito.mockingDetails
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.springframework.security.crypto.password.PasswordEncoder
import java.util.UUID

class UserServiceGenderTest {
    private val userRepository = mock(UserRepository::class.java)
    private val userRoleRepository = mock(UserRoleRepository::class.java)
    private val encoder = mock(PasswordEncoder::class.java)
    private val userService = UserService(
        userRepository = userRepository,
        userRoleRepository = userRoleRepository,
        encoder = encoder,
    )

    private val publicId: UUID = UUID.randomUUID()

    @Test
    fun `회원가입 시 성별이 담긴 유저가 저장되고 권한이 부여된다`() {
        val user = user(Gender.FEMALE)
        `when`(encoder.encode("password")).thenReturn("encoded-password")
        `when`(userRepository.save(any<UserEntity>())).thenAnswer { it.getArgument(0) }

        val saved = userService.create(user, RoleType.STUDENT)

        assertEquals(Gender.FEMALE, saved.gender)
        assertEquals("encoded-password", saved.password)
        assertEquals(StatusType.PENDING, saved.status)
        verify(userRepository).save(user)
        assertEquals(listOf(RoleType.STUDENT), savedRoles().map { it.role })
    }

    @Test
    fun `이미 존재하는 아이디면 회원가입에 실패하고 저장하지 않는다`() {
        val user = user(Gender.MALE)
        `when`(userRepository.existsByUsername("dodam")).thenReturn(true)

        assertThrows(UserAlreadyExistsException::class.java) {
            userService.create(user, RoleType.STUDENT)
        }

        verify(userRepository, never()).save(any<UserEntity>())
    }

    @Test
    fun `이미 사용 중인 전화번호면 회원가입에 실패한다`() {
        val user = user(Gender.MALE)
        `when`(userRepository.existsByPhone("01012345678")).thenReturn(true)

        assertThrows(PhoneAlreadyExistsException::class.java) {
            userService.create(user, RoleType.STUDENT)
        }

        verify(userRepository, never()).save(any<UserEntity>())
    }

    @Test
    fun `프로필 수정 시 성별이 변경되어 저장된다`() {
        val user = user(Gender.MALE)
        `when`(userRepository.findByPublicId(publicId)).thenReturn(user)
        `when`(userRepository.save(any<UserEntity>())).thenAnswer { it.getArgument(0) }

        val updated = userService.update(publicId, null, null, null, Gender.FEMALE)

        assertEquals(Gender.FEMALE, updated.gender)
        assertEquals("박준석", updated.name)
        verify(userRepository).save(user)
    }

    @Test
    fun `프로필 수정 시 성별을 보내지 않으면 기존 성별이 유지된다`() {
        val user = user(Gender.FEMALE)
        `when`(userRepository.findByPublicId(publicId)).thenReturn(user)
        `when`(userRepository.save(any<UserEntity>())).thenAnswer { it.getArgument(0) }

        val updated = userService.update(publicId, "김도담", null, null, null)

        assertEquals(Gender.FEMALE, updated.gender)
        assertEquals("김도담", updated.name)
    }

    @Test
    fun `존재하지 않는 유저의 프로필은 수정할 수 없다`() {
        `when`(userRepository.findByPublicId(publicId)).thenReturn(null)

        assertThrows(UserNotFoundException::class.java) {
            userService.update(publicId, null, null, null, Gender.FEMALE)
        }

        verify(userRepository, never()).save(any<UserEntity>())
    }

    @Test
    fun `프로필 수정 시 전화번호가 중복이면 성별도 변경되지 않는다`() {
        val user = user(Gender.MALE)
        `when`(userRepository.findByPublicId(publicId)).thenReturn(user)
        `when`(userRepository.existsByPhone("01099998888")).thenReturn(true)

        assertThrows(PhoneAlreadyExistsException::class.java) {
            userService.update(publicId, null, "01099998888", null, Gender.FEMALE)
        }

        assertEquals(Gender.MALE, user.gender)
        assertEquals("01012345678", user.phone)
        verify(userRepository, never()).save(any<UserEntity>())
    }

    /**
     * Mockito matcher 는 null 을 반환해 코틀린 null 검사에 걸리므로,
     * 저장된 권한은 mock 에 기록된 호출에서 직접 꺼낸다.
     */
    private fun savedRoles(): List<UserRoleEntity> =
        mockingDetails(userRoleRepository).invocations
            .filter { it.method.name == "saveAll" }
            .flatMap { it.getArgument<Iterable<UserRoleEntity>>(0) }

    private fun <T> any(): T = org.mockito.ArgumentMatchers.any()

    private fun user(gender: Gender) = UserEntity(
        username = "dodam",
        name = "박준석",
        password = "password",
        phone = "01012345678",
        status = StatusType.PENDING,
        gender = gender,
    )
}
