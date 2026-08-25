package com.b1nd.dodamdodam.outsleeping.application.outsleeping.data

import com.b1nd.dodamdodam.grpc.outsleeping.GetOutSleepingResponse
import com.b1nd.dodamdodam.grpc.outsleeping.OutSleeping
import com.b1nd.dodamdodam.grpc.user.UserResponse
import com.b1nd.dodamdodam.outsleeping.application.outsleeping.data.request.ApplyOutSleepingRequest
import com.b1nd.dodamdodam.outsleeping.application.outsleeping.data.response.DeadlineResponse
import com.b1nd.dodamdodam.outsleeping.application.outsleeping.data.response.MyOutSleepingResponse
import com.b1nd.dodamdodam.outsleeping.application.outsleeping.data.response.OutSleepingResponse
import com.b1nd.dodamdodam.outsleeping.application.outsleeping.data.response.StudentResponse
import com.b1nd.dodamdodam.outsleeping.domain.deadline.entity.OutSleepingDeadlineEntity
import com.b1nd.dodamdodam.outsleeping.domain.outsleeping.entity.OutSleepingEntity
import com.b1nd.dodamdodam.outsleeping.domain.outsleeping.enumeration.OutSleepingStatusType
import java.util.UUID


fun OutSleepingEntity.toGrpcResponse(): OutSleeping =
    OutSleeping.newBuilder()
        .setUserId(userId.toString())
        .setStartAt(startAt.toString())
        .setEndAt(endAt.toString())
        .setStatus(status.toString())
        .build()

fun List<OutSleepingEntity>.toGetOutSleepings(): GetOutSleepingResponse =
    GetOutSleepingResponse.newBuilder()
        .addAllOutSleepings(map { it.toGrpcResponse() })
        .build()

fun ApplyOutSleepingRequest.toEntity(userId: UUID, type: OutSleepingStatusType) = OutSleepingEntity(
    userId = userId,
    reason = reason,
    startAt = startAt,
    endAt = endAt,
    statusType = type
)

fun OutSleepingEntity.toResponse(userInfo: UserResponse?) = OutSleepingResponse(
    publicId = publicId!!,
    reason = reason,
    status = status,
    statusType = statusType,
    student = userInfo?.student?.toStudentResponse(userInfo.name),
    startAt = startAt,
    endAt = endAt,
)

fun OutSleepingEntity.toMyResponse() = MyOutSleepingResponse(
    publicId = publicId!!,
    reason = reason,
    status = status,
    statusType = statusType,
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
