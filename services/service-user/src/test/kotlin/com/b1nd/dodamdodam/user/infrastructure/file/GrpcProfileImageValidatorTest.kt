package com.b1nd.dodamdodam.user.infrastructure.file

import com.b1nd.dodamdodam.grpc.file.ProfileImageServiceGrpc
import com.b1nd.dodamdodam.grpc.file.ValidateProfileImageRequest
import com.b1nd.dodamdodam.grpc.file.ValidateProfileImageResponse
import com.b1nd.dodamdodam.user.domain.user.exception.InvalidProfileImageException
import com.b1nd.dodamdodam.user.domain.user.exception.ProfileImageValidationUnavailableException
import io.grpc.Context
import io.grpc.ManagedChannelBuilder
import io.grpc.ServerBuilder
import io.grpc.Status
import io.grpc.stub.StreamObserver
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.test.util.ReflectionTestUtils
import java.util.concurrent.TimeUnit

class GrpcProfileImageValidatorTest {
    private var valid = true
    private var error: Status? = null
    private var receivedUrl: String? = null
    private var deadlineMillis: Long? = null
    private val fileService = object : ProfileImageServiceGrpc.ProfileImageServiceImplBase() {
        override fun validateProfileImage(
            request: ValidateProfileImageRequest,
            observer: StreamObserver<ValidateProfileImageResponse>,
        ) {
            receivedUrl = request.url
            deadlineMillis = Context.current().deadline?.timeRemaining(TimeUnit.MILLISECONDS)

            val status = error
            if (status != null) {
                observer.onError(status.asRuntimeException())
                return
            }

            val response = ValidateProfileImageResponse.newBuilder()
                .setValid(valid)
                .build()
            observer.onNext(response)
            observer.onCompleted()
        }
    }
    private val server = ServerBuilder.forPort(0)
        .addService(fileService)
        .build()
        .start()
    private val channel = ManagedChannelBuilder.forAddress("127.0.0.1", server.port)
        .usePlaintext()
        .build()
    private val validator = GrpcProfileImageValidator().also {
        ReflectionTestUtils.setField(it, "stub", ProfileImageServiceGrpc.newBlockingStub(channel))
    }

    @AfterEach
    fun close() {
        channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS)
        server.shutdownNow().awaitTermination(5, TimeUnit.SECONDS)
    }

    @Test
    fun `URL을 전달하고 호출 시간 제한을 적용한다`() {
        validator.validate("https://storage.example/image.png")

        assertEquals("https://storage.example/image.png", receivedUrl)
        assertTrue(deadlineMillis!! in 1..8000)
    }

    @Test
    fun `파일 검증 거부를 사용자 오류로 전달한다`() {
        valid = false

        assertThrows(InvalidProfileImageException::class.java) {
            validator.validate("bad-url")
        }
    }

    @Test
    fun `통신 장애와 시간 초과 시 저장을 허용하지 않는다`() {
        listOf(Status.UNAVAILABLE, Status.DEADLINE_EXCEEDED).forEach { status ->
            error = status

            assertThrows(ProfileImageValidationUnavailableException::class.java) {
                validator.validate("some-url")
            }
        }
    }

    @Test
    fun `공백과 과도한 길이의 URL은 원격 호출 전에 거부한다`() {
        listOf(" ", "x".repeat(2049)).forEach { url ->
            assertThrows(InvalidProfileImageException::class.java) {
                validator.validate(url)
            }
        }

        assertNull(receivedUrl)
    }
}
