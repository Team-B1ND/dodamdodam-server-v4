package com.b1nd.dodamdodam.user.application.openapi.data

import com.b1nd.dodamdodam.core.security.passport.enumerations.RoleType
import com.b1nd.dodamdodam.user.domain.user.data.StudentDetails
import com.b1nd.dodamdodam.user.domain.user.data.UserWithDetails
import com.b1nd.dodamdodam.user.domain.user.enumeration.Gender
import com.b1nd.dodamdodam.user.domain.user.enumeration.StatusType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.UUID

class OpenApiUserMapperGenderTest {

    @Test
    fun `오픈 API 유저 응답에 성별이 포함된다`() {
        val response = userWithDetails(Gender.FEMALE).toUserInfoResponse()

        assertEquals(Gender.FEMALE, response.gender)
        assertEquals("dodam", response.username)
        assertEquals("박준석", response.name)
        assertEquals(2, response.student!!.grade)
    }

    @Test
    fun `모든 성별이 오픈 API 응답으로 그대로 전달된다`() {
        Gender.entries.forEach { gender ->
            assertEquals(gender, userWithDetails(gender).toUserInfoResponse().gender)
        }
    }

    private fun userWithDetails(gender: Gender) = UserWithDetails(
        publicId = UUID.randomUUID(),
        username = "dodam",
        name = "박준석",
        phone = "01012345678",
        gender = gender,
        profileImage = null,
        status = StatusType.ACTIVE,
        roles = setOf(RoleType.STUDENT),
        student = StudentDetails(2, 3, 11, false),
        teacher = null,
        createdAt = LocalDateTime.of(2026, 8, 30, 9, 0),
    )
}
