package com.b1nd.dodamdodam.nightstudy.application.openapi.data

import com.b1nd.dodamdodam.grpc.user.StudentInfo
import com.b1nd.dodamdodam.grpc.user.UserResponse
import com.b1nd.dodamdodam.nightstudy.application.openapi.data.response.OpenApiNightStudyResponse
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.command.NightStudyWithMembersCommand
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.command.NightStudyWithMembersCommand.Participation
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.command.NightStudyWithMembersCommand.PeriodAssignment
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.enumeration.NightStudyType
import com.b1nd.dodamdodam.nightstudy.domain.room.policy.StudyRoomPolicy

fun UserResponse.toStudent(participation: Participation): OpenApiNightStudyResponse.Student? {
    val student = student ?: return null
    return OpenApiNightStudyResponse.Student(
        name = name,
        grade = student.grade,
        room = student.room,
        number = student.number,
        attended = OpenApiNightStudyResponse.Attendance(
            period1 = participation.attendance.period1,
            period2 = participation.attendance.period2,
        ),
        studyRoom = OpenApiNightStudyResponse.StudyRoom(
            period1 = participation.assignment.period1.toRoom(student),
            period2 = participation.assignment.period2.toRoom(student),
        ),
    )
}

fun List<NightStudyWithMembersCommand>.toOpenApiNightStudyResponse(
    userMap: Map<String?, UserResponse>
): List<OpenApiNightStudyResponse> {
    return mapNotNull { command ->
        val leader = command.leaderId
            ?.let { id -> userMap[id.toString()]?.toStudent(command.participations[id]) }
            ?: return@mapNotNull null

        val members = command.memberIds.mapNotNull { id ->
            userMap[id.toString()]?.toStudent(command.participations[id])
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

private fun PeriodAssignment?.toRoom(student: StudentInfo): OpenApiNightStudyResponse.Room? {
    if (this == null) return null

    if (type == NightStudyType.PROJECT) {
        val name = projectRoomName ?: return null
        val floor = projectRoomFloor ?: return null
        return OpenApiNightStudyResponse.Room(name = name, floor = floor)
    }

    val name = StudyRoomPolicy.classRoomName(student.grade, student.room) ?: return null
    return OpenApiNightStudyResponse.Room(
        name = name,
        floor = StudyRoomPolicy.classRoomFloor(student.grade, student.room),
    )
}
