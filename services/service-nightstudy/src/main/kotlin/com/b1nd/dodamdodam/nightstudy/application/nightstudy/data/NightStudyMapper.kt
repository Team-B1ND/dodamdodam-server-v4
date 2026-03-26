package com.b1nd.dodamdodam.nightstudy.application.nightstudy.data

import com.b1nd.dodamdodam.grpc.user.UserResponse
import com.b1nd.dodamdodam.nightstudy.application.nightstudy.data.request.PersonalNightStudyApplyRequest
import com.b1nd.dodamdodam.nightstudy.application.nightstudy.data.request.ProjectNightStudyApplyRequest
import com.b1nd.dodamdodam.nightstudy.application.nightstudy.data.response.NightStudyApplicationResponse
import com.b1nd.dodamdodam.nightstudy.application.nightstudy.data.response.NightStudyApplicantResponse
import com.b1nd.dodamdodam.nightstudy.application.nightstudy.data.response.PersonalNightStudyResponse
import com.b1nd.dodamdodam.nightstudy.application.nightstudy.data.response.ProjectNightStudyResponse
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.command.NightStudyWithMembersCommand
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.entity.NightStudyEntity
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.enumeration.NightStudyType
import java.util.UUID

fun PersonalNightStudyApplyRequest.toEntity() = NightStudyEntity(
    type = NightStudyType.PERSONAL,
    description = description,
    period = period,
    startAt = startAt,
    endAt = endAt,
    needPhone = needPhone,
    needPhoneReason = needPhoneReason,
)

fun ProjectNightStudyApplyRequest.toEntity() = NightStudyEntity(
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
    status = status
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
    status = status,
    room = room?.let { ProjectNightStudyResponse.RoomInfo(id = it.id!!, name = it.name) },
)

fun UserResponse.toOpenApiUserInfoResponse() = NightStudyApplicantResponse(
    publicId = UUID.fromString(publicId),
    username = username,
    name = name,
    phone = phone?.takeIf { it.isNotBlank() },
    profileImage = profileImage?.takeIf { it.isNotBlank() },
    status = status,
    roles = rolesList,
    student = student?.let {
        NightStudyApplicantResponse.StudentInfo(
            grade = it.grade,
            room = it.room,
            number = it.number,
        )
    },
    teacher = teacher?.let {
        NightStudyApplicantResponse.TeacherInfo(
            position = it.position,
        )
    },
)

fun NightStudyEntity.toOpenApiNightStudyResponse(
    leader: NightStudyApplicantResponse,
    members: List<NightStudyApplicantResponse>
) = NightStudyApplicationResponse(
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
    room = room?.let { NightStudyApplicationResponse.RoomInfo(id = it.id!!, name = it.name) },
)

fun List<NightStudyWithMembersCommand>.toNightStudyApplicationResponses(
    usersMap: Map<String, NightStudyApplicantResponse>
): List<NightStudyApplicationResponse> {
    return mapNotNull { command ->
        command.leaderId?.let { leaderId ->
            usersMap[leaderId.toString()]?.let { leader ->
                command.nightStudy.toOpenApiNightStudyResponse(
                    leader = leader,
                    members = command.memberIds.mapNotNull { memberId -> usersMap[memberId.toString()] }
                )
            }
        }
    }
}

fun NightStudyEntity.toNightStudyApplicationDetailResponse(
    leaderId: UUID?,
    memberIds: List<UUID>,
    usersMap: Map<String, NightStudyApplicantResponse>
): NightStudyApplicationResponse {
    return toOpenApiNightStudyResponse(
        leader = usersMap[leaderId.toString()]!!,
        members = memberIds.mapNotNull { usersMap[it.toString()] }
    )
}