package com.b1nd.dodamdodam.user.domain.user.exception

import com.b1nd.dodamdodam.core.common.exception.ExceptionCode
import org.springframework.http.HttpStatus

enum class UserExceptionCode(
    override val status: HttpStatus,
    override val message: String
): ExceptionCode {
    INVALID_PROFILE_IMAGE(HttpStatus.BAD_REQUEST, "프로필에는 우리 저장소의 10MB 이하 정적 이미지만 사용할 수 있어요."),
    PROFILE_IMAGE_VALIDATION_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "프로필 이미지를 확인할 수 없어요. 잠시 후 다시 시도해 주세요."),
    USER_ALREADY_EXISTED(HttpStatus.CONFLICT, "이미 존재하는 유저예요."),
    USER_NOT_FOUNDED(HttpStatus.NOT_FOUND, "유저가 존재하지 않아요."),
    USER_PASSWORD_INCORRECT(HttpStatus.UNAUTHORIZED, "유저의 아이디 또는 비밀번호가 올바르지 않아요."),
    PHONE_ALREADY_EXISTED(HttpStatus.CONFLICT, "이미 사용 중인 전화번호예요.")
    ;
}