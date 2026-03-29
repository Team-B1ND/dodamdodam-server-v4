package com.b1nd.dodamdodam.gateway.infrastructure.gateway.filter

import com.b1nd.dodamdodam.core.common.data.webclient.WebClientApiResponse
import com.b1nd.dodamdodam.core.common.exception.auth.AuthTokenExceptionCode
import com.b1nd.dodamdodam.core.common.exception.auth.InvalidTokenSignatureException
import com.b1nd.dodamdodam.core.common.exception.auth.TokenExpiredException
import com.b1nd.dodamdodam.core.common.exception.base.BaseInternalServerException
import com.b1nd.dodamdodam.gateway.infrastructure.auth.client.data.ExchangePassportResponse

fun WebClientApiResponse<ExchangePassportResponse>.toPassportOrThrow(): String {
    when {
        code == AuthTokenExceptionCode.TOKEN_EXPIRED.name -> throw TokenExpiredException()
        is4xxError() -> throw InvalidTokenSignatureException()
        is5xxError() -> throw BaseInternalServerException()
    }
    return data?.passport ?: throw BaseInternalServerException()
}

fun Throwable.rethrowIfAuthError() {
    if (this is TokenExpiredException || this is InvalidTokenSignatureException) throw this
}
