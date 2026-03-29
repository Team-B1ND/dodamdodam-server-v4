package com.b1nd.dodamdodam.nightstudy.application.ban.data

import com.b1nd.dodamdodam.nightstudy.application.ban.data.request.BanUserRequest
import com.b1nd.dodamdodam.nightstudy.application.ban.data.response.BannedUserResponse
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
