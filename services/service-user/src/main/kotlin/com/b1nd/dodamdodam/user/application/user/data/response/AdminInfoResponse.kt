package com.b1nd.dodamdodam.user.application.user.data.response

import com.b1nd.dodamdodam.user.domain.admin.entity.AdminEntity

data class AdminInfoResponse(
    val githubId: String,
) {
    companion object {
        fun fromAdminEntity(entity: AdminEntity) = AdminInfoResponse(entity.githubId)
    }
}