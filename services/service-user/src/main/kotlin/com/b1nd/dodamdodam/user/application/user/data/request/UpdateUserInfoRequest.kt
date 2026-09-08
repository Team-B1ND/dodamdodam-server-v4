package com.b1nd.dodamdodam.user.application.user.data.request

import com.b1nd.dodamdodam.user.domain.user.enumeration.Gender

data class UpdateUserInfoRequest(
    val name: String?,
    val phone: String?,
    val profileImage: String?,
    val gender: Gender?,
)
