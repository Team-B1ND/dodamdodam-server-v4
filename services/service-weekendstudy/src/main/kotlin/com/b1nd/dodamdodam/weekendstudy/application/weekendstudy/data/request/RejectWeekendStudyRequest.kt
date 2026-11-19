package com.b1nd.dodamdodam.weekendstudy.application.weekendstudy.data.request

import jakarta.validation.constraints.NotBlank

data class RejectWeekendStudyRequest(
    @NotBlank
    val rejectionReason: String,
)
