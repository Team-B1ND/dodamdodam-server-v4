package com.b1nd.dodamdodam.user.application.user

import com.b1nd.dodamdodam.user.domain.student.entity.StudentEntity
import com.b1nd.dodamdodam.user.domain.student.exception.StudentNotFoundException
import com.b1nd.dodamdodam.user.domain.student.service.StudentService
import com.b1nd.dodamdodam.user.domain.user.entity.UserEntity
import com.b1nd.dodamdodam.user.domain.user.enumeration.Gender
import com.b1nd.dodamdodam.user.domain.user.enumeration.StatusType
import com.b1nd.dodamdodam.user.domain.user.exception.UserNotFoundException
import com.b1nd.dodamdodam.user.domain.user.service.UserService
import com.b1nd.dodamdodam.user.infrastructure.sms.GabiaSmsSender
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito.doThrow
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.mockito.Mockito.mockingDetails
import org.mockito.Mockito.verify
import org.springframework.http.HttpStatus
import org.springframework.web.client.HttpServerErrorException
import org.springframework.web.client.ResourceAccessException
import java.util.UUID

class UserSmsUseCaseTest {
    private val userService = mock(UserService::class.java)
    private val studentService = mock(StudentService::class.java)
    private val gabiaSmsSender = mock(GabiaSmsSender::class.java)
    private val useCase = UserSmsUseCase(
        userService = userService,
        studentService = studentService,
        gabiaSmsSender = gabiaSmsSender,
    )

    private val studentId: UUID = UUID.randomUUID()

    @Test
    fun `보호자 연락처로 문자를 발송하고 접수 결과를 반환한다`() {
        val user = user()
        `when`(userService.get(studentId)).thenReturn(user)
        `when`(studentService.get(user)).thenReturn(StudentEntity(user, 2, 3, 11, parentPhone = "01099998888"))

        val accepted = useCase.sendParentSms(studentId, "외박이 승인되었어요.")

        assertEquals(true, accepted)
        verify(gabiaSmsSender).send("01099998888", "외박이 승인되었어요.")
    }

    @Test
    fun `보호자 연락처가 없으면 문자를 발송하지 않는다`() {
        val user = user()
        `when`(userService.get(studentId)).thenReturn(user)
        `when`(studentService.get(user)).thenReturn(StudentEntity(user, 2, 3, 11))

        val accepted = useCase.sendParentSms(studentId, "외박이 승인되었어요.")

        assertEquals(false, accepted)
        assertEquals(0, mockingDetails(gabiaSmsSender).invocations.size)
    }

    @Test
    fun `학생 정보가 없으면 문자를 발송하지 않는다`() {
        val user = user()
        `when`(userService.get(studentId)).thenReturn(user)
        `when`(studentService.get(user)).thenThrow(StudentNotFoundException())

        val accepted = useCase.sendParentSms(studentId, "외박이 승인되었어요.")

        assertEquals(false, accepted)
        assertEquals(0, mockingDetails(gabiaSmsSender).invocations.size)
    }

    @Test
    fun `유저가 없으면 문자를 발송하지 않는다`() {
        `when`(userService.get(studentId)).thenThrow(UserNotFoundException())

        val accepted = useCase.sendParentSms(studentId, "외박이 승인되었어요.")

        assertEquals(false, accepted)
        assertEquals(0, mockingDetails(gabiaSmsSender).invocations.size)
    }

    @Test
    fun `가비아가 오류를 응답하면 예외 없이 false를 반환한다`() {
        val user = user()
        `when`(userService.get(studentId)).thenReturn(user)
        `when`(studentService.get(user)).thenReturn(StudentEntity(user, 2, 3, 11, parentPhone = "01099998888"))
        doThrow(HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR))
            .`when`(gabiaSmsSender).send("01099998888", "외박이 승인되었어요.")

        val accepted = useCase.sendParentSms(studentId, "외박이 승인되었어요.")

        assertEquals(false, accepted)
    }

    @Test
    fun `가비아 연결에 실패해도 예외 없이 false를 반환한다`() {
        val user = user()
        `when`(userService.get(studentId)).thenReturn(user)
        `when`(studentService.get(user)).thenReturn(StudentEntity(user, 2, 3, 11, parentPhone = "01099998888"))
        doThrow(ResourceAccessException("handshake_failure"))
            .`when`(gabiaSmsSender).send("01099998888", "외박이 승인되었어요.")

        val accepted = useCase.sendParentSms(studentId, "외박이 승인되었어요.")

        assertEquals(false, accepted)
    }

    private fun user() = UserEntity(
        username = "dodam",
        name = "박준석",
        password = "password",
        phone = "01012345678",
        status = StatusType.ACTIVE,
        gender = Gender.MALE,
    )
}
