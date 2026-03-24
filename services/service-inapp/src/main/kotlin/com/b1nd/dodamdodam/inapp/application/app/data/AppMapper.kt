package com.b1nd.dodamdodam.inapp.application.app.data

import com.b1nd.dodamdodam.inapp.application.app.data.request.CreateAppRequest
import com.b1nd.dodamdodam.inapp.application.app.data.request.EditAppRequest
import com.b1nd.dodamdodam.grpc.user.UserResponse
import com.b1nd.dodamdodam.inapp.application.app.data.response.ActiveAppResponse
import com.b1nd.dodamdodam.inapp.application.app.data.response.AppDetailResponse
import com.b1nd.dodamdodam.inapp.application.app.data.response.AppReleaseDetailResponse
import com.b1nd.dodamdodam.inapp.application.app.data.response.AppReleaseResponse
import com.b1nd.dodamdodam.inapp.application.app.data.response.AppSummaryResponse
import com.b1nd.dodamdodam.inapp.application.app.data.response.ReleaseUserResponse
import com.b1nd.dodamdodam.inapp.domain.app.command.CreateAppCommand
import com.b1nd.dodamdodam.inapp.domain.app.command.EditAppCommand
import com.b1nd.dodamdodam.inapp.domain.app.entity.AppEntity
import com.b1nd.dodamdodam.inapp.domain.app.entity.AppReleaseEntity
import java.util.UUID

fun AppReleaseEntity.toResponse(userMap: Map<UUID, UserResponse>) = AppReleaseResponse(
    releaseId = publicId!!,
    releaseUrl = releaseUrl,
    memo = memo,
    denyResult = denyResult,
    status = status,
    enabled = enabled,
    updatedUser = userMap[updatedUser]?.let {
        ReleaseUserResponse(
            userId = updatedUser,
            name = it.name,
            profileImage = if (it.hasProfileImage()) it.profileImage else null,
        )
    },
    createdAt = createdAt,
    modifiedAt = modifiedAt,
)

fun List<AppReleaseEntity>.toResponses(userMap: Map<UUID, UserResponse>) = map { it.toResponse(userMap) }

fun AppReleaseEntity.toDetailResponse(releaseNote: String?) = AppReleaseDetailResponse(
    releaseId = publicId!!,
    releaseUrl = releaseUrl,
    memo = memo,
    denyResult = denyResult,
    status = status,
    enabled = enabled,
    releaseNote = releaseNote,
    createdAt = createdAt,
    modifiedAt = modifiedAt,
)

fun AppEntity.toDetailResponse(
    releases: List<AppReleaseEntity>,
    userMap: Map<UUID, UserResponse> = emptyMap(),
) = AppDetailResponse(
    appId = publicId!!,
    teamId = team.publicId!!,
    name = name,
    subtitle = subtitle,
    description = description,
    iconUrl = iconUrl,
    darkIconUrl = darkIconUrl,
    inquiryMail = inquiryMail,
    active = releases.any { it.enabled },
    releases = releases.toResponses(userMap),
)

fun AppEntity.toSummaryResponse() = AppSummaryResponse(
    appId = publicId!!,
    teamId = team.publicId!!,
    name = name,
    subtitle = subtitle,
    description = description,
    iconUrl = iconUrl,
    darkIconUrl = darkIconUrl,
    inquiryMail = inquiryMail,
    releaseEnabled = releaseEnabled,
    releaseStatus = releaseStatus,
)

fun List<AppEntity>.toSummaryResponses() = map { it.toSummaryResponse() }

fun AppEntity.toActiveAppResponse() = ActiveAppResponse(
    appId = publicId!!,
    name = name,
    subtitle = subtitle,
    description = description,
    iconUrl = iconUrl,
    darkIconUrl = darkIconUrl,
)

fun AppEntity.toActiveAppResponse(releasePublicId: java.util.UUID?, s3BaseUrl: String?) = ActiveAppResponse(
    appId = publicId!!,
    name = name,
    subtitle = subtitle,
    description = description,
    iconUrl = iconUrl,
    darkIconUrl = darkIconUrl,
    appUrl = if (!s3BaseUrl.isNullOrBlank() && releasePublicId != null) {
        "${s3BaseUrl.trimEnd('/')}/inapp/$publicId/releases/$releasePublicId/index.html"
    } else null,
)

fun CreateAppRequest.toCommand() = CreateAppCommand(
    teamId = teamId,
    name = name,
    subtitle = subtitle,
    description = description,
    iconUrl = iconUrl,
    darkIconUrl = darkIconUrl,
    inquiryMail = inquiryMail,
    githubReleaseUrl = githubReleaseUrl,
)

fun EditAppRequest.toCommand() = EditAppCommand(
    appId = appId,
    name = name,
    subtitle = subtitle,
    description = description,
    iconUrl = iconUrl,
    darkIconUrl = darkIconUrl,
    inquiryMail = inquiryMail,
)
