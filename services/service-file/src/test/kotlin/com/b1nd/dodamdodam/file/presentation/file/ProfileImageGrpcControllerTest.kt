package com.b1nd.dodamdodam.file.presentation.file

import com.b1nd.dodamdodam.file.application.file.ProfileImageValidationService
import com.b1nd.dodamdodam.file.domain.exception.InvalidProfileImageException
import com.b1nd.dodamdodam.grpc.file.ValidateProfileImageRequest
import com.b1nd.dodamdodam.grpc.file.ValidateProfileImageResponse
import io.grpc.Status
import io.grpc.stub.StreamObserver
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.doThrow
import org.mockito.Mockito.mock

class ProfileImageGrpcControllerTest {
    private val service = mock(ProfileImageValidationService::class.java)
    private val controller = ProfileImageGrpcController(service)
    private val request = ValidateProfileImageRequest.newBuilder()
        .setUrl("test-url")
        .build()

    @Test
    fun `검증 성공과 거부를 응답한다`() {
        val success = Observer()
        controller.validateProfileImage(request, success)
        assertEquals(true, success.response?.valid)
        assertTrue(success.completed)

        doThrow(InvalidProfileImageException()).`when`(service).validate("test-url")
        val invalid = Observer()
        controller.validateProfileImage(request, invalid)
        assertEquals(false, invalid.response?.valid)
        assertTrue(invalid.completed)
    }

    @Test
    fun `저장소 장애에는 성공 응답을 보내지 않는다`() {
        doThrow(IllegalStateException("storage unavailable")).`when`(service).validate("test-url")
        val observer = Observer()
        controller.validateProfileImage(request, observer)
        assertNull(observer.response)
        assertFalse(observer.completed)
        assertEquals(Status.Code.UNAVAILABLE, Status.fromThrowable(observer.error!!).code)
    }

    private class Observer : StreamObserver<ValidateProfileImageResponse> {
        var response: ValidateProfileImageResponse? = null
        var error: Throwable? = null
        var completed = false

        override fun onNext(value: ValidateProfileImageResponse) {
            response = value
        }

        override fun onError(t: Throwable) {
            error = t
        }

        override fun onCompleted() {
            completed = true
        }
    }
}
