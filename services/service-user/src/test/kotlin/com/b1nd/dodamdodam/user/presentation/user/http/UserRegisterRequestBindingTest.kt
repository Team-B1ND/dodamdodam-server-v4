package com.b1nd.dodamdodam.user.presentation.user.http

import com.b1nd.dodamdodam.core.common.data.Response
import com.b1nd.dodamdodam.user.application.user.UserUseCase
import com.b1nd.dodamdodam.user.application.user.data.request.StudentRegisterRequest
import com.b1nd.dodamdodam.user.application.user.data.request.TeacherRegisterRequest
import com.b1nd.dodamdodam.user.application.user.data.request.UpdateUserInfoRequest
import com.b1nd.dodamdodam.user.domain.user.enumeration.Gender
import com.b1nd.dodamdodam.user.infrastructure.exception.UserExceptionHandler
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.mockito.Mockito.mockingDetails
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.springframework.http.MediaType
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders

class UserRegisterRequestBindingTest {
    private val userUseCase = mock(UserUseCase::class.java)
    private val mockMvc = MockMvcBuilders.standaloneSetup(UserController(userUseCase))
        .setControllerAdvice(UserExceptionHandler())
        .setMessageConverters(MappingJackson2HttpMessageConverter(jacksonObjectMapper()))
        .build()

    @Test
    fun `유효한 학생 회원가입 요청은 성별과 함께 유스케이스로 전달된다`() {
        `when`(userUseCase.registerStudent(any())).thenReturn(Response.created("학생 계정이 생성되었어요."))

        mockMvc.perform(
            post("/register-student")
                .contentType(MediaType.APPLICATION_JSON)
                .content(studentBody(gender = "\"FEMALE\""))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status").value(201))
            .andExpect(jsonPath("$.message").value("학생 계정이 생성되었어요."))

        val request = requestsTo("registerStudent").single() as StudentRegisterRequest
        assertEquals(Gender.FEMALE, request.gender)
        assertEquals(2, request.grade)
    }

    @Test
    fun `성별 없이 학생 회원가입을 요청하면 400을 응답한다`() {
        mockMvc.perform(
            post("/register-student")
                .contentType(MediaType.APPLICATION_JSON)
                .content(studentBody(gender = null))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.message").value("요청 형식이 올바르지 않아요."))

        verify(userUseCase, never()).registerStudent(any())
    }

    @Test
    fun `존재하지 않는 성별로 회원가입을 요청하면 400을 응답한다`() {
        mockMvc.perform(
            post("/register-student")
                .contentType(MediaType.APPLICATION_JSON)
                .content(studentBody(gender = "\"UNKNOWN\""))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.message").value("요청 형식이 올바르지 않아요."))

        verify(userUseCase, never()).registerStudent(any())
    }

    @Test
    fun `성별에 null을 명시해도 400을 응답한다`() {
        mockMvc.perform(
            post("/register-student")
                .contentType(MediaType.APPLICATION_JSON)
                .content(studentBody(gender = "null"))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.message").value("요청 형식이 올바르지 않아요."))

        verify(userUseCase, never()).registerStudent(any())
    }

    @Test
    fun `본문이 깨져 있으면 400을 응답한다`() {
        mockMvc.perform(
            post("/register-student")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{ not json }")
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.message").value("요청 형식이 올바르지 않아요."))

        verify(userUseCase, never()).registerStudent(any())
    }

    @Test
    fun `모든 성별로 학생 회원가입을 요청할 수 있다`() {
        `when`(userUseCase.registerStudent(any())).thenReturn(Response.created("학생 계정이 생성되었어요."))

        Gender.entries.forEach { gender ->
            mockMvc.perform(
                post("/register-student")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(studentBody(gender = "\"${gender.name}\""))
            ).andExpect(status().isOk)
        }

        assertEquals(
            Gender.entries.toList(),
            requestsTo("registerStudent").map { (it as StudentRegisterRequest).gender },
        )
    }

    @Test
    fun `유효한 선생님 회원가입 요청은 성별과 함께 유스케이스로 전달된다`() {
        `when`(userUseCase.registerTeacher(any())).thenReturn(Response.created("선생님 계정이 생성되었어요."))

        mockMvc.perform(
            post("/register-teacher")
                .contentType(MediaType.APPLICATION_JSON)
                .content(teacherBody(gender = "\"MALE\""))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status").value(201))

        val request = requestsTo("registerTeacher").single() as TeacherRegisterRequest
        assertEquals(Gender.MALE, request.gender)
        assertEquals("담임", request.position)
    }

    @Test
    fun `성별 없이 선생님 회원가입을 요청하면 400을 응답한다`() {
        mockMvc.perform(
            post("/register-teacher")
                .contentType(MediaType.APPLICATION_JSON)
                .content(teacherBody(gender = null))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.message").value("요청 형식이 올바르지 않아요."))

        verify(userUseCase, never()).registerTeacher(any())
    }

    @Test
    fun `프로필 수정은 성별만 보내도 처리된다`() {
        `when`(userUseCase.updateUser(any())).thenReturn(Response.ok("유저 정보가 변경되었어요."))

        mockMvc.perform(
            patch("/")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"gender": "FEMALE"}""")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.message").value("유저 정보가 변경되었어요."))

        val request = requestsTo("updateUser").single() as UpdateUserInfoRequest
        assertEquals(Gender.FEMALE, request.gender)
        assertNull(request.name)
        assertNull(request.phone)
        assertNull(request.profileImage)
    }

    @Test
    fun `프로필 수정은 성별 없이도 처리된다`() {
        `when`(userUseCase.updateUser(any())).thenReturn(Response.ok("유저 정보가 변경되었어요."))

        mockMvc.perform(
            patch("/")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"name": "김도담"}""")
        )
            .andExpect(status().isOk)

        val request = requestsTo("updateUser").single() as UpdateUserInfoRequest
        assertNull(request.gender)
        assertEquals("김도담", request.name)
    }

    @Test
    fun `프로필 수정에 존재하지 않는 성별을 보내면 400을 응답한다`() {
        mockMvc.perform(
            patch("/")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"gender": "UNKNOWN"}""")
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.message").value("요청 형식이 올바르지 않아요."))

        verify(userUseCase, never()).updateUser(any())
    }

    private fun studentBody(gender: String?) = buildString {
        append("""{"username":"dodam","name":"박준석","password":"password",""")
        append(""""phone":"01012345678","grade":2,"room":3,"number":11""")
        gender?.let { append(""","gender":$it""") }
        append("}")
    }

    private fun teacherBody(gender: String?) = buildString {
        append("""{"username":"teacher","name":"김선생","password":"password",""")
        append(""""phone":"01087654321","position":"담임"""")
        gender?.let { append(""","gender":$it""") }
        append("}")
    }

    private fun <T> any(): T = org.mockito.ArgumentMatchers.any()

    /**
     * Mockito matcher 는 null 을 반환해 코틀린 null 검사에 걸리므로,
     * 컨트롤러가 넘긴 요청은 mock 에 기록된 호출에서 직접 꺼낸다.
     */
    private fun requestsTo(method: String): List<Any> =
        mockingDetails(userUseCase).invocations
            .filter { it.method.name == method }
            .map { it.getArgument(0) }
}
