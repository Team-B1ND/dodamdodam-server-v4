package com.b1nd.dodamdodam.oauth.support

import com.b1nd.dodamdodam.grpc.auth.AuthTokenServiceGrpcKt
import com.b1nd.dodamdodam.grpc.auth.IssueTokenRequest
import net.devh.boot.grpc.client.inject.GrpcClient
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class AuthTokenClient {

    @GrpcClient("service-auth")
    private lateinit var stub: AuthTokenServiceGrpcKt.AuthTokenServiceCoroutineStub

    suspend fun issueToken(userPublicId: UUID): String {
        val request = IssueTokenRequest.newBuilder()
            .setUserPublicId(userPublicId.toString())
            .build()
        val response = stub.issueToken(request)
        return response.accessToken
    }
}
