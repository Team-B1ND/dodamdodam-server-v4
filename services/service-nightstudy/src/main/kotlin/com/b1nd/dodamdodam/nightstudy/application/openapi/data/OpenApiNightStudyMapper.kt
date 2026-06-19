package com.b1nd.dodamdodam.nightstudy.application.openapi.data

import com.b1nd.dodamdodam.grpc.user.UserResponse
import com.b1nd.dodamdodam.nightstudy.application.nightstudy.data.response.NightStudyApplicantResponse
import com.b1nd.dodamdodam.nightstudy.application.openapi.data.response.OpenApiNightStudyResponse
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.command.NightStudyWithMembersCommand

fun UserResponse.toStudent(attended: Boolean): OpenApiNightStudyResponse.Student? {
    val student = student ?: return null
    return OpenApiNightStudyResponse.Student(
        name = name,
        grade = student.grade,
        room = student.room,
        number = student.number,
        attended = attended
    )
}

fun List<NightStudyWithMembersCommand>.toOpenApiNightStudyResponse(
    userMap: Map<String?, UserResponse>
): List<OpenApiNightStudyResponse> {
    return mapNotNull { command ->
        val leader = command.leaderId
            ?.let { id -> userMap[id.toString()]?.toStudent(id in command.attendedUserIds) }
            ?: return@mapNotNull null

        val members = command.memberIds.mapNotNull { id ->
            userMap[id.toString()]?.toStudent(id in command.attendedUserIds)
        }

        with(command.nightStudy) {
            OpenApiNightStudyResponse(
                name = name,
                description = description,
                period = period,
                type = type,
                startAt = startAt,
                endAt = endAt,
                status = status,
                leader = leader,
                members = members,
            )
        }
    }
}