package com.b1nd.dodamdodam.inapp.application.app.data.response

import com.b1nd.dodamdodam.inapp.domain.app.entity.AppEntity
import java.util.UUID

data class GetAllAppsResponse(
    val appId: UUID?,
    val name: String,
    val subtitle: String
) {
    companion object {
        fun of(app: AppEntity) =
            GetAllAppsResponse(
                appId = app.publicId,
                name = app.name,
                subtitle = app.subtitle
            )

        fun fromList(apps: List<AppEntity>): List<GetAllAppsResponse> {
            return apps.map { of(it) }
        }
    }
}
