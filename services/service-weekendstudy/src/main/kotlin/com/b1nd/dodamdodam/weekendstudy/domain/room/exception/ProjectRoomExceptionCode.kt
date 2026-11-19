package com.b1nd.dodamdodam.weekendstudy.domain.room.exception

import com.b1nd.dodamdodam.core.common.exception.ExceptionCode
import org.springframework.http.HttpStatus

enum class ProjectRoomExceptionCode(
    override val status: HttpStatus,
    override val message: String
) : ExceptionCode {
    PROJECT_ROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "방을 찾을 수 없어요."),
    PROJECT_ROOM_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 존재하는 방 이름이에요."),
    PROJECT_ROOM_ALREADY_BLOCKED(HttpStatus.CONFLICT, "이미 차단된 교시가 있어요."),
    PROJECT_ROOM_BLOCKED(HttpStatus.CONFLICT, "희망한 방이 해당 교시에 사용할 수 없어요."),
    PROJECT_ROOM_BLOCK_INVALID_DATE_RANGE(HttpStatus.BAD_REQUEST, "시작일이 종료일보다 늦어요."),
}
