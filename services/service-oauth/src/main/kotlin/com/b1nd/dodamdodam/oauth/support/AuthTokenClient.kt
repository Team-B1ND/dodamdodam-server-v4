package com.b1nd.dodamdodam.oauth.support

import com.b1nd.dodamdodam.core.security.passport.enumerations.RoleType
import com.b1nd.dodamdodam.grpc.auth.AuthTokenServiceGrpcKt
import com.b1nd.dodamdodam.grpc.auth.IssueTokenRequest
import net.devh.boot.grpc.client.inject.GrpcClient
import org.springframework.stereotype.Component
import java.util.UUID

data class AuthTokenResult(val accessToken: String, val roles: List<RoleType>)

@Component
class AuthTokenClient {

    @GrpcClient("service-auth")
    private lateinit var stub: AuthTokenServiceGrpcKt.AuthTokenServiceCoroutineStub

    suspend fun issueToken(userPublicId: UUID): AuthTokenResult {
        val request = IssueTokenRequest.newBuilder()
            .setUserPublicId(userPublicId.toString())
            .build()
        val response = stub.issueToken(request)
        return AuthTokenResult(accessToken = response.accessToken, roles = response.rolesList.map { RoleType.valueOf(it) })
    }
}
