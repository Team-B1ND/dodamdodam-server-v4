package com.b1nd.dodamdodam.user.application.user.data.request

import com.b1nd.dodamdodam.user.domain.user.enumeration.Gender
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty

data class TeacherRegisterRequest(
    @NotBlank
    val username: String,
    @NotBlank
    val name: String,
    @NotBlank
    val password: String,
    @NotBlank
    val phone: String,
    @NotBlank
    val position: String,
    @NotEmpty
    val gender: Gender,
)
