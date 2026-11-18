package com.b1nd.dodamdodam.file.presentation.file

import com.b1nd.dodamdodam.file.application.file.ProfileImageValidationService
import com.b1nd.dodamdodam.file.domain.exception.InvalidProfileImageException
import com.b1nd.dodamdodam.grpc.file.ProfileImageServiceGrpc
import com.b1nd.dodamdodam.grpc.file.ValidateProfileImageRequest
import com.b1nd.dodamdodam.grpc.file.ValidateProfileImageResponse
import io.grpc.Status
import io.grpc.stub.StreamObserver
import net.devh.boot.grpc.server.service.GrpcService
import org.slf4j.LoggerFactory

@GrpcService
class ProfileImageGrpcController(private val validationService: ProfileImageValidationService) :
    ProfileImageServiceGrpc.ProfileImageServiceImplBase() {
    private val log = LoggerFactory.getLogger(javaClass)

    override fun validateProfileImage(request: ValidateProfileImageRequest, observer: StreamObserver<ValidateProfileImageResponse>) {
        val valid = try {
            validationService.validate(request.url)
            true
        } catch (_: InvalidProfileImageException) {
            false
        } catch (e: Exception) {
            log.warn("프로필 이미지 검증에 실패했습니다.", e)
            observer.onError(Status.UNAVAILABLE.withDescription("Profile image validation unavailable").asRuntimeException())
            return
        }
        observer.onNext(ValidateProfileImageResponse.newBuilder().setValid(valid).build())
        observer.onCompleted()
    }
}
