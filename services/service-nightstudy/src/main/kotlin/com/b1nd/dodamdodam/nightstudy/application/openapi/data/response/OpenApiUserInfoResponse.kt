package com.b1nd.dodamdodam.nightstudy.application.openapi.data.response

import java.util.UUID

data class OpenApiUserInfoResponse(
    val publicId: UUID,
    val username: String,
    val name: String,
    val phone: String?,
    val profileImage: String?,
    val status: String,
    val roles: List<String>,
    val student: StudentInfo?,
    val teacher: TeacherInfo?,
) {
    data class StudentInfo(
        val grade: Int,
        val room: Int,
        val number: Int,
    )

    data class TeacherInfo(
        val position: String,
    )
}
