package com.b1nd.dodamdodam.outsleeping.presentation.outsleeping.grpc

import com.b1nd.dodamdodam.core.common.coroutine.CoroutineBlockingExecutor
import com.b1nd.dodamdodam.grpc.outsleeping.GetOutSleepingRequest
import com.b1nd.dodamdodam.grpc.outsleeping.GetOutSleepingResponse
import com.b1nd.dodamdodam.grpc.outsleeping.OutSleepingQueryServiceGrpcKt
import com.b1nd.dodamdodam.outsleeping.application.outsleeping.data.toGetOutSleepings
import com.b1nd.dodamdodam.outsleeping.domain.outsleeping.repository.OutSleepingRepository
import io.grpc.Status
import io.grpc.StatusRuntimeException
import net.devh.boot.grpc.server.service.GrpcService
import org.slf4j.LoggerFactory
import java.util.UUID

@GrpcService
class OutSleepingQueryGrpcController(
    private val blockingExecutor: CoroutineBlockingExecutor,
    private val outSleepingRepository: OutSleepingRepository
): OutSleepingQueryServiceGrpcKt.OutSleepingQueryServiceCoroutineImplBase() {
    private val log = LoggerFactory.getLogger(this::class.java)

    override suspend fun getOutSleeping(request: GetOutSleepingRequest): GetOutSleepingResponse {
        try {
            val userIds = request.userIdList.map { UUID.fromString(it) }
            val outSleepings =blockingExecutor.execute {
                outSleepingRepository.findAllByUserIdIn(userIds)
            }
            return outSleepings.toGetOutSleepings()
        } catch (e: Exception) {
            log.error("gRPC getOutSleeping 실패: ", e)
            throw StatusRuntimeException(
                Status.INTERNAL.withDescription(e.message).withCause(e)
            )
        }
    }
}