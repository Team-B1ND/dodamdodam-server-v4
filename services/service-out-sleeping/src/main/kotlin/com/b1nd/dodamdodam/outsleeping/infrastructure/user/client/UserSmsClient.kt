package com.b1nd.dodamdodam.outsleeping.infrastructure.user.client

import com.b1nd.dodamdodam.core.common.exception.base.BaseInternalServerException
import com.b1nd.dodamdodam.grpc.user.SendParentSmsRequest
import com.b1nd.dodamdodam.grpc.user.UserSmsServiceGrpcKt
import io.grpc.Status
import io.grpc.StatusException
import io.grpc.StatusRuntimeException
import net.devh.boot.grpc.client.inject.GrpcClient
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class UserSmsClient {
    @GrpcClient("service-user")
    private lateinit var stub: UserSmsServiceGrpcKt.UserSmsServiceCoroutineStub

    suspend fun sendParentSms(studentId: UUID, message: String): Boolean = runCatching {
        val request = SendParentSmsRequest.newBuilder()
            .setStudentPublicId(studentId.toString())
            .setMessage(message)
            .build()
        stub.sendParentSms(request).accepted
    }.getOrElse { ex ->
        when (ex) {
            is StatusException -> throw when (ex.status.code) {
                Status.Code.NOT_FOUND -> BaseInternalServerException()
                else -> BaseInternalServerException()
            }
            is StatusRuntimeException -> throw when (ex.status.code) {
                Status.Code.NOT_FOUND -> BaseInternalServerException()
                else -> BaseInternalServerException()
            }
            else -> throw ex
        }
    }
}
