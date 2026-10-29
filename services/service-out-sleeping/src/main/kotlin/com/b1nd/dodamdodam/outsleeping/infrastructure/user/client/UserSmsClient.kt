package com.b1nd.dodamdodam.outsleeping.infrastructure.user.client

import com.b1nd.dodamdodam.grpc.user.SendParentSmsRequest
import com.b1nd.dodamdodam.grpc.user.UserSmsServiceGrpcKt
import io.grpc.StatusException
import io.grpc.StatusRuntimeException
import net.devh.boot.grpc.client.inject.GrpcClient
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class UserSmsClient {
    @GrpcClient("service-user")
    private lateinit var stub: UserSmsServiceGrpcKt.UserSmsServiceCoroutineStub

    private val log = LoggerFactory.getLogger(javaClass)

    suspend fun sendParentSms(studentId: UUID, message: String): Boolean = runCatching {
        val request = SendParentSmsRequest.newBuilder()
            .setStudentPublicId(studentId.toString())
            .setMessage(message)
            .build()
        stub.sendParentSms(request).accepted
    }.getOrElse { ex ->
        when (ex) {
            is StatusException ->
                log.error("gRPC sendParentSms 실패: studentId={}, status={}, description={}", studentId, ex.status.code, ex.status.description, ex)
            is StatusRuntimeException ->
                log.error("gRPC sendParentSms 실패: studentId={}, status={}, description={}", studentId, ex.status.code, ex.status.description, ex)
            else ->
                log.error("gRPC sendParentSms 실패: studentId={}", studentId, ex)
        }
        false
    }
}
