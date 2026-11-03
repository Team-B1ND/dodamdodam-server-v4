package com.b1nd.dodamdodam.user.presentation.user.grpc

import com.b1nd.dodamdodam.core.common.coroutine.CoroutineBlockingExecutor
import com.b1nd.dodamdodam.core.common.exception.BasicException
import com.b1nd.dodamdodam.grpc.user.SendParentSmsRequest
import com.b1nd.dodamdodam.grpc.user.SendParentSmsResponse
import com.b1nd.dodamdodam.grpc.user.UserSmsServiceGrpcKt
import com.b1nd.dodamdodam.user.application.user.UserSmsUseCase
import io.grpc.Status
import io.grpc.StatusRuntimeException
import net.devh.boot.grpc.server.service.GrpcService
import org.slf4j.LoggerFactory
import java.util.UUID

@GrpcService
class UserSmsGrpcController(
    private val useCase: UserSmsUseCase,
    private val blockingExecutor: CoroutineBlockingExecutor
): UserSmsServiceGrpcKt.UserSmsServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(this::class.java)

    override suspend fun sendParentSms(request: SendParentSmsRequest): SendParentSmsResponse {
        try {
            val accepted: Boolean = blockingExecutor.execute {
                useCase.sendParentSms(UUID.fromString(request.studentPublicId), request.message)
            }
            return SendParentSmsResponse.newBuilder().setAccepted(accepted).build()
        } catch (ex: BasicException) {
            throw StatusRuntimeException(
                Status.NOT_FOUND.withDescription(ex.message)
            )
        } catch (ex: Exception) {
            log.error("보호자께 Sms 전송을 실패했습니다.", ex)
            throw StatusRuntimeException(
                Status.INTERNAL.withDescription(ex.message).withCause(ex)
            )
        }
    }
}
