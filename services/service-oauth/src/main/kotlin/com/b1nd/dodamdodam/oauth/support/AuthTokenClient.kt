package com.b1nd.dodamdodam.oauth.support

import com.b1nd.dodamdodam.grpc.auth.AuthTokenServiceGrpcKt
import com.b1nd.dodamdodam.grpc.auth.IssueTokenRequest
import net.devh.boot.grpc.client.inject.GrpcClient
import org.springframework.stereotype.Component

@Component
class AuthTokenClient {

    @GrpcClient("service-auth")
    private lateinit var stub: AuthTokenServiceGrpcKt.AuthTokenServiceCoroutineStub

    suspend fun issueToken(userPublicId: String): String {
        val request = IssueTokenRequest.newBuilder()
            .setUserPublicId(userPublicId)
            .build()
        val response = stub.issueToken(request)
        return response.accessToken
    }
}
