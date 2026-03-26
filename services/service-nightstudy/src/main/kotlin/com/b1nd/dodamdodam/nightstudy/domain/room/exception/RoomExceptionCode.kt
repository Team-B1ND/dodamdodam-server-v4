package com.b1nd.dodamdodam.nightstudy.domain.room.exception

import com.b1nd.dodamdodam.core.common.exception.ExceptionCode
import org.springframework.http.HttpStatus

enum class RoomExceptionCode(
    override val status: HttpStatus,
    override val message: String
) : ExceptionCode {
    ROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "방을 찾을 수 없어요."),
    ROOM_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 존재하는 방 이름이에요."),
}
