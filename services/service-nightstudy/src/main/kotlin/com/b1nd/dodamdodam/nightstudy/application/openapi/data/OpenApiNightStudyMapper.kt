package com.b1nd.dodamdodam.nightstudy.application.openapi.data

import com.b1nd.dodamdodam.grpc.user.UserResponse
import com.b1nd.dodamdodam.nightstudy.application.openapi.data.response.OpenApiNightStudyResponse
import com.b1nd.dodamdodam.nightstudy.application.openapi.data.response.OpenApiUserInfoResponse
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.entity.NightStudyEntity
import java.util.UUID

fun UserResponse.toOpenApiUserInfoResponse() = OpenApiUserInfoResponse(
    publicId = UUID.fromString(publicId),
    username = username,
    name = name,
    phone = if (hasPhone()) phone else null,
    profileImage = if (hasProfileImage()) profileImage else null,
    status = status,
    roles = rolesList,
    student = if (hasStudent()) {
        OpenApiUserInfoResponse.StudentInfo(
            grade = student.grade,
            room = student.room,
            number = student.number,
        )
    } else null,
    teacher = if (hasTeacher()) {
        OpenApiUserInfoResponse.TeacherInfo(
            position = teacher.position,
        )
    } else null,
)

fun NightStudyEntity.toOpenApiNightStudyResponse(
    leader: OpenApiUserInfoResponse,
    members: List<OpenApiUserInfoResponse>
) = OpenApiNightStudyResponse(
    id = id!!,
    name = name,
    description = description,
    period = period,
    startAt = startAt,
    endAt = endAt,
    needPhone = needPhone,
    needPhoneReason = needPhoneReason,
    status = status,
    leader = leader,
    members = members,
    rejectionReason = rejectionReason,
    type = type,
)
