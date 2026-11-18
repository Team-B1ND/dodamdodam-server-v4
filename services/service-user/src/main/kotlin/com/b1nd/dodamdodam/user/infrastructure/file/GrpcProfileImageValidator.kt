package com.b1nd.dodamdodam.user.infrastructure.file

import com.b1nd.dodamdodam.grpc.file.ProfileImageServiceGrpc
import com.b1nd.dodamdodam.grpc.file.ValidateProfileImageRequest
import com.b1nd.dodamdodam.user.domain.user.exception.InvalidProfileImageException
import com.b1nd.dodamdodam.user.domain.user.exception.ProfileImageValidationUnavailableException
import com.b1nd.dodamdodam.user.domain.user.service.ProfileImageValidator
import io.grpc.StatusRuntimeException
import net.devh.boot.grpc.client.inject.GrpcClient
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

@Component
class GrpcProfileImageValidator : ProfileImageValidator {
    @GrpcClient("service-file")
    private lateinit var stub: ProfileImageServiceGrpc.ProfileImageServiceBlockingStub

    override fun validate(url: String) {
        if (url.isBlank() || url.length > 2048) {
            throw InvalidProfileImageException()
        }

        val request = ValidateProfileImageRequest.newBuilder()
            .setUrl(url)
            .build()

        val response = try {
            stub.withDeadlineAfter(8, TimeUnit.SECONDS)
                .validateProfileImage(request)
        } catch (_: StatusRuntimeException) {
            throw ProfileImageValidationUnavailableException()
        }

        if (!response.valid) {
            throw InvalidProfileImageException()
        }
    }
}
