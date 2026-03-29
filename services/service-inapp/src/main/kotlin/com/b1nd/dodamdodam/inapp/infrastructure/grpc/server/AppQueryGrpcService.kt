package com.b1nd.dodamdodam.inapp.infrastructure.grpc.server

import com.b1nd.dodamdodam.grpc.inapp.AppQueryServiceGrpcKt
import com.b1nd.dodamdodam.grpc.inapp.GetAppRequest
import com.b1nd.dodamdodam.grpc.inapp.GetAppResponse
import com.b1nd.dodamdodam.inapp.domain.app.repository.AppReleaseRepository
import com.b1nd.dodamdodam.inapp.domain.app.service.AppService
import com.b1nd.dodamdodam.inapp.infrastructure.config.InAppProperties
import net.devh.boot.grpc.server.service.GrpcService
import java.util.UUID

@GrpcService
class AppQueryGrpcService(
    private val appService: AppService,
    private val appReleaseRepository: AppReleaseRepository,
    private val inAppProperties: InAppProperties,
) : AppQueryServiceGrpcKt.AppQueryServiceCoroutineImplBase() {

    override suspend fun getApp(request: GetAppRequest): GetAppResponse {
        val app = appService.getApp(UUID.fromString(request.appPublicId))
        val activeRelease = appReleaseRepository.findAllByAppAndEnabledIsTrue(app).firstOrNull()
        val appUrl = if (inAppProperties.s3BaseUrl.isNotBlank() && activeRelease != null) {
            "${inAppProperties.s3BaseUrl.trimEnd('/')}/inapp/${app.publicId}/releases/${activeRelease.publicId}/index.html"
        } else ""

        return GetAppResponse.newBuilder()
            .setAppPublicId(app.publicId.toString())
            .setName(app.name)
            .setIconUrl(app.iconUrl)
            .setDarkIconUrl(app.darkIconUrl ?: "")
            .setAppUrl(appUrl)
            .build()
    }
}
