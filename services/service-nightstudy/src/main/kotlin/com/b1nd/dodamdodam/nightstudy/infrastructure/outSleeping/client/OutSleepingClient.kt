package com.b1nd.dodamdodam.nightstudy.infrastructure.outSleeping.client

import com.b1nd.dodamdodam.core.common.exception.base.BaseInternalServerException
import com.b1nd.dodamdodam.grpc.outsleeping.GetOutSleepingRequest
import com.b1nd.dodamdodam.grpc.outsleeping.GetOutSleepingResponse
import com.b1nd.dodamdodam.grpc.outsleeping.OutSleepingQueryServiceGrpcKt
import io.grpc.Status
import io.grpc.StatusException
import io.grpc.StatusRuntimeException
import net.devh.boot.grpc.client.inject.GrpcClient
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class   OutSleepingClient {
    @GrpcClient("service-out-sleeping")
    private lateinit var stub: OutSleepingQueryServiceGrpcKt.OutSleepingQueryServiceCoroutineStub

    suspend fun getOutSleeping(userIds: List<UUID>): GetOutSleepingResponse = runCatching {
        val request = GetOutSleepingRequest.newBuilder()
            .addAllUserId(userIds.map { it.toString() })
            .build()
        return stub.getOutSleeping(request)
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