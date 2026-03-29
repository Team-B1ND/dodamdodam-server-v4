package com.b1nd.dodamdodam.user.application.user.data.request

import jakarta.validation.constraints.NotBlank
import java.util.UUID

data class GraduateStudentRequest(
    @NotBlank
    val userId: UUID,
)
