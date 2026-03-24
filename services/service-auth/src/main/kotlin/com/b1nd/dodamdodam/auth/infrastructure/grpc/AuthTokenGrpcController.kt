package com.b1nd.dodamdodam.auth.infrastructure.grpc

import com.b1nd.dodamdodam.auth.domain.principal.service.PrincipalService
import com.b1nd.dodamdodam.auth.infrastructure.security.jwt.JwtSigner
import com.b1nd.dodamdodam.core.security.jwt.data.JwtClaims
import com.b1nd.dodamdodam.grpc.auth.AuthTokenServiceGrpcKt
import com.b1nd.dodamdodam.grpc.auth.IssueTokenRequest
import com.b1nd.dodamdodam.grpc.auth.IssueTokenResponse
import net.devh.boot.grpc.server.service.GrpcService
import java.util.UUID

@GrpcService
class AuthTokenGrpcController(
    private val jwtSigner: JwtSigner,
    private val principalService: PrincipalService,
) : AuthTokenServiceGrpcKt.AuthTokenServiceCoroutineImplBase() {

    override suspend fun issueToken(request: IssueTokenRequest): IssueTokenResponse {
        val userId = UUID.fromString(request.userPublicId)
        val principal = principalService.getByUserId(userId)

        val tokens = jwtSigner.createTokens(JwtClaims(userId = principal.userId, username = principal.username))

        return IssueTokenResponse.newBuilder()
            .setAccessToken(tokens.accessToken)
            .addAllRoles(principal.roles.map { it.name })
            .build()
    }
}
