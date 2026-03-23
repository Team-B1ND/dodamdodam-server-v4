package com.b1nd.dodamdodam.nightstudy.application.nightstudy.data

import com.b1nd.dodamdodam.grpc.user.UserResponse
import com.b1nd.dodamdodam.nightstudy.application.nightstudy.data.request.ApplyPersonalNightStudyRequest
import com.b1nd.dodamdodam.nightstudy.application.nightstudy.data.request.ApplyProjectNightStudyRequest
import com.b1nd.dodamdodam.nightstudy.application.nightstudy.data.response.OpenApiNightStudyResponse
import com.b1nd.dodamdodam.nightstudy.application.nightstudy.data.response.OpenApiUserInfoResponse
import com.b1nd.dodamdodam.nightstudy.application.nightstudy.data.response.PersonalNightStudyResponse
import com.b1nd.dodamdodam.nightstudy.application.nightstudy.data.response.ProjectNightStudyResponse
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.entity.NightStudyEntity
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.enumeration.NightStudyType
import java.util.UUID

fun ApplyPersonalNightStudyRequest.toEntity() = NightStudyEntity(
    type = NightStudyType.PERSONAL,
    description = description,
    period = period,
    startAt = startAt,
    endAt = endAt,
    needPhone = needPhone,
    needPhoneReason = needPhoneReason,
)

fun ApplyProjectNightStudyRequest.toEntity() = NightStudyEntity(
    name = name,
    description = description,
    period = period,
    startAt = startAt,
    endAt = endAt,
    type = NightStudyType.PROJECT,
    needPhone = false
)

fun NightStudyEntity.toPersonalNightStudyResponse() = PersonalNightStudyResponse(
    id = publicId!!,
    description = description,
    period = period,
    startAt = startAt,
    endAt = endAt,
    needPhone = needPhone,
    needPhoneReason = needPhoneReason,
    rejectionReason = rejectionReason,
)

fun List<NightStudyEntity>.toPersonalNightStudyListResponse() = map { it.toPersonalNightStudyResponse() }

fun NightStudyEntity.toProjectNightStudyResponse(isLeader: Boolean) = ProjectNightStudyResponse(
    id = publicId!!,
    description = description,
    period = period,
    startAt = startAt,
    endAt = endAt,
    name = name!!,
    rejectionReason = rejectionReason,
    isLeader = isLeader,
)

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
    id = publicId!!,
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