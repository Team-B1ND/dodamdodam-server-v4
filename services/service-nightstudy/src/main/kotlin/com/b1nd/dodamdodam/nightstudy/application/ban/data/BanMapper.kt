package com.b1nd.dodamdodam.nightstudy.application.ban.data

import com.b1nd.dodamdodam.grpc.user.UserResponse
import com.b1nd.dodamdodam.nightstudy.application.ban.data.request.BanUserRequest
import com.b1nd.dodamdodam.nightstudy.application.ban.data.response.BannedUserResponse
import com.b1nd.dodamdodam.nightstudy.application.ban.data.response.DormitoryManagerBannedUserResponse
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.entity.NightStudyBannedEntity

fun BanUserRequest.toEntity() = NightStudyBannedEntity(
    userId = userId,
    reason = reason,
    endAt = endAt,
)

fun NightStudyBannedEntity.toResponse() = BannedUserResponse(
    userId = userId,
    reason = reason,
    endAt = endAt,
    createdAt = createdAt!!,
)

fun List<NightStudyBannedEntity>.toResponseList() = map { it.toResponse() }

fun List<NightStudyBannedEntity>.toDormitoryManagerResponseList(
    usersMap: Map<String, UserResponse>,
): List<DormitoryManagerBannedUserResponse> = mapNotNull { ban ->
    usersMap[ban.userId.toString()]?.let { user ->
        DormitoryManagerBannedUserResponse(
            userId = ban.userId,
            reason = ban.reason,
            name = user.name,
            student = user.student?.let {
                DormitoryManagerBannedUserResponse.StudentInfo(
                    grade = it.grade,
                    room = it.room,
                    number = it.number,
                )
            } ?: error("could not find user"),
            phone = user.phone.orEmpty(),
            endAt = ban.endAt,
            createdAt = ban.createdAt!!,
            )
    }
}
