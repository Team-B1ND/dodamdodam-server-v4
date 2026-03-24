package com.b1nd.dodamdodam.outsleeping.infrastructure.user.client

import com.b1nd.dodamdodam.core.common.exception.base.BaseInternalServerException
import com.b1nd.dodamdodam.grpc.user.GetUserRequest
import com.b1nd.dodamdodam.grpc.user.GetUsersRequest
import com.b1nd.dodamdodam.grpc.user.UserQueryServiceGrpcKt
import com.b1nd.dodamdodam.grpc.user.UserResponse
import io.grpc.Status
import io.grpc.StatusException
import io.grpc.StatusRuntimeException
import net.devh.boot.grpc.client.inject.GrpcClient
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class UserQueryClient {
    @GrpcClient("service-user")
    private lateinit var stub: UserQueryServiceGrpcKt.UserQueryServiceCoroutineStub

    suspend fun getUser(userId: UUID): UserResponse = runCatching {
        val request = GetUserRequest.newBuilder()
            .setPublicId(userId.toString())
            .build()
        stub.getUser(request)
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

    suspend fun getUsers(userIds: Collection<UUID>): List<UserResponse> = runCatching {
        val request = GetUsersRequest.newBuilder()
            .addAllPublicIds(userIds.map { it.toString() })
            .build()
        stub.getUsers(request).usersList
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
