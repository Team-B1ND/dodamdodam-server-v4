package com.b1nd.dodamdodam.weekendstudy.application.weekendstudy.data

import com.b1nd.dodamdodam.grpc.user.UserResponse
import com.b1nd.dodamdodam.weekendstudy.application.weekendstudy.data.request.PersonalWeekendStudyApplyRequest
import com.b1nd.dodamdodam.weekendstudy.application.weekendstudy.data.request.ProjectWeekendStudyApplyRequest
import com.b1nd.dodamdodam.weekendstudy.application.weekendstudy.data.response.PersonalWeekendStudyResponse
import com.b1nd.dodamdodam.weekendstudy.application.weekendstudy.data.response.ProjectWeekendStudyResponse
import com.b1nd.dodamdodam.weekendstudy.application.weekendstudy.data.response.WeekendStudyApplicantResponse
import com.b1nd.dodamdodam.weekendstudy.application.weekendstudy.data.response.WeekendStudyApplicationResponse
import com.b1nd.dodamdodam.weekendstudy.domain.room.entity.ProjectRoomEntity
import com.b1nd.dodamdodam.weekendstudy.domain.weekendstudy.entity.WeekendStudyEntity
import com.b1nd.dodamdodam.weekendstudy.domain.weekendstudy.entity.WeekendStudyPeriodEntity
import com.b1nd.dodamdodam.weekendstudy.domain.weekendstudy.enumeration.WeekendStudyType
import java.util.UUID

fun PersonalWeekendStudyApplyRequest.toEntity(userId: UUID) = WeekendStudyEntity(
    userId = userId,
    date = date,
    type = WeekendStudyType.PERSONAL,
)

fun ProjectWeekendStudyApplyRequest.toEntity(userId: UUID, wishRoom: ProjectRoomEntity?) = WeekendStudyEntity(
    userId = userId,
    date = date,
    type = WeekendStudyType.PROJECT,
    name = name,
    description = description,
    wishRoom = wishRoom,
)

fun List<WeekendStudyPeriodEntity>.toPersonalResponses() =
    groupBy { it.weekendStudy }.map { (weekendStudy, periods) ->
        PersonalWeekendStudyResponse(
            id = weekendStudy.publicId!!,
            date = weekendStudy.date,
            periods = periods.map { it.period },
            status = weekendStudy.status,
            rejectionReason = weekendStudy.rejectionReason,
        )
    }

fun List<WeekendStudyPeriodEntity>.toProjectResponses(userId: UUID) =
    groupBy { it.weekendStudy }.map { (weekendStudy, periods) ->
        ProjectWeekendStudyResponse(
            id = weekendStudy.publicId!!,
            name = weekendStudy.name!!,
            description = weekendStudy.description!!,
            date = weekendStudy.date,
            periods = periods.map { it.period },
            status = weekendStudy.status,
            rejectionReason = weekendStudy.rejectionReason,
            isLeader = weekendStudy.userId == userId,
            wishRoom = weekendStudy.wishRoom?.let { ProjectWeekendStudyResponse.RoomInfo(id = it.id!!, name = it.name) },
        )
    }

fun UserResponse.toApplicantResponse() = WeekendStudyApplicantResponse(
    publicId = UUID.fromString(publicId),
    username = username,
    name = name,
    phone = phone?.takeIf { it.isNotBlank() },
    profileImage = profileImage?.takeIf { it.isNotBlank() },
    status = status,
    roles = rolesList,
    student = student?.let {
        WeekendStudyApplicantResponse.StudentInfo(
            grade = it.grade,
            room = it.room,
            number = it.number,
        )
    },
    teacher = teacher?.let {
        WeekendStudyApplicantResponse.TeacherInfo(
            position = it.position,
        )
    },
)

fun List<WeekendStudyPeriodEntity>.toApplicationResponses(usersMap: Map<String, WeekendStudyApplicantResponse>) =
    groupBy { it.weekendStudy }.mapNotNull { (weekendStudy, periods) ->
        usersMap[weekendStudy.userId.toString()]?.let { leader ->
            WeekendStudyApplicationResponse(
                id = weekendStudy.publicId!!,
                type = weekendStudy.type,
                date = weekendStudy.date,
                periods = periods.filter { it.userId == weekendStudy.userId }.map { it.period },
                status = weekendStudy.status,
                rejectionReason = weekendStudy.rejectionReason,
                name = weekendStudy.name,
                description = weekendStudy.description,
                wishRoom = weekendStudy.wishRoom?.let { WeekendStudyApplicationResponse.RoomInfo(id = it.id!!, name = it.name) },
                leader = leader,
                members = periods.map { it.userId }.distinct()
                    .filter { it != weekendStudy.userId }
                    .mapNotNull { usersMap[it.toString()] },
            )
        }
    }
