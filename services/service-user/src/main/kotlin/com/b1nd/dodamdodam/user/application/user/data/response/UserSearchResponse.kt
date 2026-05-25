package com.b1nd.dodamdodam.user.application.user.data.response

import com.b1nd.dodamdodam.core.security.passport.enumerations.RoleType
import com.b1nd.dodamdodam.user.domain.user.enumeration.StatusType
import java.time.LocalDateTime
import java.util.UUID

data class UserSearchResponse(
    val publicId: UUID,
    val username: String,
    val name: String,
    val phone: String?,
    val profileImage: String?,
    val status: StatusType,
    val roles: Set<RoleType>,
    val student: StudentInfoResponse?,
    val teacher: TeacherInfoResponse?,
    val admin: AdminInfoResponse?,
    val createdAt: LocalDateTime,
)