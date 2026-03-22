package com.b1nd.dodamdodam.outsleeping.application.outsleeping.data

import com.b1nd.dodamdodam.grpc.user.UserResponse
import com.b1nd.dodamdodam.outsleeping.application.outsleeping.data.request.ApplyOutSleepingRequest
import com.b1nd.dodamdodam.outsleeping.application.outsleeping.data.response.DeadlineResponse
import com.b1nd.dodamdodam.outsleeping.application.outsleeping.data.response.DeniedOutSleepingResponse
import com.b1nd.dodamdodam.outsleeping.application.outsleeping.data.response.OutSleepingResponse
import com.b1nd.dodamdodam.outsleeping.application.outsleeping.data.response.StudentResponse
import com.b1nd.dodamdodam.outsleeping.domain.deadline.entity.OutSleepingDeadlineEntity
import com.b1nd.dodamdodam.outsleeping.domain.outsleeping.entity.OutSleepingEntity
import java.util.UUID

fun ApplyOutSleepingRequest.toEntity(userId: UUID) = OutSleepingEntity(
    userId = userId,
    reason = reason,
    startAt = startAt,
    endAt = endAt,
)

fun OutSleepingEntity.toResponse(userInfo: UserResponse?) = OutSleepingResponse(
    id = id!!,
    reason = reason,
    status = status,
    student = userInfo?.student?.toStudentResponse(userInfo.name),
    startAt = startAt,
    endAt = endAt,
)

fun OutSleepingEntity.toDeniedResponse(userInfo: UserResponse?) = DeniedOutSleepingResponse(
    id = id!!,
    reason = reason,
    status = status,
    student = userInfo?.student?.toStudentResponse(userInfo.name),
    denyReason = denyReason,
    startAt = startAt,
    endAt = endAt,
)

fun com.b1nd.dodamdodam.grpc.user.StudentInfo.toStudentResponse(name: String) = StudentResponse(
    name = name,
    grade = grade,
    room = room,
    number = number,
)

fun OutSleepingDeadlineEntity.toResponse() = DeadlineResponse(
    startDayOfWeek = startDayOfWeek,
    startTime = startTime,
    endDayOfWeek = endDayOfWeek,
    endTime = endTime,
)
