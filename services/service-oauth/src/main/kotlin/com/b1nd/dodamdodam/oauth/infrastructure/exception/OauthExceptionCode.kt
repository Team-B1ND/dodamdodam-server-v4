package com.b1nd.dodamdodam.oauth.infrastructure.exception

import com.b1nd.dodamdodam.core.common.exception.ExceptionCode
import org.springframework.http.HttpStatus

enum class OauthExceptionCode(override val status: HttpStatus, override val message: String) : ExceptionCode {
    INVALID_CLIENT(HttpStatus.UNAUTHORIZED, "클라이언트 인증에 실패했어요."),
    CLIENT_NOT_FOUND(HttpStatus.NOT_FOUND, "클라이언트를 찾을 수 없어요."),
    INVALID_REDIRECT_URI(HttpStatus.BAD_REQUEST, "등록되지 않은 리다이렉트 URI예요."),
    INVALID_SCOPE(HttpStatus.BAD_REQUEST, "허용되지 않은 권한이에요."),
    INVALID_GRANT(HttpStatus.BAD_REQUEST, "인증 코드가 유효하지 않거나 만료되었어요."),
    UNSUPPORTED_GRANT_TYPE(HttpStatus.BAD_REQUEST, "지원하지 않는 인증 방식이에요."),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "접근 권한이 없어요."),
    INVALID_CODE_VERIFIER(HttpStatus.BAD_REQUEST, "코드 검증에 실패했어요."),
    TOKEN_NOT_FOUND(HttpStatus.BAD_REQUEST, "토큰을 찾을 수 없거나 이미 폐기되었어요."),
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "요청 형식이 올바르지 않아요."),
}
