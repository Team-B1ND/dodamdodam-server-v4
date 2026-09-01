package com.b1nd.dodamdodam.user.domain.user.entity

import com.b1nd.dodamdodam.user.domain.user.enumeration.Gender
import com.b1nd.dodamdodam.user.domain.user.enumeration.StatusType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class UserEntityGenderTest {

    @Test
    fun `회원가입 시 성별이 함께 저장된다`() {
        val user = user(gender = Gender.FEMALE)

        assertEquals(Gender.FEMALE, user.gender)
        assertEquals(StatusType.PENDING, user.status)
    }

    @Test
    fun `모든 성별로 가입할 수 있다`() {
        Gender.entries.forEach { gender ->
            assertEquals(gender, user(gender = gender).gender)
        }
    }

    @Test
    fun `프로필 수정 시 성별만 변경하면 나머지 정보는 유지된다`() {
        val user = user(gender = Gender.MALE)

        user.updateInfo(name = null, phone = null, profileImage = null, gender = Gender.FEMALE)

        assertEquals(Gender.FEMALE, user.gender)
        assertEquals("박준석", user.name)
        assertEquals("01012345678", user.phone)
        assertEquals("https://image.b1nd.com/profile.png", user.profileImage)
    }

    @Test
    fun `프로필 수정 시 성별이 null이면 기존 성별을 유지한다`() {
        val user = user(gender = Gender.FEMALE)

        user.updateInfo(name = "김도담", phone = null, profileImage = null, gender = null)

        assertEquals(Gender.FEMALE, user.gender)
        assertEquals("김도담", user.name)
    }

    @Test
    fun `프로필 수정 시 이름 전화번호 프로필사진 성별을 한 번에 변경할 수 있다`() {
        val user = user(gender = Gender.MALE)

        user.updateInfo(
            name = "김도담",
            phone = "01099998888",
            profileImage = "https://image.b1nd.com/new.png",
            gender = Gender.FEMALE,
        )

        assertEquals("김도담", user.name)
        assertEquals("01099998888", user.phone)
        assertEquals("https://image.b1nd.com/new.png", user.profileImage)
        assertEquals(Gender.FEMALE, user.gender)
    }

    @Test
    fun `프로필 수정은 아이디와 비밀번호를 바꾸지 않는다`() {
        val user = user(gender = Gender.MALE)

        user.updateInfo(name = "김도담", phone = null, profileImage = null, gender = Gender.FEMALE)

        assertEquals("dodam", user.username)
        assertEquals("password", user.password)
    }

    private fun user(gender: Gender) = UserEntity(
        username = "dodam",
        name = "박준석",
        password = "password",
        profileImage = "https://image.b1nd.com/profile.png",
        phone = "01012345678",
        status = StatusType.PENDING,
        gender = gender,
    )
}
