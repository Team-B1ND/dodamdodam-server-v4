package com.b1nd.dodamdodam.user.application.user.data

import com.b1nd.dodamdodam.user.application.user.data.request.StudentRegisterRequest
import com.b1nd.dodamdodam.user.application.user.data.request.TeacherRegisterRequest
import com.b1nd.dodamdodam.user.domain.user.enumeration.Gender
import com.b1nd.dodamdodam.user.domain.user.enumeration.StatusType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class UserRegisterMapperGenderTest {

    @Test
    fun `학생 회원가입 요청의 성별이 유저 엔티티로 매핑된다`() {
        val user = studentRequest(Gender.FEMALE).toUserEntity()

        assertEquals("dodam", user.username)
        assertEquals("박준석", user.name)
        assertEquals("password", user.password)
        assertEquals("01012345678", user.phone)
        assertEquals(StatusType.PENDING, user.status)
        assertEquals(Gender.FEMALE, user.gender)
    }

    @Test
    fun `학생 회원가입 요청의 학년 반 번호는 학생 엔티티로 매핑된다`() {
        val request = studentRequest(Gender.MALE)
        val user = request.toUserEntity()

        val student = request.toStudentEntity(user)

        assertEquals(2, student.grade)
        assertEquals(3, student.room)
        assertEquals(11, student.number)
        assertEquals(user, student.user)
        assertEquals(Gender.MALE, student.user.gender)
    }

    @Test
    fun `선생님 회원가입 요청의 성별이 유저 엔티티로 매핑된다`() {
        val user = teacherRequest(Gender.MALE).toUserEntity()

        assertEquals("teacher", user.username)
        assertEquals(StatusType.PENDING, user.status)
        assertEquals(Gender.MALE, user.gender)
    }

    @Test
    fun `선생님 회원가입 요청의 직책은 선생님 엔티티로 매핑된다`() {
        val request = teacherRequest(Gender.FEMALE)
        val user = request.toUserEntity()

        val teacher = request.toTeacherEntity(user)

        assertEquals("담임", teacher.position)
        assertEquals(user, teacher.user)
    }

    @Test
    fun `모든 성별이 회원가입 요청에서 그대로 매핑된다`() {
        Gender.entries.forEach { gender ->
            assertEquals(gender, studentRequest(gender).toUserEntity().gender)
            assertEquals(gender, teacherRequest(gender).toUserEntity().gender)
        }
    }

    private fun studentRequest(gender: Gender) = StudentRegisterRequest(
        username = "dodam",
        name = "박준석",
        password = "password",
        phone = "01012345678",
        grade = 2,
        room = 3,
        number = 11,
        gender = gender,
    )

    private fun teacherRequest(gender: Gender) = TeacherRegisterRequest(
        username = "teacher",
        name = "김선생",
        password = "password",
        phone = "01087654321",
        position = "담임",
        gender = gender,
    )
}
